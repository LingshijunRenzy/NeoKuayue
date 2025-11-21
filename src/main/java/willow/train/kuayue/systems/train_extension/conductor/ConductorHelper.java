package willow.train.kuayue.systems.train_extension.conductor;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.foundation.utility.Couple;
import kasuga.lib.core.util.data_type.Pair;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.event.server.TrainCouplerPostDivideEvent;
import willow.train.kuayue.event.server.TrainCouplerPostMergeEvent;
import willow.train.kuayue.event.server.TrainCouplerPreDivideEvent;
import willow.train.kuayue.event.server.TrainCouplerPreMergeEvent;
import willow.train.kuayue.initial.AllSounds;
import willow.train.kuayue.mixins.mixin.AccessorTrain;
import willow.train.kuayue.systems.train_extension.CarriageAdditionalData;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;
import willow.train.kuayue.systems.train_extension.TrainExtensionSystem;

import java.util.*;

import static willow.train.kuayue.utils.CarriageUtil.*;

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

    public record TrainDivideRequest(
            Train loco,
            UUID newTrainUUID,
            int carriageIndex
    ) {}

    public record TrainMergeContext(
        UUID locoId,
        UUID carriageId,
        boolean isLocoHead,
        boolean isCarriageTail,
        int spacing
    ) {}

    // ------------------------------- functions ---------------------------------

    public static @Nullable Pair<ConductorProvider, Vec2> getConductorBlock(
            @NonNull BlockPos bogeyPos,
            @NonNull Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks,
            @NonNull Direction assembleDirection,
            boolean isLeading) {
        BlockPos posCache = bogeyPos;
        Pair<ConductorProvider, Vec2> provider = null;
        int distance = 0;
        while (true) {
            posCache = posCache.relative(
                    isLeading ? assembleDirection.getOpposite() : assembleDirection
            );
            distance++;
            if (!blocks.containsKey(posCache) && !blocks.containsKey(posCache.above())) {
                break;
            };
            StructureTemplate.StructureBlockInfo below = blocks.get(posCache);
            StructureTemplate.StructureBlockInfo above = blocks.get(posCache.above());
            if( below == null && above == null) continue;

            if (above != null && above.state().getBlock() instanceof ConductorProvider p) provider = Pair.of(
                    p,
                    new Vec2(distance, posCache.getY())
            );
            if (below != null && below.state().getBlock() instanceof ConductorProvider p) provider = Pair.of(
                    p,
                    new Vec2(distance, posCache.getY()));
        }
        return provider;
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

        if ((leadingA == null && trailingA == null) || (leadingB == null && trailingB == null)) {
            return TrainCollideResult.invalid();
        }
        //A head - B head
        if (leadingA != null && leadingB != null) {
            leadingA = leadingA.subtract(0, leadingA.y(), 0);
            leadingB = leadingB.subtract(0, leadingB.y(), 0);
            if (leadingA.distanceToSqr(leadingB) < distance)
                return new TrainCollideResult(
                        (byte) 1, (byte) 1,
                        leadingAConductor.getTotalOffset() + leadingBConductor.getTotalOffset()
                );
        }

        //A head - B tail
        if(leadingA != null && trailingB != null) {
            leadingA = leadingA.subtract(0, leadingA.y(), 0);
            trailingB = trailingB.subtract(0, trailingB.y(), 0);
            if (leadingA.distanceToSqr(trailingB) < distance) {
                if(Kuayue.TRAIN_EXTENSION.conductorsCoolingDown.containsKey(Couple.create(trailingBConductor.getLoc(), leadingAConductor.getLoc()))) {
                    return TrainCollideResult.invalid();
                }
                return new TrainCollideResult(
                        (byte) 1, (byte) -1,
                        leadingAConductor.getTotalOffset() + trailingBConductor.getTotalOffset()
                );
            }
        }

        //A tail - B head
        if (trailingA != null && leadingB != null) {
            trailingA = trailingA.subtract(0, trailingA.y(), 0);
            leadingB = leadingB.subtract(0, leadingB.y(), 0);
            if (trailingA.distanceToSqr(leadingB) < distance) {
                if(Kuayue.TRAIN_EXTENSION.conductorsCoolingDown.containsKey(Couple.create(trailingAConductor.getLoc(), leadingBConductor.getLoc()))) {
                    return TrainCollideResult.invalid();
                }
                return new TrainCollideResult(
                        (byte) -1, (byte) 1,
                        trailingAConductor.getTotalOffset() + leadingBConductor.getTotalOffset()
                );
            }
        }

        //A tail - B tail
        if(trailingA != null && trailingB != null) {
            trailingA = trailingA.subtract(0, trailingA.y(), 0);
            trailingB = trailingB.subtract(0, trailingB.y(), 0);
            if (trailingA.distanceToSqr(trailingB) < distance) {
                return new TrainCollideResult(
                        (byte) -1, (byte) -1,
                        trailingAConductor.getTotalOffset() + trailingBConductor.getTotalOffset()
                );
            }
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

    public static float getConductorFlatDistToSqr(Couple<ConductorLocation> couple, TrainExtensionSystem.ConductorCDInfo info) {
        Train trainA = Create.RAILWAYS.trains.get(couple.getFirst().getTrainId());
        Train trainB = Create.RAILWAYS.trains.get(couple.getSecond().getTrainId());
        if(trainA == null || trainB == null) return -1;

        Conductable first = info.conductorA;
        Conductable second = info.conductorB;

        Carriage firstCarriage = trainA.carriages.get(first.carriage());
        Carriage secondCarriage = trainB.carriages.get(second.carriage());

        Vec3 firstPos = getConductorPosition(firstCarriage, first, couple.getFirst().isLeading());
        Vec3 secondPos = getConductorPosition(secondCarriage, second, couple.getSecond().isLeading());
        if (firstPos == null || secondPos == null) return -1;

        firstPos = firstPos.subtract(0, firstPos.y(), 0);
        secondPos = secondPos.subtract(0, secondPos.y(), 0);

        return (float) firstPos.distanceToSqr(secondPos);
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

    public static boolean mergeTrains(
            Train loco, Train carriages,
            boolean isCarriageTail,
            boolean isLocoHead, int spacing,
            boolean clientSide
    ) {
        Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> locoConductors = getConductorPosition(loco);
        Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> carriageConductors = getConductorPosition(carriages);

        float oldLocoSpeed = (float) loco.speed;
        float oldCarriageSpeed = (float) carriages.speed;

        if(!clientSide) {
            TrainCouplerPreMergeEvent preEvent = new TrainCouplerPreMergeEvent(
                    new TrainMergeContext(loco.id, carriages.id, isLocoHead, isCarriageTail, spacing),
                    Pair.of(isLocoHead ? locoConductors.getFirst() : locoConductors.getSecond(),
                            isCarriageTail ? carriageConductors.getSecond() : carriageConductors.getFirst()),
                    oldLocoSpeed,
                    oldCarriageSpeed);
            MinecraftForge.EVENT_BUS.post(preEvent);
            if(preEvent.isCanceled()) {
                return false;
            }
        }

        // 获取参数
        List<Carriage> locoCarriages = loco.carriages;
        List<Integer> locoSpacing = loco.carriageSpacing;
        double[] locoStress = ((AccessorTrain) loco).getStress();

        List<Carriage> carriageCarts = carriages.carriages;
        List<Integer> cartSpacing = carriages.carriageSpacing;
        double[] cartStress = ((AccessorTrain) carriages).getStress();

        double[] neoStress = new double[locoStress.length + cartStress.length + 1];

        // 计算特效位置
        Vec3 effectPos;
        Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> conductorPos = getConductorPosition(loco);
        if (isLocoHead) {
            effectPos = conductorPos.getFirst().getSecond();
        } else {
            effectPos = conductorPos.getSecond().getSecond();
        }

        //头-头或尾-尾情况需要反转
        if(isLocoHead ^ isCarriageTail) {
            carriageCarts.forEach(c -> {
                reverseBogeys(c);
                boolean success = remapCarriageContraption(c, clientSide);
                if(!clientSide && !success){
                    TrainAdditionalData trainAdditionalData = Kuayue.TRAIN_EXTENSION.get(carriages.id);
                    if(trainAdditionalData != null){
                        int index = carriageCarts.indexOf(c);
                        if(index >= 0 && index < trainAdditionalData.getCarriages().size()){
                            CarriageAdditionalData carriageAdditionalData = trainAdditionalData.getCarriages().get(index);
                            carriageAdditionalData.shouldRemap = true;
                        }
                    }
                }
            });
            Collections.reverse(carriageCarts);
            Collections.reverse(cartSpacing);
        }
        if (isLocoHead) {
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
        loco.doubleEnded = loco.doubleEnded || carriages.doubleEnded;

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

        // 处理列车连接后的速度
        Pair<Float, Float> newSpeed = momentumExchange(loco, carriages, 0f);
        loco.speed = newSpeed != null ? newSpeed.getFirst() : oldLocoSpeed;

        if (clientSide) {
            CreateClient.RAILWAYS.removeTrain(carriages.id);
        } else {
            mergeTrainExtensionData(loco, carriages, isCarriageTail, isLocoHead);
            Create.RAILWAYS.removeTrain(carriages.id);
        }

        if(!clientSide) {
            // effects
            SoundEvent sound = AllSounds.TRAIN_COUPLER_SOUND.getSoundEvent();
            Entity entity = loco.carriages.get(0).anyAvailableEntity();
            if(entity != null) {
                entity.level().playSound(null, BlockPos.containing(effectPos), sound, entity.getSoundSource(), 0.2f, 1.0f);
                ((ServerLevel) entity.level()).sendParticles(ParticleTypes.CRIT, effectPos.x, effectPos.y, effectPos.z,
                        20, 0.2, 0.2, 0.2,0.8);
            }

            //post event
            MinecraftForge.EVENT_BUS.post(new TrainCouplerPostMergeEvent(
                    new TrainMergeContext(loco.id, carriages.id, isLocoHead, isCarriageTail, spacing),
                    Pair.of(isLocoHead ? locoConductors.getFirst() : locoConductors.getSecond(),
                            isCarriageTail ? carriageConductors.getSecond() : carriageConductors.getFirst()
                    ),
                    oldLocoSpeed,
                    oldCarriageSpeed,
                    (float) loco.speed
            ));
        }

        return true;
    }

    // here carriageIndex represents the carriage that coupler is on
    // assume that this carriage has a coupler
    public static boolean canDivideTrain(@NonNull Train train, int carriageIndex, boolean isLeading) {
        if(carriageIndex < 0 || carriageIndex > train.carriages.size() - 1) return false;

        TrainAdditionalData trainData = Kuayue.TRAIN_EXTENSION.get(train.id);
        if(trainData == null) return false;
        if(trainData.getCarriages().size() != train.carriages.size()) return false;

        if(isLeading) {
            int frontCarriageIndex = carriageIndex - 1;
            if(frontCarriageIndex < 0) return false; // no front carriage
            CarriageAdditionalData carriageData = trainData.getCarriages().get(frontCarriageIndex);
            return carriageData.getSecondConductor() != null; //front has second, this has first
        } else {
            int backCarriageIndex = carriageIndex + 1;
            if(backCarriageIndex >= train.carriages.size()) return false; // no back carriage
            CarriageAdditionalData carriageData = trainData.getCarriages().get(backCarriageIndex);
            return carriageData.getFirstConductor() != null; //this has first, back has second
        }
    }

    public static void divideTrains(
            Train loco,
            UUID newTrainUUID,
            int carriageIndex,
            boolean clientSide
    ) {
        if(loco == null) return;
        if(carriageIndex < 0 || carriageIndex >= loco.carriages.size() - 1) return;

        Conductable locoTail = null;
        Conductable carriageHead = null;
        Vec3 locoTailPos = null;
        Vec3 carriageHeadPos = null;

        //pre event
        if(!clientSide) {
            TrainAdditionalData locoData = Kuayue.TRAIN_EXTENSION.get(loco.id);
            if(locoData == null) return;
            locoTail = locoData.getConductorAt(
                    new ConductorLocation(loco.id, carriageIndex, false)
            );
            carriageHead = locoData.getConductorAt(
                    new ConductorLocation(loco.id, carriageIndex + 1, true)
            );
            locoTailPos = getConductorPosition(
                    loco.carriages.get(carriageIndex), locoTail, false);
            carriageHeadPos = getConductorPosition(
                    loco.carriages.get(carriageIndex + 1), carriageHead, true);
            if(locoTailPos == null || carriageHeadPos == null) return;

            TrainCouplerPreDivideEvent event = new TrainCouplerPreDivideEvent(
                    loco.id,
                    carriageIndex,
                    (float) loco.speed,
                    Pair.of(
                            Pair.of(locoTail, locoTailPos),
                            Pair.of(carriageHead, carriageHeadPos)
                    )
            );
            MinecraftForge.EVENT_BUS.post(event);
            if(event.isCanceled()) return;
        }


        //规定：主车在前，从车在后，从index车厢后方分开
        List<Carriage> locoCarts = loco.carriages;
        List<Integer> locoSpacing = loco.carriageSpacing;
        double[] locoStress = ((AccessorTrain) loco).getStress();

        List<Carriage> newLocoCarts = new ArrayList<>(locoCarts.subList(0, carriageIndex + 1));
        List<Carriage> newCarriageCarts = new ArrayList<>(locoCarts.subList(carriageIndex + 1, locoCarts.size()));

        List<Integer> newLocoSpacing = new ArrayList<>(locoSpacing.subList(0, carriageIndex));
        List<Integer> newCarriageSpacing = new ArrayList<>(locoSpacing.subList(carriageIndex + 1, locoSpacing.size()));

        double[] newLocoStress = new double[carriageIndex];
        double[] newCarriageStress = new double[locoStress.length - carriageIndex - 1];
        for(int i = 0; i < locoStress.length; i++){
            if(i < carriageIndex){
                newLocoStress[i] = locoStress[i];
            } else if(i > carriageIndex){
                newCarriageStress[i - carriageIndex - 1] = locoStress[i];
            }
        }

        loco.doubleEnded = isDoubleEnded(newLocoCarts);
        loco.carriages = newLocoCarts;
        loco.carriageSpacing = newLocoSpacing;
        ((AccessorTrain) loco).setStress(newLocoStress);

        Train carriage = new Train(
                newTrainUUID,
                loco.owner,
                loco.graph,
                newCarriageCarts,
                newCarriageSpacing,
                isDoubleEnded(newCarriageCarts)
        );
        ((AccessorTrain) carriage).setStress(newCarriageStress);

        Carriage c;
        for(int i = 0; i < newCarriageCarts.size(); i++){
            c = newCarriageCarts.get(i);
            c.setTrain(carriage);
            CarriageContraptionEntity entity = c.anyAvailableEntity();
            if(entity != null){
                entity.trainId = carriage.id;
                entity.carriageIndex = i;
                entity.setCarriage(c);
            }
        }
        carriage.speed = loco.speed;

        if(clientSide) {
            CreateClient.RAILWAYS.addTrain(carriage);
        } else {
            Create.RAILWAYS.addTrain(carriage);
            divideTrainExtensionData(loco, carriage, carriageIndex);

            TrainExtensionSystem.ConductorCDInfo info = new TrainExtensionSystem.ConductorCDInfo(locoTail, carriageHead);
            Kuayue.TRAIN_EXTENSION.conductorsCoolingDown.put(
                    Couple.create(locoTail.getLoc(), carriageHead.getLoc()), info
            );

            //post event
            TrainCouplerPostDivideEvent event = new TrainCouplerPostDivideEvent(
                    loco.id,
                    newTrainUUID,
                    carriageIndex,
                    (float) loco.speed,
                    Pair.of(
                            Pair.of(locoTail, locoTailPos),
                            Pair.of(carriageHead, carriageHeadPos)
                    )
            );
            MinecraftForge.EVENT_BUS.post(event);
        }
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
            boolean isCarriageTail, boolean isLocoHead) {
        TrainAdditionalData locoData = Kuayue.TRAIN_EXTENSION.get(loco.id);
        TrainAdditionalData carriageData = Kuayue.TRAIN_EXTENSION.get(carriages.id);
        if (locoData == null || carriageData == null) return;

        if(isLocoHead ^ isCarriageTail) {
            carriageData.reverse(carriages);
        }
        if (isLocoHead) {
            locoData.getCarriages().addAll(0, carriageData.getCarriages());
        } else {
            locoData.getCarriages().addAll(carriageData.getCarriages());
        }
        locoData.reIndexAll(loco);
        locoData.updateInternalConnections();
        locoData.updateConductorMap();
        Kuayue.TRAIN_EXTENSION.syncChange(locoData);
        Kuayue.TRAIN_EXTENSION.remove(carriages.id);
        Kuayue.TRAIN_EXTENSION.syncRemove(carriages.id);
    }

    public static void divideTrainExtensionData(Train loco, Train carriages, int carriageIndex) {
        TrainAdditionalData locoData = Kuayue.TRAIN_EXTENSION.get(loco.id);
        if(locoData == null) return;
        List<CarriageAdditionalData> allCarriages = locoData.getCarriages();
        if(carriageIndex < 0 || carriageIndex >= allCarriages.size() - 1) return;

        List<CarriageAdditionalData> newLocoCarriages = new ArrayList<>(allCarriages.subList(0, carriageIndex + 1));
        List<CarriageAdditionalData> newCarriageCarriages = new ArrayList<>(allCarriages.subList(carriageIndex + 1, allCarriages.size()));

        locoData.getCarriages().clear();
        locoData.getCarriages().addAll(newLocoCarriages);
        locoData.reIndexAll(loco);
        locoData.updateInternalConnections();
        locoData.updateConductorMap();

        TrainAdditionalData carriageData = new TrainAdditionalData(carriages);
        carriageData.getCarriages().addAll(newCarriageCarriages);
        carriageData.reIndexAll(carriages);
        carriageData.updateInternalConnections();
        carriageData.updateConductorMap();
        Kuayue.TRAIN_EXTENSION.add(carriageData);

        Kuayue.TRAIN_EXTENSION.syncChange(locoData);
        Kuayue.TRAIN_EXTENSION.syncChange(carriageData);
    }


    public @Nullable BlockPos collectSecondBogeyPos(CarriageContraption contraption) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> infos = contraption.getBlocks();
        BlockPos posCache = BlockPos.ZERO;
        while (true) {
            posCache = posCache.relative(contraption.getAssemblyDirection());
            if (!infos.containsKey(posCache)) break;
            StructureTemplate.StructureBlockInfo info = infos.get(posCache);
            if (info.state().getBlock() instanceof AbstractBogeyBlock)
                return posCache;
        }
        return null;
    }
}
