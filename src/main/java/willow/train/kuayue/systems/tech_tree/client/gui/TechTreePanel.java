package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.client.render.SimpleColor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;

import java.util.*;

@Getter
public class TechTreePanel extends AbstractWidget {

    private LabelLayer labels;

    private final ArrayList<LineLayer> lines;

    private final int row, column;

    @Setter
    private boolean lockDragging;

    private final SimpleColor[] colors = new SimpleColor[] {
            SimpleColor.fromHSV(0, .85f, .95f),
            SimpleColor.fromHSV(60, .9f, 1),
            SimpleColor.fromHSV(75, .7f, .8f),
            SimpleColor.fromHSV(330, .75f, .9f),
            SimpleColor.fromHSV(40, .5f, .85f)
    };

    public TechTreePanel(int x, int y, int width, int height, int row, int column) {
        super(x, y, width, height, Component.empty());
        this.row = row;
        this.column = column;
        labels = null;
        lines = new ArrayList<>();
    }

    private void setWindowForLayer(Layer layer) {
        layer.setWindow(x + 10, y + 10,
                x + width - 10, y + height - 10);
    }

    public void updateWindows() {
        setWindowForLayer(labels);
        lines.forEach(this::setWindowForLayer);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        updateWindows();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        updateWindows();
    }

    public void setSize(int width, int height) {
        super.setHeight(height);
        super.setWidth(width);
        updateWindows();
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (lockDragging) return;
        int dx = (int) Math.round(pDragX);
        int dy = (int) Math.round(pDragY);
        lines.forEach(l -> l.onDrag(dx, dy));
        if (labels == null) return;
        labels.onDrag(dx, dy);
    }

    public void adjustSize(int maxWidth, int maxHeight) {
        if (labels == null) {
            this.setWidth(maxWidth);
            this.setHeight(maxHeight);
             return;
        }
        int w = labels.getWidth();
        int h = labels.getHeight();
        this.setWidth(Math.min(w, maxWidth));
        this.setHeight(Math.min(h, maxHeight));
    }

    public void moveToWindowCentral(float scale) {
        boolean widthFlag = labels.getWidth() <= this.getWidth();
        boolean heightFlag = labels.getHeight() <= this.getHeight();
        int dy = (this.getHeight() - labels.getHeight()) / 2;
        if (widthFlag) {
            int dx = (this.getWidth() - labels.getWidth()) / 2;
            labels.onDrag(dx, dy);
            lines.forEach(l -> l.onDrag(dx, dy));
            if (heightFlag) lockDragging = true;
        } else {
            labels.onDrag(0, dy);
            lines.forEach(l -> l.onDrag(0, dy));
        }
    }

    public void compileGroup(ClientTechTreeGroup group) {
        ClientTechTreeNode rootNode = group.getRootNode();

        // get logical distribution for each column
        ArrayList<Set<ClientTechTreeNode>> stages = new ArrayList<>();
        HashSet<ClientTechTreeNode> rootSet = new HashSet<>();
        rootSet.add(rootNode);
        stages.add(rootSet);
        int counter = 0;
        while (true) {
            counter ++;
            HashSet<ClientTechTreeNode> cache = new HashSet<>();
            stages.get(stages.size() - 1).forEach(node -> {
                node.getNextNode().forEach(next -> {
                    if (group.getNodes().containsValue(next))
                        cache.add(next);
                });
            });
            if (cache.isEmpty() || counter > 999) break;
            stages.forEach(set -> set.removeAll(cache));
            stages.add(cache);
        }

        // get width and height for the board.
        int boardHeight = 0;
        for (Set<ClientTechTreeNode> set : stages)
            boardHeight = Math.max(set.size(), boardHeight);
        if (boardHeight == 0) return;
        int boardWidth = stages.size() * 2 - 1;
        boardHeight = boardHeight * 2 - 1;

        // put all labels into the board
        labels = new LabelLayer(0 ,0, boardHeight, boardWidth, 20);
        HashMap<ClientTechTreeNode, Vec2iE> positionMap = new HashMap<>();
        for (int i = 0; i < stages.size(); i++) {
            int px = i * 2;
            Set<ClientTechTreeNode> column = stages.get(i);
            int py = (boardHeight - column.size()) / 2;
            int c = 0;
            for (ClientTechTreeNode n : column) {
                addLabel(px, py + c, n);
                positionMap.put(n, new Vec2iE(px, py + c));
                c += 2;
            }
        }

        // get all lines
        RandomSource random = Minecraft.getInstance().font.random;;
        positionMap.forEach((node, pos) -> {
            Set<ClientTechTreeNode> prev = node.getPrevNode();
            ArrayList<Vec2iE> prevPos = new ArrayList<>(prev.size());
            prev.forEach(p -> {
                if (positionMap.containsKey(p))
                    prevPos.add(positionMap.get(p));
            });
            getPathBetween(prevPos, pos, colors[random.nextInt(0, 5)]);

        });
    }

    private void addLabel(int x, int y, ClientTechTreeNode node) {
        TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
        labels.setWidget(x, y, label);
    }

    private void getPathBetween(List<Vec2iE> from, Vec2iE to, @Nullable SimpleColor color) {
        if (from.isEmpty()) return;
        LineLayer layer = LineLayer.getLine(from, to, new Vec2iE(this.x, this.y),
                20, color, v -> !labels.hasWidget(v.x, v.y));
        lines.add(layer);
        setWindowForLayer(layer);
    }

    public void setX(int x) {
        int offsetX = x - this.x;
        this.x = x;
        labels.setX(x);
        lines.forEach(l -> l.setX(l.x + offsetX));
        updateWindows();
    }

    public void setY(int y) {
        int offsetY = y - this.y;
        this.y = y;
        labels.setY(y);
        lines.forEach(l -> l.setY(l.y + offsetY));
        updateWindows();
    }

    public void setPosition(int x, int y) {
        int offsetX = x - this.x;
        int offsetY = y - this.y;
        this.x = x;
        this.y = y;
        labels.setPos(x, y);
        lines.forEach(l -> l.setPos(l.x + offsetX, l.y + offsetY));
        updateWindows();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        for (LineLayer layer : lines) {
            layer.render(poseStack, mouseX, mouseY, partial);
        }
        if (labels == null) return;
        labels.render(poseStack, mouseX, mouseY, partial);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {

    }
}
