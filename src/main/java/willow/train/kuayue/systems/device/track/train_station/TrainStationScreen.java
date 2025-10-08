package willow.train.kuayue.systems.device.track.train_station;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.systems.device.track.train_station.packet.C2STrainStationInfoUpdatePacket;

public class TrainStationScreen extends AbstractContainerScreen<TrainStationMenu> {

    public EditBox fullNameInput = new EditBox(Minecraft.getInstance().font, 0, 0, 128, 12, Component.literal("Full name"));

    public EditBox shortenCodeInput = new EditBox(Minecraft.getInstance().font, 0, 16, 128, 12, Component.literal("Shorten code"));

    public Button confirmButton = Button.builder(Component.literal("OK"), this::submit).bounds(0, 32, 128, 8).build();
//    public Button confirmButton = new Button(0, 32, 128, 8, Component.literal("OK"), this::submit);

    private void submit(Button button) {
        this.onClose();
        AllPackets.CHANNEL.sendToServer(new C2STrainStationInfoUpdatePacket(menu.segmentId, new GraphStationInfo(
                fullNameInput.getValue(),
                shortenCodeInput.getValue()
        )));
    }

    public TrainStationScreen(TrainStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        fullNameInput.setValue(pMenu.info.name());
        shortenCodeInput.setValue(pMenu.info.shortenCode());
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(fullNameInput);
        this.addRenderableWidget(shortenCodeInput);
        this.addRenderableWidget(confirmButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
