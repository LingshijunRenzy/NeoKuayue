package willow.train.kuayue.systems.tech_tree.client.gui;

import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import willow.train.kuayue.systems.editable_panel.widget.ImageButton;
import willow.train.kuayue.systems.tech_tree.json.ImageJsonDefine;

import java.util.function.Function;

import static willow.train.kuayue.systems.tech_tree.client.gui.JsonImage.getPosition;

public class JsonImageButton extends ImageButton {

    @Getter
    private final ImageJsonDefine define;

    public JsonImageButton(int offsetX, int offsetY, int offsetZ,
                           int centerX, int centerY, ImageJsonDefine image,
                           Function<Integer, Integer> mapFunc,
                           OnPress onPress) {
        super(LazyRecomputable.of(image::getMaskWithUV), offsetX, offsetY,
                mapFunc.apply(image.getWidth()), mapFunc.apply(image.getHeight()),
                Component.empty(), onPress);
        this.define = image;
        updatePosition(offsetX, offsetY, centerX, centerY, mapFunc);
    }

    public void updatePosition(int offsetX, int offsetY,
                               int centerX, int centerY,
                               Function<Integer, Integer> mapFunc) {
        Pair<Integer, Integer> xAndy = getPosition(offsetX, offsetY,
                centerX, centerY, this.define, mapFunc);
        this.setPos(xAndy.getFirst(), xAndy.getSecond());
    }
}
