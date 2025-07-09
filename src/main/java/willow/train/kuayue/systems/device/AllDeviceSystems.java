package willow.train.kuayue.systems.device;

import kasuga.lib.core.create.device.TrainDeviceRegistry;
import kasuga.lib.core.create.device.TrainDeviceSystemType;
import kasuga.lib.registrations.common.MenuReg;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.device.driver.devices.power.PantographSystem;
import willow.train.kuayue.systems.device.driver.devices.power.PowerSystem;
import willow.train.kuayue.systems.device.track.train_station.TrainStationMenu;
import willow.train.kuayue.systems.device.track.train_station.TrainStationScreen;

public class AllDeviceSystems {
    public static final TrainDeviceSystemType<PantographSystem> PANTOGRAPH = new TrainDeviceSystemType<>(PantographSystem::new);

    public static final TrainDeviceSystemType<PowerSystem> POWER = new TrainDeviceSystemType<>(PowerSystem::new);

    public static final MenuReg<TrainStationMenu, TrainStationScreen> STATION_SCREEN = new MenuReg<TrainStationMenu, TrainStationScreen>("station_screen")
            .withMenuAndScreen(TrainStationMenu::new, ()->TrainStationScreen::new)
            .submit(AllElements.testRegistry);

    public static void invoke(){
        TrainDeviceRegistry.register(AllElements.testRegistry.asResource("pantograph"), PANTOGRAPH);
        TrainDeviceRegistry.register(AllElements.testRegistry.asResource("power"), POWER);
    }
}
