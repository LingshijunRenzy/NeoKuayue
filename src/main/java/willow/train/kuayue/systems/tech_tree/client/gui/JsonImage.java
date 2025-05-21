package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.tech_tree.json.ImageJsonDefine;

import java.util.function.Function;

@Getter
public class JsonImage extends AbstractWidget {

    private final ImageJsonDefine image;
    private @Nullable ImageMask mask;

    public JsonImage(int offsetX, int offsetY, int offsetZ, int centerX, int centerY,
                     ImageJsonDefine image, Function<Integer, Integer> mapFunc) {
        super(offsetX, offsetY, 0, 0, Component.empty());
        this.image = image;
        mask = compileImage(offsetX, offsetY, offsetZ, centerX, centerY, image, mapFunc);
    }

    public void setPosition(int offsetX, int offsetY, int offsetZ, int centerX, int centerY,
                            Function<Integer, Integer> mapFunc) {
        mask = compileImage(offsetX, offsetY, offsetZ, centerX, centerY, image, mapFunc);
    }

    private ImageMask compileImage(int offsetX, int offsetY, int offsetZ,
                                        int centerX, int centerY, ImageJsonDefine bgJson,
                                        Function<Integer, Integer> mapFunc) {
        if (bgJson == null) return null;
        Pair<Integer, Integer> xAndy = getPosition(offsetX, offsetY,
                centerX, centerY, bgJson, mapFunc);
        int imageZ = Math.round(bgJson.getPos().z());
        this.x = xAndy.getFirst();
        this.y = xAndy.getSecond();
        this.width = mapFunc.apply(bgJson.getWidth());
        this.height = mapFunc.apply(bgJson.getHeight());
        return bgJson.getMaskWithUV().rectangle(
                new Vector3f(xAndy.getFirst(), xAndy.getSecond(),
                        offsetZ + mapFunc.apply(imageZ)),
                bgJson.getAxisX(), bgJson.getAxisY(),
                bgJson.isXPositive(), bgJson.isYPositive(),
                mapFunc.apply(bgJson.getWidth()),
                mapFunc.apply(bgJson.getHeight())
        );
    }

    public static Pair<Integer, Integer> getPosition(int offsetX, int offsetY,
                                               int centerX, int centerY,
                                               ImageJsonDefine json,
                                               Function<Integer, Integer> mapFunc) {
        int x, y;
        if (json.isHorizontalCentered() || json.isVerticalCentered()) {
            if (json.isHorizontalCentered() && json.isVerticalCentered()) {
                x = getCenterX(offsetX, centerX, json, mapFunc);
                y = getCenterY(offsetY, centerY, json, mapFunc);
            } else if (json.isHorizontalCentered()) {
                x = getCenterX(offsetX, centerX, json, mapFunc);
                y = offsetY + mapFunc.apply(Math.round(json.getPos().y()));
            } else {
                x = offsetX + mapFunc.apply(Math.round(json.getPos().x()));
                y = getCenterY(offsetY, centerY, json, mapFunc);
            }
        } else {
            x = offsetX + mapFunc.apply(Math.round(json.getPos().x()));
            y = offsetY + mapFunc.apply(Math.round(json.getPos().y()));
            if (json.isPositionIsCenter()) {
                x -= mapFunc.apply(json.getWidth()) / 2;
                y -= mapFunc.apply(json.getHeight()) / 2;
            }
        }
        return Pair.of(x, y);
    }

    public static int getCenterX(int offsetX, int centerX, ImageJsonDefine image,
                           Function<Integer, Integer> mapFunc) {
        return offsetX + mapFunc.apply(centerX- image.getWidth() / 2);
    }

    public static int getCenterY(int offsetY, int centerY, ImageJsonDefine image,
                           Function<Integer, Integer> mapFunc) {
        return offsetY + mapFunc.apply(centerY - image.getHeight() / 2);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        if (mask != null) mask.renderToGui();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
