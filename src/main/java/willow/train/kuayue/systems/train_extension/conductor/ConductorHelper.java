package willow.train.kuayue.systems.train_extension.conductor;

import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackNode;
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
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;

import java.util.List;
import java.util.Map;

public class ConductorHelper {

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

    public static boolean isValidCollide(Pair<Byte, Byte> pair) {
        return pair.getFirst() != 0 && pair.getSecond() != 0;
    }

    public static Pair<Byte, Byte> isTwoTrainConductorCollide(
            Train trainA, Train trainB, float distance
    ) {
        distance *= distance;
        Pair<Vec3, Vec3> conductorA =  ConductorHelper.getConductorPosition(trainA);
        Pair<Vec3, Vec3> conductorB =  ConductorHelper.getConductorPosition(trainB);
        if ((conductorA.getFirst() == null && conductorA.getSecond() == null) ||
        (conductorB.getFirst() == null && conductorB.getSecond() == null)) return Pair.of((byte) 0, (byte) 0);
        if (conductorA.getFirst() != null) {
            if (conductorB.getFirst() != null &&
                    conductorA.getFirst().distanceToSqr(conductorB.getFirst()) < distance)
                return Pair.of((byte) 1, (byte) 1);
            if (conductorB.getSecond() != null &&
                    conductorA.getFirst().distanceToSqr(conductorB.getSecond()) < distance)
                return Pair.of((byte) 1, (byte) -1);
        } else if (conductorA.getSecond() != null) {
            if (conductorB.getFirst() != null &&
                conductorA.getSecond().distanceToSqr(conductorB.getFirst()) < distance) {
               return Pair.of((byte) -1, (byte) 1);
            }
            if (conductorB.getSecond() != null &&
                conductorA.getSecond().distanceToSqr(conductorB.getSecond()) < distance) {
               return Pair.of((byte) -1, (byte) -1);
            }
        }
        return Pair.of((byte) 0, (byte) 0);
    }

    public static @NotNull Pair<Vec3, Vec3> getConductorPosition(Train train) {
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
        return Pair.of(firstPos, secondPos);
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
}
