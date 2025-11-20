package willow.train.kuayue.systems.train_extension.conductor;

import kasuga.lib.core.base.NbtSerializable;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

@Getter
public class ConductorLocation implements NbtSerializable {

    private final UUID trainId;
    private final int carriageIndex;
    private final boolean isLeading;

    public ConductorLocation(UUID trianId,
                             int carriageIndex,
                             boolean isLeading) {
        this.trainId = trianId;
        this.carriageIndex = carriageIndex;
        this.isLeading = isLeading;
    }

    public ConductorLocation(CompoundTag tag) {
        this.trainId = tag.getUUID("trainId");
        this.carriageIndex = tag.getInt("carriageIndex");
        this.isLeading = tag.getBoolean("isLeading");
    }

    @Override
    public void write(CompoundTag nbt) {
        nbt.putUUID("trainId", trainId);
        nbt.putInt("carriageIndex", carriageIndex);
        nbt.putBoolean("isLeading", isLeading);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConductorLocation cl)) return false;
        return trainId.equals(cl.trainId) &&
                carriageIndex == cl.carriageIndex &&
                (isLeading == cl.isLeading);
    }

    @Override
    public int hashCode() {
        return  Objects.hash(trainId, carriageIndex, isLeading);
    }

    @Override
    @Deprecated
    public void read(CompoundTag nbt) {}
}
