package willow.train.kuayue.block.recipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.initial.recipe.AllRecipeBlock;

public class BlueprintMenu extends AbstractContainerMenu {
    protected BlueprintMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public BlueprintMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(AllRecipeBlock.BLUEPRINT_TABLE.getMenuReg().getMenuType(), containerId);
    }

    public BlueprintMenu(int containerId, Inventory inv, BlueprintBlockEntity be, ContainerData data) {
        this(AllRecipeBlock.BLUEPRINT_TABLE.getMenuReg().getMenuType(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot slot = getSlot(pIndex);
        ItemStack itemStack = slot.getItem();
        if (itemStack.equals(ItemStack.EMPTY))
            return itemStack;
        slot.remove(itemStack.getCount());
        slot.setChanged();
        return itemStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
