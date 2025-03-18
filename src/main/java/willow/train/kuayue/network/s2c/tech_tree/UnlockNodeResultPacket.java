package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.block.recipe.BlueprintScreen;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.ClientPlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

public class UnlockNodeResultPacket extends S2CPacket {

    private final PlayerData.UnlockResult result;
    private final NodeLocation location;

    public UnlockNodeResultPacket(NodeLocation nodeLocation,
                                  PlayerData.UnlockResult result) {
        this.result = result;
        this.location = nodeLocation;
    }

    public UnlockNodeResultPacket(FriendlyByteBuf buf) {
        this.location = NodeLocation.readFromByteBuf(buf);
        this.result = PlayerData.UnlockResult.fromNetwork(buf);
    }

    @Override
    public void handle(Minecraft minecraft) {
        BlueprintScreen screen = BlueprintScreen.INSTANCE;
        if (screen == null) return;
        if (!result.flag()) return;
        if (ClientPlayerData.getData().isPresent()) {
            PlayerData data = ClientPlayerData.getData().get();
            data.unlocked.addAll(result.updatedUnlockedNodes());
            data.visibleNodes.addAll(result.updatedVisibleNodes());
            data.visibleGroups.addAll(result.updatedVisibleGroups());
            data.unlockedGroups.addAll(result.updatedUnlockedGroups());
        }
        screen.handleUpdateResult(false, result);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        this.location.writeToByteBuf(friendlyByteBuf);
        result.toNetwork(friendlyByteBuf);
    }
}
