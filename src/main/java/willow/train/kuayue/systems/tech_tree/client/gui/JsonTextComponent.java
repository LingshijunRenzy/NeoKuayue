package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.systems.tech_tree.json.TextJsonDefine;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class JsonTextComponent extends AbstractWidget {

    private final LazyRecomputable<ImageMask> scrollBarUp =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(32f / 128, 94f / 128, 36f / 128, 99f / 128));

    private final LazyRecomputable<ImageMask> scrollBarDown =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(32f / 128f, 123f / 128f, 36f / 128f, 1));

    private final LazyRecomputable<ImageMask> scrollBarBtn =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(36f / 128, 96f / 128, 42f / 128, 102f / 128));

    private final @NotNull TextJsonDefine textJson;
    private final @NotNull ArrayList<MutableComponent> components;
    private boolean renderScrollBar;
    private int textIndex;
    private int renderCapacity;

    public JsonTextComponent(@NotNull TextJsonDefine textJson, int xOffset, int yOffset,
                             int centerX, int centerY, Function<Integer, Integer> mapFunc) {
        super(xOffset, yOffset, 0, 0, textJson.getComponent());
        this.textJson = textJson;
        renderScrollBar = false;
        renderCapacity = 0;
        components = new ArrayList<>();
        updatePositions(xOffset, yOffset, centerX, centerY, mapFunc);
        updateTextList();
        updateScrollBarArrowsPosition();
    }

    private void setPos(int x, int y) {
        this.setPosition(x, y);
    }

    public void updatePositions(int xOffset, int yOffset, int centerX, int centerY,
                                Function<Integer, Integer> mapFunc) {
        this.setWidth(mapFunc.apply(textJson.getWidth()));
        this.setHeight(mapFunc.apply(textJson.getHeight()));
        if (textJson.isHorizontalCenter() || textJson.isVerticalCenter()) {
            if (textJson.isHorizontalCenter() && textJson.isVerticalCenter()) {
                setPos(getCenterX(xOffset, centerX, textJson, mapFunc),
                        getCenterY(yOffset, centerY, textJson, mapFunc));
            } else if (textJson.isHorizontalCenter()) {
                setPos(getCenterX(xOffset, centerX, textJson, mapFunc),
                        getY(yOffset, textJson, mapFunc));
            } else {
                setPos(getX(xOffset, textJson, mapFunc),
                        getY(yOffset, textJson, mapFunc));
            }
        } else {
            int x = getX(xOffset, textJson, mapFunc);
            int y = getY(yOffset, textJson, mapFunc);
            if (textJson.isPositionIsCenter()) {
                x -= mapFunc.apply(textJson.getWidth()) / 2;
                y -= mapFunc.apply(textJson.getHeight()) / 2;
            }
            setPos(x, y);
        }
    }

    private void updateTextList() {
        Style style = textJson.getStyle();
        Font font = Minecraft.getInstance().font;
        String str = textJson.getComponent().getString();
        while (!str.isEmpty()) {
            String sub = font.plainSubstrByWidth(str, this.width);
            this.components.add(Component.literal(sub).setStyle(style));
        }
        updateShouldRenderScrollBar();
    }

    private void updateShouldRenderScrollBar() {
        Font font = Minecraft.getInstance().font;
        renderCapacity = height / font.lineHeight;
        renderScrollBar = renderCapacity > this.components.size();
        textIndex = 0;
    }

    private void updateScrollBarButton(float percentage) {
        if (!renderScrollBar) return;
        scrollBarBtn.get().rectangle(
                new Vector3f(this.getX() + this.width - 1,
                        this.getY() + percentage * (this.height - 12),
                        0),
                ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, 6, 6
        );
    }

    private void updateScrollBarArrowsPosition() {
        scrollBarUp.get().rectangle(
                new Vector3f(this.getX() + this.width, this.getY(), 0),
                ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, 4, 5
        );
        scrollBarDown.get().rectangle(
                new Vector3f(this.getX() + this.width, this.getY() + this.height - 5, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, 4, 5
        );
    }

    public int getX(int xOffset, TextJsonDefine textJson, Function<Integer, Integer> mapFunc) {
        return xOffset + mapFunc.apply(textJson.getX());
    }

    public int getY(int yOffset, TextJsonDefine textJson, Function<Integer, Integer> mapFunc) {
        return yOffset + mapFunc.apply(textJson.getY());
    }

    public int getCenterX(int xOffset, int centerX, TextJsonDefine textJson,
                          Function<Integer, Integer> mapFunc) {
        return xOffset + mapFunc.apply(centerX) - mapFunc.apply(textJson.getWidth()) / 2;
    }

    public int getCenterY(int yOffset, int centerY, TextJsonDefine textJson,
                          Function<Integer, Integer> mapFunc) {
        return yOffset + mapFunc.apply(centerY) - mapFunc.apply(textJson.getHeight()) / 2;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX,
                             int mouseY, float partialTick) {
        if (renderScrollBar) renderScrollBar(graphics);
        Font font = Minecraft.getInstance().font;
        for (int i = textIndex;
             i < Math.min(components.size(), textIndex + renderCapacity);
             i++) {
            MutableComponent component = components.get(i);
            font.draw(poseStack, component, x, y + i * font.lineHeight, 0xffffffff);
        }
    }

    private void renderScrollBar(GuiGraphics graphics) {
        scrollBarUp.get().renderToGui();
        scrollBarDown.get().renderToGui();
        graphics.fill(this.getX() + this.width + 2, this.getY() + 6,
                this.getX() + this.width + 4, this.getY() + this.height - 6,
                0xffffffff);
        scrollBarBtn.get().renderToGui();
    }

    public boolean isMouseOverScrollBar(double mouseX, double mouseY) {
        return mouseX >= this.getX() + this.width && mouseX <= this.getX() + this.width + 4 &&
                mouseY >= this.getY() + 6 && mouseY <= this.getY() + this.height - 6;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!(this.active && this.visible)) return false;
        if (!this.renderScrollBar) return false;
        if (!isMouseOver(pMouseX, pMouseY)) return false;
        float percentage = (float) (this.textIndex - pMouseX) / (float) this.components.size();
        updateScrollBarButton(percentage);
        return true;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        onClick(mouseX + dragX, mouseY + dragY);
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (!isMouseOverScrollBar(pMouseX, pMouseY)) return;
        float percentage = (float) (pMouseY - (float) (this.getY() + 6)) /
                (float) (this.height - 12);
        updateScrollBarButton(percentage);
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.HINT, textJson.getComponent());
    }
}
