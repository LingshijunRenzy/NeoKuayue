package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.systems.editable_panel.widget.ImageButton;
import willow.train.kuayue.systems.editable_panel.widget.OnClick;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;

public class TechTreeItemButton extends AbstractButton {

    private final OnClick<TechTreeItemButton> action;

    @Getter
    @NotNull private ItemStack stack;

    @Getter
    private final ClientTechTreeGroup group;
    public TechTreeItemButton(@NotNull ItemStack item, int pWidth, int pHeight,
                              ClientTechTreeGroup group, OnClick<TechTreeItemButton> action) {
        super(0, 0, pWidth, pHeight, Component.empty());
        this.action = action;
        stack = item;
        this.group = group;
    }

    public void setStack(@NotNull ItemStack stack) {
        this.stack = stack;
    }

    public void setX(int x) {
        super.setX(x);
    }

    public void setY(int y) {
        super.setY(y);
    }

    public void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    @Override
    public void onPress() {}

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        action.click(this, pMouseX, pMouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX,
                             int mouseY, float partialTick) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        int iX = this.getX() + (this.width - 16) / 2;
        int iY = this.getY() + (this.height - 16) / 2;
        ItemDisplayContext context = ItemDisplayContext.GUI;
        guiGraphics.renderFakeItem(stack, iX, iY);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
