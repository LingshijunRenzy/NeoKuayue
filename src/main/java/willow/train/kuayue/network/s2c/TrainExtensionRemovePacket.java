package willow.train.kuayue.network.s2c;

import kasuga.lib.core.network.S2CPacket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;

import java.util.UUID;

@Getter
public class TrainExtensionRemovePacket extends S2CPacket {

    private final UUID trainId;

    public TrainExtensionRemovePacket(UUID trainId) {
        this.trainId = trainId;
    }

    public TrainExtensionRemovePacket(FriendlyByteBuf buf) {
        this.trainId = buf.readUUID();
    }

    @Override
    public void handle(Minecraft minecraft) {
        Kuayue.TRAIN_EXTENSION.remove(this.trainId);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.trainId);
    }
}
