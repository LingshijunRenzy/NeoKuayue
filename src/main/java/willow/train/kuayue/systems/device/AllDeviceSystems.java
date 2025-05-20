package willow.train.kuayue.systems.device;

import kasuga.lib.core.create.device.TrainDeviceRegistry;
import kasuga.lib.core.create.device.TrainDeviceSystemType;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.device.driver.devices.power.PantographSystem;

public class AllDeviceSystems {
    public static final TrainDeviceSystemType<PantographSystem> PANTOGRAPH = new TrainDeviceSystemType<>(PantographSystem::new);

    public static void invoke(){
        TrainDeviceRegistry.register(AllElements.testRegistry.asResource("pantograph"), PANTOGRAPH);
    }
}
