package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.systems.editable_panel.widget.ImageButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnlockGroupBoard extends AbstractWidget {

    private static final LazyRecomputable<ImageMask> unlockGroupBoardBg = LazyRecomputable.of(
            () -> ClientInit.groupUnlockBoard.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );

    private static final LazyRecomputable<ImageMask> confirmBtnLight = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(80 / 128f, 64 / 128f, 96 / 128f, 96 / 128f)
    );

    private static final LazyRecomputable<ImageMask> confirmBtnDark = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .rectangleUV(96 / 128f, 64 / 128f, 112 / 128f, 96 / 128f)
    );

    private final List<LabelGrid> grids;

    private final ImageButton confirmBtn;

    @Setter
    @Getter
    private boolean renderConfirmBtn;

    public UnlockGroupBoard(int pX, int pY, Collection<TechTreeLabel> unlockNodes, Button.OnPress onPress) {
        super(pX, pY, 120, 80, Component.empty());
        ArrayList<TechTreeLabel> labels = new ArrayList<>(unlockNodes);
        grids = new ArrayList<>();
        int labelSize = labels.size();
        for (int i = 0; i < labelSize; i += 9) {
            grids.add(new LabelGrid(this.x + 15, this.y + 9,
                    labels.subList(i, Math.min(i + 9, labelSize))
            ));
        }
        confirmBtn = new ImageButton(confirmBtnLight, confirmBtnDark, this.x, this.y,
                16, 32, Component.empty(), onPress);
        updateAllWidgetsPos();
        renderConfirmBtn = true;
    }

    private void updateAllWidgetsPos() {
        updateBgPos();
        updateSlotPos();
        updateConfirmBtnPos();
    }

    // (30, 5)
    private void updateSlotPos() {
        for (LabelGrid grid : grids) {
            grid.setPos(this.x + 15, this.y + 9);
        }
    }

    private void updateConfirmBtnPos() {
        confirmBtn.setPos(this.x + 83,
                this.y + 16);
    }

    private void updateBgPos() {
        unlockGroupBoardBg.get().rectangle(
                new Vector3f(this.x, this.y, 0), ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, 120, 80
        );
    }

    public void setX(int x) {
        this.x = x;
        updateAllWidgetsPos();
    }

    public void setY(int y) {
        this.y = y;
        updateAllWidgetsPos();
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        updateAllWidgetsPos();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        unlockGroupBoardBg.get().renderToGui();
        confirmBtn.visible = this.visible & renderConfirmBtn;
        confirmBtn.setRenderMask(!confirmBtn.isMouseOver(mouseX, mouseY));
        for (LabelGrid grid : grids) {
            if (!grid.visible) continue;
            grid.render(poseStack, mouseX, mouseY, partial);
        }
        confirmBtn.render(poseStack, mouseX, mouseY, partial);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
