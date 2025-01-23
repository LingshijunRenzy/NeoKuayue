package willow.train.kuayue.systems.device.driver.path;

import kasuga.lib.core.client.frontend.gui.styles.node.BackgroundUVStyle;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.StaticImage;
import net.minecraftforge.common.util.Lazy;
import willow.train.kuayue.initial.AllElements;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class PathMarkRenderTexture {
    protected static Lazy<StaticImage> IMAGE = Lazy.of(()->{
        try {
            return StaticImage.createImage(AllElements.testRegistry.asResource("textures/gui/lkj2000_native/devices.png")).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    public static Lazy<ImageMask> STATION_LABEL = Lazy.of(()->{
       return IMAGE.get().getMask().rectangleUV((float)225 / 256f, (float)87 / 256f, (float)(225 + 18) / 256f,  (float)(87 + 18)/256f);
    });
}
