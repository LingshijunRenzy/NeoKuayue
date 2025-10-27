package willow.train.kuayue.block.bogey.carriage.renderer;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.block.bogey.loco.renderer.HXD3DRenderer;
import willow.train.kuayue.initial.create.AllCarriageBogeys;
import willow.train.kuayue.initial.AllElements;

import static willow.train.kuayue.block.bogey.carriage.renderer.PK209PRenderer.PK209P_WHEEL2;

public class SW220KRenderer extends BogeyRenderer {

    private static ResourceLocation asBlockModelResource(String path) {
        return AllElements.testRegistry.asResource("block/" + path);
    }

    public static PartialModel SW220K_FRAME =
            new PartialModel(asBlockModelResource("bogey/sw220k/sw220k_frame"));
    public static PartialModel SW220K_WHEEL =
            new PartialModel(asBlockModelResource("bogey/sw220k/sw220k_wheel"));

    public static double SW220K_FRAME_TRANS_Y = 0.925F;

    @Override
    public void initialiseContraptionModelData(
            MaterialManager materialManager, CarriageBogey carriageBogey) {
        this.createModelInstance(materialManager, SW220K_FRAME);
        this.createModelInstance(materialManager, SW220K_WHEEL, 2);
    }

    @Override
    public BogeySizes.BogeySize getSize() {
        return AllCarriageBogeys.sw220k.getSize();
    }

    @Override
    public void render(
            CompoundTag bogeyData,
            float wheelAngle,
            PoseStack ms,
            int light,
            VertexConsumer vb,
            boolean inContraption) {

        boolean inInstancedContraption = vb == null;

        BogeyModelData frame = getTransform(SW220K_FRAME, ms, inInstancedContraption);
        BogeyModelData[] wheels = getTransform(SW220K_WHEEL, ms, inInstancedContraption, 2);

        frame.translate(0, SW220K_FRAME_TRANS_Y, 0).render(ms, light, vb);

        for (int side : Iterate.positiveAndNegative) {
            if (!inInstancedContraption) ms.pushPose();
            BogeyModelData wheel = wheels[(side + 1) / 2];
            wheel.translate(0, 0.805, ((double) side) * 1.25d).rotateX(wheelAngle);
            wheel.render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();
        }
    }

    public static class Backward extends BogeyRenderer {

        @Override
        public void initialiseContraptionModelData(
                MaterialManager materialManager, CarriageBogey carriageBogey) {
            this.createModelInstance(materialManager, SW220K_FRAME);
            this.createModelInstance(materialManager, SW220K_WHEEL, 2);
        }

        @Override
        public BogeySizes.BogeySize getSize() {
            return AllCarriageBogeys.sw220kBackward.getSize();
        }

        @Override
        public void render(
                CompoundTag bogeyData,
                float wheelAngle,
                PoseStack ms,
                int light,
                VertexConsumer vb,
                boolean inContraption) {

            boolean inInstancedContraption = vb == null;

            BogeyModelData frame = getTransform(SW220K_FRAME, ms, inInstancedContraption);
            BogeyModelData[] wheels = getTransform(SW220K_WHEEL, ms, inInstancedContraption, 2);

            // 渲染架体
            frame.translate(0, SW220K_FRAME_TRANS_Y, 0).render(ms, light, vb);
            // 渲染轮对
            for (int side : Iterate.positiveAndNegative) {
                if (!inInstancedContraption) ms.pushPose();
                BogeyModelData wheel = wheels[(side + 1) / 2];
                wheel.translate(0, 0.805, ((double) side) * 1.25d).rotateX(wheelAngle);
                wheel.render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();
            }
        }
    }
    public static class Andesite extends SW220KRenderer {
        @Override
        public void render(
                CompoundTag bogeyData,
                float wheelAngle,
                PoseStack ms,
                int light,
                VertexConsumer vb,
                boolean inContraption) {
            ms.pushPose();
            ms.scale(1.2F, 1, 1);
            super.render(bogeyData, wheelAngle, ms, light, vb, inContraption);
            ms.popPose();
        }

        public static class Backward extends SW220KRenderer.Backward {
            @Override
            public void render(
                    CompoundTag bogeyData,
                    float wheelAngle,
                    PoseStack ms,
                    int light,
                    VertexConsumer vb,
                    boolean inContraption) {
                ms.pushPose();
                ms.scale(1.2F, 1, 1);
                super.render(bogeyData, wheelAngle, ms, light, vb, inContraption);
                ms.popPose();
            }
        }
    }
}
