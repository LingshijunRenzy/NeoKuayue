package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import kasuga.lib.core.util.data_type.Vec2i;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTree;

import java.util.HashMap;
import java.util.Set;

@Getter
public class TechTreePanel extends AbstractWidget {

    private final int row, column;
    private final ClientTechTree tree;
    private final AbstractWidget[][] table;
    private Vec2i windowLT, windowRD, leftTopCube;


    public TechTreePanel(ClientTechTree tree, int row,
                         int column, int width, int height,
                         Component message) {
        super(0, 0, width, height, message);
        this.row = row;
        this.column = column;
        this.tree = tree;
        this.table = new AbstractWidget[row][column];
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
        Vec2i size = new Vec2i(width / 20, height / 20);
        Vec2i boardSize = new Vec2i(size.x * 20, size.y * 20);
        Vec2i offset = new Vec2i((width - boardSize.x) / 2, (height - boardSize.y) / 2);
        windowLT = new Vec2i(globalLT.x + offset.x, globalLT.y + offset.y);
        windowRD = new Vec2i(globalRD.x + width - offset.x, globalRD.y + height - offset.y);
        this.leftTopCube = leftTopCube;
    }

    public @Nullable AbstractWidget get(int row, int column) {
        return table[row][column];
    }

    public void set(int row, int column, AbstractWidget widget) {
        table[row][column] = widget;
    }

    public @Nullable AbstractWidget remove(int row, int column) {
        AbstractWidget widget = table[row][column];
        table[row][column] = null;
        return widget;
    }

    public boolean has(int row, int column) {
        return table[row][column] != null;
    }

    public void setPos(int x, int y) {
        setX(x);
        setY(y);
    }

    public Vec2i getCubePos(int row, int column) {
        Vec2i offset = new Vec2i(row - leftTopCube.x, column - leftTopCube.y);
        int w = windowRD.x - windowLT.x;
        int h = windowRD.y - windowLT.y;
        return new Vec2i(this.leftTopCube.x + w / 20 * offset.x,
                this.leftTopCube.y + h / 20 * offset.y);
    }

    public HashMap<Vec2i, TechTreeLine> getLine(Set<Vec2i> from, Set<Vec2i> to) {
        return new HashMap<>();
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX,
                             int mouseY, float partialTick) {

    }

    @Override
    public void updateNarration(NarrationElementOutput output) {

    }
}
