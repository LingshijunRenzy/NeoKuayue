package willow.train.kuayue.systems.device.driver.devices.power;

import kasuga.lib.core.create.device.TrainDeviceLocation;
import kasuga.lib.core.create.device.TrainDeviceManager;
import kasuga.lib.core.create.device.TrainDeviceSystem;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PantographSystem extends TrainDeviceSystem {
    public PantographSystem(TrainDeviceManager manager) {
        super(manager);
    }

    private final HashMap<TrainDeviceLocation, Pair<Direction.AxisDirection, PantographState>> pantographStates = new HashMap<>();


    public PantographState getOrRegisterPantographState(TrainDeviceLocation locator, Direction.AxisDirection direction) {
        return pantographStates.computeIfAbsent(locator, k -> Pair.of(direction, PantographState.NONE)).getSecond();
    }

    public Iterator<Map.Entry<TrainDeviceLocation, Pair<Direction.AxisDirection, PantographState>>> getPantographStates() {
        return pantographStates.entrySet().iterator();
    }

    public void setPantographState(TrainDeviceLocation locator, PantographState state) {
        Pair<Direction.AxisDirection, PantographState> pair = pantographStates.get(locator);
        if (pair != null) {
            Direction.AxisDirection direction = pair.getFirst();
            pantographStates.put(locator, Pair.of(direction, state));
        }
    }
}
