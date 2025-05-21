package willow.train.kuayue.utils.client;

import net.minecraft.world.phys.Vec2;

public class ScreenSizeUtil {
    public static Vec2 scaleByWidth(
            Vec2 size,
            Vec2 screenSize,
            float widthPadding
    ){
        return size.scale( (screenSize.x - 2 * widthPadding) / size.x);
    }
}
