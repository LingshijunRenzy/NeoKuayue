package willow.train.kuayue.systems.train_extension.conductor;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.foundation.utility.Couple;
import kasuga.lib.core.util.data_type.Pair;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.mixins.mixin.AccessorCarriageContraption;
import willow.train.kuayue.mixins.mixin.AccessorTrain;
import willow.train.kuayue.systems.train_extension.CarriageAdditionalData;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;

import java.util.*;

public class ConductorHelper {

    // ------------------------------- records ---------------------------------

    public record TrainCollideResult(byte aFlag, byte bFlag, int spacing) {
        public static TrainCollideResult invalid() {
            return new TrainCollideResult((byte) 0, (byte) 0, -1);
        }
    }

    public record CollidedConnectors(
            byte aFlag, Conductable conductorA,
            byte bFlag, Conductable conductorB,
            int spacing
    ) {
        public static CollidedConnectors invalid() {
            return new CollidedConnectors((byte) 0, null, (byte) 0, null, -1);
        }

        public boolean isAHead() {
            return aFlag > 0;
        }

        public boolean isBHead() {
            return bFlag > 0;
        }
    }

    public record TrainSortResult(
            Train loco, Conductable locoConductor,
            Train carriages, Conductable carriageConductor,
            boolean isLocoHead, boolean shouldReverseCarriages
    ) {}

    public record TrainMergeRequest(
            Train loco, Train carriages,
            boolean shouldReverseCarriages,
            boolean isLocoHead, int spacing,
            boolean clientSide
    ) {}

    // ------------------------------- functions ---------------------------------

    public static @Nullable Pair<ConductorProvider, Integer> getConductorBlock(
            @NonNull BlockPos bogeyPos,
            @NonNull Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks,
            @NonNull Direction assembleDirection,
            boolean isLeading) {
        BlockPos posCache = bogeyPos;
        Pair<ConductorProvider, Integer> provider = null;
        int distance = 0;
        while (true) {
            posCache = posCache.relative(
                    isLeading ? assembleDirection.getOpposite() : assembleDirection
            );
            distance++;
            if (!blocks.containsKey(posCache)) break;
            StructureTemplate.StructureBlockInfo info = blocks.get(posCache);
            if (info.state.getBlock() instanceof ConductorProvider p) provider = Pair.of(p, distance);
        }
        return provider;
    }

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

    public static boolean isValidCollide(TrainCollideResult pair) {
        return pair.aFlag != 0 && pair.bFlag != 0;
    }

    public static boolean isValidCollide2(CollidedConnectors pair) {
        return pair.aFlag != 0 && pair.bFlag != 0;
    }

    public static TrainCollideResult isTwoTrainConductorCollide(
            Train trainA, Train trainB, float distance
    ) {
        distance *= distance;
        Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> conductorA =
                ConductorHelper.getConductorPosition(trainA);
        Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> conductorB =
                ConductorHelper.getConductorPosition(trainB);
        if (conductorA.getFirst() == null || conductorA.getSecond() == null ||
            conductorB.getFirst() == null || conductorB.getSecond() == null) {
            return TrainCollideResult.invalid();
        }
        Vec3 leadingA = conductorA.getFirst().getSecond();
        Conductable leadingAConductor = conductorA.getFirst().getFirst();
        Vec3 leadingB = conductorB.getFirst().getSecond();
        Conductable leadingBConductor = conductorB.getFirst().getFirst();
        Vec3 trailingA = conductorA.getSecond().getSecond();
        Conductable trailingAConductor = conductorA.getSecond().getFirst();
        Vec3 trailingB = conductorB.getSecond().getSecond();
        Conductable trailingBConductor = conductorB.getSecond().getFirst();

        if ((leadingA == null && trailingA == null) ||
        (leadingB == null && trailingB == null))
            return TrainCollideResult.invalid();
        if (leadingA != null) {
            if (leadingB != null && leadingA.distanceToSqr(leadingB) < distance)
                return new TrainCollideResult(
                        (byte) 1, (byte) 1,
                        leadingAConductor.getTotalOffset() + leadingBConductor.getTotalOffset()
                );
            if (trailingB != null && leadingA.distanceToSqr(trailingB) < distance)
                return new TrainCollideResult(
                        (byte) 1, (byte) -1,
                        leadingAConductor.getTotalOffset() + trailingBConductor.getTotalOffset()
                );
        }
        if (leadingB != null && trailingA.distanceToSqr(leadingB) < distance) {
            return new TrainCollideResult(
                    (byte) -1, (byte) 1,
                    trailingAConductor.getTotalOffset() + leadingBConductor.getTotalOffset()
            );
        }
        if (trailingB != null && trailingA.distanceToSqr(trailingB) < distance) {
            return new TrainCollideResult(
                    (byte) -1, (byte) -1,
                    trailingAConductor.getTotalOffset() + trailingBConductor.getTotalOffset()
            );
        }
        return TrainCollideResult.invalid();
    }

    public static CollidedConnectors getCollidedConnector(
            Train trainA, Train trainB, float distance
    ) {
        TrainCollideResult result = isTwoTrainConductorCollide(trainA, trainB, distance);
        if (!isValidCollide(result)) {
            return CollidedConnectors.invalid();
        }
        TrainAdditionalData dataA = Kuayue.TRAIN_EXTENSION.get(trainA.id);
        TrainAdditionalData dataB = Kuayue.TRAIN_EXTENSION.get(trainB.id);
        Objects.requireNonNull(dataA);
        Objects.requireNonNull(dataB);
        Conductable a = dataA.getConductor(result.aFlag);
        Conductable b = dataB.getConductor(result.bFlag);
        return new CollidedConnectors(
                result.aFlag(), a,
                result.bFlag(), b,
                result.spacing()
        );
    }

    public static @NotNull Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> getConductorPosition(Train train) {
        if (!Kuayue.TRAIN_EXTENSION.contains(train.id)) return Pair.of(null, null);
        TrainAdditionalData data = Kuayue.TRAIN_EXTENSION.get(train.id);
        Pair<Conductable, Conductable> sidedConductor = data.getSidedConductors();
        if (sidedConductor.getFirst() == null && sidedConductor.getSecond() == null)
            return Pair.of(null, null);
        List<Carriage> carriages = train.carriages;
        Vec3 firstPos = getConductorPosition(
                carriages.get(0), sidedConductor.getFirst(), true);
        Vec3 secondPos = getConductorPosition(
                carriages.get(carriages.size() - 1), sidedConductor.getSecond(), false);
        return Pair.of(Pair.of(sidedConductor.getFirst(), firstPos),
                Pair.of(sidedConductor.getSecond(), secondPos));
    }

    public static @Nullable Vec3 getConductorPosition(
                                            Carriage carriage,
                                            Conductable conductor,
                                            boolean isLeading
    ) {
        if (conductor == null) return null;
        CarriageBogey bogey = carriage.isOnTwoBogeys() ?
                carriage.bogeys.get(isLeading) : carriage.leadingBogey();
        Vec3 position = bogey.getAnchorPosition();
        if (position == null) return null;
        return position.add(getCarriageDirection(carriage).scale(
                ((float) conductor.getTotalOffset()) * (isLeading ? 1f : -1f))
        );
    }

    /**
     *
     * @param trainA 参与碰撞的列车 A
     * @param trainB 参与碰撞的列车 B
     * @param e 恢复系数e
     * @return 碰撞后两车各自的速度
     */
    public static @Nullable Pair<Float, Float> momentumExchange(
            Train trainA, Train trainB, float e) {
        if (!Kuayue.TRAIN_EXTENSION.contains(trainA.id) ||
            !Kuayue.TRAIN_EXTENSION.contains(trainB.id)) {
            return null;
        }
        TrainAdditionalData dataA = Kuayue.TRAIN_EXTENSION.get(trainA.id);
        TrainAdditionalData dataB = Kuayue.TRAIN_EXTENSION.get(trainB.id);
        float totalWeightA = dataA.totalWeight();
        float totalWeightB = dataB.totalWeight();
        float totalWeightAB =  totalWeightA + totalWeightB;

        float speedA = (float) trainA.speed;
        float speedB = (float) trainB.speed;
        float deltaSpeed = speedA - speedB;

        return Pair.of(
                speedA - totalWeightA / totalWeightAB * (1f + e) * deltaSpeed,
                speedB - totalWeightB / totalWeightAB * (1f + e) * deltaSpeed
        );
    }

    public static @Nullable TrainSortResult sortTrains(
            Conductable conductorA, boolean isAHead,
            Conductable conductorB, boolean isBHead,
            boolean clientSide
    ) {
        GlobalRailwayManager manager = clientSide ?
                CreateClient.RAILWAYS : Create.RAILWAYS;
        Train trainA = manager.trains.get(conductorA.train());
        Train trainB = manager.trains.get(conductorB.train());
        if (trainA == null || trainB == null) return null;

        // 速度太小也不行
        if (Math.abs(trainA.speed) - Math.abs(trainB.speed) < .01f &&
            Math.abs(trainA.speed) < .03f) return null;

        // 根据两个连接器的优先级确定顺序，优先级越小越优先作为 conduct 后的 train.
        TrainSortResult resultAB = new TrainSortResult(
                trainA, conductorA,
                trainB, conductorB,
                isAHead, !isBHead);

        TrainSortResult resultBA = new TrainSortResult(
                trainB, conductorB,
                trainA, conductorA,
                isBHead, !isAHead
        );

        if (conductorA.getPriority() < conductorB.getPriority()) {
            return resultAB;
        } else if (conductorA.getPriority() > conductorB.getPriority()) {
            return resultBA;
        }

        // 如果一头一尾，则连接器在尾部那一辆车作为合并后的 train
        if (isAHead ^ isBHead) {
            return isBHead ? resultAB : resultBA;
        }

        // 优先速度绝对值大的一方
        if (Math.abs(trainA.speed) < Math.abs(trainB.speed)) {
            return resultBA;
        }
        return resultAB;
    }

    public static boolean shouldReverseCarriage(
            Conductable conductable, boolean flag
    ) {
        if (conductable.carriage() > 0) return true;
        return !flag;
    }

    public static void mergeTrains(
            Train loco, Train carriages,
            boolean shouldReverseCarriages,
            boolean isLocoHead, int spacing,
            boolean clientSide
    ) {
        // 获取参数
        List<Carriage> locoCarriages = loco.carriages;
        List<Integer> locoSpacing = loco.carriageSpacing;
        double[] locoStress = ((AccessorTrain) loco).getStress();

        List<Carriage> carriageCarts = carriages.carriages;
        List<Integer> cartSpacing = carriages.carriageSpacing;
        double[] cartStress = ((AccessorTrain) carriages).getStress();

        // 把 carriages 反过来
        if (shouldReverseCarriages) {
            Carriage c;
            List<Carriage> cartCache = new ArrayList<>(carriageCarts.size());
            for (int i = carriageCarts.size() - 1; i > -1; i--) {
                c = carriageCarts.get(i);
                cartCache.add(c);
                reverseBogeys(c);
            }
            carriageCarts = cartCache;

            List<Integer> spacingCache = new ArrayList<>(cartSpacing.size());
            for (int i = cartSpacing.size() - 1; i > -1; i--) {
                spacingCache.add(cartSpacing.get(i));
            }
            cartSpacing = spacingCache;

            double[] stressCache = new double[cartStress.length];
            for (int i = cartStress.length - 1; i > -1; i--) {
                stressCache[i] = cartStress[cartStress.length - i - 1];
            }
            cartStress = stressCache;
        }

        double[] neoStress = new double[locoStress.length + cartStress.length + 1];
        if (isLocoHead) {
            if (!shouldReverseCarriages) {
                carriageCarts.forEach(ConductorHelper::reverseBogeys);
            }
            locoCarriages.addAll(0, carriageCarts);
            locoSpacing.addAll(0, cartSpacing);
            locoSpacing.add(cartSpacing.size(), spacing);
            copyStress(cartStress, locoStress, neoStress);
        } else {
            locoCarriages.addAll(carriageCarts);
            locoSpacing.add(spacing);
            locoSpacing.addAll(cartSpacing);
            copyStress(locoStress, cartStress, neoStress);
        }

        ((AccessorTrain) loco).setStress(neoStress);

        Carriage c;
        for (int i = 0; i < locoCarriages.size(); i++) {
            c = locoCarriages.get(i);
            c.setTrain(loco);
            CarriageContraptionEntity entity = c.anyAvailableEntity();
            if (entity != null) {
                entity.trainId = loco.id;
                entity.carriageIndex = i;
                entity.setCarriage(c);

            }
            c.presentConductors = Couple.create(i > 0, i < locoCarriages.size() - 1);
        }

        if (clientSide) {
            CreateClient.RAILWAYS.removeTrain(carriages.id);
        } else {
            Create.RAILWAYS.removeTrain(carriages.id);
        }
        mergeTrainExtensionData(loco, carriages, shouldReverseCarriages, isLocoHead);
    }

    private static void copyStress(double[] locoStress, double[] cartStress, double[] neoStress) {
        System.arraycopy(locoStress, 0, neoStress, 0, locoStress.length);
        neoStress[locoStress.length] = 0;
        for (int i = 0; i < cartStress.length; i++) {
            neoStress[locoStress.length + 1 + i] = cartStress[i];
        }
    }

    public static void mergeTrainExtensionData(
            Train loco, Train carriages,
            boolean shouldReverseCarriages, boolean isLocoHead) {
        TrainAdditionalData locoData = Kuayue.TRAIN_EXTENSION.get(loco.id);
        TrainAdditionalData carriageData = Kuayue.TRAIN_EXTENSION.get(carriages.id);
        if (locoData == null || carriageData == null) return;

        List<CarriageAdditionalData> carriageAdditional = carriageData.getCarriages();
        if (shouldReverseCarriages) {
            ArrayList<CarriageAdditionalData> cache = new ArrayList<>(carriageAdditional.size());
            for (int i = locoData.getCarriages().size() - 1; i > -1; i--) {
                cache.add(carriageAdditional.get(i));
            }
            carriageAdditional = cache;
        }
        if (isLocoHead) {
            locoData.getCarriages().addAll(0, carriageAdditional);
        } else {
            locoData.getCarriages().addAll(carriageAdditional);
        }
        Kuayue.TRAIN_EXTENSION.remove(carriages.id);
        locoData.updateInternalConnections();
    }

    public static void reverseBogeys(Carriage carriage) {
        if (!carriage.isOnTwoBogeys()) return;
        Couple<CarriageBogey> bogeys = carriage.bogeys;

        carriage.bogeys = Couple.create(bogeys.getSecond(), bogeys.getFirst());

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

    public @Nullable BlockPos collectSecondBogeyPos(CarriageContraption contraption) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> infos = contraption.getBlocks();
        BlockPos posCache = BlockPos.ZERO;
        while (true) {
            posCache = posCache.relative(contraption.getAssemblyDirection());
            if (!infos.containsKey(posCache)) break;
            StructureTemplate.StructureBlockInfo info = infos.get(posCache);
            if (info.state.getBlock() instanceof AbstractBogeyBlock)
                return posCache;
        }
        return null;
    }
}
