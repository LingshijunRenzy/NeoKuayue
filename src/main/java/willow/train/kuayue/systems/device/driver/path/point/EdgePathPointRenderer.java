package willow.train.kuayue.systems.device.driver.path.point;

import willow.train.kuayue.systems.device.driver.path.list.EdgePathPoint;

public interface EdgePathPointRenderer<T extends EdgePathPoint> {
    public void render(EdgePathPoint point, float left, float top, float xScale, float yScale);
}
