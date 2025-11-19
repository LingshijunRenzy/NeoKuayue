package willow.train.kuayue.event.server;

import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

@Getter
public class TrainCrashEvent extends Event {
    public final UUID trainId;
    public final UUID otherId;
    public final float crashDeltaSpeed;
    public final float reverseCoefficient;
    public final Pair<Float, Float> speedBeforeCrash;
    public final Pair<Float, Float> speedAfterCrash;

    public TrainCrashEvent(UUID trainId, UUID otherId, float crashDeltaSpeed,
                           float reverseCoefficient,
                           Pair<Float, Float> speedBeforeCrash,
                           Pair<Float, Float> speedAfterCrash) {
        this.trainId = trainId;
        this.otherId = otherId;
        this.crashDeltaSpeed = crashDeltaSpeed;
        this.reverseCoefficient = reverseCoefficient;
        this.speedBeforeCrash = speedBeforeCrash;
        this.speedAfterCrash = speedAfterCrash;
    }
}
