package willow.train.kuayue.systems.train_extension;

import com.simibubi.create.content.trains.entity.Train;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;

public class TrainExtensionConstants {

    public static final HashMap<Train, Train> colliedTrains = new HashMap<>();

    @SubscribeEvent
    public static void onLevelUnloaded(LevelEvent.Unload event) {
        colliedTrains.clear();
    }
}
