package willow.train.kuayue.block.panels.block_entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import willow.train.kuayue.block.panels.block_entity.SingleArmPantographBlockEntity;
import willow.train.kuayue.block.panels.pantograph.IPantographModel;
import willow.train.kuayue.block.panels.pantograph.PantographProps;

import java.util.HashMap;

public class SingleArmPantographRenderer implements
        BlockEntityRenderer<SingleArmPantographBlockEntity>, IPantographModel {

    public static float STEP = 0.025f;

    public SingleArmPantographRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SingleArmPantographBlockEntity pBlockEntity,
                       float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        BlockState blockState = pBlockEntity.getBlockState();
        boolean risen = pBlockEntity.isRisen();
        PantographProps pantographType = pBlockEntity.getPantographType();

        // 获取玩家朝向
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();

        double pullRodAngle = 0.0;

        HashMap<String, Double> pantoModelMap = getPantoModelMapByType(pantographType, pullRodAngle);
    }

    protected HashMap<String, Double> getPantoModelMapByType (PantographProps pantographType, double pullRodAngle) {

        HashMap<String, Double> pantoModelMap = new HashMap<>();

        pantoModelMap = singleArmPantographModel(
                pantographType.getBaselineLength(),
                pantographType.getLargeArmLength(),
                pantographType.getPullRodLength(),
                pantographType.getConnectingRodLength(),
                pantographType.getSmallArmAngle(),
                pantographType.getSmallArmLength(),
                pullRodAngle
        );
        return pantoModelMap;
    }
}
