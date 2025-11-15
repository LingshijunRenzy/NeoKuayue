package willow.train.kuayue.behaviour;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.TrainDividePacket;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;
import willow.train.kuayue.utils.client.ComponentTranslationTool;

import java.util.UUID;

public class CouplerInteractionBehaviour extends MovingInteractionBehaviour {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if(!AllItems.WRENCH.isIn(player.getItemInHand(activeHand))) return false;
        if(!(contraptionEntity instanceof CarriageContraptionEntity cce) ||
                !(contraptionEntity.getContraption() instanceof CarriageContraption cc)) return false;

        Direction assemblyDirection = cc.getAssemblyDirection();
        int coord = assemblyDirection.getAxis() == Direction.Axis.X ? localPos.getX() : localPos.getZ();
        boolean isLeading = coord * assemblyDirection.getAxisDirection().getStep() < 0;

        int carriageIndex = cce.carriageIndex;
        Train train = cce.getCarriage().train;
        if(train == null) return false;

        boolean canDivide = ConductorHelper.canDivideTrain(train, carriageIndex, isLeading);
        boolean isClientSide = player.level.isClientSide;
        carriageIndex = isLeading ? carriageIndex - 1 : carriageIndex;
        if(canDivide && !isClientSide) {
            ComponentTranslationTool.showSuccess(player, "coupler_interaction_divide", true);
            UUID newTrainId = UUID.randomUUID();
            ConductorHelper.divideTrains(train, newTrainId, carriageIndex, false);
            ConductorHelper.TrainDivideRequest request = new ConductorHelper.TrainDivideRequest(train, newTrainId, carriageIndex);
            AllPackets.CHANNEL.boardcastToClients(
                    new TrainDividePacket(request), (ServerLevel) player.level, player.blockPosition()
            );
        }

        return true;
    }
}
