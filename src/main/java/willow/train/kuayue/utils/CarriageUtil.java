package willow.train.kuayue.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.MutablePair;
import willow.train.kuayue.mixins.mixin.AccessorCarriageBogey;
import willow.train.kuayue.mixins.mixin.AccessorCarriageContraption;
import willow.train.kuayue.mixins.mixin.AccessorContraption;

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
    public static boolean remapCarriageContraption(Carriage carriage, boolean isClientSide) {
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

        //blocks
        HashMap<BlockPos, StructureTemplate.StructureBlockInfo> newBlocks = new HashMap<>();
        cc.getBlocks().forEach((k,v) -> {
            BlockPos newPos = transform.apply(k);
            newBlocks.put(newPos, StructureTransformUtil.getTransformedStructureBlockInfo(v, transform));
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
            StructureTemplate.StructureBlockInfo newInfo = StructureTransformUtil.getTransformedStructureBlockInfo(v, transform);

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

        //client
        if(isClientSide) {
            //modelData
            Map<BlockPos, ModelData> newModelData = new HashMap<>();
            cc.modelData.forEach((k,v) -> {
                newModelData.put(transform.apply(k), v);
            });
            cc.modelData = newModelData;

            //presentBlockEntites
            Map<BlockPos, BlockEntity> newEntityData = new HashMap<>();
            Map<BlockEntity, BlockEntity> oldToNewEntityData =  new HashMap<>();
            cc.presentBlockEntities.forEach((k,v) -> {
                BlockPos newPos = transform.apply(k);

                BlockEntity newBE = StructureTransformUtil.getTransformedBlockEntity(v, transform);
                if (newBE != null) {
                    newBE.setChanged();
                    if(newBE instanceof SmartBlockEntity sbe) {
                        sbe.requestModelDataUpdate();
                        sbe.notifyUpdate();
                    }
                    newEntityData.put(newPos, newBE);
                    oldToNewEntityData.put(v, newBE);
                }
            });
            cc.presentBlockEntities.clear();
            cc.presentBlockEntities.putAll(newEntityData);

            //maybeInstancedBlockEntities
            for(int i = 0; i < cc.maybeInstancedBlockEntities.size(); i++) {
                BlockEntity oldEntity = cc.maybeInstancedBlockEntities.get(i);
                BlockEntity newEntity = oldToNewEntityData.get(oldEntity);
                if(newEntity != null) {
                    cc.maybeInstancedBlockEntities.set(i, newEntity);
                } else {
                    newEntity = StructureTransformUtil.getTransformedBlockEntity(oldEntity, transform);
                    if (newEntity != null) {
                        newEntity.setChanged();
                        cc.maybeInstancedBlockEntities.set(i, newEntity);
                    }
                }
            }

            //specialRenderedBlockEntitie
            cc.specialRenderedBlockEntities.replaceAll(oldToNewEntityData::get);
        }

        //assemblyDirection
        //((AccessorCarriageContraption) cc).setAssemblyDirection(assemblyDirection.getOpposite());

        //update collision
        cc.invalidateColliders();

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
