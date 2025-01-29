package willow.train.kuayue.block.recipe;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.initial.recipe.AllRecipeBlock;

import java.util.Arrays;
import java.util.List;

public class BlueprintBlockEntity extends SmartBlockEntity implements Container, MenuProvider {

    private final ItemStack[] contents;
    public BlueprintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        contents = new ItemStack[getContainerSize()];
    }

    public BlueprintBlockEntity(BlockPos pos, BlockState state) {
        this(AllRecipeBlock.BLUEPRINT_TABLE.getBlock().getBlockEntityType(),
                pos, state);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        CompoundTag contentTag = new CompoundTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.equals(ItemStack.EMPTY))
                continue;
            CompoundTag t = new CompoundTag();
            stack.save(t);
            contentTag.put(String.valueOf(i), t);
        }
        tag.put("content", contentTag);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        CompoundTag contentTag = tag.getCompound("content");
        for (String key : contentTag.getAllKeys()) {
            CompoundTag t = contentTag.getCompound(key);
            if (t.isEmpty()) continue;
            ItemStack stack = ItemStack.of(t);
            int value;
            try {
                value = Integer.parseInt(key);
            } catch (Exception e) {continue;}
            contents[value] = stack;
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public int getContainerSize() {
        return 13;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : contents) {
            if (stack != null && !stack.equals(ItemStack.EMPTY))
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        if (getContainerSize() <= pSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = contents[pSlot];
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        if (getContainerSize() <= pSlot)
            return ItemStack.EMPTY;
        ItemStack stack = getItemSafe(pSlot);
        if (stack.equals(ItemStack.EMPTY))
            return ItemStack.EMPTY;
        if (stack.getCount() <= pAmount) {
            contents[pSlot] = null;
            return stack;
        }
        ItemStack s = stack.copy();
        stack.shrink(pAmount);
        s.setCount(pAmount);
        notifyUpdate();
        return s;
    }

    private ItemStack getItemSafe(int slot) {
        ItemStack stack = contents[slot];
        if (stack == null || stack.equals(ItemStack.EMPTY))
            return ItemStack.EMPTY;
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        if (getContainerSize() <= pSlot)
            return ItemStack.EMPTY;
        ItemStack stack = getItemSafe(pSlot);
        contents[pSlot] = null;
        return stack;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        if (getContainerSize() <= pSlot) return;
        contents[pSlot] = pStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }

    @Override
    public void clearContent() {
        Arrays.fill(contents, null);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new BlueprintMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(getContainerSize()));
    }
}
