package willow.train.kuayue.systems.device.driver.path.point;

import willow.train.kuayue.systems.device.driver.path.list.EdgePathPoint;
import willow.train.kuayue.systems.device.driver.path.point.mark.StationMark;
import willow.train.kuayue.systems.device.driver.path.point.mark.StationMarkRenderer;

import java.util.HashMap;

public class EdgePathPointRenderManager {
    protected static HashMap<Class<?>, EdgePathPointRenderer<?>> RENDERERS = new HashMap<>();
    public static <T extends EdgePathPoint> void registerRenderer(
            Class<T> type,
            EdgePathPointRenderer<T> renderer
    ){
        RENDERERS.put(type, renderer);
    }

    public static void render(
            EdgePathPoint point,
            float left,
            float top,
            float yScale,
            float xScale
    ){
        EdgePathPointRenderer<?> renderer = RENDERERS.get(point.getClass());
        renderer.render(point, left, top, xScale, yScale);
    }

    static {
        registerRenderer(StationMark.class, new StationMarkRenderer());
    }
}
