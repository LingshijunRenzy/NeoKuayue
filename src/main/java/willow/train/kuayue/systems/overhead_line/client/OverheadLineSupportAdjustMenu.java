package willow.train.kuayue.systems.overhead_line.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;

public class OverheadLineSupportAdjustMenu extends AbstractContainerMenu {

    private OverheadLineSupportBlockEntity blockEntity;

    public OverheadLineSupportAdjustMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);

        this.addDataSlot(
                new DataSlot() {
                    @Override
                    public int get() {
                        return 0;
                    }

                    @Override
                    public void set(int pValue) {}
                });
    }

    public OverheadLineSupportAdjustMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(AllOverheadLineMenuScreens.OVERHEAD_LINE_SUPPORT_ADJUST.getMenuType(), containerId);
        BlockEntity entity = inv.player.level().getBlockEntity(extraData.readBlockPos());
        if (entity instanceof OverheadLineSupportBlockEntity) {
            this.blockEntity = (OverheadLineSupportBlockEntity) entity;
        }
    }

    public OverheadLineSupportAdjustMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(AllOverheadLineMenuScreens.OVERHEAD_LINE_SUPPORT_ADJUST.getMenuType(), containerId);
        this.blockEntity = (OverheadLineSupportBlockEntity) entity;
    }

    public OverheadLineSupportAdjustMenu(int containerId, Inventory inv, OverheadLineSupportBlockEntity blockEntity) {
        super(AllOverheadLineMenuScreens.OVERHEAD_LINE_SUPPORT_ADJUST.getMenuType(), containerId);
        this.blockEntity = blockEntity;
    }

    public OverheadLineSupportBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return false;
        }
        return pPlayer.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5
        ) <= 64.0;
    }
}
