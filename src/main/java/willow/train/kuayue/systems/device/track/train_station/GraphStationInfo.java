package willow.train.kuayue.systems.device.track.train_station;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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

    public static GraphStationInfo EMPTY = new GraphStationInfo("", "");

    public CompoundTag toNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", name);
        compoundTag.putString("shortenCode", shortenCode);
        return compoundTag;
    }

    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(name);
        byteBuf.writeUtf(shortenCode);
    }

    public static GraphStationInfo read(FriendlyByteBuf byteBuf) {
        return new GraphStationInfo(
                byteBuf.readUtf(),
                byteBuf.readUtf()
        );
    }
}
