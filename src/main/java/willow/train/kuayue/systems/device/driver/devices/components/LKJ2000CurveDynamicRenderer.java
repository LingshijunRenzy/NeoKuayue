package willow.train.kuayue.systems.device.driver.devices.components;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import kasuga.lib.core.client.frontend.common.layouting.LayoutBox;
import kasuga.lib.core.client.frontend.rendering.RenderContext;
import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.StaticImage;
import kasuga.lib.core.client.render.texture.StaticImageHolder;
import kasuga.lib.core.client.render.texture.Vec2f;
import kasuga.lib.core.resource.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.mixins.mixin.client.TextureManagerAccessor;
import willow.train.kuayue.systems.device.driver.path.SpeedCurveGenerator;
import willow.train.kuayue.systems.device.driver.path.renderer.LKJ2000SpeedLimitCurveRenderer;

import java.util.UUID;

public class LKJ2000CurveDynamicRenderer {
    UUID uuid;
    ResourceLocation location;
    TargetTexture texture;
    LKJ2000SpeedLimitCurveRenderer renderer;

    int width, height;

    ImageMask mask;
    private SpeedCurveGenerator generator;
    private float scale;
    private TextureTarget target;

    LKJ2000CurveDynamicRenderer(){
        uuid = UUID.randomUUID();
        location = new Resources.CheatResourceLocation(AllElements.testRegistry.namespace,"lkj2000/"+uuid);
    }
    public void init(SpeedCurveGenerator generator, float scale, int width, int height){
        if( texture != null )
            close();
        if(width == 0 || height == 0) {
            return;
        }
        this.generator = generator;
        this.scale = scale;
        TextureTarget textureTarget = new TextureTarget(width, height, false, true);
        TargetTexture texture = new TargetTexture(textureTarget);
        Minecraft.getInstance().textureManager.register(location, texture);
        renderer = new LKJ2000SpeedLimitCurveRenderer(textureTarget, generator, scale);
        this.texture = texture;
        this.target = textureTarget;
        this.width = width;
        this.height = height;
        renderer.init();
        renderer.setDisplayMaxSpeed(140.0F);
        renderCurve();
    }

    public void close() {
        if( texture == null )
            return;
        ((TextureManagerAccessor) Minecraft.getInstance().textureManager).getByPath().remove(location);
        this.texture.close();
        this.target.destroyBuffers();
        this.renderer.destroy();
        this.texture = null;
        this.renderer = null;
        this.target = null;
    }

    public void renderCurve(){
        renderer.render();
    }

    public void render(RenderContext context, LayoutBox coordinate) {
        if (texture == null)
            return;

        if (context.getContextType() == RenderContext.RenderContextType.WORLD || context.getBufferSource() != null) {
            RenderType type = RenderType.text(location);
            VertexConsumer consumer = context.getBufferSource().getBuffer(type);
            Matrix4f matrix = context.poseMatrix();
            SimpleColor color = SimpleColor.fromRGBA(255, 255, 255, 255);
            int light = context.getLight();
            buildVertex(consumer, matrix, new Vector3f(coordinate.x, -coordinate.y, 0.5f), new Vec2f(0, 1), color, light);

            buildVertex(consumer, matrix, new Vector3f(coordinate.x, -coordinate.y - coordinate.height, 0.5f), new Vec2f(0, 0), color, light);

            buildVertex(consumer, matrix, new Vector3f(coordinate.x + coordinate.width, -coordinate.y - coordinate.height, 0.5f), new Vec2f(1, 0), color, light);

            buildVertex(consumer, matrix, new Vector3f(coordinate.x + coordinate.width, -coordinate.y, 0.5f), new Vec2f(1, 1), color, light);

        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, location);
            RenderSystem.enableBlend();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            Matrix4f poseMatrix = context.poseMatrix();
            buffer.vertex(poseMatrix, coordinate.x, coordinate.y, 0).uv(0, 1).endVertex();
            buffer.vertex(poseMatrix, coordinate.x, coordinate.y + coordinate.height, 0).uv(0, 0).endVertex();
            buffer.vertex(poseMatrix, coordinate.x + coordinate.width, coordinate.y + coordinate.height, 0).uv(1, 0).endVertex();
            buffer.vertex(poseMatrix, coordinate.x + coordinate.width, coordinate.y, 0).uv(1, 1).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.disableBlend();
        }
    }

    private void buildVertex(VertexConsumer consumer, Matrix4f matrix,
                             Vector3f pos, Vec2f uv, SimpleColor color, int light) {
        consumer.vertex(matrix, pos.x(), pos.y(), pos.z())
                .color(color.getfR(), color.getfG(), color.getfB(), color.getA())
                .uv(uv.x(), uv.y())
                .uv2(light)
                .endVertex();
    }

    public void updateSize(int width, int height) {
        if( texture == null )
            return;
        if(  width != this.width || height != this.height ){
            this.init(generator, scale, width, height);
        }
    }

    boolean isDirty = false;

    public void update(){
        this.renderer.update();
        if(isDirty) {
            renderCurve();
        }
    }
}
