package willow.train.kuayue.systems.overhead_line.packet;

import kasuga.lib.core.network.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;

public class C2SOverheadLineSupportAdjustPacket extends C2SPacket {
    public final BlockPos pos;
    public final float xOffset;
    public final float yOffset;
    public final float zOffset;
    public final float rotation;

    public C2SOverheadLineSupportAdjustPacket(FriendlyByteBuf buf) {
        super(buf);
        this.pos = buf.readBlockPos();
        this.xOffset = buf.readFloat();
        this.yOffset = buf.readFloat();
        this.zOffset = buf.readFloat();
        this.rotation = buf.readFloat();
    }

    public C2SOverheadLineSupportAdjustPacket(BlockPos pos, float xOffset, float yOffset, float zOffset, float rotation) {
        super();
        this.pos = pos;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.rotation = rotation;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ServerLevel level = player.getLevel();
            if (level == null) return;

            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof OverheadLineSupportBlockEntity supportBlockEntity) {
                supportBlockEntity.setTransformParameters(xOffset, yOffset, zOffset, rotation);
            }
        });
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeFloat(xOffset);
        buf.writeFloat(yOffset);
        buf.writeFloat(zOffset);
        buf.writeFloat(rotation);
    }
}
