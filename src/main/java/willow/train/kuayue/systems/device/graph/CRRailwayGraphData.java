package willow.train.kuayue.systems.device.graph;

import com.simibubi.create.content.trains.GlobalRailwayManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import willow.train.kuayue.systems.device.graph.station.GraphStation;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class CRRailwayGraphData extends SavedData {
    HashMap<UUID, GraphStation> stations = new HashMap<>();

    public GraphStation getOrCreateStation(UUID stationId){
        return stations.computeIfAbsent(stationId, GraphStation::new);
    }

    public void removeStation(UUID stationId) {
        stations.remove(stationId);
    }

    public void read(LevelAccessor level, CompoundTag tag){
        stations.clear();
        ListTag stationList = tag.getList("Stations", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < stationList.size(); i++) {
            CompoundTag stationTag = stationList.getCompound(i);
            GraphStation station = GraphStation.read(level, stationTag);
            stations.put(station.uuid, station);
        }
    }

    public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        ListTag stationList = new ListTag();
        for (GraphStation station : stations.values()) {
            stationList.add(station.write());
        }
        tag.put("Stations", stationList);
        return tag;
    }

    public Optional<GraphStation> getOptionalStation(UUID segmentId) {
        return Optional.ofNullable(stations.get(segmentId));
    }

    public void notifyStationGC(GraphStation station) {
        if(station.isOrphan()) {
            removeStation(station.uuid);
        }
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        // save for each station
        return save();
    }

    public static CRRailwayGraphData load(CompoundTag compoundTag) {
        return null;
    }
}
