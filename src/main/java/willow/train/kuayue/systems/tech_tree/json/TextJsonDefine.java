package willow.train.kuayue.systems.tech_tree.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;

@Getter
public class TextJsonDefine {

    private final String textKey;
    private final TextType type;
    private final int x, y, width, height;
    private final boolean horizontalCenter, verticalCenter,
            positionIsCenter;
    private final Style style;

    public TextJsonDefine(JsonObject json) {
        textKey = json.get("textKey").getAsString();
        if (json.has("style")) {
            Style.Serializer serializer = new Style.Serializer();
            Style s = serializer.deserialize(json.getAsJsonObject("style"),
                    Style.class, null);
            style = s == null ? Style.EMPTY : s;
        } else {
            style = Style.EMPTY;
        }
        if (!json.has("type")) {
            type = TextType.LITERAL;
        } else {
            type = TextType.fromString(json.get("type").getAsString());
        }
        if (!json.has("pos")) {
            x = 0;
            y = 0;
            horizontalCenter = false;
            verticalCenter = false;
            positionIsCenter = false;
        } else {
            JsonElement posElement = json.get("pos");
            if (!posElement.isJsonObject()) {
                x = 0;
                y = 0;
                horizontalCenter = false;
                verticalCenter = false;
                positionIsCenter = false;
            } else {
                JsonElement vecElement = posElement.getAsJsonObject().get("vec");
                if (!vecElement.isJsonArray()) {
                    x = 0;
                    y = 0;
                } else {
                    JsonArray vecArray = vecElement.getAsJsonArray();
                    if (vecArray.size() < 2) {
                        x = 0;
                        y = 0;
                    } else {
                        x = vecArray.get(0).getAsInt();
                        y = vecArray.get(1).getAsInt();
                    }
                }
                JsonObject posObject = json.getAsJsonObject("pos");
                verticalCenter = posObject.has("vertical_center") &&
                        posObject.get("vertical_center").getAsBoolean();
                horizontalCenter = posObject.has("horizontal_center") &&
                        posObject.get("horizontal_center").getAsBoolean();
                positionIsCenter = posObject.has("position_is_center") &&
                        posObject.get("position_is_center").getAsBoolean();
            }
        }
        if (!json.has("size")) {
            width = 0;
            height = 0;
        } else {
            JsonElement sizeElement = json.get("size");
            if (!sizeElement.isJsonArray()) {
                width = 0;
                height = 0;
            } else {
                JsonArray sizeArray = sizeElement.getAsJsonArray();
                if (sizeArray.size() < 2) {
                    width = 0;
                    height = 0;
                } else {
                    width = sizeArray.get(0).getAsInt();
                    height = sizeArray.get(1).getAsInt();
                }
            }
        }
    }

    public MutableComponent getComponent() {
        return switch (type) {
            case LITERAL -> Component.literal(textKey).setStyle(style);
            case TRANSLATABLE -> Component.translatable(textKey).setStyle(style);
            case KEY_BIND -> Component.keybind(textKey).setStyle(style);
        };
    }

    public static enum TextType implements StringRepresentable {
        LITERAL,
        TRANSLATABLE,
        KEY_BIND;

        public static TextType fromString(String string) {
            return switch (string) {
                case "translatable" -> TRANSLATABLE;
                case "key_bind" -> KEY_BIND;
                default -> LITERAL;
            };
        }

        @Override
        public String getSerializedName() {
            return switch (this) {
                case LITERAL -> "literal";
                case TRANSLATABLE -> "translatable";
                case KEY_BIND -> "key_bind";
            };
        }
    }
}
