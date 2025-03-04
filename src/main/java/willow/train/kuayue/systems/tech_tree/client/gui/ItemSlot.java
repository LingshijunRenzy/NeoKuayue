package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.royawesome.jlibnoise.module.combiner.Min;
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
        this.x = x;
        this.y = y;
    }

    public ItemStack shrink(int count) {
        itemStack.shrink(count);
        if (itemStack.isEmpty()) {
            itemStack = ItemStack.EMPTY;
        }
        return itemStack;
    }

    public static int getRed() {
        return ALPHA + RED.getRed();
    }

    public static int getGreen() {
        return ALPHA + GREEN.getGreen();
    }

    public static int getWhite() {
        return ALPHA + 0xffffff;
    }

    public void setRenderRedMask(boolean mask) {
        renderRedMask = mask;
        renderGreenMask = false;
        permanentGreenMask = false;
    }

    public void setPermanentRedMask(boolean mask) {
        permanentRedMask = mask;
        setRenderRedMask(true);
    }

    public void setRenderGreenMask(boolean mask) {
        renderGreenMask = mask;
        renderRedMask = false;
        permanentGreenMask = false;
    }

    public void setPermanentGreenMask(boolean mask) {
        permanentGreenMask = mask;
        setRenderGreenMask(true);
    }


    @Override
    public void renderButton(@NotNull PoseStack poseStack,
                             int mouseX, int mouseY, float partial) {
        if (itemStack != null && itemStack != ItemStack.EMPTY) {
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            renderer.renderAndDecorateItem(itemStack, this.x, this.y);
            renderer.renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, x, y);
        }
        renderItemMask(poseStack, renderRedMask, permanentRedMask, mouseX, mouseY, getRed());
        renderItemMask(poseStack, renderGreenMask, permanentGreenMask, mouseX, mouseY, getGreen());
        renderItemMask(poseStack, renderMouseOverMask, false, mouseX, mouseY, getWhite());
    }

    private void renderItemMask(PoseStack pose, boolean renderMask, boolean permanentMask,
                                double mouseX, double mouseY, int color) {
        if (!renderMask) return;
        if (permanentMask || (renderMouseOverMask && isMouseOver(mouseX, mouseY))) {
            fill(pose, this.x, this.y, this.x + this.width, this.y + this.height, color);
        }
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.HINT, itemStack.getDisplayName());
    }
}
