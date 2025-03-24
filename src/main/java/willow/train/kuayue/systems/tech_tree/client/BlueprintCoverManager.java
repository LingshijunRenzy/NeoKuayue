package willow.train.kuayue.systems.tech_tree.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.tech_tree.json.BlueprintCover;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlueprintCoverManager implements ResourceManagerReloadListener {

    public static final BlueprintCoverManager INSTANCE = new BlueprintCoverManager();

    public static final String PATH = "blueprint_cover/";

    @Getter
    private final HashMap<ResourceLocation, BlueprintCover> covers;

    public static BlueprintCoverManager getInstance() {
        return INSTANCE;
    }

    private BlueprintCoverManager() {
        covers = new HashMap<>();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        covers.clear();
        Map<ResourceLocation, Resource> map = resourceManager.listResources(PATH, img -> true);
        map.forEach((rl, resource) -> {
            try {
                JsonElement element = JsonParser.parseReader(resource.openAsReader());
                if (!element.isJsonObject()) {
                    Kuayue.LOGGER.error(
                            "File {} is not a valid image json definition.", rl);
                    return;
                }
                JsonObject json = element.getAsJsonObject();
                BlueprintCover cover = new BlueprintCover(json);
                covers.put(cover.getIdentifier(), cover);
            } catch (IOException e) {
                Kuayue.LOGGER.error("Failed to read cover {}", rl, e);
            } catch (JsonSyntaxException e) {
                Kuayue.LOGGER.error("Failed to parse cover {}", rl, e);
            } catch (Exception e) {
                Kuayue.LOGGER.error(
                        "Failed to read cover {} due to unexpected error.", rl, e);
            }
        });
    }

    public @Nullable BlueprintCover getCover(ResourceLocation rl) {
        return covers.getOrDefault(rl, null);
    }

    public boolean hasCover(ResourceLocation rl) {
        return covers.containsKey(rl);
    }

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(getInstance());
    }
}
