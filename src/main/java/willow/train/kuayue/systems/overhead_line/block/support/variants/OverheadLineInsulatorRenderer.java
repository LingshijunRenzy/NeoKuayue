package willow.train.kuayue.systems.overhead_line.block.support.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import kasuga.lib.core.client.model.BedrockModelLoader;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlock;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportInsulatorBlock;

import java.util.List;

public abstract class OverheadLineInsulatorRenderer {

    public static class ARenderer extends OverheadLineInsulatorRenderer implements BlockEntityRenderer<OverheadLineSupportBlockEntity> {
        public ARenderer(BlockEntityRendererProvider.Context context) {}
        @Override
        public void render(OverheadLineSupportBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();
            pPoseStack.mulPoseMatrix(
                    AllOverheadLineSupportModels.getDirectionOf.apply(
                            pBlockEntity.getBlockState().getValue(OverheadLineSupportBlock.FACING), 1.0f
                    )
            );
            (pBlockEntity.getBlockState().getValue(OverheadLineSupportInsulatorBlock.WALL)
                    ? AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_INSULATOR_A_WALL
                    :AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_INSULATOR_A)
                        .render(
                                pPoseStack,
                                pBufferSource,
                                pPackedLight,
                                pPackedOverlay
                        );
            pPoseStack.popPose();
        }
    }

    public static class BRenderer extends OverheadLineInsulatorRenderer implements BlockEntityRenderer<OverheadLineSupportBlockEntity> {
        public BRenderer(BlockEntityRendererProvider.Context context) {}
        @Override
        public void render(OverheadLineSupportBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();
            pPoseStack.mulPoseMatrix(
                    AllOverheadLineSupportModels.getDirectionOf.apply(
                            pBlockEntity.getBlockState().getValue(OverheadLineSupportBlock.FACING), 1.0f
                    )
            );
            (pBlockEntity.getBlockState().getValue(OverheadLineSupportInsulatorBlock.WALL)
                    ? AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_INSULATOR_B_WALL
                    :AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_INSULATOR_B)
                    .render(
                            pPoseStack,
                            pBufferSource,
                            pPackedLight,
                            pPackedOverlay
                    );
            pPoseStack.popPose();
        }
    }
}
