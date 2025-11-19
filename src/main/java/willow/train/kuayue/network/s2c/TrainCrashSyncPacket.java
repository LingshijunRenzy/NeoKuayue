package willow.train.kuayue.network.s2c;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.entity.Train;
import kasuga.lib.core.network.S2CPacket;
import kasuga.lib.core.util.ComponentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;
import java.util.UUID;

public class TrainCrashSyncPacket extends S2CPacket {

    private final String causeKey;

    private final UUID trianId;

    public TrainCrashSyncPacket(String causeKey, UUID trianId) {
        this.causeKey = causeKey;
        this.trianId = trianId;
    }

    public TrainCrashSyncPacket(FriendlyByteBuf buf) {
        this.causeKey = buf.readUtf();
        this.trianId = UUID.fromString(buf.readUtf());
    }

    @Override
    public void handle(Minecraft minecraft) {
        if (minecraft.player == null) return;
        Train train = CreateClient.RAILWAYS.trains.get(trianId);
        if (train == null) return;
        train.crash();
        MutableComponent component = (MutableComponent)
                ComponentHelper.translatable(causeKey, train.name);
        component.setStyle(
                component.getStyle().
                        applyFormat(ChatFormatting.RED).withBold(true)
        );
        minecraft.player.displayClientMessage(
                component,
                false
        );
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(causeKey);
        friendlyByteBuf.writeUtf(trianId.toString());
    }
}
