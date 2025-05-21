package willow.train.kuayue.systems.overhead_line.block.support.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TransformationHelper;
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

    public static class C1ItemRenderer extends BlockEntityWithoutLevelRenderer {

        public C1ItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
            super(pBlockEntityRenderDispatcher, pEntityModelSet);
        }

        public C1ItemRenderer() {this(null, null);}

        @Override
        public void onResourceManagerReload(ResourceManager pResourceManager) {}

        @Override
        public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();

            applySupportCTransform(pPoseStack, pTransformType);

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

    public static void applySupportCTransform(PoseStack poseStack, ItemTransforms.TransformType type){
        switch (type) {
            case GUI:
                poseStack.scale(0.35f, 0.35f, 0.35f);
                poseStack.translate(1.45, 1.125, 0);
                poseStack.mulPose(
                        Quaternion.fromXYZDegrees(new Vector3f(30,45,0))
                );
                break;
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                break;
            case FIXED:
                break;
            case THIRD_PERSON_LEFT_HAND:
                poseStack.scale(0.75f, 0.75f, 0.75f);
                poseStack.translate(1, 0.75, 0);
                poseStack.mulPose(
                        Quaternion.fromXYZDegrees(new Vector3f(90,-180,0))
                );
                break;
            case THIRD_PERSON_RIGHT_HAND:
                poseStack.scale(0.75f, 0.75f, 0.75f);
                poseStack.translate(0, 1.75, 0);
                poseStack.mulPose(
                        Quaternion.fromXYZDegrees(new Vector3f(90,0,0))
                );
                break;
            case GROUND:
                break;
        }
    }

    public static class C2ItemRenderer extends BlockEntityWithoutLevelRenderer {

        public C2ItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
            super(pBlockEntityRenderDispatcher, pEntityModelSet);
        }

        public C2ItemRenderer() {this(null, null);}

        @Override
        public void onResourceManagerReload(ResourceManager pResourceManager) {}

        @Override
        public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
            pPoseStack.pushPose();

            applySupportCTransform(pPoseStack, pTransformType);

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
