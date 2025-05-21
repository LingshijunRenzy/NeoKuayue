package willow.train.kuayue.network.c2s.tech_tree;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.tech_tree.UnlockNodeResultPacket;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerDataManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeNode;

public class UnlockNodePacket extends C2SPacket {

    private final NodeLocation node;
    public UnlockNodePacket(NodeLocation node) {
        this.node = node;
    }

    public UnlockNodePacket(FriendlyByteBuf buf) {
        node = NodeLocation.readFromByteBuf(buf);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ServerLevel level = player.getLevel();
            PlayerData data = PlayerDataManager.MANAGER.getPlayerData(player);
            if (data == null) {
                sendReply(player, PlayerData.UnlockResult.failedEmpty());
                return;
            }
            TechTreeNode techTreeNode = TechTreeManager.MANAGER.getNode(node);
            if (techTreeNode == null) {
                sendReply(player, PlayerData.UnlockResult.failedEmpty());
                return;
            }
            sendReply(player, data.unlock(level, player, techTreeNode));
        });
    }

    public void sendReply(ServerPlayer player, PlayerData.UnlockResult result) {
        AllPackets.TECH_TREE_CHANNEL.sendToClient(new UnlockNodeResultPacket(this.node, result), player);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        node.writeToByteBuf(friendlyByteBuf);
    }
}
