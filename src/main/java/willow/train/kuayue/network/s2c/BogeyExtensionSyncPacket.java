package willow.train.kuayue.network.s2c;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.bogey_weight.BogeyAdditionalData;
import willow.train.kuayue.systems.train_extension.bogey_weight.BogeyExtensionSystem;

import java.util.HashMap;
import java.util.HashSet;

public class BogeyExtensionSyncPacket extends S2CPacket {

    private final HashSet<BogeyAdditionalData> data;

    public BogeyExtensionSyncPacket(BogeyExtensionSystem system) {
        data = new HashSet<>(system.getBogeyData().values());
    }

    public BogeyExtensionSyncPacket(FriendlyByteBuf buf) {
        data = new HashSet<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            CompoundTag nbt = buf.readNbt();
            if (nbt == null) continue;
            data.add(new BogeyAdditionalData(nbt));
        }
    }

    @Override
    public void handle(Minecraft minecraft) {
        Kuayue.TRAIN_EXTENSION.BOGEY_EXTENSION.clientSync(data);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(data.size());
        data.forEach(d -> {
            CompoundTag nbt = new CompoundTag();
            d.write(nbt);
            friendlyByteBuf.writeNbt(nbt);
        });
    }
}
