package willow.train.kuayue.systems.tech_tree.server;

import lombok.Getter;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.utils.graph.GraphCycleDetector;

import java.util.*;

@Getter
public class TechTreeCycleHandler {
    private final List<List<NodeLocation>> detectedCyclePaths = new ArrayList<>();
    private final Set<NodeLocation> nodeLocationsInCycles = new HashSet<>();

    public void handle(TechTreeManager manager) {
        findCycles(manager);
        breakCycles(manager);
    }

    public boolean hasCycles() {
        return !detectedCyclePaths.isEmpty();
    }

    private void findCycles(TechTreeManager manager){
        detectedCyclePaths.clear();
        nodeLocationsInCycles.clear();

        Map<NodeLocation, List<NodeLocation>> adjacencyList = buildAdjacencyList(manager);

        Set<NodeLocation> allNodes = adjacencyList.keySet();
        GraphCycleDetector.CycleResult<NodeLocation> result = GraphCycleDetector.dfs(
                allNodes,
                node -> adjacencyList.getOrDefault(node, new ArrayList<>())
        );

        if(result.hasCycle()){
            detectedCyclePaths.addAll(result.getCycles());
            nodeLocationsInCycles.addAll(result.getNodesInCycles());
            Kuayue.LOGGER.warn("Found {} cycles in the tech tree.", detectedCyclePaths.size());
            for(String cycleStr : cyclesToString()){
                Kuayue.LOGGER.warn(cycleStr);
            }
        }
    }

    private Map<NodeLocation, List<NodeLocation>> buildAdjacencyList(TechTreeManager manager){
        Map<NodeLocation, List<NodeLocation>> adjacencyList = new HashMap<>();

        manager.trees().forEach((namespaces, tree) -> {
            tree.getGroups().forEach((name, group) -> {
                group.getNodes().forEach((loc, node) -> {
                    adjacencyList.putIfAbsent(loc, new ArrayList<>());

                    node.getNext().forEach(next -> {
                        adjacencyList.get(loc).add(next.getLocation());
                    });
                });
            });
        });

        return adjacencyList;
    }

    private void breakCycles(TechTreeManager manager){
        if(!hasCycles()) return;

        for(List<NodeLocation> path : detectedCyclePaths){
            if(path.size() < 2) continue;
            NodeLocation source = path.get(path.size() - 1);
            NodeLocation target = path.get(0);
            breakEdge(manager, source, target);
        }
    }

    private void breakEdge(TechTreeManager manager, NodeLocation source, NodeLocation target){
        if(manager == null) return;

        TechTreeNode sourceNode = manager.getNode(source);
        TechTreeNode targetNode = manager.getNode(target);
        if(sourceNode == null || targetNode == null) {
            Kuayue.LOGGER.error("Cannot break edge from {} to {}: one of the nodes does not exist.", source, target);
            return;
        }

        sourceNode.removeNext(targetNode);
        targetNode.removePrev(sourceNode);

        Kuayue.LOGGER.warn("Break edge from {} to {}.", source, target);
    }

    public List<String> cyclesToString(){
        List<String> res = new ArrayList<>();
        for(int i = 0; i < detectedCyclePaths.size(); i++){
            List<NodeLocation> cycle = detectedCyclePaths.get(i);
            StringBuilder pathStr = new StringBuilder();
            for (NodeLocation nodeLoc : cycle) {
                pathStr.append(nodeLoc.getName()).append(" -> ");
            }
            if (pathStr.length() > 4) {
                pathStr.setLength(pathStr.length() - 4);
            }
            res.add("Cycle " + (i + 1) + ": " + pathStr);
        }
        return res;
    }

    public List<String> brokenEdgesToString(){
        List<String> res = new ArrayList<>();
        for(int i = 0; i < detectedCyclePaths.size(); i++){
            List<NodeLocation> cycle = detectedCyclePaths.get(i);
            if(cycle.size() < 2) continue;
            NodeLocation source = cycle.get(cycle.size() - 1);
            NodeLocation target = cycle.get(0);
            res.add("Edge " + (i + 1) + " from: " + source.getName() + " to: " + target.getName());
        }
        return res;
    }
}
