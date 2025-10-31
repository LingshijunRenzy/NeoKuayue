package willow.train.kuayue.network.s2c;

import com.simibubi.create.CreateClient;
import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;
import willow.train.kuayue.systems.train_extension.TrainExtensionSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrainExtensionSyncPacket extends S2CPacket {

    private final HashMap<UUID, TrainAdditionalData> data;

    public TrainExtensionSyncPacket(FriendlyByteBuf buf) {
        data = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            CompoundTag nbt = buf.readNbt();
            if (nbt == null) continue;
            TrainAdditionalData d = new TrainAdditionalData(nbt);
            data.put(uuid, d);
        }
    }

    public TrainExtensionSyncPacket(TrainExtensionSystem system) {
        data = system.getData();
    }

    @Override
    public void handle(Minecraft minecraft) {
        Kuayue.TRAIN_EXTENSION.clientSync(this.data);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(data.size());
        for (Map.Entry<UUID, TrainAdditionalData> entry : data.entrySet()) {
            friendlyByteBuf.writeUUID(entry.getKey());
            CompoundTag nbt = new CompoundTag();
            entry.getValue().write(nbt);
            friendlyByteBuf.writeNbt(nbt);
        }
    }
}
