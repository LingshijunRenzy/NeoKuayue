package willow.train.kuayue.network.c2s.tech_tree;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.tech_tree.CanUnlockNodeS2CPacket;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerDataManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeNode;

public class CanUnlockNodePacket extends C2SPacket {
    private final NodeLocation location;

    public CanUnlockNodePacket(NodeLocation location) {
        this.location = location;
    }

    public CanUnlockNodePacket(FriendlyByteBuf buf) {
        location = NodeLocation.readFromByteBuf(buf);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            PlayerData data = PlayerDataManager.MANAGER.getOrCreatePlayerData(player);
            TechTreeNode node = TechTreeManager.MANAGER.getNode(location);
            if (node == null) {
                sendReply(player, PlayerData.CheckReason.exceptionCase("Required Node must not be null."));
                return;
            }
            PlayerData.CheckReason reason = data.canUnlock(player, node);
            sendReply(player, reason);
        });
    }

    public void sendReply(ServerPlayer player, PlayerData.CheckReason reason) {
        AllPackets.TECH_TREE_CHANNEL.sendToClient(new CanUnlockNodeS2CPacket(this.location, reason), player);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        location.writeToByteBuf(friendlyByteBuf);
    }
}
