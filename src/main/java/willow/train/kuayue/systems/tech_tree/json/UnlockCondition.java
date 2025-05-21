package willow.train.kuayue.systems.tech_tree.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;
import willow.train.kuayue.systems.tech_tree.server.TechTreeGroup;
import willow.train.kuayue.systems.tech_tree.server.TechTreeNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnlockCondition {

    private final boolean reverse;
    private final double percentage;
    private final Set<NodeLocation> nodePool;

    public UnlockCondition(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            reverse = jsonObj.has("reverse") &&
                    jsonObj.get("reverse").getAsBoolean();
            percentage = jsonObj.has("percentage") ?
                    jsonObj.get("percentage").getAsDouble() : 99f;
            nodePool = new HashSet<>();
            if (jsonObj.has("nodes")) {
                JsonArray jsonNodes = jsonObj.getAsJsonArray("nodes");
                jsonNodes.forEach(element -> {
                    NodeLocation loc = new NodeLocation(element.getAsString());
                    nodePool.add(loc);
                });
            }
            return;
        } else {
            reverse = false;
            nodePool = Set.of();
            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    percentage = primitive.getAsDouble();
                } else {
                    String value = primitive.getAsString();
                    if (value.equals("any")) {
                        percentage = 0.001f;
                    } else {
                        percentage = 99f;
                    }
                }
            } else {
                percentage = 99f;
            }
        }
    }

    public Set<NodeLocation> checkNode(TechTreeNode node, PlayerData data) {
        return check(node.getPrev(), data);
    }

    public Set<NodeLocation> checkGroup(TechTreeGroup group, PlayerData data) {
        return check(group.getPrev(), data);
    }

    public Set<NodeLocation> check(List<TechTreeNode> prevNode, PlayerData data) {
        ArrayList<TechTreeNode> prev = new ArrayList<>(prevNode);
        if (!nodePool.isEmpty()) {
            prev.removeIf(n -> {
                if (reverse) {
                    return nodePool.contains(n.getLocation());
                } else {
                    return !nodePool.contains(n.getLocation());
                }
            });
        }
        Set<NodeLocation> result = new HashSet<>();
        Set<NodeLocation> unlocked = data.unlocked;
        double prevSize = prevNode.size();
        double count = 0;
        for (TechTreeNode node : prev) {
            if (unlocked.contains(node.getLocation())) {
                count++;
            } else {
                result.add(node.getLocation());
                continue;
            }
            if ((count / prevSize * 100d) >= percentage) {
                result.clear();
                return result;
            }
        }
        return result;
    }
}
