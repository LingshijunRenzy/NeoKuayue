package willow.train.kuayue.network.s2c;

import com.simibubi.create.CreateClient;
import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;

public class TrainDividePacket extends S2CPacket {

    private final ConductorHelper.TrainDivideRequest request;

    public TrainDividePacket(ConductorHelper.TrainDivideRequest request) {
        this.request = request;
    }

    public TrainDividePacket(FriendlyByteBuf buf) {
        this.request = new ConductorHelper.TrainDivideRequest(
                CreateClient.RAILWAYS.trains.get(buf.readUUID()),
                buf.readUUID(),
                buf.readInt()
        );
    }

    @Override
    public void handle(Minecraft minecraft) {
        ConductorHelper.divideTrains(request.loco(), request.newTrainUUID(), request.carriageIndex(), true);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(request.loco().id);
        buf.writeUUID(request.newTrainUUID());
        buf.writeInt(request.carriageIndex());
    }
}
