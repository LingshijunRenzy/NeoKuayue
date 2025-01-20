package willow.train.kuayue.systems.device.driver.path.point;

import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathPoint;

public class SimpleEdgePoint implements EdgePathPoint {
    private final double distance;

    public SimpleEdgePoint(double distance){
        this.distance = distance;
    }
    @Override
    public int compareTo(@NotNull EdgePathPoint object) {
        return Double.compare(distance(), object.distance());
    }

    @Override
    public double distance() {
        return distance;
    }
}
