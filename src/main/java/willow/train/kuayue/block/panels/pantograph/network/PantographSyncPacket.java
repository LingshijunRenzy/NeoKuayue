package willow.train.kuayue.block.panels.pantograph.network;

import com.simibubi.create.content.contraptions.Contraption;
import kasuga.lib.core.network.S2CPacket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import willow.train.kuayue.block.panels.pantograph.CurrOverheadLineCache;

import java.util.Objects;

public class PantographSyncPacket extends S2CPacket {

    @Getter
    private final int entityId;

    @Getter
    private final BlockPos localPos;

    @Getter
    private final CurrOverheadLineCache cache;

    public PantographSyncPacket(Contraption contraption, BlockPos localPos, CurrOverheadLineCache cache) {
        this.cache = cache;
        this.localPos = localPos;
        this.entityId = contraption.entity.getId();
    }

    public PantographSyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.localPos = buf.readBlockPos();
        CompoundTag nbt = buf.readNbt();
        nbt = Objects.requireNonNullElse(nbt, new CompoundTag());
        this.cache = new CurrOverheadLineCache();
        this.cache.read(nbt);
    }


    @Override
    public void handle(Minecraft minecraft) {
        ClientSyncManager.getInstance().push(this);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag nbt = new CompoundTag();
        cache.write(nbt);
        friendlyByteBuf.writeInt(this.entityId);
        friendlyByteBuf.writeBlockPos(this.localPos);
        friendlyByteBuf.writeNbt(nbt);
    }
}
