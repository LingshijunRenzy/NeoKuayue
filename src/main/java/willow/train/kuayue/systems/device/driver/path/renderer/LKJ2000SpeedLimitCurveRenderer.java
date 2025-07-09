package willow.train.kuayue.systems.device.driver.path.renderer;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import kasuga.lib.core.client.render.texture.StaticImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.device.driver.path.SpeedCurveGenerator;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathPoint;
import willow.train.kuayue.systems.device.driver.path.list.SpeedEdgePathLine;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathLine;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathLineList;
import willow.train.kuayue.systems.device.driver.path.point.EdgePathPointRenderManager;
import willow.train.kuayue.systems.device.driver.path.point.mark.StationMark;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class LKJ2000SpeedLimitCurveRenderer {

    private final float scale;
    protected SpeedCurveGenerator generator;
    protected SpeedCurveLineRenderer curveLineRenderer;
    protected TextureTarget textureTarget;
    protected StaticImage image;
    protected float yDistance = 0.0F;

    protected float displayMaxSpeed = 140.0F;

    public boolean shouldRerenderCurve = true;


    public void setDisplayMaxSpeed(float displayMaxSpeed) {
        this.displayMaxSpeed = displayMaxSpeed;
        shouldRerenderCurve = true;
    }

    public LKJ2000SpeedLimitCurveRenderer(
            TextureTarget textureTarget,
            SpeedCurveGenerator generator,
            float scale){
        this.textureTarget = textureTarget;
        this.generator = generator;
        this.curveLineRenderer = new SpeedCurveLineRenderer(generator.curveLine);
        yDistance = (float) (generator.forwardDistance - generator.backwardDistance);
        this.scale = scale;
    }

    public void init(){
        textureTarget.setClearColor(0F,0F,0F,0F);
        /*
        texture = new TextureTargetTexture(textureTarget);
        try{
            image = StaticImage.createImage(
                    AllElements.testRegistry.asResource("curve_renderer_" + System.identityHashCode(this)),
                    texture
            ).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
             */
        curveLineRenderer.init();
        shouldRerenderCurve = true;
    }

    public void render(){
        if( curveLineRenderer == null || textureTarget == null )
            return;
        this.textureTarget.clear(true);
        this.textureTarget.bindWrite(true);
        this.renderCurve();
        this.textureTarget.unbindWrite();
    }

    public void destroy(){
        this.textureTarget.destroyBuffers();
        image = null;
        curveLineRenderer.destroy();
    }

    private void renderCurve() {
        float tWidth = this.textureTarget.width * scale;
        float tHeight = this.textureTarget.height * scale;
        Matrix4f matrix4f = Matrix4f.orthographic(tWidth,-tHeight, -1000, 1000);
        tHeight = tHeight - 1;
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.setIdentity();
        float left = 0;
        float xScale = tHeight / displayMaxSpeed;
        float yScale = tWidth / yDistance;
        Map.Entry<Double, Double> last = null;
        TreeMap<Double, Double> curve = new TreeMap<>(generator.getSpeedMap());
        float firstDistance = (float) generator.distance;
        float backDistance = (float) (generator.backwardDistance + firstDistance);
        float aheadDistance = (float) (generator.forwardDistance + firstDistance);

        float nextPositionX = Float.MAX_VALUE;
        float nextSpeedLimit = (float) generator.maxSpeed;

        boolean penetrate = false;
        float penetrateSpeedLimit = 0.0F;

        PoseStack pose = new PoseStack();

        while(nextPositionX > 0){
            if(curve.isEmpty()){
                drawLinearSpeedSegment(
                        0,
                        nextPositionX * yScale,
                        nextSpeedLimit * xScale,
                        nextSpeedLimit * xScale,
                        tHeight
                );
                break;
            }

            Map.Entry<Double, Double> entry = curve.pollLastEntry();

            float thisPositionX = (entry.getKey().floatValue() - backDistance);
            float speed = entry.getValue().floatValue();

            if(speed <= nextSpeedLimit && !penetrate) {
                drawLinearSpeedSegment(
                        thisPositionX * yScale,
                        (nextPositionX - thisPositionX) * yScale,
                        nextSpeedLimit * xScale,
                        speed * xScale,
                        tHeight
                );
            } else {
                float breakDistance = (float) Math.abs( penetrate ? generator.getBreakDistance(penetrateSpeedLimit, speed) : generator.getBreakDistance(nextSpeedLimit, speed));
                float stopBreakPositionX = nextPositionX;
                float startLinePositionX = thisPositionX;
                float startBreakPositionX = Math.max(nextPositionX - breakDistance, startLinePositionX);
                float stopLinePositionX = startBreakPositionX;

                boolean notEnoughDistanceBreak = true;

                if(stopLinePositionX > startLinePositionX){
                    drawLinearSpeedSegment(
                            startLinePositionX * yScale,
                            (stopLinePositionX - startLinePositionX) * yScale,
                            speed * xScale,
                            speed * xScale,
                            tHeight
                    );
                    notEnoughDistanceBreak = false;
                }

                if(notEnoughDistanceBreak && !curve.isEmpty() && curve.lastEntry().getValue() >= speed) {
                    penetrate = true;
                    penetrateSpeedLimit = speed;
                    continue;
                }

                penetrate = false;


                this.curveLineRenderer.renderToScreen(
                        startBreakPositionX * yScale,
                        tHeight - (float) generator.maxSpeed * xScale,
                        (stopBreakPositionX - startBreakPositionX) * yScale,
                        (float) generator.maxSpeed * xScale,
                        1,0,0,
                        notEnoughDistanceBreak ? (float) Math.sqrt(
                                Math.pow(nextSpeedLimit, 2) + 2 * (stopBreakPositionX - startBreakPositionX) * this.generator.acceleration
                        ) : speed,
                        nextSpeedLimit,
                        yScale
                );
            }

            if(nextPositionX + generator.backwardDistance > 0 && nextSpeedLimit != speed){
                Minecraft.getInstance().font.draw(
                        pose,
                        String.format("%.0f", nextSpeedLimit),
                        nextPositionX * yScale,
                        tHeight - nextSpeedLimit * xScale - Minecraft.getInstance().font.lineHeight,
                        0xFFFF80
                );
            }
            nextPositionX = thisPositionX;
            nextSpeedLimit = speed;
        }

        for (EdgePathPoint selectedPoint : generator.points.getSelectedPoints()) {
            EdgePathPointRenderManager.render(
                    selectedPoint,
                    (float) (selectedPoint.distance() - backDistance) * yScale,
                    tHeight,
                    yScale,
                    xScale
            );
        }
    }

    private void drawLinearSpeedSegment(
            float left,
            float tWidth,
            float nextMaxSpeed,
            float thisMaxSpeed,
            float top
    ) {
        // System.out.printf("left: %f, tWidth: %f, previousMaxSpeed: %f, thisMaxSpeed: %f, top: %f\n", left, tWidth, nextMaxSpeed, thisMaxSpeed, top);
        float r = 1.0f;
        float g = 0.0f;
        float b = 0.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        renderQuad(bufferBuilder, left, top - thisMaxSpeed, tWidth, 1, r, g, b);
        renderQuad(bufferBuilder, left + tWidth, top - Math.max(nextMaxSpeed, thisMaxSpeed), 1, Math.abs(nextMaxSpeed - thisMaxSpeed), r, g, b);
        renderQuad(bufferBuilder, left, top - thisMaxSpeed + 1, tWidth, 3, 0.3f, 0.3f, 0.3f);

        tesselator.end();
        RenderSystem.disableBlend();
    }

    public static void renderQuad(BufferBuilder bufferBuilder,float x, float y, float width, float height, float r, float g, float b){
        bufferBuilder.vertex(x, y + height, 0.0D).color(r, g, b, 1.0F).endVertex();
        bufferBuilder.vertex(x + width, y + height, 0.0D).color(r, g, b, 1.0F).endVertex();
        bufferBuilder.vertex(x + width, y, 0.0D).color(r, g, b, 1.0F).endVertex();
        bufferBuilder.vertex(x, y, 0.0D).color(r, g, b, 1.0F).endVertex();

    }

    public static void renderLayer(){
    }

    public static void test(){
        TextureTarget textureTarget = new TextureTarget(800, 420, false, true);

        SpeedCurveGenerator generator = new SpeedCurveGenerator();
        generator.setMaxSpeed(100D);
        generator.setAcceleration(3D);

        EdgePathLineList<SpeedEdgePathLine> speedLines = generator.specialLines;

        speedLines.addPathLine(new TestSpeedLine(-400.0D, 200.0D, 000.0D));
        speedLines.addPathLine(new TestSpeedLine(-200.0D, 1400.0D, 80.0D));
        speedLines.addPathLine(new TestSpeedLine(1200.0D, 800.0D, 45.0D));
        TestSpeedLine entryLine = new TestSpeedLine(2000.0D, 1200.0D, 0D);
        speedLines.addPathLine(entryLine);

        generator.points.addPathPoint(new StationMark(100D, "良乡"));

        LKJ2000SpeedLimitCurveRenderer renderer = new LKJ2000SpeedLimitCurveRenderer(textureTarget, generator, 0.5f);

        renderer.init();
        
        renderer.displayMaxSpeed = 140.0F;
        
        for(int i=0; i< 20; i++){
            renderer.render();

            for (Map.Entry<Double, Double> doubleDoubleEntry : generator.getSpeedMap().entrySet()) {
                System.out.println("Speed limit: On " + doubleDoubleEntry.getKey() + "m - " + doubleDoubleEntry.getValue() + "km/h");
            }

            Screenshot.grab(Minecraft.getInstance().gameDirectory, textureTarget, (message) -> {
                System.out.println("Screenshot saved: " + message.toString());
            });

            if(i == 10){
                speedLines.removePathLine(entryLine);
                speedLines.addPathLine(new TestSpeedLine(2000.0D, 1400.0D, 80.0D));
            }

            generator.addDistance(100.0D);
        }

        renderer.destroy();
    }

    public void update() {
        if(shouldRerenderCurve) {
            curveLineRenderer.render();
            shouldRerenderCurve = false;
        }
    }

    public static class TestSpeedLine implements SpeedEdgePathLine {
        private final double distance;
        private final double length;
        private final double speed;

        public TestSpeedLine(double distance, double length, double speed) {
            this.distance = distance;
            this.length = length;
            this.speed = speed;
        }

        @Override
        public int compareTo(@NotNull EdgePathLine object) {
            return Double.compare(distance(), object.distance());
        }

        @Override
        public double distance() {
            return distance;
        }

        @Override
        public double length() {
            return length;
        }

        @Override
        public double getSpeed() {
            return speed;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof TestSpeedLine that)) return false;
            return Double.compare(distance, that.distance) == 0 && Double.compare(length, that.length) == 0 && Double.compare(speed, that.speed) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, length, speed);
        }
    }
}
