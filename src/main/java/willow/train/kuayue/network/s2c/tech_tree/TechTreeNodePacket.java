package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.tech_tree.client.ClientNetworkCache;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;
import willow.train.kuayue.systems.tech_tree.server.TechTreeNode;

import java.util.UUID;

public class TechTreeNodePacket extends S2CPacket {

    private TechTreeNode node;
    private final UUID batch;
    private ClientTechTreeNode clientNode;

    public TechTreeNodePacket(UUID batch, TechTreeNode node) {
        this.node = node;
        this.batch = batch;
    }

    public TechTreeNodePacket(FriendlyByteBuf buf) {
        super(buf);
        batch = buf.readUUID();
        this.clientNode = new ClientTechTreeNode(buf);
    }

    @Override
    public void handle(Minecraft minecraft) {
        ClientNetworkCache.INSTANCE.addNode(clientNode);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(batch);
        node.toNetwork(friendlyByteBuf);
    }
}
