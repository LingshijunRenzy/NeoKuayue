package willow.train.kuayue.event.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import java.util.HashMap;
import java.util.Map;

public class ItemTooltipConfig {
    private static final Map<String, TooltipInfo> itemTooltipMap = new HashMap<>();

    static {
        addItemTooltip("ss8_head", "tip.kuayue.ss8_head", ChatFormatting.GREEN);
        addItemTooltip("ss3_head", "tip.kuayue.ss3_head", ChatFormatting.GREEN);
    }

    private static void addItemTooltip(String itemId, String translationKey, ChatFormatting color) {
        itemTooltipMap.put(itemId, new TooltipInfo(translationKey, color));
    }

    public static TooltipInfo getTooltipInfo(String itemId) {
        return itemTooltipMap.get(itemId);
    }

    public static class TooltipInfo {
        private final String translationKey;
        private final ChatFormatting color;

        public TooltipInfo(String translationKey, ChatFormatting color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public MutableComponent createTooltipComponent() {
            return Component.translatable(translationKey).setStyle(Style.EMPTY.withColor(color));
        }
    }
}