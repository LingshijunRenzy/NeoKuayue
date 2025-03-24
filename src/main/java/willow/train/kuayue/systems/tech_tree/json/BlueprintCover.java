package willow.train.kuayue.systems.tech_tree.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.tech_tree.client.gui.JsonTextComponent;

@Getter
public class BlueprintCover {

    private final ResourceLocation identifier;

    private final boolean isDefaultCover;

    private final @Nullable ImageJsonDefine background, title, buttonBg;
    private final @Nullable TextJsonDefine description;

    public BlueprintCover(JsonObject json) {
        this.identifier = new ResourceLocation(json.get("id").getAsString());
        this.isDefaultCover = json.has("is_default") &&
                json.get("is_default").getAsBoolean();
        background = json.has("background") ?
                new ImageJsonDefine(json.get("background").getAsJsonObject()) : null;
        title = json.has("title") ?
                new ImageJsonDefine(json.get("title").getAsJsonObject()) : null;
        buttonBg = json.has("button_background") ?
                new ImageJsonDefine(json.get("button_background").getAsJsonObject()) :
                null;
        description = json.has("description") ?
                new TextJsonDefine(json.get("description").getAsJsonObject()) : null;
    }

    public boolean hasBackground() {
        return background != null;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasButtonBg() {
        return buttonBg != null;
    }

    public boolean hasDescription() {
        return description != null;
    }
}
