package willow.train.kuayue.systems.device.track.train_station.packet;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.device.track.train_station.GraphStationInfo;

import java.util.Objects;
import java.util.UUID;

public class C2STrainStationInfoUpdatePacket extends C2SPacket {
    public final UUID id;
    public final GraphStationInfo info;
    public C2STrainStationInfoUpdatePacket(FriendlyByteBuf buf) {
        super(buf);
        this.id = buf.readUUID();
        this.info = new GraphStationInfo(Objects.requireNonNull(buf.readNbt()));
    }

    public C2STrainStationInfoUpdatePacket(UUID id, GraphStationInfo info) {
        super();
        this.id = id;
        this.info = info;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            Kuayue.RAILWAY.getSavedData().getOptionalStation(this.id)
                    .ifPresent(station -> {
                        station.updateInfo(this.info);
                    });
        });
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(id);
        friendlyByteBuf.writeNbt(info.toNbt());
    }
}
