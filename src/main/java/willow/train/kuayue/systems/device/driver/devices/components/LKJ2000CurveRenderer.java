package willow.train.kuayue.systems.device.driver.devices.components;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import kasuga.lib.KasugaLib;
import kasuga.lib.core.client.frontend.common.layouting.LayoutBox;
import kasuga.lib.core.client.frontend.common.layouting.LayoutNode;
import kasuga.lib.core.client.frontend.gui.GuiContext;
import kasuga.lib.core.client.frontend.gui.nodes.GuiViewNode;
import kasuga.lib.core.client.frontend.rendering.RenderContext;
import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.client.render.texture.StaticImage;
import kasuga.lib.core.client.render.texture.Vec2f;
import kasuga.lib.core.javascript.engine.annotations.HostAccess;
import kasuga.lib.core.util.LazyRecomputable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeRenderTypes;
import willow.train.kuayue.systems.device.driver.path.SpeedCurveGenerator;
import willow.train.kuayue.systems.device.driver.path.renderer.LKJ2000SpeedLimitCurveRenderer;

import java.io.IOException;

public class LKJ2000CurveRenderer extends GuiViewNode {
    protected SpeedCurveGenerator generator;
    protected LayoutBox layoutBox;

    protected LazyRecomputable<TextureTarget> texture = LazyRecomputable.of(
            ()->{
                if(layoutBox == null || layoutBox.width == 0 || layoutBox.height == 0){
                    return null;
                }

                return new TextureTarget(
                        (int)(layoutBox.width*2),
                        (int)(layoutBox.height*2),
                        true,
                        true
                );
            }
    );

    protected LazyRecomputable<LKJ2000SpeedLimitCurveRenderer> curveRenderer = LazyRecomputable.of(
            ()->{
                if(texture.get() == null || generator == null)
                    return null;
                return new LKJ2000SpeedLimitCurveRenderer(
                        texture.get(),
                        generator,
                        0.5f
                );
            }
    );

    protected LazyRecomputable<RenderType> renderType = LazyRecomputable.of(()->{
        TextureTarget textureTarget = texture.get();
        return null;
    });

    public LKJ2000CurveRenderer(GuiContext context) {
        super(context);
    }

    @Override
    public void render(Object source, RenderContext context) {
        super.render(source, context);
    }

    @Override
    public float renderNode(Object source, RenderContext context) {
        if (!this.getLayoutManager().hasSource(source)) {
            return 0.0F;
        }

        LayoutNode layout = this.getLayoutManager().getSourceNode(source);
        LayoutBox coordinate = layout.getPosition();

        if(layoutBox != coordinate){
            if(texture.get() != null){
                RenderSystem.recordRenderCall(()->{
                    texture.get().destroyBuffers();
                });
            }
            texture.clear();
            curveRenderer.clear();
            renderType.clear();
            layoutBox = coordinate;
        }

        if(texture.get() == null){
            return 0.0F;
        }

        context.pose().pushPose();
        context.pose().translate(0, 0, 0.003);
        if(context.getContextType() == RenderContext.RenderContextType.SCREEN){
            VertexConsumer consumer = context.getBufferSource().getBuffer(renderType.get());
            Matrix4f matrix = context.poseMatrix();
            SimpleColor color = SimpleColor.fromRGB(1,1,1);
            int light = context.getLight();
            buildVertex(consumer, matrix, new Vector3f(coordinate.x, coordinate.y, 0), new Vec2f(0,0), color, light);
            buildVertex(consumer, matrix, new Vector3f(coordinate.x, coordinate.y + coordinate.height,0), new Vec2f(0,1), color, light);
            buildVertex(consumer, matrix, new Vector3f(coordinate.x + coordinate.width, coordinate.y + coordinate.height, 0), new Vec2f(1,1), color, light);
            buildVertex(consumer, matrix, new Vector3f(coordinate.x, coordinate.y + coordinate.height,0), new Vec2f(1,0), color, light);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture.get().getColorTextureId());
            RenderSystem.setShaderColor(1,1,1,1);
            RenderSystem.enableBlend();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(coordinate.x, coordinate.y, 0).uv(0, 0).endVertex();
            buffer.vertex(coordinate.x, coordinate.y + coordinate.height, 0).uv(0, 1).endVertex();
            buffer.vertex(coordinate.x + coordinate.width, coordinate.y + coordinate.height, 0).uv(1,1).endVertex();
            buffer.vertex(coordinate.x, coordinate.y + coordinate.height,0).uv(1,0).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.disableBlend();
        }
        context.pose().popPose();
        return 0.003F;
    }

    public void renderTick(){
        if(layoutBox == null || layoutBox.width == 0 || layoutBox.height == 0 || generator == null)
            return;
        curveRenderer.get().render();
    }

    @HostAccess.Export
    public void setGenerator(SpeedCurveGenerator generator) {
        this.generator = generator;
        curveRenderer.clear();
    }

    private void buildVertex(VertexConsumer consumer, Matrix4f matrix,
                             Vector3f pos, Vec2f uv, SimpleColor color, int light) {
        consumer.vertex(matrix, pos.x(), pos.y(), pos.z())
                .color(color.getfR(), color.getfG(), color.getfB(), color.getA())
                .uv(uv.x(), uv.y())
                .uv2(light)
                .endVertex();
    }

    public static void init(){
        KasugaLib
                .STACKS
                .GUI
                .orElseThrow()
                .nodeTypeRegistry
                .register("kuayue:lkj_2000_curve", LKJ2000CurveRenderer::new);
    }
}
