package willow.train.kuayue.systems.overhead_line.block.support.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlock;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;

public abstract class OverheadLineSupportCRenderer {

    public static class C1Renderer implements BlockEntityRenderer<OverheadLineSupportBlockEntity> {
        public C1Renderer(BlockEntityRendererProvider.Context context) {}
        @Override
        public void render(OverheadLineSupportBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();
            pPoseStack.mulPoseMatrix(
                    AllOverheadLineSupportModels.getDirectionOf.apply(
                            pBlockEntity.getBlockState().getValue(OverheadLineSupportBlock.FACING), 1.3f
                    )
            );
            pPoseStack.translate(0,0,-0.4);
            AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_A2_MODEL.render(
                    pPoseStack,
                    pBufferSource,
                    pPackedLight,
                    pPackedOverlay
            );
            pPoseStack.translate(0,0,0.8);
            AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_A1_MODEL.render(
                    pPoseStack,
                    pBufferSource,
                    pPackedLight,
                    pPackedOverlay
            );
            pPoseStack.popPose();
        }
    }

    public static class C2Renderer implements BlockEntityRenderer<OverheadLineSupportBlockEntity> {
        public C2Renderer(BlockEntityRendererProvider.Context context) {}
        @Override
        public void render(OverheadLineSupportBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();
            pPoseStack.mulPoseMatrix(
                    AllOverheadLineSupportModels.getDirectionOf.apply(
                            pBlockEntity.getBlockState().getValue(OverheadLineSupportBlock.FACING), 1.3f
                    )
            );
            pPoseStack.translate(0,0,-0.4);
            AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_A1_MODEL.render(
                    pPoseStack,
                    pBufferSource,
                    pPackedLight,
                    pPackedOverlay
            );
            pPoseStack.translate(0,0,0.8);
            AllOverheadLineSupportModels.OVERHEAD_LINE_SUPPORT_A2_MODEL.render(
                    pPoseStack,
                    pBufferSource,
                    pPackedLight,
                    pPackedOverlay
            );
            pPoseStack.popPose();
        }
    }
}
