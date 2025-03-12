package willow.train.kuayue.network.s2c.tech_tree;

import kasuga.lib.core.network.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.block.recipe.BlueprintScreen;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

public class CanUnlockGroupS2CPacket extends S2CPacket {

    private final ResourceLocation groupId;
    private final PlayerData.CheckReason reason;

    public CanUnlockGroupS2CPacket(final ResourceLocation groupId,
                                   PlayerData.CheckReason reason) {
        this.groupId = groupId;
        this.reason = reason;
    }

    public CanUnlockGroupS2CPacket(final FriendlyByteBuf buffer) {
        super(buffer);
        this.groupId = buffer.readResourceLocation();
        this.reason = PlayerData.CheckReason.fromNetwork(buffer);
    }

    @Override
    public void handle(Minecraft minecraft) {
        BlueprintScreen screen = BlueprintScreen.INSTANCE;
        if (screen == null) return;
        if (screen.getChosenGroup() == null) return;
        if (!reason.flag()) {
            screen.unableToUnlockGroup(groupId, reason);
        } else {
            screen.ableToUnlockGroup(groupId, reason);
        }
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(groupId);
        reason.toNetwork(friendlyByteBuf);
    }
}
