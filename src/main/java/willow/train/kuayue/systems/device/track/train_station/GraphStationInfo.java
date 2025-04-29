package willow.train.kuayue.systems.device.track.train_station;

import net.minecraft.nbt.CompoundTag;

public record GraphStationInfo(
        String name,
        String shortenCode
) {
    public GraphStationInfo(CompoundTag compoundTag) {
        this(
                compoundTag.getString("name"),
                compoundTag.getString("shortenCode")
        );
    }

    public CompoundTag toNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", name);
        compoundTag.putString("shortenCode", shortenCode);
        return compoundTag;
    }
}
