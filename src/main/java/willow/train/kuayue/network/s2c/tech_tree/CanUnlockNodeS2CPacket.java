package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.block.recipe.BlueprintScreen;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

public class CanUnlockNodeS2CPacket extends S2CPacket {

    private final PlayerData.CheckReason reason;
    private final NodeLocation node;

    public CanUnlockNodeS2CPacket(NodeLocation node, PlayerData.CheckReason reason) {
        this.reason = reason;
        this.node = node;
    }

    public CanUnlockNodeS2CPacket(FriendlyByteBuf buf) {
        this.node = NodeLocation.readFromByteBuf(buf);
        this.reason = PlayerData.CheckReason.fromNetwork(buf);
    }

    @Override
    public void handle(Minecraft minecraft) {
        BlueprintScreen screen = BlueprintScreen.INSTANCE;
        if (screen == null) return;
        if (!screen.isFocusingLabel(node)) return;
        if (!reason.flag()) {
            screen.unableToUnlock(reason.requiredNodes(), reason.requiredItems());
        } else {
            screen.ableToUnlock();
        }
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        node.writeToByteBuf(friendlyByteBuf);
        reason.toNetwork(friendlyByteBuf);
    }
}
