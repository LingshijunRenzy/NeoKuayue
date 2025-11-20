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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import willow.train.kuayue.systems.train_extension.client.CouplerOverlayRenderer;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;
import willow.train.kuayue.utils.client.ContraptionAimUtil;

public class TrainCouplerClientTickEvents {

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
            CouplerOverlayRenderer.canDivide = null;
            return;
        }

        Pair<AbstractContraptionEntity, BlockHitResult> hitResultPair = ContraptionAimUtil.getTargetContraptionBlock(player, 5.0D);
        if(hitResultPair == null || !(hitResultPair.getFirst() instanceof CarriageContraptionEntity cce)) {
            CouplerOverlayRenderer.canDivide = null;
            return;
        }

        Carriage carriage = cce.getCarriage();
        if(carriage == null) {
            CouplerOverlayRenderer.canDivide = null;
            return;
        }

        Train train = carriage.train;
        if(train == null) {
            CouplerOverlayRenderer.canDivide = null;
            return;
        }

        if(!(cce.getContraption() instanceof CarriageContraption cc)) {
            CouplerOverlayRenderer.canDivide = null;
            return;
        }

        BlockPos localPos = hitResultPair.getSecond().getBlockPos();

        if(cc.getBlocks().get(localPos).state().getBlock() instanceof ConductorProvider) {
            Direction assemblyDirection = cc.getAssemblyDirection();
            int coord = assemblyDirection.getAxis() == Direction.Axis.X ? localPos.getX() : localPos.getZ();
            boolean isLeading = coord * assemblyDirection.getAxisDirection().getStep() < 0;

            CouplerOverlayRenderer.canDivide = ConductorHelper.canDivideTrain(train, cce.carriageIndex, isLeading);
        } else {
            CouplerOverlayRenderer.canDivide = null;
        }
    }
}
