package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.NineSlicedImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.initial.ClientInit;

public class TechTreeTitleLabel extends AbstractWidget {

    private static final LazyRecomputable<NineSlicedImageMask> bgMask = LazyRecomputable.of(() ->
            ((NineSlicedImageMask) ClientInit.blueprintButtons.getImageSafe().get()
                    .getNineSlicedMask().rectangleUV(0, 56f / 128f,
            48 / 128f, 81f / 128f)));

    private @NotNull Component title;
    private int length = -1;

    public TechTreeTitleLabel(Component title) {
        super(0, 0, 0, 0, Component.empty());
        this.active = false;
        this.title = title;
        bgMask.get().setBordersDirectly(0.125f, 0.875f, 0, 1);
        getTitleWidth();
    }

    public void setTitle(@NotNull Component title) {
        this.title = title;
        getTitleWidth();
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void getTitleWidth() {
        Font font = Minecraft.getInstance().font;
        if (title.getString().length() > 20) {
            String cache = title.getString().substring(0, 17);
            title = Component.literal(cache + "...");
        }
        length = font.width(title);
        this.width = length + 20;
        this.height = 25;
    }


    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX,
                             int mouseY, float partialTick) {
        if (length < 0) getTitleWidth();
        if (length <= 0) return;
        NineSlicedImageMask mask = bgMask.get();
        mask.rectangle(new Vector3f(this.x, this.y, 0), ImageMask.Axis.X,
                ImageMask.Axis.Y, true, true, width, height);
        mask.renderToGui();
        Font font = Minecraft.getInstance().font;
        font.draw(poseStack, title, x + 10, y + 8, 0xffffffff);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }
}
