package willow.train.kuayue.systems.tech_tree.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.json.TechTreeData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TechTreeManager implements ResourceManagerReloadListener {
    private final HashMap<String, TechTree> trees;
    @Getter
    private final Set<String> namespaces;
    public static final TechTreeManager MANAGER = new TechTreeManager();

    protected TechTreeManager() {
        trees = new HashMap<>();
        namespaces = new HashSet<>();
    }

    public HashMap<String, TechTree> trees() {
        return trees;
    }

    public @Nullable TechTreeNode getNode(NodeLocation location) {
        String namespace = location.getNamespace();
        TechTree tree = trees.getOrDefault(namespace, null);
        if (tree == null) return null;
        return tree.getNodes().getOrDefault(location, null);
    }

    public boolean containsNode(NodeLocation location) {
        return getNode(location) != null;
    }

    public @Nullable TechTree getTree(String namespace) {
        return trees.getOrDefault(namespace, null);
    }

    public boolean containsTree(String namespace) {
        return trees.containsKey(namespace);
    }

    public @Nullable TechTreeGroup getGroup(ResourceLocation groupLocation) {
        TechTree tree = getTree(groupLocation.getNamespace());
        if (tree == null) return null;
        return tree.getGroups().getOrDefault(groupLocation.getPath(), null);
    }

    public boolean containsGroup(ResourceLocation location) {
        return getGroup(location) != null;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        this.namespaces.clear();
        this.namespaces.addAll(resourceManager.getNamespaces());
    }

    public void loadData(@NotNull ResourceManager manager) {
        namespaces.clear();
        namespaces.addAll(manager.getNamespaces());
        Map<ResourceLocation, Resource> resources =
                manager.listResources("tech_tree", (rl) -> true);
        for (String namespace : namespaces) {
            JsonObject treeObject = new JsonObject();
            treeObject.addProperty("version", Kuayue.TECH_TREE_VERSION);
            JsonObject groupsObject = new JsonObject();
            treeObject.add("groups", groupsObject);
            resources.forEach((rl, techTreeResource) -> {
                try {
                    JsonElement element = JsonParser.parseReader(techTreeResource.openAsReader());
                    if (!element.isJsonObject()) return;
                    JsonObject groupObject = element.getAsJsonObject();
                    String version = groupObject.get("version").getAsString();
                    if (version == null || !version.equals(Kuayue.TECH_TREE_VERSION)) {
                        Kuayue.LOGGER.error("Incompatible tech tree group: {}, need version {}, found version {}",
                                rl, Kuayue.TECH_TREE_VERSION, version == null ? "null" : version);
                        return;
                    }
                    ResourceLocation id = new ResourceLocation(groupObject.get("id").getAsString());
                    if (!id.getNamespace().equals(namespace)) return;
                    groupsObject.add(id.getPath(), element);
                } catch (JsonParseException e) {
                    Kuayue.LOGGER.error("Failed to parse tech tree json: {}", rl, e);
                } catch (Exception e) {
                    Kuayue.LOGGER.error("Failed to read tech tree json: {}", rl, e);
                }
            });
            TechTreeData data = new TechTreeData(namespace, treeObject);
            TechTree tree = new TechTree(data);
            tree.grepNbt(manager);
            this.trees.put(namespace, tree);
        }
        connectNodes();
    }

    public void connectNodes() {
        trees.forEach((namespace, tree) -> {
            tree.compileConnections();
        });
    }
}
