package willow.train.kuayue.systems.device.driver.path.renderer;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import willow.train.kuayue.event.client.ClientRenderTickManager;
import willow.train.kuayue.initial.AllElements;

import java.io.IOException;
import java.util.ArrayList;

public class SpeedCurveLineRenderer {
    SpeedCurveLine generator;
    TextureTarget textureTarget;

    public SpeedCurveLineRenderer(SpeedCurveLine generator){
        this.generator = generator;
    }

    public void init(){
        this.textureTarget = new TextureTarget(256,256,false,true);
        textureTarget.setClearColor(0F,0F,0F,0F);
    }

    public void destroy(){
        this.textureTarget.destroyBuffers();
    }

    public void render(){
        if(textureTarget == null)
            return;
        this.textureTarget.clear(true);
        this.textureTarget.bindWrite(true);
        this.renderCurve();
        this.textureTarget.unbindWrite();
    }

    private void renderCurve() {

        Matrix4f matrix4f = Matrix4f.orthographic(256,-256, -1000, 1000);
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.setIdentity();
        ArrayList<Float> list = generator.baked;
        int size = list.size();
        int width = textureTarget.viewWidth;
        int height = textureTarget.viewHeight;

        System.out.println("View width: " + width + "View height: " + height);

        float xScale = width / (float) size;
        float yScale = height / (float)generator.maxSpeed;

        float previousValue = 0;


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < size; i++) {
            float currentValue = list.get(i);

            float x = (i * xScale) + xScale;
            float y = (previousValue * yScale);
            float w = 1;
            float h = Math.max((currentValue - previousValue) * yScale, 1);

            y -= h*0.5f;
            h *= 2;


            renderQuad(bufferBuilder, x, y, w, h, 1.0f, 1.0f, 1.0f);


            // System.out.printf("x: %f, y: %f, w: %f, h: %f\n", x, y, w, h);
            previousValue = currentValue;
        }

        tesselator.end();

        RenderSystem.disableBlend();

        //Minecraft.getInstance().font.draw(new PoseStack(),"aaa",20,20,7105644);

    }

    private void renderQuad(BufferBuilder bufferBuilder, float x, float y, float width, float height, float r, float g, float b){
        bufferBuilder.vertex(x, y + height, 0.0D).color(r, g, b, 1.0F).endVertex();
        bufferBuilder.vertex(x + width, y + height, 0.0D).color(r, g, b, 1.0F).endVertex();
        bufferBuilder.vertex(x + width, y, 0.0D).color(r, g, b, 1.0F).endVertex();
        bufferBuilder.vertex(x, y, 0.0D).color(r, g, b, 1.0F).endVertex();
    }

    public void output(){
        Screenshot.grab(Minecraft.getInstance().gameDirectory, textureTarget, (m)->{
            System.out.println(m.toString());
        });
    }

    private void renderTextureQuad(
            float x, float y, float width, float height,
            float uvX, float uvY, float uvWidth, float uvHeight,
            float r, float g, float b){
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, textureTarget.getColorTextureId());
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(x, y + height, 0.0D).uv(uvX, uvY + uvHeight).color(r,g,b,1).endVertex();
        bufferbuilder.vertex(x + width, y + height, 0.0D).uv(uvX + uvWidth, uvY + uvHeight).color(r,g,b,1).endVertex();
        bufferbuilder.vertex(x + width, y, 0.0D).uv(uvX + uvWidth, uvY).color(r,g,b,1).endVertex();
        bufferbuilder.vertex(x, y, 0.0D).uv(uvX, uvY).color(r,g,b,1).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

    public float renderToScreen(
            float x, float y, float width, float height,
            float r, float g, float b,
            float currentMaxSpeed, float nextMaxSpeed,
            float yScale) {
        if (nextMaxSpeed >= currentMaxSpeed)
            return 0f;

        // x = v^2 / (2a)
        float uv1 = (float) Math.pow(currentMaxSpeed / generator.maxSpeed, 2);
        float uv2 = (float) Math.pow(nextMaxSpeed / generator.maxSpeed, 2);

        // renderQuad(x, y, width, height, 1,1,1); // uv2, 1, uv1 , -1

        renderTextureQuad(x, y, width, height, uv1, 0, uv2-uv1, 1, r, g, b);
        return uv1 - uv2;
    }

    public static void test(){
        SpeedCurveLineRenderer renderer = new SpeedCurveLineRenderer(new SpeedCurveLine(100, 10));
        renderer.init();
        renderer.generator.bake();
        renderer.render();
        renderer.output();
        renderer.destroy();
    }
}
