package willow.train.kuayue.event.server;

import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;

import java.util.UUID;

@Getter
public class TrainCouplerPostDivideEvent extends Event {
    public final UUID locoId;
    public final UUID carriageId;
    public final int carriageIndex;
    public final float locoSpeed;
    public final Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> conductors;

    public TrainCouplerPostDivideEvent(
            UUID locoId,
            UUID carriageId,
            int carriageIndex,
            float locoSpeed,
            Pair<Pair<Conductable, Vec3>, Pair<Conductable, Vec3>> conductors) {
        this.locoId = locoId;
        this.carriageId = carriageId;
        this.carriageIndex = carriageIndex;
        this.locoSpeed = locoSpeed;
        this.conductors = conductors;
    }
}
