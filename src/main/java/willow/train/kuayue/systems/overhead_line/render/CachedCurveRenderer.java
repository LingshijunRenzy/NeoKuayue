package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public class CachedCurveRenderer {
    public static void render(AnimModel curveModel, RenderCurve curve, PoseStack pose, MultiBufferSource bufferSource, int overlay) {
        pose.pushPose();
        Matrix4f original = pose.last().pose().copy();
        for (Pair<Matrix4f, Integer> part : curve) {
            pose.last().pose().multiply(part.getFirst());
            curveModel.render(pose, bufferSource, part.getSecond(), overlay);
            pose.last().pose().load(original);
        }
        pose.popPose();
    }
}
