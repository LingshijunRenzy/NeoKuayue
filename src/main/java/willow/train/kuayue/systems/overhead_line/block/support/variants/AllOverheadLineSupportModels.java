package willow.train.kuayue.systems.overhead_line.block.support.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import kasuga.lib.core.client.model.BedrockModelLoader;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlock;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AllOverheadLineSupportModels {

    public static AnimModel OVERHEAD_LINE_SUPPORT_A1_MODEL;

    public static AnimModel OVERHEAD_LINE_SUPPORT_A2_MODEL;

    public static AnimModel OVERHEAD_LINE_SUPPORT_C1_MODEL;

    public static AnimModel OVERHEAD_LINE_SUPPORT_C2_MODEL;

    public static AnimModel OVERHEAD_LINE_SUPPORT_INSULATOR_A;

    public static AnimModel OVERHEAD_LINE_SUPPORT_INSULATOR_A_WALL;

    public static AnimModel OVERHEAD_LINE_SUPPORT_INSULATOR_B;

    public static AnimModel OVERHEAD_LINE_SUPPORT_INSULATOR_B_WALL;

    public static AnimModel OVERHEAD_LINE_END_COUNTERWEIGHT;
    public static AnimModel OVERHEAD_LINE_END_COUNTERWEIGHT_SMALL;

    public static AnimModel OVERHEAD_LINE_END_COUNTERWEIGHT_EMPTY;
    public static AnimModel OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE;
    public static AnimModel OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE_SMALL;
    public static AnimModel OVERHEAD_LINE_WEIGHT;
    public static AnimModel OVERHEAD_LINE_WEIGHT_ON_GROUND;
    public static AnimModel OVERHEAD_LINE_WEIGHT_SMALL;

    public static AnimModel KUAYUE_TEST;
    public static AnimModel KUAYUE_TEST_LINE;

    public static final BiFunction<Direction, Float, Matrix4f> getDirectionOf = Util.memoize((d, b)->{
        PoseStack compiler = new PoseStack();


        compiler.translate(0.5,0.5,0.5);
        compiler.mulPose(d.getRotation());
        compiler.mulPose(new Quaternion(-90, -90, 0, true));
        compiler.translate((b - 1)/3f, (b - 1)/2f, 0);
        compiler.scale(b, b, b);
        compiler.translate(-0.5,-0.5,-0.5);

        return compiler.last().pose();
    });

    public static void renderConnectionPointTest(OverheadLineSupportBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedOverlay){
        List<Vec3> connections = pBlockEntity.getActualConnectionPoints();

        for (Vec3 connection : connections) {
            pPoseStack.pushPose();
            pPoseStack.translate(connection.x, connection.y, connection.z);
            AllOverheadLineSupportModels.KUAYUE_TEST.render(pPoseStack, pBufferSource, LightTexture.FULL_BRIGHT, pPackedOverlay);
            pPoseStack.popPose();
        }
    }

    public static void invoke(){
        OVERHEAD_LINE_SUPPORT_A1_MODEL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_grid_a1"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_A2_MODEL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_grid_a2"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_C1_MODEL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_grid_c"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_C2_MODEL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_grid_c2"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_INSULATOR_A = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_insulator_a"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_INSULATOR_A_WALL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_insulator_a_wall"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_INSULATOR_B = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_insulator_b"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_SUPPORT_INSULATOR_B_WALL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_insulator_b_wall"),
                RenderType.cutoutMipped()
        );
        KUAYUE_TEST = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/test/test"),
                RenderType.cutoutMipped()
        );
        KUAYUE_TEST_LINE = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/test/test_line"),
                RenderType.cutoutMipped()
        );

        OVERHEAD_LINE_END_COUNTERWEIGHT = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_end_counterweight"),
                RenderType.cutoutMipped()
        );

        OVERHEAD_LINE_END_COUNTERWEIGHT_SMALL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_end_counterweight_small"),
                RenderType.cutoutMipped()
        );

        OVERHEAD_LINE_END_COUNTERWEIGHT_EMPTY = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_end_counterweight_empty"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_end_counterweight_hanger_line"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE_SMALL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_end_counterweight_hanger_line_small"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_WEIGHT = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_weight"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_WEIGHT_ON_GROUND = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_weight_on_ground"),
                RenderType.cutoutMipped()
        );
        OVERHEAD_LINE_WEIGHT_SMALL = BedrockModelLoader.getModel(
                AllElements.testRegistry.asResource("block/overhead_line/overhead_line_weight_small"),
                RenderType.cutoutMipped()
        );

        OVERHEAD_LINE_SUPPORT_A1_MODEL.init();
        OVERHEAD_LINE_SUPPORT_A2_MODEL.init();
        OVERHEAD_LINE_SUPPORT_C1_MODEL.init();
        OVERHEAD_LINE_SUPPORT_C2_MODEL.init();
        OVERHEAD_LINE_SUPPORT_INSULATOR_A.init();
        OVERHEAD_LINE_SUPPORT_INSULATOR_A_WALL.init();
        OVERHEAD_LINE_SUPPORT_INSULATOR_B.init();
        OVERHEAD_LINE_SUPPORT_INSULATOR_B_WALL.init();

        OVERHEAD_LINE_END_COUNTERWEIGHT.init();
        OVERHEAD_LINE_END_COUNTERWEIGHT_SMALL.init();
        OVERHEAD_LINE_END_COUNTERWEIGHT_EMPTY.init();
        OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE.init();
        OVERHEAD_LINE_END_COUNTERWEIGHT_HANGER_LINE_SMALL.init();
        OVERHEAD_LINE_WEIGHT.init();
        OVERHEAD_LINE_WEIGHT_ON_GROUND.init();
        OVERHEAD_LINE_WEIGHT_SMALL.init();

        KUAYUE_TEST.init();
        KUAYUE_TEST_LINE.init();
    }
}
