package willow.train.kuayue.network.c2s;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.editable_panel.EditablePanelEditMenu;

public class NbtC2SPacket extends C2SPacket {
    private final BlockPos pos;
    private final CompoundTag nbt;

    public NbtC2SPacket(BlockPos pos, BlockEntity be) {
        super();
        this.pos = pos;
        this.nbt = be.saveWithoutMetadata();
    }

    public NbtC2SPacket(BlockPos pos, CompoundTag nbt) {
        super();
        this.pos = pos;
        this.nbt = nbt;
    }

    public NbtC2SPacket(FriendlyByteBuf buf) {
        super(buf);
        this.pos = buf.readBlockPos();
        this.nbt = buf.readNbt();
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        ServerLevel level = (ServerLevel) player.level();
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            entity.load(nbt);
            entity.setChanged();
            level.getChunkSource().blockChanged(pos);

            AbstractContainerMenu menu = player.containerMenu;
            if (!(menu instanceof EditablePanelEditMenu editMenu)) {
                Kuayue.LOGGER.warn("Received NbtC2SPacket, but menu not instanceof EditablePanelEditMenu");
                return;
            }

            boolean success = editMenu.updatePanelNbt(this.nbt, player);
            if (!success) {
                Kuayue.LOGGER.warn("Failed to update panel NBT for player: " + player.getName().getString());
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf
                .writeBlockPos(pos)
                .writeNbt(nbt);
    }
}
