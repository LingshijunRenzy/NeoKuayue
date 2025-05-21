package willow.train.kuayue.systems.device.driver.path.data;

import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathLine;
import willow.train.kuayue.systems.device.driver.path.list.SpeedEdgePathLine;

public class AnnounceEdgePathLine implements SpeedEdgePathLine {
    @Override
    public int compareTo(@NotNull EdgePathLine object) {
        return 0;
    }

    @Override
    public double distance() {
        return 0;
    }

    @Override
    public double length() {
        return 0;
    }

    @Override
    public double getSpeed() {
        return 0;
    }
}
