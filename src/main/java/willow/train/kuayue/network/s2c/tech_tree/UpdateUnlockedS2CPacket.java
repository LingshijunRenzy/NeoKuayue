package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import willow.train.kuayue.systems.tech_tree.player.ClientPlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerDataManager;

public class UpdateUnlockedS2CPacket extends S2CPacket {

    private PlayerData playerData;

    public UpdateUnlockedS2CPacket(Player player) {
        playerData = PlayerDataManager.MANAGER.getOrCreatePlayerData(player);
    }

    public UpdateUnlockedS2CPacket(final FriendlyByteBuf buf) {
        ClientPlayerData.updateDataFromNetwork(buf);
    }

    @Override
    public void handle(Minecraft minecraft) {

    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        playerData.toNetwork(friendlyByteBuf);
    }
}
