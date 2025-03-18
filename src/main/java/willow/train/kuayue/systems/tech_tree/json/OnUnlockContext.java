package willow.train.kuayue.systems.tech_tree.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kasuga.lib.core.util.ComponentHelper;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import willow.train.kuayue.systems.tech_tree.NodeLocation;

import java.util.*;

public class OnUnlockContext {
    
    public final String description;
    @Getter
    private final NodeLocation[] unlockNodes;
    @Getter
    private final HashMap<ResourceLocation, Collection<String>> unlockAdvancements;
    @Getter
    private final ItemContext[] items;
    
    public OnUnlockContext(TechTreeGroupData group, JsonObject json) {
        description = json.get("description").getAsString();

        if (json.has("advancements") && json.get("advancements").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("advancements");
            unlockAdvancements = new HashMap<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                JsonElement advJson = array.get(i);
                if (advJson.isJsonArray()) {
                    JsonObject advObj = advJson.getAsJsonObject();
                    JsonArray advArray = advObj.getAsJsonArray("criteria");
                    List<String> list = new ArrayList<>(advArray.size());
                    advArray.forEach(element -> list.add(element.getAsString()));
                    unlockAdvancements.put(new ResourceLocation(advObj.get("id").getAsString()), list);
                } else {
                    unlockAdvancements.put(new ResourceLocation(advJson.getAsString()), List.of());
                }
            }
        } else {
            unlockAdvancements  = new HashMap<>();
        }

        if (json.has("items") && json.get("items").isJsonObject()) {
            JsonObject obj = json.getAsJsonObject("items");
            items = new ItemContext[obj.size()];
            int counter = 0;
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                items[counter] = new ItemContext(entry);
                counter++;
            }
        } else items = new ItemContext[0];

        if (json.has("nodes") && json.get("nodes").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("nodes");
            unlockNodes = new NodeLocation[array.size()];
            for (int i = 0; i < array.size(); i++) {
                unlockNodes[i] = new NodeLocation(group.tree.namespace, group.identifier, array.get(i).getAsString());
            }
        } else {
            unlockNodes = new NodeLocation[0];
        }
    }

    public void loadAllNbt(ResourceManager manager) {
        for (ItemContext context : items) {
            context.updateNbt(manager);
        }
    }

    public Set<ItemStack> getReward() {
        Set<ItemStack> result = new HashSet<>();
        for (ItemContext context : items) {
            result.addAll(context.getItem());
        }
        return result;
    }

    public Component getDescription() {
        return ComponentHelper.translatable(description);
    }
}
