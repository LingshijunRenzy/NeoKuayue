package willow.train.kuayue.systems.tech_tree.client.gui;

import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoardLayer<T extends AbstractWidget> extends Layer {
    protected final AbstractWidget[][] board;
    protected final int row, column, squareSide;
    public BoardLayer(int x, int y, int row, int column, int squareSide) {
        super(x, y, column * squareSide, row * squareSide);
        this.row = row;
        this.column = column;
        this.squareSide = squareSide;
        this.board = new AbstractWidget[row][column];
    }

    public void setWidget(int x, int y, T widget) {
        if (!isValidLocation(x, y)) return;
        Vec2iE pos = getLocation(x, y);
        widget.setX(pos.x);
        widget.setY(pos.y);
        board[y][x] = widget;
        this.addRenderableWidget(widget);
    }

    public boolean hasWidget(int x, int y) {
        if (!isValidLocation(x, y)) return false;
        return board[y][x] != null;
    }

    public boolean removeWidget(int x, int y) {
        if (!isValidLocation(x, y)) return false;
        if (board[y][x] == null) return false;
        board[y][x] = null;
        return true;
    }

    public @Nullable AbstractWidget getWidget(int x, int y) {
        if (!isValidLocation(x, y)) return null;
        return board[y][x];
    }

    public T getWidgetOrDefault(int x, int y, T defaultValue) {
        AbstractWidget widget = getWidget(x, y);
        return widget == null ? defaultValue : (T) widget;
    }

    public boolean computeIfAbsent(int x, int y, Supplier<T> supplier) {
        if (!isValidLocation(x, y)) return false;
        if (board[y][x] != null) return false;
        board[y][x] = supplier.get();
        return true;
    }

    public void forEachInColumn(int column, BoardIterator<T> iter) {
        if (column < 0 || column >= this.column) return;
        for (int i = 0; i < row; i++) {
            iter.consume(column, i, (T) board[i][column]);
        }
    }

    public void forEachInRow(int row, BoardIterator<T> iter) {
        if (row < 0 || row >= this.row) return;
        for (int i = 0; i < column; i++) {
            iter.consume(i, row, (T) board[row][i]);
        }
    }

    public void forEach(BoardIterator<T> iter) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                iter.consume(j, i, (T) board[i][j]);
            }
        }
    }

    public boolean isValidLocation(int x, int y) {
        if (x < 0 || y < 0) return false;
        return x < column && y < row;
    }

    public Vec2iE getLocation(int x, int y) {
        return new Vec2iE(this.getX() + x * squareSide, this.getY() + y * squareSide);
    }

    public interface BoardIterator<T extends AbstractWidget> {
        void consume(int x, int y, T widget);
    }
}
