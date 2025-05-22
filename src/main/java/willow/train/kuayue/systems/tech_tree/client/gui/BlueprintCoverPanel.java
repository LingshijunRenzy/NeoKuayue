package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.systems.editable_panel.widget.ImageButton;
import willow.train.kuayue.systems.tech_tree.json.BlueprintCover;
import willow.train.kuayue.systems.tech_tree.json.ImageJsonDefine;

import java.util.function.Function;

public class BlueprintCoverPanel extends AbstractWidget {

    @Getter
    private final BlueprintCover cover;
    private final JsonImageButton entryButton;
    private final JsonImage background, title;
    private final JsonTextComponent description;

    public BlueprintCoverPanel(int offsetX, int offsetY, int offsetZ,
                               int centerX, int centerY,
                               Function<Integer, Integer> mapFunc,
                               BlueprintCover cover, Button.OnPress onPress) {
        super(0, 0, 0, 0, Component.empty());
        this.cover = cover;
        entryButton = cover.hasButtonBg() ? new JsonImageButton(offsetX, offsetY, offsetZ,
                centerX, centerY, cover.getButtonBg(), mapFunc, onPress) : null;
        background = cover.hasBackground() ? new JsonImage(offsetX, offsetY, offsetZ,
                centerX, centerY, cover.getBackground(), mapFunc) : null;
        title = cover.hasTitle() ? new JsonImage(offsetX, offsetY, offsetZ,
                centerX, centerY, cover.getTitle(), mapFunc) : null;
        description = cover.hasDescription() ? new JsonTextComponent(cover.getDescription(),
                offsetX, offsetY, centerX, centerY, mapFunc) : null;
        updateSelfParams();
    }

    private void updateSelfParams() {
        if (background != null) {
            this.setPosition(background.getX(), background.getY());
            this.width = background.getWidth();
            this.height = background.getHeight();
        }
    }

    public void updatePosition(int offsetX, int offsetY, int offsetZ,
                               int centerX, int centerY,
                               Function<Integer, Integer> mapFunc) {
        if (entryButton != null) entryButton.updatePosition(offsetX, offsetY, centerX,
                centerY, mapFunc);
        if (background != null) background.setPosition(offsetX, offsetY, offsetZ,
                centerX, centerY, mapFunc);
        if (title != null) title.setPosition(offsetX, offsetY, offsetZ,
                centerX, centerY, mapFunc);
        if (description != null) description.updatePositions(offsetX, offsetY,
                centerX, centerY, mapFunc);
        updateSelfParams();
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics poseStack, int mouseX,
                             int mouseY, float partial) {
        if (background != null) background.render(poseStack, mouseX, mouseY, partial);
        if (title != null) title.render(poseStack, mouseX, mouseY, partial);
        if (entryButton != null) {
            entryButton.visible = this.visible;
            entryButton.render(poseStack, mouseX, mouseY, partial);
        }
        if (description != null) {
            description.visible = this.visible;
            description.render(poseStack, mouseX, mouseY, partial);
        }
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (description != null && description.isMouseOver(pMouseX, pMouseY))
            description.onClick(pMouseX, pMouseY);
        if (entryButton == null || !entryButton.isMouseOver(pMouseX, pMouseY)) return;
        entryButton.onClick(pMouseX, pMouseY);
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (description != null && description.isMouseOver(pMouseX, pMouseY)) {
            description.onDrag(pMouseX, pMouseY, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (description != null && description.isMouseOver(pMouseX, pMouseY)) {
            return description.mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        return false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        if (description != null) description.updateNarration(pNarrationElementOutput);
    }
}
