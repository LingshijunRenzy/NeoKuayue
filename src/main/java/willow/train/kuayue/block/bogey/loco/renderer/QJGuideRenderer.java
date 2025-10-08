package willow.train.kuayue.block.bogey.loco.renderer;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.utility.NBTHelper;
import kasuga.lib.core.create.BogeyDataConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.initial.create.AllLocoBogeys;

public class QJGuideRenderer extends BogeyRenderer {

    private static ResourceLocation asBlockModelResource(String path) {
        return AllElements.testRegistry.asResource("block/" + path);
    }
    public static final PartialModel
            QJ_GUIDE_FRAME = new PartialModel(asBlockModelResource("bogey/qj/qj_guide_frame")),
            QJ_GUIDE_WHEEL = new PartialModel(asBlockModelResource("bogey/qj/qj_guide_wheel"));
    @Override
    public void initialiseContraptionModelData(
            MaterialManager materialManager, CarriageBogey carriageBogey) {
        this.createModelInstance(materialManager, QJ_GUIDE_FRAME);
        this.createModelInstance(materialManager, QJ_GUIDE_WHEEL, 1);
    }

    @Override
    public BogeySizes.BogeySize getSize() {
        return AllLocoBogeys.qjGuide.getSize();
    }
    @Override
    public void render(
            CompoundTag bogeyData,
            float wheelAngle,
            PoseStack ms,
            int light,
            VertexConsumer vb,
            boolean inContraption) {

        Direction direction =
                bogeyData.contains(BogeyDataConstants.BOGEY_ASSEMBLY_DIRECTION_KEY)
                        ? NBTHelper.readEnum(
                        bogeyData,
                        BogeyDataConstants.BOGEY_ASSEMBLY_DIRECTION_KEY,
                        Direction.class)
                        : Direction.NORTH;

        boolean inInstancedContraption = vb == null;

        BogeyModelData frame = getTransform(QJ_GUIDE_FRAME, ms, inInstancedContraption);
        BogeyModelData[] wheels = getTransform(QJ_GUIDE_WHEEL, ms, inInstancedContraption, 1);

        if (direction == Direction.SOUTH || direction == Direction.EAST) {
            if (inContraption) {
                frame.translate(0, 0, 0.3).render(ms, light, vb);

                for (int side = -1; side <0; side++) {
                    if (!inInstancedContraption) ms.pushPose();
                    BogeyModelData wheel = wheels[side + 1];
                    wheel.translate(0, 0.77, -2)
                            .rotateX(wheelAngle)
                            .render(ms, light, vb);
                    if (!inInstancedContraption) ms.popPose();
                }
            } else {
                frame.rotateY(180).translate(0, 0, 0.3).render(ms, light, vb);

                for (int side = -1; side < 0; side++) {
                    if (!inInstancedContraption) ms.pushPose();
                    BogeyModelData wheel = wheels[side + 1];
                    wheel.translate(0, 0.77, 2)
                            .rotateX(wheelAngle)
                            .render(ms, light, vb);
                    if (!inInstancedContraption) ms.popPose();
                }
            }
        } else {
            frame.rotateY(180).translate(0, 0, 0.3).render(ms, light, vb);

            for (int side = -1; side < 0; side++) {
                if (!inInstancedContraption) ms.pushPose();
                BogeyModelData wheel = wheels[side + 1];
                wheel.translate(0, 0.77, 2)
                        .rotateX(wheelAngle)
                        .render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();
            }
        }
    }


    public static class Andesite extends QJGuideRenderer {
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
