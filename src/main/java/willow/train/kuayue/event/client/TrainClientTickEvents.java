package willow.train.kuayue.event.client;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import willow.train.kuayue.utils.client.StatusOverlayRenderer;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;
import willow.train.kuayue.systems.train_extension.conductor.registry.ConductorCandidateRegistry;
import willow.train.kuayue.utils.client.ContraptionAimUtil;

public class TrainClientTickEvents {

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        updateGuiState();
    }

    public static void updateGuiState() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if(player == null || mc.level == null) {
            return;
        }

        if(!AllItems.WRENCH.isIn(player.getMainHandItem())) {
            StatusOverlayRenderer.setVisible(false);
            return;
        }

        Pair<AbstractContraptionEntity, BlockHitResult> hitResultPair = ContraptionAimUtil.getTargetContraptionBlock(player, 5.0D);
        if(hitResultPair == null || !(hitResultPair.getFirst() instanceof CarriageContraptionEntity cce)) {
            StatusOverlayRenderer.setVisible(false);
            return;
        }

        Carriage carriage = cce.getCarriage();
        if(carriage == null) {
            StatusOverlayRenderer.setVisible(false);
            return;
        }

        Train train = carriage.train;
        if(train == null) {
            StatusOverlayRenderer.setVisible(false);
            return;
        }

        if(!(cce.getContraption() instanceof CarriageContraption cc)) {
            StatusOverlayRenderer.setVisible(false);
            return;
        }

        BlockPos localPos = hitResultPair.getSecond().getBlockPos();

        if(ConductorCandidateRegistry.getProvider(cc.getBlocks().get(localPos).state) != null) {
            Direction assemblyDirection = cc.getAssemblyDirection();
            int coord = assemblyDirection.getAxis() == Direction.Axis.X ? localPos.getX() : localPos.getZ();
            boolean isLeading = coord * assemblyDirection.getAxisDirection().getStep() < 0;

            boolean canDivide = ConductorHelper.canDivideTrain(train, cce.carriageIndex, isLeading);
            if(canDivide) {
                StatusOverlayRenderer.setShowInfo(AllItems.WRENCH.asStack(), Component.translatable("gui.kuayue.coupler.can_divide"));
            } else {
                StatusOverlayRenderer.setShowInfo(AllItems.WRENCH.asStack(), Component.translatable("gui.kuayue.coupler.cannot_divide"));
            }
            StatusOverlayRenderer.setVisible(true);
        } else {
            StatusOverlayRenderer.setVisible(false);
        }
    }
}
