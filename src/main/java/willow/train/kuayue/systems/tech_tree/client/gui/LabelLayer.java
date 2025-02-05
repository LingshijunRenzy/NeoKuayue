package willow.train.kuayue.systems.tech_tree.client.gui;

public class LabelLayer extends BoardLayer<TechTreeLabel> {
    public LabelLayer(int x, int y, int row, int column, int squareSide) {
        super(x, y, row, column, squareSide);
    }

    @Override
    public void setWidget(int x, int y, TechTreeLabel widget) {
        if (!isValidLocation(x, y)) return;
        Vec2iE pos = getLocation(x, y);
        widget.setPos(pos.x, pos.y);
        board[y][x] = widget;
        this.addRenderableWidget(widget);
    }

    public void setX(int x) {
        int offset = x - this.x;
        widgets.forEach(widget -> ((TechTreeLabel) widget).setX(widget.x + offset));
        this.x = x;
    }

    public void setY(int y) {
        int offset = y - this.y;
        widgets.forEach(widget -> ((TechTreeLabel) widget).setY(widget.y + offset));
        this.y = y;
    }
}
