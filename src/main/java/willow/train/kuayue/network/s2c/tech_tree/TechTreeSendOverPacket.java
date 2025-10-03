package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager;

public class TechTreeSendOverPacket extends S2CPacket {

    public TechTreeSendOverPacket() {
    }

    public TechTreeSendOverPacket(FriendlyByteBuf friendlyByteBuf) {
        super(friendlyByteBuf);
    }

    @Override
    public void handle(Minecraft minecraft) {
        ClientTechTreeManager.MANAGER.trees().forEach((s, tree) -> {
            tree.update();
        });
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {

    }
}
