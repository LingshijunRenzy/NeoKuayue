package willow.train.kuayue.systems.device.graph.track;

import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllEdgePoints;
import willow.train.kuayue.systems.device.graph.station.GraphStation;
import willow.train.kuayue.systems.device.track.exit.StationExit;
import willow.train.kuayue.systems.device.track.train_station.TrainStation;

import java.util.HashMap;
import java.util.UUID;

public class StationTrack {
    GraphStation graphStation;
    UUID id;

    String name;

    public StationTrack(GraphStation station, UUID id) {
        this.id = id;
        this.graphStation = station;
    }

    public GraphStation getGraphStation() {
        return graphStation;
    }

    public UUID getId() {
        return id;
    }

    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Id", id);
        nbt.putString("Name", name);
        ListTag exitsTag = new ListTag();
        for (UUID exitId : exits.keySet()) {
            CompoundTag exitTag = new CompoundTag();
            exitTag.putUUID("ExitId", exitId);
            exitTag.putBoolean("Front", exitFronts.get(exitId));
            exitsTag.add(exitTag);
        }
        nbt.put("Exits", exitsTag);
        return nbt;
    }

    public static StationTrack read(TrackGraph graph, GraphStation station, CompoundTag trackTag) {
        StationTrack track = new StationTrack(station, trackTag.getUUID("Id"));
        track.name = trackTag.getString("Name");
        ListTag exitsTag = trackTag.getList("Exits", 10);
        for (int i = 0; i < exitsTag.size(); i++) {
            CompoundTag exitTag = exitsTag.getCompound(i);
            UUID exitId = exitTag.getUUID("ExitId");
            boolean front = exitTag.getBoolean("Front");
            StationExit exitSignal = graph.getPoint(AllEdgePoints.EXIT_SIGNAL, exitId);
            if(exitSignal == null) {
                Kuayue.LOGGER.error("Exit signal with ID {} not found in graph for station track {}", exitId, track.id);
                continue;
            }
            track.addExitSignal(exitSignal, front);
        }
        return track;
    }

    HashMap<UUID, StationExit> exits = new HashMap<>();
    HashMap<UUID, Boolean> exitFronts = new HashMap<>();

    public void addExitSignal(StationExit exitSignal, boolean front) {
        exits.put(exitSignal.getId(), exitSignal);
        exitFronts.put(exitSignal.getId(), front);
    }

    public void removeExitSignal(StationExit exitSignal, boolean front) {
        exits.put(exitSignal.getId(), exitSignal);
        exitFronts.put(exitSignal.getId(), front);
    }

    public boolean isEmptyExit() {
        return exits.isEmpty();
    }
}
