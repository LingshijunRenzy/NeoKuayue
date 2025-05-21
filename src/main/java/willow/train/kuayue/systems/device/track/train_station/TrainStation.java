package willow.train.kuayue.systems.device.track.train_station;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import kasuga.lib.KasugaLib;
import kasuga.lib.core.create.boundary.CustomSegmentUtil;
import kasuga.lib.core.create.boundary.CustomTrackSegment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.device.AllDeviceEdgePoints;
import willow.train.kuayue.systems.device.graph.CRRailwayGraphData;
import willow.train.kuayue.systems.device.graph.KuaYueRailwayManager;
import willow.train.kuayue.systems.device.graph.station.GraphStation;
import willow.train.kuayue.systems.device.track.entry.StationSegment;

import java.util.Objects;
import java.util.UUID;

public class TrainStation extends SingleBlockEntityEdgePoint {

    protected UUID segmentId;

    protected GraphStationInfo localInfo = null;

    @Override
    public boolean canMerge() {
        return false;
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        BlockState state = blockEntity.getBlockState();
    }

    @Override
    public void invalidate(LevelAccessor level) {
        super.invalidate(level);
    }

    @Override
    public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        super.read(buffer, dimensions);
    }

    @Override
    public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
        super.read(nbt, migration, dimensions);
    }

    @Override
    public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        super.write(buffer, dimensions);
    }

    @Override
    public void write(CompoundTag nbt, DimensionPalette dimensions) {
        super.write(nbt, dimensions);
    }


    private int lazyTickCount = -1;

    @Override
    public void tick(TrackGraph graph, boolean preTrains) {

        super.tick(graph, preTrains);

        if(preTrains) {
            return;
        }


        if(lazyTickCount >= 0 && lazyTickCount++ < 20) return;
        lazyTickCount = 0;

        TrackEdge edge = graph.getConnection(this.edgeLocation.map(graph::locateNode));
        CustomTrackSegment segment =
                CustomSegmentUtil.getSegment(
                        graph,
                        edge,
                        AllDeviceEdgePoints.STATION_ENTRY.getType(),
                        this.getLocationOn(edge)
                );

        if(!(segment instanceof StationSegment stationSegment)) {
            return;
        }

        UUID newSegmentId = stationSegment.getSegmentId();

        if(Objects.equals(newSegmentId,segmentId)){

            Kuayue.RAILWAY.SERVER.getOptionalStation(segmentId)
                    .ifPresent((station)->{
                        this.localInfo = station.getStationInfo();
                    });
            return;
        }

        if(segmentId != null){
            Kuayue.RAILWAY.SERVER.getOptionalStation(segmentId)
                    .ifPresent((station)->{
                        station.removeStation(this);
                        Kuayue.RAILWAY.SERVER.notifyStationGC(station);
                    });
        }

        if(newSegmentId != null) {
            GraphStation station = Kuayue.RAILWAY.SERVER.getOrCreateStation(newSegmentId);
            station.addStation(this);
            if(localInfo != null){
                station.updateInfo(localInfo);
            }
        }

        segmentId = newSegmentId;
    }

    @Override
    public void onRemoved(TrackGraph graph) {
        super.onRemoved(graph);

        Kuayue.RAILWAY.SERVER.getOptionalStation(segmentId)
                .ifPresent((station)->{
                    station.removeStation(this);
                    Kuayue.RAILWAY.SERVER.notifyStationGC(station);
                });
    }

}
