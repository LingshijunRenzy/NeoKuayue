package willow.train.kuayue.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.tuple.MutablePair;
import willow.train.kuayue.mixins.mixin.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CarriageUtil {
    public static Vec3 getCarriageDirection(Carriage carriage) {
        CarriageBogey leadingBogey = carriage.leadingBogey();
        if (leadingBogey.leading().edge == null) return Vec3.ZERO;
        if (carriage.isOnTwoBogeys()) {
            CarriageBogey trailingBogey = carriage.trailingBogey();
            if (trailingBogey.leading().edge == null) return Vec3.ZERO;
            return leadingBogey.getAnchorPosition().subtract(trailingBogey.getAnchorPosition()).normalize();
        }
        Train train = carriage.train;
        Vec3 leading = leadingBogey.leading().getPosition(train.graph, leadingBogey.isUpsideDown());
        return leading.subtract(leadingBogey.getAnchorPosition()).normalize();
    }

    //对反转转向架的车厢重新映射车厢Contraption
    public static boolean remapCarriage(Carriage carriage, boolean isClientSide) {
        if(carriage == null) return false;

        CarriageContraptionEntity cce = carriage.anyAvailableEntity();
        if (cce == null) return false;

        Contraption contraption = cce.getContraption();
        if(!(contraption instanceof CarriageContraption cc)) return false;

        Direction assemblyDirection = cc.getAssemblyDirection();
        int bogeySpacing = carriage.bogeySpacing;
        StructureTransform transform = new StructureTransform(
                BlockPos.ZERO.relative(assemblyDirection, bogeySpacing),
                Direction.Axis.Y, Rotation.CLOCKWISE_180, Mirror.NONE
        );

        Map<BlockPos, BlockPos> fluidTankControllerTransform = new HashMap<>();
        cc.getBlocks().forEach((k,v) -> {
            if(v.nbt != null && v.nbt.contains("Controller")) {
                CompoundTag tag = v.nbt.getCompound("Controller");
                BlockPos oldController = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
                if(oldController.equals(k)) {
                    BlockPos newController = transform.apply(oldController);
                    int width = v.nbt.getInt("Size") - 1;
                    newController = new BlockPos(newController.getX() - width, newController.getY(), newController.getZ() - width);
                    fluidTankControllerTransform.put(oldController, newController);
                }
            }
        });
        boolean remapped = remapCarriageContraption(carriage, isClientSide, cce, transform, fluidTankControllerTransform);
        boolean storageRemapped = remapCarriageStorage(carriage, cce, transform, fluidTankControllerTransform);

        return remapped && storageRemapped;
    }


    public static boolean remapCarriageContraption(@NonNull Carriage carriage, boolean isClientSide, CarriageContraptionEntity cce, StructureTransform transform, Map<BlockPos, BlockPos> fluidTankTransform) {
        Contraption contraption = cce.getContraption();
        if(!(contraption instanceof CarriageContraption cc)) return false;
        Direction assemblyDirection = cc.getAssemblyDirection();
        int bogeySpacing = carriage.bogeySpacing;

        //blocks
        HashMap<BlockPos, StructureTemplate.StructureBlockInfo> newBlocks = new HashMap<>();
        cc.getBlocks().forEach((k,v) -> {
            BlockPos newPos = transform.apply(k);
            StructureTemplate.StructureBlockInfo newInfo = StructureTransformUtil.getTransformedStructureBlockInfo(v, transform);

            //special handle for fluid tank controller nbt
            if(newInfo.nbt != null && newInfo.nbt.contains("Controller")) {
                CompoundTag tag = newInfo.nbt.getCompound("Controller");
                BlockPos oldController = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
                BlockPos newController = fluidTankTransform.get(oldController);
                tag.putInt("X", newController.getX());
                tag.putInt("Y", newController.getY());
                tag.putInt("Z", newController.getZ());
                newInfo.nbt.put("Controller", tag);
                if(k.equals(oldController)) {
                    //put old controller nbt into new position
                    StructureTemplate.StructureBlockInfo tankInfo = new StructureTemplate.StructureBlockInfo(
                            fluidTankTransform.get(oldController),
                            newInfo.state.rotate(Rotation.CLOCKWISE_180),
                            newInfo.nbt
                    );
                    newBlocks.put(newController, tankInfo);
                } else if (newPos.equals(newController)) {
                    StructureTemplate.StructureBlockInfo tankInfo = new StructureTemplate.StructureBlockInfo(
                            transform.apply(oldController),
                            newInfo.state.rotate(Rotation.CLOCKWISE_180),
                            newInfo.nbt
                    );
                    newBlocks.put(transform.apply(oldController), tankInfo);
                } else {
                    newBlocks.put(newPos, newInfo);
                }
            } else {
                newBlocks.put(newPos, newInfo);
            }
        });
        ((AccessorContraption) cc).setBlocks(newBlocks);

        //actors
        List<MutablePair<StructureTemplate.StructureBlockInfo, MovementContext>> actors = cc.getActors();
        for(int i = 0; i < actors.size(); i++) {

            MutablePair<StructureTemplate.StructureBlockInfo, MovementContext> actor = actors.get(i);
            StructureTemplate.StructureBlockInfo newInfo = StructureTransformUtil.getTransformedStructureBlockInfo(actor.getLeft(), transform);

            MovementContext movementContext = actor.getRight();
            movementContext.localPos = transform.apply(movementContext.localPos);
            movementContext.state = movementContext.state.rotate(Rotation.CLOCKWISE_180);
            movementContext.blockEntityData =  StructureTransformUtil.getTransformedBlockEntityNbt(movementContext.blockEntityData, transform);

            actors.set(i, MutablePair.of(newInfo, movementContext));
        }

        //interactors
        Map<BlockPos, MovingInteractionBehaviour> interactors = new HashMap<>();
        cc.getInteractors().forEach((k,v) -> {
            interactors.put(transform.apply(k), v);
        });
        ((AccessorContraption) cc).setInteractors(interactors);

        //superglue
        List<AABB> superglues = ((AccessorContraption) cc).getSuperglue();
        for(int i = 0; i < superglues.size(); i++) {
            AABB superglue = superglues.get(i);
            BlockPos start = new BlockPos(- (int) superglue.minX + 1, (int) superglue.minY, - (int) superglue.minZ + 1)
                    .relative(assemblyDirection, bogeySpacing);
            BlockPos end = new BlockPos(- (int) superglue.maxX + 1, (int) superglue.maxY, - (int) superglue.maxZ + 1)
                    .relative(assemblyDirection, bogeySpacing);
            superglues.set(i, new AABB(start, end));
        }

        //seats
        cc.getSeats().replaceAll(transform::apply);

        //stabilizedSubContraptions
        Map<UUID, BlockFace> stabilizedSubContraptions = new HashMap<>();
        ((AccessorContraption) cc).getStabilizedSubContraptions().forEach((k,v) -> {
            BlockPos newPos = transform.apply(v.getPos());
            Direction newDirection = v.getOppositeFace();

            stabilizedSubContraptions.put(k, new BlockFace(newPos, newDirection));
        });
        ((AccessorContraption) cc).setStabilizedSubContraptions(stabilizedSubContraptions);

        //capturedMultiblocks
        Multimap<BlockPos, StructureTemplate.StructureBlockInfo> capturedMultiblocks = ArrayListMultimap.create();
        ((AccessorContraption) cc).getCapturedMultiblocks().forEach((k,v) -> {
            if(k == null || v == null) return;

            BlockPos newPos = transform.apply(k);
            // multiblocks.info are referencing the same info in blocks
            BlockPos newInfoPos = transform.apply(v.pos);
            StructureTemplate.StructureBlockInfo newInfo = cc.getBlocks().get(newInfoPos);

            capturedMultiblocks.put(newPos, newInfo);
        });
        ((AccessorContraption) cc).setCapturedMultiblocks(capturedMultiblocks);

        //initialPassengers
        Map<BlockPos, Entity> initialPassengers = new HashMap<>();
        ((AccessorContraption) cc).getInitialPassengers().forEach((k,v) -> {
            BlockPos newPos = transform.apply(k);
            initialPassengers.put(newPos, v);
        });
        ((AccessorContraption) cc).setInitialPassengers(initialPassengers);

        //make sure client has correctly rendered contents
        if(isClientSide) {
            cc.presentBlockEntities.forEach((k, v) -> {
                if(v instanceof FluidTankBlockEntity ft && ft.isController()) {
                    CompoundTag tag = new CompoundTag();
                    ft.write(tag, false);
                    CompoundTag tankContent = tag.getCompound("TankContent");

                    BlockPos newPos = transform.apply(k);
                    int width = tag.getInt("Size") - 1;
                    newPos = new BlockPos(newPos.getX() - width, newPos.getY(), newPos.getZ() - width);

                    cc.getBlocks().get(newPos).nbt.put("TankContent", tankContent);
                }
            });

            MountedStorageManager storage = ((AccessorContraption) cc).getStorage();
            Map<BlockPos, MountedFluidStorage> newFluidStorage = new HashMap<>();
            ((AccessorMountedStorageManager) storage).getFluidStorage().forEach((k,v) -> {
                BlockPos newPos = fluidTankTransform.get(k);
                newFluidStorage.put(newPos, v);
            });
            ((AccessorMountedStorageManager) storage).setFluidStorage(newFluidStorage);

            CompoundTag tag = cc.writeNBT(false);
            cc.modelData.clear();
            cc.presentBlockEntities.clear();
            cc.maybeInstancedBlockEntities.clear();
            cc.specialRenderedBlockEntities.clear();

            cc.readNBT(cce.level, tag, false);
            ContraptionRenderDispatcher.invalidate(cc);

            ((AccessorContraption) cc).getStorage().bindTanks(cc.presentBlockEntities);
            for (BlockEntity be : cc.presentBlockEntities.values()) {
                if(be instanceof PortableFluidInterfaceBlockEntity pfi) {
                    ((AccessorPortableFluidInterfaceBlockEntity) pfi).invokeStopTransferring();
                    pfi.startTransferringTo(cc, 0);
                }
            }
        }

        //update collision
        cc.invalidateColliders();

        return true;
    }

    public static boolean remapCarriageStorage(@NonNull Carriage carriage, CarriageContraptionEntity cce, StructureTransform transform, Map<BlockPos, BlockPos> fluidTankTransform) {

        MountedStorageManager manager = carriage.storage;
        if (manager == null) return false;

        Map<BlockPos, MountedStorage> newStorage = new HashMap<>();
        ((AccessorMountedStorageManager) manager).getStorage().forEach((k, v) -> {
            BlockPos newPos = transform.apply(k);
            newStorage.put(newPos, v);
        });
        ((AccessorMountedStorageManager) manager).setStorage(newStorage);

        Map<BlockPos, MountedFluidStorage> newFluidStorage = new HashMap<>();
        ((AccessorMountedStorageManager) manager).getFluidStorage().forEach((k, v) -> {
            BlockPos newPos = fluidTankTransform.get(k);
            newFluidStorage.put(newPos, v);
        });
        ((AccessorMountedStorageManager) manager).setFluidStorage(newFluidStorage);

        manager.createHandlers();
        ((AccessorTrainCargoManager) manager).invokeChangeDetected();
        carriage.storage.resetIdleCargoTracker();

        cce.syncCarriage();

        return true;
    }

    public static boolean isDoubleEnded(List<Carriage> carriages) {
        for(Carriage carriage : carriages) {
            CarriageContraptionEntity cce = carriage.anyAvailableEntity();
            if(cce == null) continue;
            Contraption contraption = cce.getContraption();
            if(!(contraption instanceof CarriageContraption cc)) continue;
            if(cc.hasBackwardControls()) return true;
        }
        return false;
    }

    public static void reverseBogeys(Carriage carriage) {

        if (!carriage.isOnTwoBogeys()) {
            CarriageBogey bogey =  carriage.bogeys.getFirst();
            Couple<TravellingPoint> points = ((AccessorCarriageBogey) bogey).getPoints();

            //翻转Point内的属性
            for(boolean originalFirstPoint : Iterate.trueAndFalse) {
                points.get(originalFirstPoint).reverse(carriage.train.graph);
            }
            //交换bogey的points
            ((AccessorCarriageBogey) bogey).setPoints(Couple.create(points.getSecond(), points.getFirst()));

            carriage.bogeys.setFirst(bogey);
            return;
        }

        Couple<CarriageBogey> newBogeys = carriage.bogeys;

        for(boolean originalFirstBogey : Iterate.trueAndFalse) {
            CarriageBogey bogey = carriage.bogeys.get(originalFirstBogey);
            Couple<TravellingPoint> points = ((AccessorCarriageBogey) bogey).getPoints();

            for(boolean originalFirstPoint : Iterate.trueAndFalse) {
                points.get(originalFirstPoint).reverse(carriage.train.graph);
            }

            boolean isLeading = ((AccessorCarriageBogey) bogey).isLeading();
            ((AccessorCarriageBogey) bogey).setLeading(!isLeading);

            //交换bogey的points
            ((AccessorCarriageBogey) bogey).setPoints(Couple.create(points.getSecond(), points.getFirst()));
        }

        carriage.bogeys = Couple.create(
                carriage.bogeys.getSecond(),
                carriage.bogeys.getFirst()
        );

        CarriageContraptionEntity cce = carriage.anyAvailableEntity();
        if (cce == null) return;
        cce.setCarriage(carriage);
//        com.simibubi.create.content.contraptions.Contraption c = cce.getContraption();
//        if (!(c instanceof CarriageContraption cc)) return;
//        AccessorCarriageContraption acc = (AccessorCarriageContraption) cc;
//        BlockPos secondBogeyPos = cc.getSecondBogeyPos();
//        if (secondBogeyPos == null) return;
//        acc.setSecondBogeyPos(
//                new BlockPos(- secondBogeyPos.getX(),
//                        0, - secondBogeyPos.getZ())
//        );
    }
}
