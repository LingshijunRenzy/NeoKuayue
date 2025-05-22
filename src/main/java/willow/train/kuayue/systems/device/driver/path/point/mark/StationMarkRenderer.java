package willow.train.kuayue.systems.device.driver.path.point.mark;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.StaticImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.device.driver.path.PathMarkRenderTexture;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathPoint;
import willow.train.kuayue.systems.device.driver.path.point.EdgePathPointRenderer;
import willow.train.kuayue.systems.device.driver.path.renderer.LKJ2000SpeedLimitCurveRenderer;

import java.io.IOException;

public class StationMarkRenderer implements EdgePathPointRenderer<StationMark> {
    @Override
    public void render(EdgePathPoint point, float left, float top, float xScale, float yScale) {
        if(!(point instanceof StationMark stationMark)){
            return;
        }
        ImageMask mask = PathMarkRenderTexture.STATION_LABEL.get();
        mask.setPosition(
                left - 6,0, 0,
                left + 6, 0,0,
                left - 6,12,0,
                left + 6,12,0
        );
        mask.renderToGui();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        LKJ2000SpeedLimitCurveRenderer.renderQuad(bufferBuilder ,left, 10, 1, top, 0,0, 1);

        tesselator.end();

        RenderSystem.disableBlend();
        GuiGraphics guiGraphics = new GuiGraphics(
                Minecraft.getInstance(),
                Minecraft.getInstance().renderBuffers().bufferSource()
        );
        guiGraphics.drawString(Minecraft.getInstance().font, stationMark.getStationName(),
                Math.round(left + 8), 1, 0xffffff);
        // Minecraft.getInstance().font.draw(new PoseStack(), stationMark.getStationName(), left + 8, 1, 0xFFFFFF);
    }
}
