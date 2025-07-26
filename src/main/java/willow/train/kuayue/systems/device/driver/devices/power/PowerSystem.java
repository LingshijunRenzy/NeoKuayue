package willow.train.kuayue.systems.device.driver.devices.power;

import kasuga.lib.core.create.device.TrainDeviceManager;
import kasuga.lib.core.create.device.TrainDeviceSystem;
import kasuga.lib.core.javascript.engine.JavascriptValue;
import kasuga.lib.core.javascript.engine.annotations.HostAccess;

import java.util.*;

public class PowerSystem extends TrainDeviceSystem {

    private double speed;
    private double targetSpeed;

    private int directionState = 0;

    private int powerState = 0;

    private int breakIState = 0;

    private int breakDState = 0;



    public PowerSystem(TrainDeviceManager manager) {
        super(manager);
    }

    @HostAccess.Export
    public int getDirectionState() {
        return directionState;
    }

    @HostAccess.Export
    public void setDirectionState(JavascriptValue value) {
        this.directionState = value.asInt();
    }

    @HostAccess.Export
    public int getSimplePowerState() {
        return powerState;
    }

    @HostAccess.Export
    public void setSimplePowerState(JavascriptValue value) {
        this.powerState = value.asInt();
    }

    @HostAccess.Export
    public int getSimpleBreakIState() {
        return breakIState;
    }

    @HostAccess.Export
    public void setSimpleBreakIState(JavascriptValue value) {
        this.breakIState = value.asInt();
    }

    @HostAccess.Export
    public int getSimpleBreakDState() {
        return breakDState;
    }

    @HostAccess.Export
    public void setSimpleBreakDState(JavascriptValue value) {
        this.breakDState = value.asInt();
    }


    @Override
    public Optional<Double> beforeSpeed() {
        this.updateSpeed();
        return speed == 0 ? Optional.empty() : Optional.of(speed);
    }

    private void updateSpeed() {
        targetSpeed = powerState * 0.2 * directionState;
        double delta = targetSpeed - speed;
        speed = (speed + Math.signum(delta) * Math.max(Math.signum(directionState) == Math.signum(speed) ? 0 : Float.MIN_VALUE, Math.abs(delta) * 0.0002)) * (0.9999 - breakIState * 0.0025 - breakDState * 0.005);
        if(Math.abs(speed) < 0.00001)
            speed = 0;
    }
}