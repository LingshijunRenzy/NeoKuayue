package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.core.util.NameUtil;

import java.util.ArrayList;

@Getter
public class Layer extends AbstractWidget {

    protected final ArrayList<AbstractWidget> widgets;

    protected final ArrayList<AbstractWidget> renderableWidgets;

    protected Vec2iE windowLeftTop, windowRightDown;

    public Layer(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        widgets = new ArrayList<>();
        renderableWidgets = new ArrayList<>();
        windowLeftTop = null;
        windowRightDown = null;
    }

    public void onDrag(int dx, int dy) {
        Vec2iE lT = windowLeftTop, rD = windowRightDown;
        setPos(this.x + dx, this.y + dy);
        windowLeftTop = lT;
        windowRightDown = rD;
    }

    public void addWidget(AbstractWidget widget) {
        widgets.add(widget);
    }

    public void removeWidget(AbstractWidget widget) {
        renderableWidgets.remove(widget);
        widgets.remove(widget);
    }

    public void addRenderableWidget(AbstractWidget widget) {
        widgets.add(widget);
        renderableWidgets.add(widget);
    }

    public void addRenderableOnly(AbstractWidget widget) {
        renderableWidgets.add(widget);
    }

    public void setX(int x) {
        int offset = x - this.x;
        widgets.forEach(widget -> {
            widget.x += offset;
        });
        this.x = x;
    }

    public void setY(int y) {
        int offset = y - this.y;
        widgets.forEach(widget -> {
            widget.y += offset;
        });
        this.y = y;
    }

    public void setPos(int x, int y) {
        setX(x);
        setY(y);
    }

    public void setWindow(int leftTopX, int leftTopY, int rightDownX, int rightDownY) {
        this.windowLeftTop = new Vec2iE(Math.min(leftTopX, rightDownX), Math.min(leftTopY, rightDownY));
        this.windowRightDown = new Vec2iE(Math.max(leftTopX, rightDownX), Math.max(leftTopY, rightDownY));
    }

    public void removeWindow() {
        this.windowRightDown = null;
        this.windowLeftTop = null;
    }

    public boolean hasWindow() {
        return windowLeftTop != null || windowRightDown != null;
    }

    @Override
    public void renderButton(@NonNull PoseStack poseStack, int mouseX,
                             int mouseY, float partialTick) {
        renderableWidgets.forEach(
                widget -> {
                    if (windowLeftTop != null && windowRightDown != null) {
                        int centerX = widget.x + widget.getWidth() / 2;
                        int centerY = widget.y + widget.getHeight() / 2;
                        if (centerX < windowLeftTop.x || centerX >= windowRightDown.x) return;
                        if (centerY < windowLeftTop.y || centerY >= windowRightDown.y) return;
                    }
                    widget.render(poseStack, mouseX, mouseY, partialTick);
                }
        );
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean flag = false;
        for (AbstractWidget widget : widgets) {
            flag |= widget.mouseClicked(pMouseX, pMouseY, pButton);
        }
        return flag;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        boolean flag = false;
        for (AbstractWidget widget : widgets) {
            flag |= widget.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return flag;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        boolean flag = false;
        for (AbstractWidget widget : widgets) {
            flag |= widget.mouseReleased(pMouseX, pMouseY, pButton);
        }
        return flag;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        boolean flag = false;
        for (AbstractWidget widget : widgets) {
            flag |= widget.mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        return flag;
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        widgets.forEach(widget -> widget.mouseMoved(pMouseX, pMouseY));
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean flag = false;
        for (AbstractWidget widget : widgets) {
            flag |= widget.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return flag;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        boolean flag = false;
        for (AbstractWidget widget : widgets) {
            flag |= widget.keyReleased(pKeyCode, pScanCode, pModifiers);
        }
        return flag;
    }

    @Override
    public void updateNarration(@NonNull NarrationElementOutput output) {
        for (AbstractWidget widget : widgets) {
            if (widget.isHoveredOrFocused()) widget.updateNarration(output);
        }
    }
}
