package willow.train.kuayue.systems.device.driver.devices.power;

import kasuga.lib.core.create.device.TrainDeviceManager;
import kasuga.lib.core.create.device.TrainDeviceSystem;
import kasuga.lib.core.javascript.engine.JavascriptValue;
import kasuga.lib.core.javascript.engine.annotations.HostAccess;

import java.util.*;

public class PowerSystem extends TrainDeviceSystem {

    private int state;// P1~P8 ( 1~8 ); N1~N4( -1 ~ -4  )


    public PowerSystem(TrainDeviceManager manager) {
        super(manager);
    }

    @HostAccess.Export
    public int getSimplePowerState() {
        return state;
    }

    @HostAccess.Export
    public void setSimplePowerState(JavascriptValue value) {
        this.state = value.asInt();
    }

    @Override
    public Optional<Double> beforeSpeed() {
        return Optional.empty();
    }
}