package willow.train.kuayue.network.s2c;

import com.simibubi.create.CreateClient;
import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;

public class TrainMergePacket extends S2CPacket {

    private final ConductorHelper.TrainMergeRequest request;

    public TrainMergePacket(ConductorHelper.TrainMergeRequest request) {
        this.request = request;
    }

    public TrainMergePacket(FriendlyByteBuf buf) {
        this.request = new ConductorHelper.TrainMergeRequest(
                CreateClient.RAILWAYS.trains.get(buf.readUUID()),
                CreateClient.RAILWAYS.trains.get(buf.readUUID()),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt(),
                true
        );
    }

    @Override
    public void handle(Minecraft minecraft) {
        ConductorHelper.mergeTrains(
                request.loco(), request.carriages(),
                request.shouldReverseCarriages(), request.isLocoHead(),
                request.spacing(), true);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(request.loco().id);
        buf.writeUUID(request.carriages().id);
        buf.writeBoolean(request.shouldReverseCarriages());
        buf.writeBoolean(request.isLocoHead());
        buf.writeInt(request.spacing());
    }
}
