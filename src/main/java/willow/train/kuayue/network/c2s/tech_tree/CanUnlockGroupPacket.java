package willow.train.kuayue.network.c2s.tech_tree;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.tech_tree.CanUnlockGroupS2CPacket;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerDataManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeGroup;
import willow.train.kuayue.systems.tech_tree.server.TechTreeManager;

public class CanUnlockGroupPacket extends C2SPacket {

    private final ResourceLocation groupId;

    public CanUnlockGroupPacket(ResourceLocation groupId) {
        this.groupId = groupId;
    }

    public CanUnlockGroupPacket(FriendlyByteBuf buf) {
        this.groupId = buf.readResourceLocation();
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            PlayerData data = PlayerDataManager.MANAGER.getOrCreatePlayerData(player);
            TechTreeGroup group = TechTreeManager.MANAGER.getGroup(groupId);
            if (group == null) {
                sendReply(player,
                        PlayerData.CheckReason.exceptionCase("invalid group id."));
                return;
            }
            PlayerData.CheckReason reason = data.canUnlock(player, group);
            sendReply(player, reason);
        });
    }

    private void sendReply(ServerPlayer player, PlayerData.CheckReason reason) {
        AllPackets.TECH_TREE_CHANNEL.sendToClient(
                new CanUnlockGroupS2CPacket(groupId, reason), player);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(groupId);
    }
}
