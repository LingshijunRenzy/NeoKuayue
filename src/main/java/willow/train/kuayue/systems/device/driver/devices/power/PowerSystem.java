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
        return Optional.of(speed);
    }

    private void updateSpeed() {
        targetSpeed = powerState * 0.2 * directionState;
        speed = (speed + (targetSpeed - speed) * 0.01) * (0.999 - breakIState * 0.008 - breakDState * 0.01);
    }
}