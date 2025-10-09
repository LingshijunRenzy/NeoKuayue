package willow.train.kuayue.utils.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通用图环检测器
 * @param <T> 图节点类型
 */
public class GraphCycleDetector<T> {

    @FunctionalInterface
    public interface GraphAdapter<T> {
        List<T> getNeighbors(T node);
    }

    public static class CycleResult<T> {
        private final List<List<T>> cycles;
        private final Set<T> nodesInCycles;

        public CycleResult(List<List<T>> cycles) {
            this.cycles = new ArrayList<>(cycles);
            this.nodesInCycles = new HashSet<>();
            for(List<T> cycle : cycles) {
                nodesInCycles.addAll(cycle);
            }
        }

        public List<List<T>> getCycles() {
            return cycles;
        }

        public Set<T> getNodesInCycles() {
            return nodesInCycles;
        }

        public boolean hasCycle() {
            return !cycles.isEmpty();
        }
    }

    public static <T> CycleResult<T> dfs(Set<T> nodes, GraphAdapter<T> adapter) {
        List<List<T>> allCycles = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Set<T> visiting = new HashSet<>();

        for (T node : nodes) {
            if (!visited.contains(node)) {
                dfsVisit(node, adapter, visited, visiting, new ArrayList<>(), allCycles);
            }
        }

        return new CycleResult<>(allCycles);
    }

    private static <T> void dfsVisit(
            T current,
            GraphAdapter<T> adapter,
            Set<T> visited,
            Set<T> visiting,
            List<T> path,
            List<List<T>> allCycles) {
        visiting.add(current);
        path.add(current);

        for (T neighbor : adapter.getNeighbors(current)) {
            if (visiting.contains(neighbor)) {
                int index = path.indexOf(neighbor);
                if (index != -1) {
                    List<T> cycle = new ArrayList<>(path.subList(index, path.size()));
                    allCycles.add(cycle);
                }
            } else if (!visited.contains(neighbor)) {
                dfsVisit(neighbor, adapter, visited, visiting, path, allCycles);
            }
        }

        visiting.remove(current);
        path.remove(path.size() - 1);
        visited.add(current);
    }
}
