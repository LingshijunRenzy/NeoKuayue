package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.util.data_type.Pair;
import kasuga.lib.core.util.data_type.Vec2i;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTree;

import java.util.*;

@Getter
public class TechTreePanel extends AbstractWidget {

    private final int row, column;
    private final ClientTechTree tree;
    private final AbstractWidget[][] table;
    private final AbstractWidget[][] links;
    private Vec2i windowLT, windowRD, leftTopCube;
    private static final int widgetHeight = 20, widgetWidth = 20;


    public TechTreePanel(ClientTechTree tree, int row,
                         int column, int width, int height,
                         Component message) {
        super(0, 0, width, height, message);
        this.row = row;
        this.column = column;
        this.tree = tree;
        this.table = new AbstractWidget[row][column];
        this.links = new AbstractWidget[row][column];
        updateWindow(new Vec2i());
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    private void updateWindow(Vec2i leftTopCube) {
        Vec2i globalLT = new Vec2i(x, y);
        Vec2i globalRD = new Vec2i(x + width, y + height);
        Vec2i size = new Vec2i(width / widgetWidth, height / widgetHeight);
        Vec2i boardSize = new Vec2i(size.x * widgetWidth, size.y * widgetHeight);
        Vec2i offset = new Vec2i((width - boardSize.x) / 2, (height - boardSize.y) / 2);
        windowLT = new Vec2i(globalLT.x + offset.x, globalLT.y + offset.y);
        windowRD = new Vec2i(globalRD.x + width - offset.x, globalRD.y + height - offset.y);
        this.leftTopCube = leftTopCube;
    }

    public @Nullable AbstractWidget get(int x, int y) {
        return table[y][x];
    }

    public @Nullable AbstractWidget getLink(int x, int y) {
        return links[y][x];
    }

    public void set(int x, int y, AbstractWidget widget) {
        table[y][x] = widget;
    }

    public void setLink(int x, int y, AbstractWidget widget) {
        links[y][x] = widget;
    }

    public @Nullable AbstractWidget remove(int x, int y) {
        AbstractWidget widget = table[y][x];
        table[y][x] = null;
        return widget;
    }

    public @Nullable AbstractWidget removeLink(int x, int y) {
        AbstractWidget widget = links[y][x];
        links[y][x] = null;
        return widget;
    }

    public boolean has(int x, int y) {
        if (x < 0 || x >= this.column) return true;
        if (y < 0 || y >= this.row) return true;
        return table[y][x] != null;
    }

    public boolean hasLink(int x, int y) {
        if (x < 0 || x >= this.column) return false;
        if (y < 0 || y >= this.row) return false;
        return links[y][x] != null;
    }

    public void setPos(int x, int y) {
        setX(x);
        setY(y);
    }

    public Vec2i getCubePos(int row, int column) {
        Vec2i offset = new Vec2i(row - leftTopCube.x, column - leftTopCube.y);
        int w = windowRD.x - windowLT.x;
        int h = windowRD.y - windowLT.y;
        return new Vec2i(this.leftTopCube.x + w / widgetWidth * offset.x,
                this.leftTopCube.y + h / widgetHeight * offset.y);
    }

    public void getLine(HashMap<Vec2i, TechTreeLine> line,
                        Connection connection) {

    }

    public void aStar(int[][] board, Vec2iE target) {
        for (int[] ints : board) {
            Arrays.fill(ints, -1);
        }
        if (board.length < 1) return;
        int column = board[0].length;
        if (column < 1) return;
        int row = board.length;
        HashSet<Vec2iE> boarders = new HashSet<>();
        HashSet<Vec2iE> scanned = new HashSet<>();
        HashSet<Vec2iE> cache = new HashSet<>();
        boarders.add(target);
        while (!boarders.isEmpty()) {
            for (Vec2iE boarder : boarders) {
                aStarInner(board, boarder, cache, true);
                aStarInner(board, boarder, cache, false);
            }
        }
    }

    private void aStarInner(int[][] board, Vec2iE boarder,
                            HashSet<Vec2iE> cache, boolean xOry) {
        int boarderValue = board[boarder.y][boarder.x];
        for (int i = -1; i < 2; i += 2) {
            int px = boarder.x + (xOry ? i : 0);
            int py = boarder.y + (!xOry ? i : 0);
            if (px < 0 || px >= column ||
                    py < 0 || py >= row)
                continue;
            if (board[py][px] < -1)
                continue;
            if (has(px, py) || hasLink(px, py)) {
                board[py][px] = -2;
                continue;
            }
            cache.add(new Vec2iE(py, px));
            if (board[py][px] == -1) {
                board[py][px] = boarderValue + 1;
                continue;
            }
            board[py][px] = Math.min(board[py][px], boarderValue + 1);
        }
    }


    private int switchSide(int input) {
        return Integer.compare(input, 0);
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX,
                             int mouseY, float partialTick) {

    }

    @Override
    public void updateNarration(NarrationElementOutput output) {

    }

    @Getter
    public static class Connection {

        private final Vec2i from, to;
        private boolean stepOnFrom, stepOnTo;

        public Connection(Vec2i from, Vec2i to) {
            this.from = from;
            this.to = to;
        }

        public Connection stepOnFrom(boolean stepOnFrom) {
            this.stepOnFrom = stepOnFrom;
            return this;
        }

        public Connection stepOnTo(boolean stepOnTo) {
            this.stepOnTo = stepOnTo;
            return this;
        }
    }
}
