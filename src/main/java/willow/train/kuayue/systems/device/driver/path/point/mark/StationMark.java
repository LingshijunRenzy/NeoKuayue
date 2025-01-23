package willow.train.kuayue.systems.device.driver.path.point.mark;

import willow.train.kuayue.systems.device.driver.path.point.SimpleEdgePoint;

public class StationMark extends SimpleEdgePoint {
    public final String stationName;
    public StationMark(double distance, String stationName) {
        super(distance);
        this.stationName = stationName;
    }

    public String getStationName() {
        return stationName;
    }
}
