package willow.train.kuayue.systems.device.driver.path.point.mark;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.StaticImage;
import net.minecraft.client.Minecraft;
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
        LKJ2000SpeedLimitCurveRenderer.renderQuad(left, 10, 1, top, 0,0, 1);
        Minecraft.getInstance().font.draw(new PoseStack(), stationMark.getStationName(), left + 8, 1, 0xFFFFFF);
    }
}
