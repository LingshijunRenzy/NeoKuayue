package willow.train.kuayue.systems.tech_tree.client;

import willow.train.kuayue.systems.tech_tree.client.gui.Vec2iE;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class AStarPathFinding {

    public static HashMap<Vec2iE, List<Vec2iE>> findPaths
            (int row, int column, Collection<Vec2iE> starts, Vec2iE target, Vec2iE offset, Predicate<Vec2iE> predicate) {
        HashMap<Vec2iE, List<Vec2iE>> paths = new HashMap<>();
        int[][] board = new int[row][column];
        HashSet<Vec2iE> targets = new HashSet<>();
        targets.add(target);
        for (Vec2iE start : starts) {
            aStar(board, targets, offset, predicate);
            List<Vec2iE> result = pathFinding(board, start);
            if (!verify(targets, start, result))
                return paths;
            paths.put(start, result);
            targets.addAll(result);
        }
        return paths;
    }

    private static void printBoard(int[][] board, Collection<Vec2iE> starts,
                            Collection<Vec2iE> targets,
                            Collection<Vec2iE> path) {
        if (board.length < 1) return;
        if (board[0].length < 1) return;
        int row = board.length;
        int column = board[0].length;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                Vec2iE p = new Vec2iE(j, i);
                if (starts.contains(p)) {
                    System.out.print("AA ");
                    continue;
                }
                if (targets.contains(p)) {
                    System.out.print("BB ");
                    continue;
                }
                if (path.contains(p)) {
                    System.out.print("XX ");
                    continue;
                }
                int value = board[i][j];
                System.out.print(value > 0 && value < 10 ? ("0" + value) : value);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static boolean verify
            (Set<Vec2iE> targets, Vec2iE start, List<Vec2iE> path) {
        if (path.size() < 1)
            return false;
        Vec2iE last = path.get(path.size() - 1);
        boolean flag = false;
        for (Vec2iE v : targets) {
            if (v.nextTo(last)) {
                flag = true;
                break;
            }
        }
        return start.nextTo(path.get(0)) && flag;
    }

    public static void aStar(int[][] board, Set<Vec2iE> targets, Vec2iE offset, Predicate<Vec2iE> predicate) {
        for (int[] ints : board) {
            Arrays.fill(ints, -1);
        }
        if (board.length < 1) return;
        int column = board[0].length;
        if (column < 1) return;
        int row = board.length;
        HashSet<Vec2iE> scanned = new HashSet<>();
        HashSet<Vec2iE> cache = new HashSet<>();
        HashSet<Vec2iE> boarders = new HashSet<>(targets);
        targets.forEach(t -> board[t.y][t.x] = 0);
        while (!boarders.isEmpty()) {
            for (Vec2iE boarder : boarders) {
                if (scanned.contains(boarder)) continue;
                aStarInner(board, boarder, cache, column, row, true, scanned, offset, predicate);
                aStarInner(board, boarder, cache, column, row, false, scanned, offset, predicate);
                scanned.add(boarder);
            }
            boarders.clear();
            boarders.addAll(cache);
            cache.clear();
        }
    }

    private static void aStarInner(int[][] board, Vec2iE boarder,
                                   HashSet<Vec2iE> cache, int column,
                                   int row, boolean xOry, Set<Vec2iE> scanned,
                                   Vec2iE offset, Predicate<Vec2iE> predicate) {
        int boarderValue = board[boarder.y][boarder.x];
        for (int i = -1; i < 2; i += 2) {
            int px = boarder.x + (xOry ? i : 0);
            int py = boarder.y + (!xOry ? i : 0);
            if (px < 0 || px >= column ||
                    py < 0 || py >= row)
                continue;
            if (board[py][px] < -1) continue;
            if (board[py][px] == 0) continue;
            Vec2iE p = new Vec2iE(px, py);
            if (!predicate.test(p.copy().add(offset))) {
                board[py][px] = -2;
                continue;
            }
            if (!scanned.contains(p)) cache.add(p);
            if (board[py][px] == -1) {
                board[py][px] = boarderValue + 1;
                continue;
            }
            board[py][px] = Math.min(board[py][px], boarderValue + 1);
        }
    }

    public static List<Vec2iE> pathFinding(int[][] board, Vec2iE position) {
        LinkedList<Vec2iE> path = new LinkedList<>();
        ArrayList<Vec2iE> cache = new ArrayList<>(4);
        Vec2iE last = null, pos = position;
        while (true) {
            Vec2iE result = pathFindingStep(board, pos, cache, last);
            if (result.equals(pos)) return path;
            path.add(result);
            last = pos;
            pos = result;
        }
    }

    private static Vec2iE pathFindingStep(int[][] board, Vec2iE position, List<Vec2iE> stack,
                                   @Nullable Vec2iE lastPosition) {
        if (board.length < 1) return position;
        if (board[0].length < 1) return position;
        int row = board.length;
        int column = board[0].length;
        stack.clear();
        Vec2iE v;
        int minValue = Integer.MAX_VALUE;
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j > -2; j -= 2) {
                v = new Vec2iE(position.x + (i < 1 ? j : 0),
                        position.y + (i > 0 ? j : 0));
                if (v.x < 0 || v.x >= column ||
                    v.y < 0 || v.y >= row) continue;
                int value = board[v.y][v.x];
                if (value < 0) continue;
                if (value == 0) return position;
                if (value < minValue) {
                    stack.clear();
                    stack.add(v);
                    minValue = value;
                    continue;
                }
                if (value == minValue) {
                    stack.add(v);
                }
            }
        }
        if (stack.isEmpty()) return position;
        if (stack.size() == 1 || lastPosition == null) {
            return stack.get(0);
        }
        Vec2iE offset = position.copy().subtract(lastPosition);
        for (Vec2iE p : stack) {
            int offsetX = p.x - position.x;
            int offsetY = p.y - position.y;
            if (offsetX == offset.x && offsetY == offset.y) return p;
        }
        return stack.get(0);
    }

    public static int getColumnHeight(int number) {
        if (number < 0) return 0;
        if (number < 2) return number;
        return number * 2 - 1;
    }

    public static int[] getDistribute(int columnStart, int number) {
        if (number <= 0) return new int[0];
        if (number == 1) return new int[]{columnStart};
        int[] result = new int[number];
        int counter = 0;
        for (int i = number % 2 == 0 ? 0 : 1; counter < number; i+=2) {
            result[counter] = columnStart + i;
            counter += 1;
        }
        return result;
    }

    public static int[] generatePattern(int n) {
        int[] result = new int[9];
        // 初始化所有元素为-1
        for (int i = 0; i < 9; i++) {
            result[i] = -1;
        }
        if (n == 0) {
            return result;
        }
        int start = 4 - (n - 1);
        int end = 4 + (n - 1);
        for (int i = start; i <= end; i++) {
            int relativePos = i - start;
            result[i] = (relativePos % 2 == 0) ? 1 : 0;
        }
        return result;
    }

    public static void main(String[] args) {
        // 测试用例
        System.out.println(java.util.Arrays.toString(generatePattern(0)));
        System.out.println(java.util.Arrays.toString(generatePattern(1)));
        System.out.println(java.util.Arrays.toString(generatePattern(2)));
        System.out.println(java.util.Arrays.toString(generatePattern(3)));
        System.out.println(java.util.Arrays.toString(generatePattern(4)));
        System.out.println(java.util.Arrays.toString(generatePattern(5)));
    }
}
