package willow.train.kuayue.systems.editable_panel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.block.panels.block_entity.EditablePanelEntity;
import willow.train.kuayue.initial.AllMenuScreens;

public class EditablePanelEditMenu extends AbstractContainerMenu {

    EditablePanelEntity editablePanelEntity;

    public EditablePanelEditMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
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

    public EditablePanelEditMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(2));
    }

    public EditablePanelEditMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(AllMenuScreens.EDITABLE_PANEL.getMenuType(), containerId);
        this.editablePanelEntity = (EditablePanelEntity) entity;
    }

    public EditablePanelEditMenu setEditablePanelEntity(EditablePanelEntity editablePanelEntity) {
        this.editablePanelEntity = editablePanelEntity;
        return this;
    }

    public EditablePanelEntity getEditablePanelEntity() {
        return editablePanelEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public boolean updatePanelNbt(CompoundTag nbt, ServerPlayer player) {
        if (this.editablePanelEntity == null) {
            Kuayue.LOGGER.warn("EditablePanelEntity is null for player {}", player.getName().getString());
            return false;
        }

        if (!this.stillValid(player)) {
            Kuayue.LOGGER.warn("Player {} attempted to update panel NBT but menu is no longer valid",
                    player.getName().getString());
            return false;
        }

        BlockPos panelPos = this.editablePanelEntity.getBlockPos();
        double distanceSq = player.distanceToSqr(Vec3.atCenterOf(panelPos));
        if (distanceSq > 64.0) {
            Kuayue.LOGGER.warn("Player {} is too far from panel at {} (distance: {})",
                    player.getName().getString(), panelPos, Math.sqrt(distanceSq));
            return false;
        }

        ServerLevel playerLevel = player.getLevel();
        if (!playerLevel.equals(this.editablePanelEntity.getLevel())) {
            Kuayue.LOGGER.warn("Player {} and panel are in different dimensions",
                    player.getName().getString());
            return false;
        }

        if (!isValidEditablePanelNbt(nbt)) {
            Kuayue.LOGGER.warn("Invalid NBT data received from player {}",
                    player.getName().getString());
            return false;
        }

        if (!canPlayerEditPanel(player, panelPos, playerLevel)) {
            Kuayue.LOGGER.warn("Player {} does not have permission to edit panel at {}",
                    player.getName().getString(), panelPos);
            return false;
        }

        try {
            this.editablePanelEntity.load(nbt);
            this.editablePanelEntity.setChanged();
            playerLevel.getChunkSource().blockChanged(panelPos);

            Kuayue.LOGGER.debug("Successfully updated panel NBT at {} for player {}",
                    panelPos, player.getName().getString());
            return true;

        } catch (Exception e) {
            Kuayue.LOGGER.error("Error updating panel NBT for player {}: {}",
                    player.getName().getString(), e.getMessage());
            return false;
        }
    }

    private boolean isValidEditablePanelNbt(CompoundTag nbt) {
        if (!nbt.contains("data", CompoundTag.TAG_COMPOUND)) {
            return false;
        }

        CompoundTag dataTag = nbt.getCompound("data");
        
        // 白名单检查
        String[] allowedFields = {
            "color", "data0", "data1", "data2", "data3", "data4", 
            "revert", "offset_x", "offset_y"
        };

        for (String key : dataTag.getAllKeys()) {
            boolean isAllowed = false;
            for (String allowedField : allowedFields) {
                if (key.equals(allowedField)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                Kuayue.LOGGER.warn("Invalid field found in panel NBT: {}", key);
                return false;
            }
        }

        if (dataTag.contains("color")) {
            if (!dataTag.contains("color", CompoundTag.TAG_INT)) {
                return false;
            }
            int color = dataTag.getInt("color");
            if (color < 0 || color > 0xFFFFFF) {
                return false;
            }
        }

        for (int i = 0; i <= 4; i++) {
            String fieldName = "data" + i;
            if (dataTag.contains(fieldName)) {
                if (!dataTag.contains(fieldName, CompoundTag.TAG_STRING)) {
                    return false;
                }
                String value = dataTag.getString(fieldName);
                if (value.length() > 100) {
                    return false;
                }
            }
        }
        
        if (dataTag.contains("revert")) {
            if (!dataTag.contains("revert", CompoundTag.TAG_BYTE)) {
                return false;
            }
            byte revert = dataTag.getByte("revert");
            if (revert != 0 && revert != 1) {
                return false;
            }
        }
        
        if (dataTag.contains("offset_x")) {
            if (!dataTag.contains("offset_x", CompoundTag.TAG_FLOAT)) {
                return false;
            }
            float offsetX = dataTag.getFloat("offset_x");
            if (Math.abs(offsetX) > 10.0f) {
                return false;
            }
        }
        
        if (dataTag.contains("offset_y")) {
            if (!dataTag.contains("offset_y", CompoundTag.TAG_FLOAT)) {
                return false;
            }
            float offsetY = dataTag.getFloat("offset_y");
            if (Math.abs(offsetY) > 10.0f) {
                return false;
            }
        }

        return true;
    }

    private boolean canPlayerEditPanel(ServerPlayer player, BlockPos pos, ServerLevel level) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof EditablePanelEntity)) {
            return false;
        }
        //可选添加其他权限检查
        return true;
    }
}
