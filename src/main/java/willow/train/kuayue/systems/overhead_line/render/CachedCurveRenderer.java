package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.client.renderer.MultiBufferSource;

public class CachedCurveRenderer {
    public static void render(AnimModel curveModel, RenderCurve curve, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay) {
        pose.pushPose();
        Matrix4f original = pose.last().pose().copy();
        for (Matrix4f matrix : curve.getMatrix()) {
            pose.last().pose().multiply(matrix);
            curveModel.render(pose, bufferSource, light, overlay);
            pose.last().pose().load(original);
        }
        pose.popPose();
    }
}
