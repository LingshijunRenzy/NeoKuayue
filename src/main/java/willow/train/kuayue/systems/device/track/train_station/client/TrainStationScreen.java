package willow.train.kuayue.systems.device.track.train_station.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import willow.train.kuayue.systems.device.track.train_station.GraphStationInfo;

import java.util.UUID;

public class TrainStationScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    EditBox nameBox = new EditBox(this.font, 0, 0, 100, 20, Component.literal("Name"));
    EditBox shortenCodeBox = new EditBox(this.font, 0, 20, 100, 20, Component.literal("Shorten Code"));

    public TrainStationScreen(AbstractContainerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(nameBox);
        this.addRenderableWidget(shortenCodeBox);
        this.addRenderableWidget(new Button(0, 0, 100, 20, Component.literal("Save"), button -> {

        }));
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {

    }
}
