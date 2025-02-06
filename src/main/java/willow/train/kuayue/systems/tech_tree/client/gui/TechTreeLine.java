package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Vec2i;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import willow.train.kuayue.initial.ClientInit;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.function.Consumer;

public class TechTreeLine extends AbstractWidget {

    private static final LazyRecomputable<ImageMask> linePixelMask = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(39f / 128f, 32f / 128f, 40f / 128f, 33f / 128f)
    );

    private static final LazyRecomputable<ImageMask> lineUpArrowMask = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(36f / 128f, 32f / 128f, 44f / 128f, 36f / 128f)
    );

    private static final LazyRecomputable<ImageMask> lineDownArrowMask = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(36f / 128f, 44f / 128f, 44f / 128f, 48f / 128f)
    );

    private static final LazyRecomputable<ImageMask> lineLeftArrowMask = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(32f / 128f, 36f / 128f, 36f / 128f, 44f / 128f)
    );

    private static final LazyRecomputable<ImageMask> lineRightArrowMask = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(44f / 128f, 36f / 128f, 48f / 128f, 44f / 128f)
    );

    private final LazyRecomputable<ImageMask> pixelMask = LazyRecomputable.of(
            () -> linePixelMask.get().copyWithOp(o -> o)
    );

    private final ImageMask[] masks;
    private final ImageMask[] arrows;
    /*
    0 -> up
    1 -> left
    2 -> down
    3 -> right
     */
    private final boolean[] shouldRender;
    /*
    0 -> up
    1 -> left
    2 -> down
    3 -> right
     */
    private final boolean[] shouldRenderArrow;

    private final boolean[] tail;

    @Getter
    private SimpleColor color;

    public TechTreeLine(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.masks = new ImageMask[4];
        this.arrows = new ImageMask[4];
        this.shouldRender = new boolean[]
                {false, false, false, false};
        this.shouldRenderArrow = new boolean[]
                {false, false, false, false};
        this.tail = new boolean[]
                {false, false, false, false};
        updateMasks();
        updateArrows();
        color = SimpleColor.fromRGBInt(0xffffff);
    }

    public Vec2i getCenter() {
        return new Vec2i(this.x + this.width / 2,
                this.y + this.height / 2);
    }

    public void setColor(SimpleColor color) {
        this.color = color;
        updateColor();
    }

    public void updateColor() {
        ImageMask m;
        for (int i = 0; i < 4; i++) {
            m = masks[i];
            if (m != null) {
                m.setColor(color);
            }
            m = arrows[i];
            if (m != null)
                m.setColor(color);
        }
    }

    public void setX(int x) {
        this.x = x;
        updateMasks();
        updateArrows();
    }

    public void setY(int y) {
        this.y = y;
        updateMasks();
        updateArrows();
    }

    public void setPos(Vec2i pos) {
        this.x = pos.x;
        this.y = pos.y;
        updateMasks();
        updateArrows();
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        updateMasks();
        updateArrows();
    }

    public void renderUp(boolean render) {
        shouldRender(0, render);
    }

    public void renderLeft(boolean render) {
        shouldRender(1, render);
    }

    public void renderDown(boolean render) {
        shouldRender(2, render);
    }

    public void renderRight(boolean render) {
        shouldRender(3, render);
    }

    public void renderUpArrow(boolean render) {
        shouldRenderArrow(0, render);
    }

    public void renderLeftArrow(boolean render) {
        shouldRenderArrow(1, render);
    }

    public void renderDownArrow(boolean render) {
        shouldRenderArrow(2, render);
    }

    public void renderRightArrow(boolean render) {
        shouldRenderArrow(3, render);
    }

    public void shouldRenderArrow(int index, boolean render) {
        renderArrow(index, render);
        if (render) setTail(index, true);
    }

    public void shouldRenderArrow(Side side, boolean render) {
        shouldRenderArrow(side.getIndex(), render);
    }

    public void shouldRender(int index, boolean render) {
        this.shouldRender[index] = render;
    }

    public void shouldRender(Side side, boolean render) {
        shouldRender(side.getIndex(), render);
    }

    public void renderArrow(int index, boolean render) {
        this.shouldRenderArrow[index] = render;
    }

    public boolean isUpRendered() {
        return this.shouldRender[0];
    }

    public boolean isLeftRendered() {
        return this.shouldRender[1];
    }

    public boolean isDownRendered() {
        return this.shouldRender[2];
    }

    public boolean isRightRendered() {
        return this.shouldRender[3];
    }

    public boolean isUpArrowRendered() {
        return this.shouldRenderArrow[0];
    }

    public boolean isLeftArrowRendered() {
        return this.shouldRenderArrow[1];
    }

    public boolean isDownArrowRendered() {
        return this.shouldRenderArrow[2];
    }

    public boolean isRightArrowRendered() {
        return this.shouldRenderArrow[3];
    }

    public boolean isRendered(int index) {
        return this.shouldRender[index];
    }

    public boolean isArrowRendered(int index) {
        return this.shouldRenderArrow[index];
    }


    public void setUpTail(boolean tail) {
        setTail(0, tail);
    }

    public void setLeftTail(boolean tail) {
        setTail(1, tail);
    }

    public void setDownTail(boolean tail) {
        setTail(2, tail);
    }

    public void setRightTail(boolean tail) {
        setTail(3, tail);
    }

    public void setTail(int index, boolean tail) {
        boolean flag = this.tail[index] != tail;
        this.tail[index] = tail;
        if (flag) updateMasks();
    }

    public void setTail(Side side, boolean tail) {
        setTail(side.getIndex(), tail);
    }

    public boolean isUpTailed() {
        return this.tail[0];
    }

    public boolean isLeftTailed() {
        return this.tail[1];
    }

    public boolean isDownTailed() {
        return this.tail[2];
    }

    public boolean isRightTailed() {
        return this.tail[3];
    }

    public boolean isTailed(int index) {
        return this.tail[index];
    }

    public boolean isTailed(Side side) {
        return isTailed(side.index);
    }

    public void updateMasks() {
        Vec2i center = getCenter();
        // up
        masks[0] = pixelMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(center.x - 1, this.y + (tail[0] ? 2 : 0), 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        2, (float) this.height / 2 - (tail[0] ? 2 : 0))
        );
        // down
        masks[2] = pixelMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(center.x - 1, center.y, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        2, (float) this.height / 2 - (tail[2] ? 2 : 0))
        );
        // left
        masks[1] = pixelMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(this.x + (tail[1] ? 2 : 0), center.y - 1, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        (float) this.width / 2 - (tail[1] ? 2 : 0), 2)
        );
        // right
        masks[3] = pixelMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(center.x, center.y - 1, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        (float) this.width / 2 - (tail[3] ? 2 : 0), 2)
        );
        updateColor();
    }

    public void updateArrows() {
        Vec2i center = getCenter();
        arrows[0] = lineUpArrowMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(center.x - 4, y, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        8, 4)
        );
        arrows[1] = lineLeftArrowMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(x, center.y - 4, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        4, 8)
        );
        arrows[2] = lineDownArrowMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(center.x - 4, y + height - 4, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        8, 4)
        );
        arrows[3] = lineRightArrowMask.get().copyWithOp(
                o -> o.rectangle(new Vector3f(x + width - 4, center.y - 4, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                        4, 8)
        );
        updateColor();
    }

    public void render(ImageMask[] m, boolean[] b, Consumer<Object> updateFunc) {
        for (int i = 0; i < 4; i++) {
            ImageMask mask = m[i];
            if (mask == null) {
                updateFunc.accept(null);
                return;
            }
            if (!b[i]) continue;
            mask.renderToGui();
        }
    }

    @Override
    public void renderButton(@NonNull PoseStack poseStack, int mouseX,
                             int mouseY, float partialTick) {
        render(masks, shouldRender, (obj) -> {
            updateMasks();
            updateColor();
        });
        render(arrows, shouldRenderArrow, (obj) -> {
            updateArrows();
            updateColor();
        });
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {}

    @Getter
    public static enum Side {

        UP(0),
        LEFT(1),
        DOWN(2),
        RIGHT(3);

        private final int index;
        private Side(int index){
            this.index = index;
        }

        public Vec2iE getNearBy(Vec2iE position) {
            return switch (this) {
                case DOWN -> position.copy().add(0, 1);
                case UP -> position.copy().add(0, -1);
                case LEFT -> position.copy().add(-1, 0);
                case RIGHT -> position.copy().add(1, 0);
            };
        }

        public static HashMap<Side, Vec2iE> getNearBys(Vec2iE position) {
            HashMap<Side, Vec2iE> result = new HashMap<>();
            result.put(UP, UP.getNearBy(position));
            result.put(DOWN, DOWN.getNearBy(position));
            result.put(LEFT, LEFT.getNearBy(position));
            result.put(RIGHT, RIGHT.getNearBy(position));
            return result;
        }
    }
}
