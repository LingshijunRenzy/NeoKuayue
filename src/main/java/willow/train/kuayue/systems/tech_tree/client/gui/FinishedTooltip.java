package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FinishedTooltip extends AbstractWidget {

    private final MutableComponent title;

    @Getter
    private final String identifier;
    private final List<Component> descriptions;

    private final LazyRecomputable<ImageMask> completedStamp =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(0, 96 / 128f, 32 / 128f, 1));

    private final LazyRecomputable<ImageMask> slidingBar =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(32 / 128f, 94 / 128f, 36 / 128f, 1));

    private final LazyRecomputable<ImageMask> slidingBarButton =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(36 / 128f, 96 / 128f, 42 / 128f, 102 / 128f));

    @Getter
    private int index = 0;

    @Setter
    @Getter
    private boolean hasBar = false;

    //(53, 102), (169, 38)

    public static final int fontColor = 0xffffffff;


    public FinishedTooltip(String identifier, String titleKey, String descriptionKey,
                           int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.identifier = identifier;
        this.title = Component.translatable(titleKey).setStyle(Style.EMPTY.withBold(true));
        this.descriptions = new ArrayList<>();
        MutableComponent component = Component.translatable(descriptionKey);
        Font font = Minecraft.getInstance().font;
        this.descriptions.addAll(Tooltip.lineFeed(component, font, width - 45));
        hasBar = (descriptions.size() + 1) * font.lineHeight + 5 > height;
        setImagePositions();
    }

    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
        setImagePositions();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        Font font = Minecraft.getInstance().font;
        int baseX = this.getX() + 5;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0D, 0.0D, 400.0D);
        Matrix4f matrix4f = graphics.pose().last().pose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(title, baseX, this.getY(), fontColor,
                false, matrix4f, buffer, false,
                0, 15728880);
        int counter = 0;
        for (int i = index; i < descriptions.size(); i++) {
            int by = 5 + (counter + 1) * font.lineHeight;
            if (by + font.lineHeight >= this.getHeight()) break;
            Component component = descriptions.get(i);
            font.drawInBatch(component, baseX,
                    this.getY() + by, fontColor,
                    false, matrix4f, buffer, false,
                    0, 15728880);
            counter++;
        }
        buffer.endBatch();
        completedStamp.get().renderToGui();
        if (hasBar) {
            slidingBar.get().renderToGui();
            slidingBarButton.get().renderToGui();
        }
        graphics.pose().popPose();
    }

    private void setImagePositions() {
        completedStamp.get().rectangle(new Vector3f(this.getX() + this.getWidth() - 30, this.getY() + (float) this.getHeight() / 2 - 14, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 28, 28);
        slidingBar.get().rectangle(new Vector3f(this.getX() + this.getWidth() - 37, this.getY() + (float) this.getHeight() / 2 - 17, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 4, 34);
        slidingBarButton.get().rectangle(new Vector3f(this.getX() + this.getWidth() - 38, this.getY() + (float) this.getHeight() / 2 - 13, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 6, 6);
    }

    private void updateSlidingButtonPosition() {
        if (this.descriptions.size() > 1) {
            this.index = Math.max(Math.min(index, descriptions.size() - 1), 0);
            slidingBarButton.get().rectangle(new Vector3f(this.getX() + this.getWidth() - 38,
                            this.getY() + (float) this.height / 2 - 13 + ((float) index / this.descriptions.size()) * 22, 0),
                    ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 6, 6);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!(this.active && this.visible)) return false;
        if (!this.isValidClickButton(pButton)) return false;
        boolean flag = clickedOnSlideBar(pMouseX, pMouseY) && this.clicked(pMouseX, pMouseY);
        if (flag) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(pMouseX, pMouseY);
        }
        return flag;
    }

    public boolean clickedOnSlideBar(double pMouseX, double pMouseY) {
        if (!isHasBar()) return false;
        return  (pMouseX >= this.getX() + this.getWidth() - 38 && pMouseX <= this.getX() + this.getWidth() - 32 &&
        pMouseY >= this.getY() + (float) this.getHeight() / 2 - 13 && pMouseY <= this.getY() + (float) this.getHeight() / 2 + 13);
    }

    private Pair<Float, Float> getYBorders() {
        return Pair.of(this.getY() + (float) this.getHeight() / 2 - 13, this.getY() + (float) this.getHeight() / 2 + 13);
    }

    private float getBarHeight() {
        return 26;
    }

    private float getPercentage(double y) {
        return (float) (y - getYBorders().getFirst()) / getBarHeight();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!(this.active && this.visible)) return false;
        if (!this.isHasBar()) return false;
        if (!isMouseOver(pMouseX, pMouseY)) return false;
        this.index = Math.max(Math.min(index - (int) Math.round(pDelta), descriptions.size() - 1), 0);
        updateSlidingButtonPosition();
        return true;
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        onClick(pMouseX + pDragX, pMouseY + pDragY);
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        float percentage = Math.max(Math.min(getPercentage(pMouseY), 1), 0);
        this.index = Math.round((descriptions.size() - 1) * percentage);
        updateSlidingButtonPosition();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FinishedTooltip tooltip)) return false;
        return this.identifier.equals(tooltip.identifier);
    }

    public boolean is(Object obj) {
        if (!(obj instanceof ClientTechTreeGroup) &&
                !(obj instanceof ClientTechTreeNode)) return false;
        if (obj instanceof ClientTechTreeGroup group)
            return this.identifier.equals(group.getId().toString());
        if (obj instanceof ClientTechTreeNode node)
            return this.identifier.equals(node.location.toString());
        return false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {

    }
}
