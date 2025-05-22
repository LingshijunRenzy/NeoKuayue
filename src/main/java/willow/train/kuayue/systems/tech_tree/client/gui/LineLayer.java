package willow.train.kuayue.systems.tech_tree.client.gui;

import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.util.data_type.Vec2i;
import lombok.Getter;
import net.minecraft.client.gui.components.AbstractWidget;
import willow.train.kuayue.systems.tech_tree.client.AStarPathFinding;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
public class LineLayer extends BoardLayer<TechTreeLine> {

    private SimpleColor color;
    public LineLayer(int x, int y, int row, int column, int squareSide) {
        super(x, y, row, column, squareSide);
        color = SimpleColor.fromRGBInt(0xffffff);
    }

    public void setColor(SimpleColor color) {
        this.color = color;
        forEach((px, py, line) -> {
            if (line == null) return;
            line.setColor(color);
        });
    }

    public void setX(int x) {
        int offset = x - this.getX();
        widgets.forEach(widget -> ((TechTreeLine) widget).setX(widget.getX() + offset));
        super.setX(x);
    }

    public void setY(int y) {
        int offset = y - this.getY();
        widgets.forEach(widget -> ((TechTreeLine) widget).setY(widget.getY() + offset));
        super.setY(y);
    }

    @Override
    public void setWidget(int x, int y, TechTreeLine widget) {
        if (!isValidLocation(x, y)) return;
        Vec2iE pos = getLocation(x, y);
        widget.setPos(pos);
        board[y][x] = widget;
        this.addRenderableWidget(widget);
        widget.setColor(color);
    }

    public static void main(String[] args) {
        // getLine(List.of(new Vec2iE()), new Vec2iE(10, 10), null, v -> true);
    }

    public static LineLayer getLine(List<Vec2iE> from, Vec2iE to, Vec2iE offset,
                                    int squareSide, @Nullable SimpleColor color,
                                    Predicate<Vec2iE> predicate) {
        if (from.isEmpty())
            return new LineLayer(to.x, to.y, 1, 1, 20);
        Vec2iE leftTop = to.copy(), maxFrom = from.get(0).copy(), rightDown = to.copy();
        for (Vec2iE p : from) {
            leftTop.x = Math.min(p.x, leftTop.x);
            leftTop.y = Math.min(p.y, leftTop.y);
            rightDown.x = Math.max(p.x, rightDown.x);
            rightDown.y = Math.max(p.y, rightDown.y);
            maxFrom.x = Math.max(p.x, maxFrom.x);
            maxFrom.y = Math.max(p.y, maxFrom.y);
        }
        int column = rightDown.x - leftTop.x + 1;
        int row = rightDown.y - leftTop.y + 1;
        LineLayer layer = new LineLayer(offset.x + leftTop.x * squareSide,
                offset.y + leftTop.y * squareSide,
                row, column, squareSide);
        if (color != null) layer.setColor(color);
        ArrayList<Vec2iE> fromPos = new ArrayList<>();
        from.forEach(p -> fromPos.add(p.copy().subtract(leftTop)));
        Vec2iE toPos = to.copy().subtract(leftTop);
        HashMap<Vec2iE, List<Vec2iE>> lines = AStarPathFinding.findPaths(row, column, fromPos,
                toPos, leftTop, predicate);
        Set<Vec2iE> collect = new HashSet<>();
        lines.values().forEach(collect::addAll);
        for (Vec2iE fPoint : lines.keySet()) {
            List<Vec2iE> line = lines.get(fPoint);
            for (Vec2iE linePoint : line) {
                TechTreeLine lineWidget = new TechTreeLine(0, 0, 20, 20);
                layer.setWidget(linePoint.x, linePoint.y, lineWidget);
                HashMap<TechTreeLine.Side, Vec2iE> nearBys =
                        TechTreeLine.Side.getNearBys(linePoint);
                for (Map.Entry<TechTreeLine.Side, Vec2iE> nearby : nearBys.entrySet()) {
                    Vec2iE value = nearby.getValue();
                    if (value.x < 0 || value.y < 0 ||
                        value.x >= column || value.y >= row) continue;
                    boolean hasFrom = fromPos.contains(value),
                        inCollect = collect.contains(value),
                        pointAtTo = value.equals(toPos);
                    lineWidget.shouldRender(nearby.getKey(), inCollect || hasFrom || pointAtTo);
                    lineWidget.shouldRenderArrow(nearby.getKey(), pointAtTo);
                    lineWidget.setTail(nearby.getKey(), hasFrom);
                }
            }
        }

        return layer;
    }
}
