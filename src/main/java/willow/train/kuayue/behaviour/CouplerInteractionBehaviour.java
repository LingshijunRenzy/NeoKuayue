package willow.train.kuayue.behaviour;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class CouplerInteractionBehaviour extends MovingInteractionBehaviour {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        //ComponentTranslationTool.showSuccess(player, "coupler_interaction_handling", true);
        if(!AllItems.WRENCH.isIn(player.getItemInHand(activeHand))) return false;
        if(!(contraptionEntity instanceof CarriageContraptionEntity cce) ||
                !(contraptionEntity.getContraption() instanceof CarriageContraption cc)) return false;

        Direction assemblyDirection = cc.getAssemblyDirection();
        int coord = assemblyDirection.getAxis() == Direction.Axis.X ? localPos.getX() : localPos.getZ();
        boolean isLeading = coord * assemblyDirection.getAxisDirection().getStep() > 0;



        return true;
    }
}
