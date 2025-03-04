package willow.train.kuayue.systems.tech_tree.client;

import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class ClientTechTree {

    private final String namespace;

    private final HashMap<String, ClientTechTreeGroup> groups;

    private final HashMap<NodeLocation, ClientTechTreeNode> nodes;

    private final int groupSize, nodeSize;

    public ClientTechTree(FriendlyByteBuf buf) {
        groups = new HashMap<>();
        namespace = buf.readUtf();

        this.groupSize = buf.readInt();
        this.nodeSize = buf.readInt();
        for (int i = 0; i < groupSize; i++) {
            groups.put(buf.readUtf(), null);
        }

        nodes = new HashMap<>();
    }

    public void update() {
        HashSet<ClientTechTreeNode> needToAdd = new HashSet<>();
        nodes.forEach((loc, node) -> {
            if (node.getNextNode().size() < node.getNext().size()) {
                node.getNext().forEach(n -> {
                    for (ClientTechTreeNode node1 : node.getNextNode()) {
                        if (node1.location.equals(n)) return;
                    }
                    ClientTechTreeNode neoNext = ClientTechTreeManager.getInstance().getNode(n);
                    if (neoNext == null) return;
                    needToAdd.add(neoNext);
                });
                node.getNextNode().addAll(needToAdd);
                needToAdd.clear();
            }
            if (node.getPrevNode().size() >= node.getPrev().size()) return;
            node.getPrev().forEach(n -> {
                for (ClientTechTreeNode node1 : node.getPrevNode()) {
                    if (node1.location.equals(n)) return;
                }
                ClientTechTreeNode neoPrev = ClientTechTreeManager.getInstance().getNode(n);
                if (neoPrev == null) return;
                needToAdd.add(neoPrev);
            });
            node.getPrevNode().addAll(needToAdd);
            needToAdd.clear();
        });
    }

    public Set<ClientTechTreeGroup> getVisiblePart(PlayerData data) {
        Set<ClientTechTreeGroup> visibleGroups = new HashSet<>();
        this.groups.forEach((grpName, grp) -> {
            if (data.visibleGroups.contains(grp.getId()))
                visibleGroups.add(grp);
            if (data.unlockedGroups.contains(grp.getId()))
                visibleGroups.add(grp);
        });
        Set<ClientTechTreeGroup> result = new HashSet<>();
        HashMap<NodeLocation, ClientTechTreeNode> nodes = new HashMap<>();
        visibleGroups.forEach(grp -> {
            Pair<ClientTechTreeGroup, Map<NodeLocation, ClientTechTreeNode>> pair = grp.getVisiblePart(data);
            nodes.putAll(pair.getSecond());
            result.add(pair.getFirst());
        });
        result.forEach(grp -> {
            grp.getNodes().forEach((location, node) -> {
                node.getPrev().forEach(nodeLocation -> {
                    ClientTechTreeNode n = nodes.getOrDefault(nodeLocation, null);
                    if (n == null) return;
                    node.getPrevNode().add(n);
                });
                node.getNext().forEach(nodeLocation -> {
                    ClientTechTreeNode n = nodes.getOrDefault(nodeLocation, null);
                    if (n == null) return;
                    node.getNextNode().add(n);
                });
            });
        });
        return result;
    }
}
