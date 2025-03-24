package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.block.recipe.BlueprintScreen;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager;
import willow.train.kuayue.systems.tech_tree.player.ClientPlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

public class UnlockGroupResultPacket extends S2CPacket {

    private final ResourceLocation group;
    private final PlayerData.UnlockResult result;
    public UnlockGroupResultPacket(ResourceLocation groupId,
                                   PlayerData.UnlockResult result) {
        this.group = groupId;
        this.result = result;
    }

    public UnlockGroupResultPacket(FriendlyByteBuf buf) {
        super(buf);
        this.group = buf.readResourceLocation();
        this.result = PlayerData.UnlockResult.fromNetwork(buf);
    }


    @Override
    public void handle(Minecraft minecraft) {
        if (ClientPlayerData.getData().isEmpty()) return;
        PlayerData data = ClientPlayerData.getData().get();
        data.unlockedGroups.addAll(result.updatedUnlockedGroups());
        data.visibleGroups.addAll(result.updatedVisibleGroups());
        data.unlocked.addAll(result.updatedUnlockedNodes());
        data.visibleNodes.addAll(result.updatedVisibleNodes());
        BlueprintScreen screen = BlueprintScreen.INSTANCE;
        if (screen == null) return;
        screen.handleUpdateResult(true, result);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(group);
        result.toNetwork(friendlyByteBuf);
    }
}
