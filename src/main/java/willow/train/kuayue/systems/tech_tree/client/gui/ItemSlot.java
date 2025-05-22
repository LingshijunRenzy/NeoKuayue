package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Getter
public class ItemSlot extends AbstractWidget {

    @Setter
    private ItemStack itemStack;


    private boolean renderRedMask, permanentRedMask,
                    renderGreenMask, permanentGreenMask;

    @Setter
    private boolean renderMouseOverMask = true;

    public static final Color RED = Color.RED, GREEN = Color.GREEN;
    public static final int ALPHA = 0x80000000;

    public ItemSlot(int pX, int pY) {
        super(pX, pY, 16, 16, Component.empty());
        itemStack = ItemStack.EMPTY;
    }

    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    public ItemStack shrink(int count) {
        itemStack.shrink(count);
        if (itemStack.isEmpty()) {
            itemStack = ItemStack.EMPTY;
        }
        return itemStack;
    }

    public boolean isEmpty() {
        return itemStack == null || itemStack == ItemStack.EMPTY;
    }

    public static int getRed() {
        return ALPHA + RED.getRGB();
    }

    public static int getGreen() {
        return ALPHA + GREEN.getRGB();
    }

    public static int getWhite() {
        return ALPHA + 0xffffff;
    }

    public void setRenderRedMask(boolean mask) {
        renderRedMask = mask;
        if (renderRedMask) {
            renderGreenMask = false;
            permanentGreenMask = false;
        }
    }

    public void setPermanentRedMask(boolean mask) {
        permanentRedMask = mask;
        if (permanentRedMask) setRenderRedMask(true);
    }

    public void setRenderGreenMask(boolean mask) {
        renderGreenMask = mask;
        if (renderRedMask) {
            renderRedMask = false;
            permanentGreenMask = false;
        }
    }

    public void setPermanentGreenMask(boolean mask) {
        permanentGreenMask = mask;
        if (permanentGreenMask) setRenderGreenMask(true);
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics graphics,
                             int mouseX, int mouseY, float partial) {
        if (itemStack != null && itemStack != ItemStack.EMPTY) {
            graphics.renderItem(itemStack, this.getX(), this.getY());
            graphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, this.getX(), this.getY());
        }
        renderItemMask(graphics, renderRedMask, permanentRedMask, mouseX, mouseY, getRed());
        renderItemMask(graphics, renderGreenMask, permanentGreenMask, mouseX, mouseY, getGreen());
        renderItemMask(graphics, renderMouseOverMask, false, mouseX, mouseY, getWhite());
    }

    private void renderItemMask(GuiGraphics graphics, boolean renderMask, boolean permanentMask,
                                double mouseX, double mouseY, int color) {
        if (!renderMask) return;
        if (permanentMask || (renderMouseOverMask && isMouseOver(mouseX, mouseY))) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);
        }
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.HINT, itemStack.getDisplayName());
    }
}
