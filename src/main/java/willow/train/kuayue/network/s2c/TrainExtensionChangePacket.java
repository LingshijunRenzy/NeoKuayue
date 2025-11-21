package willow.train.kuayue.network.s2c;

import kasuga.lib.core.network.S2CPacket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;

@Getter
public class TrainExtensionChangePacket extends S2CPacket {

    private final TrainAdditionalData data;

    public TrainExtensionChangePacket(TrainAdditionalData data) {
        this.data = data;
    }

    public TrainExtensionChangePacket(FriendlyByteBuf buf) {
        TrainAdditionalData data = null;
        CompoundTag nbt = buf.readNbt();
        if(nbt != null) {
            data = new TrainAdditionalData(nbt);
        }
        this.data = data;
    }

    @Override
    public void handle(Minecraft minecraft) {
        if(this.data != null) {
            Kuayue.TRAIN_EXTENSION.add(this.data);
        }
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag nbt = new CompoundTag();
        this.data.write(nbt);
        friendlyByteBuf.writeNbt(nbt);
    }
}
