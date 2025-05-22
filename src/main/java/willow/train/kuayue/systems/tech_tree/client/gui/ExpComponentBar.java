package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import willow.train.kuayue.initial.ClientInit;

import java.awt.*;

public class ExpComponentBar extends AbstractWidget {

    private final LazyRecomputable<ImageMask> yesBg =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(48f / 128f, 96f / 128f, 57f / 128f, 107f / 128f));

    private final LazyRecomputable<ImageMask> noBg =
            LazyRecomputable.of(() -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(48f / 128f, 112f / 128f, 57f / 128f, 123f / 128f));

    @Setter
    private boolean canUnlock = false;
    private MutableComponent component;

    private static int GREEN = Color.GREEN.getRGB(),
                        RED = Color.RED.getRGB();

    public ExpComponentBar(int pX, int pY) {
        super(pX, pY, 0, 0, Component.empty());
        setUnlock(0, false);
        setPos(pX, pY);
    }

    public void setUnlock(int level, boolean canUnlock) {
        this.component = Component.literal(String.valueOf(level));
        this.canUnlock = canUnlock;
        updateSize();
    }

    public void setPos(int x, int y) {
        this.setPosition(x, y);
        setImagePos();
    }

    public void updateSize() {
        Font font = Minecraft.getInstance().font;
        this.setWidth(12 + font.width(component));
        this.setHeight(11);
    }

    private void setImagePos() {
        this.yesBg.get().rectangle(new Vector3f(this.x, this.y, 0), ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, 9, 11);
        this.noBg.get().rectangle(new Vector3f(this.x, this.y, 0), ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, 9, 11);
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partial) {
        Font font = Minecraft.getInstance().font;
        if (canUnlock) yesBg.get().renderToGui();
        else noBg.get().renderToGui();
        font.draw(poseStack, component, this.x + 12,
                this.y + (float) (12 - font.lineHeight) / 2f, canUnlock ? GREEN : RED);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.HINT, component);
    }
}
