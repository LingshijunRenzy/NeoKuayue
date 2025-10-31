package willow.train.kuayue.event.server;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.Test;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class TrainCouplerTickEvents {

    private static final HashSet<UUID> removed = new HashSet<>();

    public static HashSet<UUID> trackingTrains = new HashSet<>();

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        for (UUID id : Kuayue.TRAIN_EXTENSION.trainsToRemove) {
            Train train = Create.RAILWAYS.trains.get(id);
            if (train == null) {
                removed.add(id);
                continue;
            }
            if (train.owner == null) continue;
            GlobalStation station = train.getCurrentStation();
            if (station == null) continue;
            ServerLevel level = event.getServer().getLevel(station.blockEntityDimension);
            if (level == null) continue;
            BlockEntity be = level.getBlockEntity(station.blockEntityPos);

            if (!(be instanceof StationBlockEntity sbe)) continue;
            if (!train.canDisassemble()) continue;
            station.nearestTrain = new WeakReference<>(train);
            if (sbe.tryDisassembleTrain((ServerPlayer) level.getPlayerByUUID(train.owner))) {
                Create.RAILWAYS.removeTrain(id);
                removed.add(id);
            }
        }
        Kuayue.TRAIN_EXTENSION.trainsToRemove.removeAll(removed);
        removed.clear();

        for (Map.Entry<UUID, Train> entry : Create.RAILWAYS.trains.entrySet()) {
            Train train = entry.getValue();
            for (Map.Entry<UUID, Train> e2 : Create.RAILWAYS.trains.entrySet()) {
                Train t2 = e2.getValue();
                if (t2 == train) continue;
                Pair<Byte, Byte> collide = ConductorHelper
                        .isTwoTrainConductorCollide(train, t2, .125f);
                if (ConductorHelper.isValidCollide(collide)) {
                    System.out.println("conductor1: " + collide.getFirst() +
                            ", conductor2: " + collide.getSecond());
                }
            }

        }
        // NOTICE: 列车速度自带方向属性
        // NOTICE: 列车的bogey的TravellingPoint也自带方向属性, 可以用于单转向架列车方向的判定

//        for (UUID id : trackingTrains) {
//            Test.trackTrainData(id);
//        }
//        if (event.phase == TickEvent.Phase.START) {
//            int i = 0;
//            Train trainA = null, trainB = null;
//            for (Map.Entry<UUID, Train> entry : Create.RAILWAYS.trains.entrySet()) {
//                if (i > 1) break;
//                if (i > 0) {
//                    trainB = entry.getValue();
//                } else {
//                    trainA = entry.getValue();
//                }
//                i++;
//            }
//            boolean flag = false;
//            if (trainA != null && trainB != null && flag) {
//                Test.tryMigrateTrains(trainA, trainB,
//                        event.getServer().getLevel(ServerLevel.OVERWORLD),
//                        false);
//            }
//        }
    }
}
