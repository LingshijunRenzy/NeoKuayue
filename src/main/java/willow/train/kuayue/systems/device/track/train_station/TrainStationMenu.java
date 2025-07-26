package willow.train.kuayue.systems.device.track.train_station;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.block.recipe.BlueprintBlockEntity;
import willow.train.kuayue.initial.recipe.AllRecipeBlock;
import willow.train.kuayue.systems.device.AllDeviceSystems;

import java.util.UUID;

public class TrainStationMenu extends AbstractContainerMenu {


    protected UUID segmentId;

    protected GraphStationInfo info;


    protected TrainStationMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public TrainStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(AllDeviceSystems.STATION_SCREEN.getMenuType(), containerId);
        this.segmentId = data.readUUID();
        this.info = GraphStationInfo.read(data);
    }

    public TrainStationMenu(int containerId, Inventory inv, TrainStationBlockEntity be, ContainerData data) {
        this(AllDeviceSystems.STATION_SCREEN.getMenuType(), containerId);
    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
