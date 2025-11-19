package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.tech_tree.client.ClientNetworkCache;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTree;
import willow.train.kuayue.systems.tech_tree.server.TechTree;

import java.util.UUID;

public class TechTreePacket extends S2CPacket {

    private TechTree tree;
    private final UUID batch;
    private ClientTechTree clientTree;

    public TechTreePacket(UUID batch, TechTree tree) {
        this.tree = tree;
        this.batch = batch;
    }

    public TechTreePacket(FriendlyByteBuf buf) {
        super(buf);
        this.batch = buf.readUUID();
        this.clientTree = new ClientTechTree(buf);
    }

    @Override
    public void handle(Minecraft minecraft) {
        ClientNetworkCache.INSTANCE.setTree(clientTree);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(batch);
        tree.toNetwork(friendlyByteBuf);
    }
}
