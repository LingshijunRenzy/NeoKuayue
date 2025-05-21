package willow.train.kuayue.systems.tech_tree.server;

import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.packs.resources.ResourceManager;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.json.TechTreeData;

import java.util.HashMap;
import java.util.HashSet;

@Getter
public class TechTree {

    public final TechTreeData data;

    private final HashMap<String, TechTreeGroup> groups;

    private final HashMap<NodeLocation, TechTreeNode> nodes;

    public TechTree(TechTreeData data) {
        this.data = data;
        this.groups = new HashMap<>();
        this.nodes = new HashMap<>();
        collectGroups();
    }

    protected void collectGroups() {
        data.getGroups().forEach((name, dat) -> {
            groups.put(name, new TechTreeGroup(this, dat));
        });
    }

    protected void compileConnections() {
        nodes.forEach((loc, node) -> {
            node.compileConnections();
        });
        HashSet<String> groupsToRemove = new HashSet<>();
        groups.forEach((str, grp) -> {
            if (grp.hasRing()) {
                Kuayue.LOGGER.error("Found cycle(s) in Tech Tree Group <" + grp.tree.getNamespace() +
                        ":" + str + ">, that group would not be applied.");
                groupsToRemove.add(str);
                return;
            }
            nodes.values().forEach(n -> {
                if (!n.getNextGroups().contains(grp)) return;
                grp.addPrev(n);
            });
        });
        groupsToRemove.forEach(groups::remove);
    }

    protected void grepNbt(ResourceManager manager) {
        groups.forEach((loc, group) -> {
            group.data.loadAllNbt(manager);
        });
    }

    protected void grepNodes(HashMap<NodeLocation, TechTreeNode> grepNodes) {
        nodes.putAll(grepNodes);
    }

    public String getNamespace() {
        return data.namespace;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(getNamespace());

        buf.writeInt(groups.size());
        buf.writeInt(nodes.size());
        groups.forEach((loc, grp) -> buf.writeUtf(loc));
        // groups.forEach((location, grp) -> grp.toNetwork(buf));
    }
}
