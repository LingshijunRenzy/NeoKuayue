package willow.train.kuayue.network.c2s.tech_tree;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.tech_tree.UnlockGroupResultPacket;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerDataManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeGroup;
import willow.train.kuayue.systems.tech_tree.server.TechTreeManager;

public class UnlockGroupPacket extends C2SPacket {

    private final ResourceLocation group;
    public UnlockGroupPacket(ResourceLocation location) {
        this.group = location;
    }

    public UnlockGroupPacket(FriendlyByteBuf buf) {
        this.group = buf.readResourceLocation();
    }
    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            PlayerData data = PlayerDataManager.MANAGER.getOrCreatePlayerData(player);
            TechTreeGroup grp = TechTreeManager.MANAGER.getGroup(group);
            ServerLevel world = (ServerLevel) player.level();
            if (grp == null) {
                sendReply(player, PlayerData.UnlockResult.failedEmpty());
                return;
            }
            PlayerData.UnlockResult result = data.unlock(world, player, grp);
            sendReply(player, result);
        });
    }

    private void sendReply(ServerPlayer player, PlayerData.UnlockResult result) {
        AllPackets.TECH_TREE_CHANNEL.sendToClient(new UnlockGroupResultPacket(group, result), player);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(group);
    }
}
