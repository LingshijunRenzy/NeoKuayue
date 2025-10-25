package willow.train.kuayue.systems.overhead_line.block.support.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.KuayueConfig;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineEndWeightBlockEntity;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlock;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportInsulatorBlock;

import java.util.List;

public class OverheadLineEndCounterWeightRenderer implements BlockEntityRenderer<OverheadLineSupportBlockEntity>  {
    public OverheadLineEndCounterWeightRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OverheadLineSupportBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();

        OverheadLineEndWeightBlockEntity blockEntity = (OverheadLineEndWeightBlockEntity) pBlockEntity;

        pPoseStack.mulPoseMatrix(
                AllOverheadLineSupportModels.getDirectionOf.apply(
                        pBlockEntity.getBlockState().getValue(OverheadLineSupportBlock.FACING), 1.3f
                )
        );

        AllOverheadLineSupportModels.applyRotation(pPoseStack, pBlockEntity);
        AllOverheadLineSupportModels.applyOffset(pPoseStack, pBlockEntity);

        switch (blockEntity.getRenderState()){
            case EMPTY -> {
                this.renderEmpty(blockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
                break;
            }
            case DUAL -> {
                this.renderDual(blockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
                break;
            }
            case SINGLE -> {
                this.renderSingle(blockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
                break;
            }
        }

        pPoseStack.popPose();
    }

    private void renderEmpty(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        pPoseStack.pushPose();
        AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT_EMPTY.render(
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        this.renderWeight(
                pBlockEntity,
                AllOverheadLineSupportModels.OVERHEAD_LINE_WEIGHT_ON_GROUND,
                true,
                pPartialTick,
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        pPoseStack.popPose();
    }


    private void renderSingle(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        pPoseStack.pushPose();
        AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT_SMALL.render(
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        this.renderHangerLineSmall(
                pBlockEntity,
                pPartialTick,
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        this.renderWeight(
                pBlockEntity,
                AllOverheadLineSupportModels.OVERHEAD_LINE_WEIGHT_SMALL,
                false,
                pPartialTick,
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        pPoseStack.popPose();
    }

    private void renderDual(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        pPoseStack.pushPose();
        AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT.render(
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        this.renderHangerLine(
                pBlockEntity,
                pPartialTick,
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        this.renderWeight(
                pBlockEntity,
                AllOverheadLineSupportModels.OVERHEAD_LINE_WEIGHT,
                false,
                pPartialTick,
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );

        pPoseStack.popPose();
    }


    private void renderWeight(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            AnimModel model,
            boolean ground,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        int intHeight = pBlockEntity.getHeight();
        float height = (float) pBlockEntity.getHeight();
        float yoffset = pBlockEntity.getYOffset();

        if(intHeight < 1) {
            return;
        }

        if(!ground){
            height -= KuayueConfig.CONFIG.getDoubleValue("OVERHEAD_LINE_END_WEIGHT_HEIGHT").floatValue();
        }
        if(height < 1.0) {
            height = 1.0f;
        }
        pPoseStack.translate(0, - (height/1.3f + yoffset - 1) ,0);
        model.render(
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );
    }

    private void renderHangerLineSmall(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        pPoseStack.pushPose();
        pPoseStack.translate(-0.82f, -0.15f, 0.0);
        for(int i = 0; i < pBlockEntity.getHeight() + pBlockEntity.getYOffset() - 3; i++) {
            pPoseStack.translate(0.0, -0.77f, 0.0);
            AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE_SMALL.render(
                    pPoseStack,
                    pBufferSource,
                    pPackedLight,
                    pPackedOverlay
            );
        }
        pPoseStack.popPose();
    }

    private void renderHangerLine(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        pPoseStack.pushPose();
        pPoseStack.translate(-0.2f, -0.15f, 0.0);
        for(int i = 0; i < pBlockEntity.getHeight() + pBlockEntity.getYOffset() - 3; i++) {
            pPoseStack.translate(0.0, -0.77f, 0.0);
            AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE.render(
                    pPoseStack,
                    pBufferSource,
                    pPackedLight,
                    pPackedOverlay
            );
        }
        pPoseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return KuayueConfig.CONFIG.getIntValue("OVERHEAD_LINE_SUPPORT_RENDER_DISTANCE");
    }
}
