package willow.train.kuayue.systems.overhead_line.block.support.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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

        if (blockEntity.getRenderState() == OverheadLineEndWeightBlockEntity.RenderState.EMPTY) {
            pPoseStack.mulPoseMatrix(
                    AllOverheadLineSupportModels.getDirectionOf.apply(
                            pBlockEntity.getBlockState().getValue(OverheadLineSupportBlock.FACING), 1.3f
                    )
            );
        } else {
            pPoseStack.mulPoseMatrix(createScaledRotationMatrix(blockEntity.getDynamicRotationAngle() + 180f, 1.3f));
        }

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

        // 渲染调试线段 (仅在调试模式下)
        if (Minecraft.getInstance().options.renderDebug) {
            this.renderDebugLines(pPoseStack, pBufferSource, blockEntity, pPackedLight);
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
    }


    private void renderSingle(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT_SMALL.render(
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
    }

    private void renderDual(
            OverheadLineEndWeightBlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            int pPackedLight,
            int pPackedOverlay
    ) {
        AllOverheadLineSupportModels.OVERHEAD_LINE_END_COUNTERWEIGHT.render(
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

        if(intHeight < 1) {
            return;
        }

        if(!ground){
            height -= KuayueConfig.CONFIG.getDoubleValue("OVERHEAD_LINE_END_WEIGHT_HEIGHT").floatValue();
        }
        if(height < 1.0) {
            height = 1.0f;
        }
        pPoseStack.translate(0, - (height - 1) ,0);
        model.render(
                pPoseStack,
                pBufferSource,
                pPackedLight,
                pPackedOverlay
        );
    }

    private Matrix4f createScaledRotationMatrix(float yRotationDegrees, float scale) {
        PoseStack tempStack = new PoseStack();

        // 以方块中心为旋转轴的纯动态旋转 + 缩放
        tempStack.translate(0.5, 0.5, 0.5);
        tempStack.mulPose(Vector3f.YP.rotationDegrees(yRotationDegrees));
        tempStack.scale(scale, scale, scale);
        tempStack.translate(-0.5, -0.5, -0.5);

        return tempStack.last().pose();
    }

    private void renderDebugLines(PoseStack pPoseStack, MultiBufferSource pBufferSource,
                                  OverheadLineEndWeightBlockEntity blockEntity, int pPackedLight) {

        // 使用 Tesselator 直接渲染线段，参考项目中的成功实现
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(4.0f); // 设置线宽为4像素
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = pPoseStack.last().pose();

        // 1. 白色方向线 - 从方块中心指向EndWeight方向
        Vec3 center = new Vec3(0.5, 0.5, 0.5);
        float angle = blockEntity.getDynamicRotationAngle();
        Vec3 direction = new Vec3(Math.cos(Math.toRadians(angle)), 0, Math.sin(Math.toRadians(angle)));
        Vec3 endpoint = center.add(direction.scale(2.0)); // 2格长度

        bufferBuilder.vertex(matrix, (float)center.x, (float)center.y, (float)center.z)
                .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix, (float)endpoint.x, (float)endpoint.y, (float)endpoint.z)
                .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();

        // 2. 三轴坐标线 - 在连接点位置
        List<Vec3> connectionPoints = blockEntity.getConnectionPoints();
        for (Vec3 point : connectionPoints) {
            renderAxisLinesWithBuffer(bufferBuilder, matrix, point);
        }

        tesselator.end();
        RenderSystem.lineWidth(1.0f); // 恢复默认线宽
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void renderAxisLinesWithBuffer(BufferBuilder bufferBuilder, Matrix4f matrix, Vec3 center) {
        float length = 0.5f;

        // X轴 - 红色
        bufferBuilder.vertex(matrix, (float)center.x, (float)center.y, (float)center.z)
                .color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix, (float)center.x + length, (float)center.y, (float)center.z)
                .color(1.0f, 0.0f, 0.0f, 1.0f).endVertex();

        // Y轴 - 绿色
        bufferBuilder.vertex(matrix, (float)center.x, (float)center.y, (float)center.z)
                .color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix, (float)center.x, (float)center.y + length, (float)center.z)
                .color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();

        // Z轴 - 蓝色
        bufferBuilder.vertex(matrix, (float)center.x, (float)center.y, (float)center.z)
                .color(0.0f, 0.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix, (float)center.x, (float)center.y, (float)center.z + length)
                .color(0.0f, 0.0f, 1.0f, 1.0f).endVertex();
    }
}
