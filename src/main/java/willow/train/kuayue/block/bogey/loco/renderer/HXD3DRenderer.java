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

public class HXD3DRenderer extends BogeyRenderer {


    private static ResourceLocation asBlockModelResource(String path) {
        return AllElements.testRegistry.asResource("block/" + path);
    }

    public static final PartialModel
            HXD3D_FRAME = new PartialModel(asBlockModelResource("bogey/hxd3d/hxd3d_frame")),
            HXD3D_WHEEL = new PartialModel(asBlockModelResource("bogey/hxd3d/hxd3d_wheel"));

    @Override
    public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {
        boolean forwards = BogeyDataConstants.isForwards(bogeyData, inContraption);

        Direction direction =
                bogeyData.contains(BogeyDataConstants.BOGEY_ASSEMBLY_DIRECTION_KEY)
                        ? NBTHelper.readEnum(
                        bogeyData,
                        BogeyDataConstants.BOGEY_ASSEMBLY_DIRECTION_KEY,
                        Direction.class)
                        : Direction.NORTH;

        boolean inInstancedContraption = vb == null;

        BogeyModelData frame = getTransform(HXD3D_FRAME, ms, inInstancedContraption);
        BogeyModelData[] wheels = getTransform(HXD3D_WHEEL, ms, inInstancedContraption, 3);

        if (!inContraption) {
            // 正向转向架未组装架体
            frame.rotateY(180).translate(0, 0.012, 0).render(ms, light, vb);

            // 正向转向架未组装轮对
            if (!inInstancedContraption) ms.pushPose();
            wheels[0].translate(0, 0.905, -0.23).rotateY(180).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            if (!inInstancedContraption) ms.pushPose();
            wheels[1].translate(0, 0.905, 1.69).rotateY(180).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            if (!inInstancedContraption) ms.pushPose();
            wheels[2].translate(0, 0.905, -2.365).rotateY(180).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            return;
        }

        if (direction == Direction.NORTH || direction == Direction.WEST) {
            // 正向转向架北西方向架体
            frame.rotateY(180).translate(0, 0.012, 0).render(ms, light, vb);

            // 正向转向架北西方向轮对
            if (!inInstancedContraption) ms.pushPose();
            wheels[0].translate(0, 0.905, -0.23).rotateY(180).rotateX(-wheelAngle).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            if (!inInstancedContraption) ms.pushPose();
            wheels[1].translate(0, 0.905, 1.69).rotateY(180).rotateX(-wheelAngle).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            if (!inInstancedContraption) ms.pushPose();
            wheels[2].translate(0, 0.905, -2.365).rotateY(180).rotateX(-wheelAngle).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            return;
        }

        // 正向转向架南东方向架体
        frame.translate(0, 0.012, 0).render(ms, light, vb);

        // 正向转向架南东方向轮对
        if (!inInstancedContraption) ms.pushPose();
        wheels[0].translate(0, 0.905, 0.23).rotateX(wheelAngle).render(ms, light, vb);
        if (!inInstancedContraption) ms.popPose();

        if (!inInstancedContraption) ms.pushPose();
        wheels[1].translate(0, 0.905, 2.365).rotateX(wheelAngle).render(ms, light, vb);
        if (!inInstancedContraption) ms.popPose();

        if (!inInstancedContraption) ms.pushPose();
        wheels[2].translate(0, 0.905, -1.69).rotateX(wheelAngle).render(ms, light, vb);
        if (!inInstancedContraption) ms.popPose();
    }

    @Override
    public BogeySizes.BogeySize getSize() {
        return AllLocoBogeys.hxd3d.getSize();
    }

    @Override
    public void initialiseContraptionModelData(MaterialManager materialManager, CarriageBogey carriageBogey) {
        this.createModelInstance(materialManager, HXD3D_FRAME);
        this.createModelInstance(materialManager, HXD3D_WHEEL, 3);
    }

    public static class Backward extends BogeyRenderer {

        @Override
        public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {
            boolean forwards = BogeyDataConstants.isForwards(bogeyData, inContraption);

            Direction direction =
                    bogeyData.contains(BogeyDataConstants.BOGEY_ASSEMBLY_DIRECTION_KEY)
                            ? NBTHelper.readEnum(
                            bogeyData,
                            BogeyDataConstants.BOGEY_ASSEMBLY_DIRECTION_KEY,
                            Direction.class)
                            : Direction.NORTH;

            wheelAngle = -wheelAngle;
            boolean inInstancedContraption = vb == null;

            BogeyModelData frame = getTransform(HXD3D_FRAME, ms, inInstancedContraption);
            BogeyModelData[] wheels = getTransform(HXD3D_WHEEL, ms, inInstancedContraption, 3);

            // 反向转向架未组装
            if (!inContraption) {
                // 未组装架体
                frame.translate(0, 0.012, 0).render(ms, light, vb);

                // 未组装轮对
                if (!inInstancedContraption) ms.pushPose();
                wheels[0].translate(0, 0.905, 0.23).render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();

                if (!inInstancedContraption) ms.pushPose();
                wheels[1].translate(0, 0.905, -1.69).render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();

                if (!inInstancedContraption) ms.pushPose();
                wheels[2].translate(0, 0.905, 2.365).render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();
                return;
            }

            // 反向转向架北西方向已组装
            if (direction == Direction.NORTH || direction == Direction.WEST) {
                // 北西方向已组装架体
                frame.translate(0, 0.012, 0).render(ms, light, vb);

                // 北西方向已组装轮对
                if (!inInstancedContraption) ms.pushPose();
                wheels[0].translate(0, 0.905, 0.23).rotateX(-wheelAngle).render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();

                if (!inInstancedContraption) ms.pushPose();
                wheels[1].translate(0, 0.905, -1.69).rotateX(-wheelAngle).render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();

                if (!inInstancedContraption) ms.pushPose();
                wheels[2].translate(0, 0.905, 2.365).rotateX(-wheelAngle).render(ms, light, vb);
                if (!inInstancedContraption) ms.popPose();

                return;
            }

            // 反向转向架南东方向已组装
            // 南东方向已组装架体
            frame.rotateY(180).translate(0, 0.012, 0).render(ms, light, vb);

            // 南东方向已组装轮对
            if (!inInstancedContraption) ms.pushPose();
            wheels[0].translate(0, 0.905, -0.23).rotateX(-wheelAngle).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            if (!inInstancedContraption) ms.pushPose();
            wheels[1].translate(0, 0.905, -2.365).rotateX(-wheelAngle).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();

            if (!inInstancedContraption) ms.pushPose();
            wheels[2].translate(0, 0.905, 1.685).rotateX(-wheelAngle).render(ms, light, vb);
            if (!inInstancedContraption) ms.popPose();
        }

        @Override
        public BogeySizes.BogeySize getSize() {
            return AllLocoBogeys.hxd3dBackward.getSize();
        }

        @Override
        public void initialiseContraptionModelData(MaterialManager materialManager, CarriageBogey carriageBogey) {
            this.createModelInstance(materialManager, HXD3D_FRAME);
            this.createModelInstance(materialManager, HXD3D_WHEEL, 3);
        }
    }
}
