package willow.train.kuayue.systems.device.driver.devices.components;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import kasuga.lib.KasugaLib;
import kasuga.lib.core.client.frontend.common.layouting.LayoutBox;
import kasuga.lib.core.client.frontend.common.layouting.LayoutNode;
import kasuga.lib.core.client.frontend.gui.GuiContext;
import kasuga.lib.core.client.frontend.gui.nodes.GuiViewNode;
import kasuga.lib.core.client.frontend.rendering.RenderContext;
import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.client.render.texture.Vec2f;
import willow.train.kuayue.event.client.ClientRenderTickManager;
import willow.train.kuayue.systems.device.driver.path.SpeedCurveGenerator;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathLineList;
import willow.train.kuayue.systems.device.driver.path.list.SpeedEdgePathLine;
import willow.train.kuayue.systems.device.driver.path.point.mark.StationMark;
import willow.train.kuayue.systems.device.driver.path.renderer.LKJ2000SpeedLimitCurveRenderer;

import java.util.HashMap;


public class LKJ2000CurveRenderNode extends GuiViewNode implements ClientRenderTickManager.TickReceiver {
    public LKJ2000CurveRenderNode(GuiContext context) {
        super(context);
        SpeedCurveGenerator generator = new SpeedCurveGenerator();
        generator.setMaxSpeed(100D);
        generator.setAcceleration(3D);

        EdgePathLineList<SpeedEdgePathLine> speedLines = generator.specialLines;

        speedLines.addPathLine(new LKJ2000SpeedLimitCurveRenderer.TestSpeedLine(-400.0D, 400.0D, 000.0D));
        speedLines.addPathLine(new LKJ2000SpeedLimitCurveRenderer.TestSpeedLine(0.0D, 1200.0D, 80.0D));
        speedLines.addPathLine(new LKJ2000SpeedLimitCurveRenderer.TestSpeedLine(1200.0D, 800.0D, 45.0D));
        LKJ2000SpeedLimitCurveRenderer.TestSpeedLine entryLine = new LKJ2000SpeedLimitCurveRenderer.TestSpeedLine(2000.0D, 1200.0D, 0D);
        speedLines.addPathLine(entryLine);

        generator.points.addPathPoint(new StationMark(100D, "Á¼Ïç"));
        this.generator = generator;
        ClientRenderTickManager.register(this);
    }

    HashMap<Object, LKJ2000CurveDynamicRenderer> renderers = new HashMap<>();

    SpeedCurveGenerator generator;

    public static void init(){
        KasugaLib
                .STACKS
                .GUI
                .orElseThrow()
                .nodeTypeRegistry
                .register("kuayue:lkj_2000_curve", LKJ2000CurveRenderNode::new);
    }

    @Override
    public float renderNode(Object source, RenderContext context) {
        if (!this.getLayoutManager().hasSource(source)) {
            return 0.0F;
        }

        LayoutNode layout = this.getLayoutManager().getSourceNode(source);
        LayoutBox coordinate = layout.getPosition();

        LKJ2000CurveDynamicRenderer renderer = renderers.get(source);

        if(coordinate.width == 0 || coordinate.height == 0)
            return 0.0F;

        if (renderer == null) {
            renderer = new LKJ2000CurveDynamicRenderer();
            renderer.init(generator, 0.5f, (int) (coordinate.width * 4), (int) (coordinate.height * 4));
            renderers.put(source, renderer);
        }

        renderer.updateSize(((int) coordinate.width * 4), ((int) coordinate.height * 4));
        context.pose().pushPose();
        context.pose().translate(0, 0, 0.006F);
        this.background.render(context, coordinate.x, coordinate.y, coordinate.width, coordinate.height);
        context.pose().translate(0, 0, 0.006F);
        renderer.render(context, coordinate);
        context.pose().popPose();
        return 0.012F;
    }

    @Override
    public void close() {
        super.close();
        ClientRenderTickManager.unregister(this);
        for (LKJ2000CurveDynamicRenderer renderer : renderers.values()) {
            renderer.close();
        }
        renderers.clear();
    }

    @Override
    public void onRenderTick() {
        for (LKJ2000CurveDynamicRenderer renderer : renderers.values()) {
            renderer.update();
            renderer.renderCurve();
        }
    }
}
