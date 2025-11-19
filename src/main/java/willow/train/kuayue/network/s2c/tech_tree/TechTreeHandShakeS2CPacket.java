package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.c2s.tech_tree.TechTreeHandShakeC2SPacket;
import willow.train.kuayue.systems.tech_tree.NetworkState;
import willow.train.kuayue.systems.tech_tree.client.ClientNetworkCache;

import java.util.UUID;

public class TechTreeHandShakeS2CPacket extends S2CPacket {

    private final UUID batchId;

    public TechTreeHandShakeS2CPacket(UUID batch) {
        batchId = batch;
    }

    public TechTreeHandShakeS2CPacket(FriendlyByteBuf buf) {
        super(buf);
        this.batchId = buf.readUUID();
    }
    @Override
    public void handle(Minecraft minecraft) {
        Kuayue.LOGGER.debug("[CLIENT] TechTree HandshakePacket called, batchId: {}", batchId);
        NetworkState state = ClientNetworkCache.INSTANCE.queryState(batchId);
        if (state == NetworkState.STANDING_BY) {
            Kuayue.LOGGER.debug("[CLIENT] TechTree ClientNetworkCache Starting batch: " + batchId);
            ClientNetworkCache.INSTANCE.startBatch(batchId);
        }
        //AllPackets.TECH_TREE_CHANNEL.sendToServer(new TechTreeHandShakeC2SPacket(state));
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(batchId);
    }
}
