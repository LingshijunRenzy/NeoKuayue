package willow.train.kuayue.systems.overhead_line.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import willow.train.kuayue.systems.overhead_line.packet.C2SOverheadLineSupportAdjustPacket;

public class OverheadLineSupportAdjustScreen extends AbstractContainerScreen<OverheadLineSupportAdjustMenu> {

    private OverheadLineSupportBlockEntity blockEntity;
    private OverheadLineSupportSlider xOffsetSlider, yOffsetSlider, zOffsetSlider, rotationSlider;
    private EditBox xOffsetEditBox, yOffsetEditBox, zOffsetEditBox, rotationEditBox;
    private Button confirmButton, cancelButton;

    private float tempXOffset, tempYOffset, tempZOffset, tempRotation;
    private float originalXOffset, originalYOffset, originalZOffset, originalRotation;

    private static final int SLIDER_WIDTH = 200;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_SPACING = 25;
    private static final int EDITBOX_WIDTH = 60;
    private static final int EDITBOX_HEIGHT = 20;
    private static final int EDITBOX_MARGIN = 10;
    private static final int GUI_LEFT = 20;
    private static final int GUI_TOP = 20;

    public OverheadLineSupportAdjustScreen(OverheadLineSupportAdjustMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.blockEntity = pMenu.getBlockEntity();
        this.imageWidth = 256;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        if (blockEntity == null) {
            return;
        }

        originalXOffset = blockEntity.getXOffset();
        originalYOffset = blockEntity.getYOffset();
        originalZOffset = blockEntity.getZOffset();
        originalRotation = blockEntity.getManualRotation();

        tempXOffset = originalXOffset;
        tempYOffset = originalYOffset;
        tempZOffset = originalZOffset;
        tempRotation = originalRotation;

        this.leftPos = GUI_LEFT;
        this.topPos = GUI_TOP;

        int startX = leftPos;
        int startY = topPos;

        boolean isEndWeight = isEndWeightBlock();

        xOffsetSlider = addRenderableWidget(new OverheadLineSupportSlider(
                startX, startY, SLIDER_WIDTH, SLIDER_HEIGHT,
                (tempXOffset + 1.0f) / 2.0f,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.x_offset"),
                OffsetType.X
        ));

        xOffsetEditBox = addRenderableWidget(new EditBox(font,
                startX + SLIDER_WIDTH + EDITBOX_MARGIN, startY, EDITBOX_WIDTH, EDITBOX_HEIGHT,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.x_offset")));
        xOffsetEditBox.setValue(String.format("%.2f", tempXOffset));
        xOffsetEditBox.setResponder(value -> onEditBoxChanged(OffsetType.X, value));
        xOffsetEditBox.setMaxLength(10);
        xOffsetEditBox.setBordered(true);

        yOffsetSlider = addRenderableWidget(new OverheadLineSupportSlider(
                startX, startY + SLIDER_SPACING, SLIDER_WIDTH, SLIDER_HEIGHT,
                (tempYOffset + 1.0f) / 2.0f,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.y_offset"),
                OffsetType.Y
        ));

        yOffsetEditBox = addRenderableWidget(new EditBox(font,
                startX + SLIDER_WIDTH + EDITBOX_MARGIN, startY + SLIDER_SPACING, EDITBOX_WIDTH, EDITBOX_HEIGHT,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.y_offset")));
        yOffsetEditBox.setValue(String.format("%.2f", tempYOffset));
        yOffsetEditBox.setResponder(value -> onEditBoxChanged(OffsetType.Y, value));
        yOffsetEditBox.setMaxLength(10);
        yOffsetEditBox.setBordered(true);

        zOffsetSlider = addRenderableWidget(new OverheadLineSupportSlider(
                startX, startY + SLIDER_SPACING * 2, SLIDER_WIDTH, SLIDER_HEIGHT,
                (tempZOffset + 1.0f) / 2.0f,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.z_offset"),
                OffsetType.Z
        ));

        zOffsetEditBox = addRenderableWidget(new EditBox(font,
                startX + SLIDER_WIDTH + EDITBOX_MARGIN, startY + SLIDER_SPACING * 2, EDITBOX_WIDTH, EDITBOX_HEIGHT,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.z_offset")));
        zOffsetEditBox.setValue(String.format("%.2f", tempZOffset));
        zOffsetEditBox.setResponder(value -> onEditBoxChanged(OffsetType.Z, value));
        zOffsetEditBox.setMaxLength(10);
        zOffsetEditBox.setBordered(true);

        int buttonYOffset = SLIDER_SPACING * 3;
        if (!isEndWeight) {
            rotationSlider = addRenderableWidget(new OverheadLineSupportSlider(
                    startX, startY + SLIDER_SPACING * 3, SLIDER_WIDTH, SLIDER_HEIGHT,
                    (tempRotation + 45.0f) / 90.0f,
                    Component.translatable("gui.kuayue.overhead_line_support_adjust.rotation"),
                    OffsetType.ROTATION
            ));

            rotationEditBox = addRenderableWidget(new EditBox(font,
                    startX + SLIDER_WIDTH + EDITBOX_MARGIN, startY + SLIDER_SPACING * 3, EDITBOX_WIDTH, EDITBOX_HEIGHT,
                    Component.translatable("gui.kuayue.overhead_line_support_adjust.rotation")));
            rotationEditBox.setValue(String.format("%.2f", tempRotation));
            rotationEditBox.setResponder(value -> onEditBoxChanged(OffsetType.ROTATION, value));
            rotationEditBox.setMaxLength(10);
            rotationEditBox.setBordered(true);

            buttonYOffset = SLIDER_SPACING * 4;
        }

        int buttonY = startY + buttonYOffset + 10;
        confirmButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.kuayue.overhead_line_support_adjust.confirm"),
                this::onConfirm).bounds(startX, buttonY, 70, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.kuayue.overhead_line_support_adjust.reset"),
                this::onReset).bounds(startX + 75, buttonY, 70, 20).build());

        cancelButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.kuayue.overhead_line_support_adjust.cancel"),
                this::onCancel).bounds(startX + 150, buttonY, 70, 20).build());
    }

    private boolean isEndWeightBlock() {
        if (blockEntity == null) return false;
        String blockName = blockEntity.getBlockState().getBlock().getClass().getSimpleName();
        return blockName.contains("EndWeight");
    }

    private void onEditBoxChanged(OffsetType type, String value) {
        if (value.isEmpty()) return;

        try {
            float parsedValue = Float.parseFloat(value);

            float minValue, maxValue;
            switch (type) {
                case X, Y, Z -> {
                    minValue = -1.0f;
                    maxValue = 1.0f;
                }
                case ROTATION -> {
                    minValue = -45.0f;
                    maxValue = 45.0f;
                }
                default -> {
                    return;
                }
            }

            float clampedValue = Math.max(minValue, Math.min(maxValue, parsedValue));

            switch (type) {
                case X -> {
                    tempXOffset = clampedValue;
                    if (xOffsetSlider != null) {
                        xOffsetSlider.setSliderValue((clampedValue + 1.0f) / 2.0f);
                    }
                }
                case Y -> {
                    tempYOffset = clampedValue;
                    if (yOffsetSlider != null) {
                        yOffsetSlider.setSliderValue((clampedValue + 1.0f) / 2.0f);
                    }
                }
                case Z -> {
                    tempZOffset = clampedValue;
                    if (zOffsetSlider != null) {
                        zOffsetSlider.setSliderValue((clampedValue + 1.0f) / 2.0f);
                    }
                }
                case ROTATION -> {
                    tempRotation = clampedValue;
                    if (rotationSlider != null) {
                        rotationSlider.setSliderValue((clampedValue + 45.0f) / 90.0f);
                    }
                }
            }

            if (blockEntity != null) {
                blockEntity.setTransformParameters(tempXOffset, tempYOffset, tempZOffset, tempRotation);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void onConfirm(Button button) {
        if (blockEntity != null) {
            AllPackets.CHANNEL.sendToServer(new C2SOverheadLineSupportAdjustPacket(
                    blockEntity.getBlockPos(), tempXOffset, tempYOffset, tempZOffset, tempRotation));
        }
        this.onClose();
    }

    private void onReset(Button button) {
        tempXOffset = 0.0f;
        tempYOffset = 0.0f;
        tempZOffset = 0.0f;
        tempRotation = 0.0f;

        if (xOffsetSlider != null) xOffsetSlider.setValueAndApply(0.5);
        if (yOffsetSlider != null) yOffsetSlider.setValueAndApply(0.5);
        if (zOffsetSlider != null) zOffsetSlider.setValueAndApply(0.5);
        if (rotationSlider != null) rotationSlider.setValueAndApply(0.5);

        // 重置输入框的值
        if (xOffsetEditBox != null) xOffsetEditBox.setValue("0.00");
        if (yOffsetEditBox != null) yOffsetEditBox.setValue("0.00");
        if (zOffsetEditBox != null) zOffsetEditBox.setValue("0.00");
        if (rotationEditBox != null) rotationEditBox.setValue("0.00");

        if (blockEntity != null) {
            blockEntity.setTransformParameters(tempXOffset, tempYOffset, tempZOffset, tempRotation);
        }
    }

    private void onCancel(Button button) {
        if (blockEntity != null) {
            blockEntity.setTransformParameters(originalXOffset, originalYOffset, originalZOffset, originalRotation);
        }
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }

    @Override
    public boolean isPauseScreen() {
        return super.isPauseScreen();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawString(font,
                Component.translatable("gui.kuayue.overhead_line_support_adjust.title"),
                leftPos, topPos - 15, 0xFFFFFF);

        int labelX = leftPos + SLIDER_WIDTH + EDITBOX_MARGIN + EDITBOX_WIDTH + 5;
        // pGuiGraphics.drawString(font, "Value", labelX, topPos + 5, 0xAAAAA);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            onCancel(null);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (xOffsetSlider != null && xOffsetSlider.isBeingDragged()) {
            return xOffsetSlider.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        if (yOffsetSlider != null && yOffsetSlider.isBeingDragged()) {
            return yOffsetSlider.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        if (zOffsetSlider != null && zOffsetSlider.isBeingDragged()) {
            return zOffsetSlider.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        if (rotationSlider != null && rotationSlider.isBeingDragged()) {
            return rotationSlider.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (xOffsetSlider != null && xOffsetSlider.isBeingDragged()) {
            return xOffsetSlider.mouseReleased(pMouseX, pMouseY, pButton);
        }

        if (yOffsetSlider != null && yOffsetSlider.isBeingDragged()) {
            return yOffsetSlider.mouseReleased(pMouseX, pMouseY, pButton);
        }

        if (zOffsetSlider != null && zOffsetSlider.isBeingDragged()) {
            return zOffsetSlider.mouseReleased(pMouseX, pMouseY, pButton);
        }

        if (rotationSlider != null && rotationSlider.isBeingDragged()) {
            return rotationSlider.mouseReleased(pMouseX, pMouseY, pButton);
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private void updateTempValue(OffsetType type, float value) {
        switch (type) {
            case X:
                tempXOffset = value;
                if (xOffsetEditBox != null && !xOffsetEditBox.isFocused()) {
                    xOffsetEditBox.setValue(String.format("%.2f", value));
                }
                break;
            case Y:
                tempYOffset = value;
                if (yOffsetEditBox != null && !yOffsetEditBox.isFocused()) {
                    yOffsetEditBox.setValue(String.format("%.2f", value));
                }
                break;
            case Z:
                tempZOffset = value;
                if (zOffsetEditBox != null && !zOffsetEditBox.isFocused()) {
                    zOffsetEditBox.setValue(String.format("%.2f", value));
                }
                break;
            case ROTATION:
                tempRotation = value;
                if (rotationEditBox != null && !rotationEditBox.isFocused()) {
                    rotationEditBox.setValue(String.format("%.2f", value));
                }
                break;
        }

        if (blockEntity != null) {
            blockEntity.setTransformParameters(tempXOffset, tempYOffset, tempZOffset, tempRotation);
        }
    }

    private enum OffsetType {
        X, Y, Z, ROTATION
    }

    public class OverheadLineSupportSlider extends AbstractSliderButton {
        private final Component label;
        private final OffsetType offsetType;
        private boolean onClicked = false;

        public OverheadLineSupportSlider(int x, int y, int width, int height, double value, Component label, OffsetType offsetType) {
            super(x, y, width, height, Component.empty(), value);
            this.label = label;
            this.offsetType = offsetType;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (offsetType == OffsetType.ROTATION) {
                float rotationValue = (float) ((value - 0.5) * 90.0);
                this.setMessage(Component.translatable("gui.kuayue.overhead_line_support_adjust.rotation").append(" = " + String.format("%.1f°", rotationValue)));
            } else {
                float offsetValue = (float) ((value - 0.5) * 2.0);
                this.setMessage(label.copy().append(Component.literal(" = " + String.format("%.2f", offsetValue))));
            }
        }

        @Override
        protected void applyValue() {
            float actualValue;
            if (offsetType == OffsetType.ROTATION) {
                actualValue = (float) ((value - 0.5) * 90.0);
            } else {
                actualValue = (float) ((value - 0.5) * 2.0);
            }

            updateTempValue(offsetType, actualValue);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        @Override
        public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }

        @Override
        public void onClick(double pMouseX, double pMouseY) {
            super.onClick(pMouseX, pMouseY);
            this.onClicked = true;
        }

        @Override
        public void onRelease(double pMouseX, double pMouseY) {
            super.onRelease(pMouseX, pMouseY);
            this.onClicked = false;
        }

        @Override
        public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
            if (!onClicked) return false;

            double percentage = (pMouseX - this.getX()) / (double) this.width;
            percentage = Math.max(0.0, Math.min(1.0, percentage));

            this.value = percentage;
            updateMessage();
            applyValue();

            return true;
        }

        public void setSliderValue(double newValue) {
            this.value = Math.max(0.0, Math.min(1.0, newValue));
            updateMessage();
        }

        public void setValueAndApply(double newValue) {
            this.value = Math.max(0.0, Math.min(1.0, newValue));
            updateMessage();
            applyValue();
        }

        public boolean isBeingDragged() {
            return onClicked;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        // do nothing
    }
}
