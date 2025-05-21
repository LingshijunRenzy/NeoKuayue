package willow.train.kuayue.systems.device.driver.path;

import willow.train.kuayue.systems.device.driver.path.list.*;
import willow.train.kuayue.systems.device.driver.path.range.LineRange;
import willow.train.kuayue.systems.device.driver.path.range.MergeRange;
import willow.train.kuayue.systems.device.driver.path.renderer.SpeedCurveLine;

import java.util.List;
import java.util.TreeMap;

public class SpeedCurveGenerator {
    public double backwardDistance = -1000.0D; // 1km
    public double forwardDistance = 3000.0D; // 3km
    protected EdgePathPointList<SpeedEdgePathPoint> linePoints
            = new EdgePathPointList<>(forwardDistance, backwardDistance); // 前方信号机

    protected EdgePathLineList<SpeedEdgePathLine> announceLines
            = new EdgePathLineList<>(forwardDistance, backwardDistance); // 揭示


    public EdgePathLineList<SpeedEdgePathLine> specialLines
            = new EdgePathLineList<>(forwardDistance, backwardDistance); // 特殊行车

    public EdgePathPointList<EdgePathPoint> points = new EdgePathPointList<>(
            forwardDistance,
            backwardDistance
    );


    protected MergeRange range;

    public double distance = 0.0D;
    public double maxSpeed = 0.0D;
    public double acceleration = 1.0D;

    public double specialSpeed = Double.NaN;

    public final SpeedCurveLine curveLine = new SpeedCurveLine(0,1.0D);
    protected float maxBreakDistance = 0.0F;

    public SpeedCurveGenerator(){
        range = new MergeRange(List.of(
                new MergeRange(List.of(
                        new LineRange<>(announceLines)
                ), MergeRange.MergingRole.MIN),
                new LineRange<>(specialLines)
        ), MergeRange.MergingRole.MAX);

        curveLine.bake();
    }

    public void setAcceleration(double acceleration){
        this.acceleration = acceleration;
        this.maxBreakDistance = ((float) getBreakDistance(maxSpeed, 0));
        curveLine.setAcceleration(acceleration);
        curveLine.bake();
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        this.maxBreakDistance = ((float) getBreakDistance(maxSpeed, 0));
        curveLine.setMaxSpeed(maxSpeed);
        curveLine.bake();
    }

    public double getBreakDistance(double speed, double targetSpeed){
        // v^2 - v_0 ^2 = 2 ax
        return (speed * speed - targetSpeed * targetSpeed) *0.5 / acceleration;
    }

    protected double getBreakingLimitSpeed(double speed, double targetSpeed, double deltaDistance){
        return Math.max(Math.sqrt(speed * speed - 2 * acceleration * deltaDistance), targetSpeed);
    }

    public double getSpeedLimit(){
        TreeMap<Double, Double> speedLimitMap = range.finalGet();
        var back = speedLimitMap.floorEntry(distance);
        var ahead = speedLimitMap.ceilingEntry(distance);

        if(!Double.isNaN(specialSpeed)){
            return specialSpeed;
        }

        if(back == null){
            return maxSpeed;
        }

        if(ahead == null){
            return back.getValue();
        }

        double aheadDistance = ahead.getKey();
        double backSpeed = back.getValue();
        double aheadSpeed = ahead.getValue();
        double breakDistance = getBreakDistance(backSpeed, aheadSpeed);

        if(this.distance + breakDistance > aheadDistance){
            return getBreakingLimitSpeed(backSpeed, aheadSpeed, aheadDistance - this.distance);
        }

        return backSpeed;
    }

    public TreeMap<Double, Double> getSpeedMap(){
        return range.finalGet();
    }

    public void addDistance(double distance) {
        this.distance += distance;
        this.linePoints.addDistance(distance);
        this.announceLines.addDistance(distance);
        this.specialLines.addDistance(distance);
    }

    protected double getLeftPositionInCurve(double speed) {
        return getBreakDistance(maxSpeed, 0);
    }
}
