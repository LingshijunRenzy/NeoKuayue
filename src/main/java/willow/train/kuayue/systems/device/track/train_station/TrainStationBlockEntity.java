package willow.train.kuayue.systems.device.track.train_station;

import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import willow.train.kuayue.initial.AllEdgePoints;
import willow.train.kuayue.systems.device.AllDeviceBlockEntities;

import java.util.List;

public class TrainStationBlockEntity extends SmartBlockEntity {
    public TrackTargetingBehaviour<TrainStation> edgePoint;

    public TrainStationBlockEntity(BlockPos blockPos, BlockState state) {
        super(AllDeviceBlockEntities.STATION_BLOCK_ENTITY.getType(), blockPos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, AllEdgePoints.TRAIN_STATION));
    }

    public InteractionResult onUse(Level pLevel, Player pPlayer) {
        if(pLevel.isClientSide) return null;
        return null;
    }
}
