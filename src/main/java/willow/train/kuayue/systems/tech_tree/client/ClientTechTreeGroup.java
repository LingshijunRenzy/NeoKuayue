package willow.train.kuayue.systems.tech_tree.client;

import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class ClientTechTreeGroup {

    private final ResourceLocation id;

    private final @Nullable ResourceLocation coverId;

    private final String titleKey, descriptionKey;

    private final ItemStack icon;

    private final NodeLocation root;

    @Setter
    private ClientTechTreeNode rootNode;

    private final HashSet<NodeLocation> prev;

    private final HashMap<NodeLocation, ClientTechTreeNode> nodes;

    private final int prevSize, nodeSize;

    public ClientTechTreeGroup(FriendlyByteBuf buf) {
        id = buf.readResourceLocation();
        String coverString = buf.readUtf();
        if (coverString.equals("null")) coverId = null;
        else coverId = buf.readResourceLocation();
        titleKey = buf.readUtf();
        descriptionKey = buf.readUtf();
        icon = buf.readItem();
        root = NodeLocation.readFromByteBuf(buf);

        prev = new HashSet<>();
        prevSize = buf.readInt();
        for (int i = 0; i < prevSize; i++) {
            prev.add(NodeLocation.readFromByteBuf(buf));
        }

        nodes = new HashMap<>();
        nodeSize = buf.readInt();
        for (int i = 0; i < nodeSize; i++) {
            nodes.put(NodeLocation.readFromByteBuf(buf), null);
        }

        rootNode = null;
    }

    public ClientTechTreeGroup(ResourceLocation id, ResourceLocation coverId,
                               String titleKey,
                               String descriptionKey, ItemStack icon,
                               NodeLocation root, HashSet<NodeLocation> prev,
                               HashMap<NodeLocation, ClientTechTreeNode> nodes) {
        this.id = id;
        this.coverId = coverId;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.icon = icon;
        this.root = root;

        this.prev = prev;
        this.nodes = nodes;
        this.prevSize = prev.size();
        this.nodeSize = nodes.size();
        this.rootNode = nodes.get(root);
    }

    public Pair<ClientTechTreeGroup, Map<NodeLocation, ClientTechTreeNode>> getVisiblePart(PlayerData data) {
        HashMap<NodeLocation, ClientTechTreeNode> nodes = new HashMap<>(this.nodes);
        HashSet<NodeLocation> prev = new HashSet<>(this.prev);

        HashSet<NodeLocation> shouldBeRemoved = new HashSet<>();
        for (Map.Entry<NodeLocation, ClientTechTreeNode> entry : nodes.entrySet()) {
            if (!data.visibleNodes.contains(entry.getKey()) && !data.unlocked.contains(entry.getKey())) {
                shouldBeRemoved.add(entry.getKey());
            }
        }
        shouldBeRemoved.forEach(nodes::remove);
        HashMap<NodeLocation, ClientTechTreeNode> neoNodes = new HashMap<>();
        nodes.forEach((location, node) -> {
            neoNodes.put(location, node.copy());
        });
        return Pair.of(new ClientTechTreeGroup(id, coverId, titleKey, descriptionKey, icon, root, prev, neoNodes), neoNodes);
    }
}
