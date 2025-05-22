package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.editable_panel.widget.OnClick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LabelGrid extends AbstractWidget {

    @Getter
    private final List<TechTreeLabel> labels;

    @Setter
    private OnClick<TechTreeLabel> onClick = (label, px, py) -> {};

    public LabelGrid(int pX, int pY, @NotNull List<TechTreeLabel> labels) {
        super(pX, pY, 0, 0, Component.empty());
        if (labels.size() < 10) {
            this.labels = labels;
        } else {
            int counter = 0;
            this.labels = new ArrayList<>(9);
            for (TechTreeLabel label : labels) {
                this.labels.add(label);
                counter ++;
                if (counter > 8) break;
            }
        }
        setWidthAndHeight();
    }

    public void setPos(int x, int y) {
        int offsetX = x - this.getX();
        int offsetY = y - this.getY();
        super.setX(x);
        super.setY(y);
        labels.forEach(label -> label.setPos(label.getX() + offsetX, label.getY() + offsetY));
    }

    public void setWidthAndHeight() {
        if (labels.size() < 2) {
            setWidth(20);
            setHeight(20);
            for (TechTreeLabel label : labels) {
                label.setPos(this.getX(), this.getY());
            }
        } else if (labels.size() == 2) {
            setWidth(41);
            setHeight(20);
            int counter = 0;
            for (TechTreeLabel label : labels) {
                label.setPos(this.getX() + (counter == 0 ? 0 : 21), this.getY());
                counter++;
            }
        } else if (labels.size() < 5) {
            setWidth(41);
            setHeight(41);
            int counter = 0;
            for (TechTreeLabel label : labels) {
                int labelX;
                if (labels.size() == 3 && counter == 2) {
                    labelX = this.getX() + 21 / 2;
                } else {
                    labelX = this.getX() + (counter % 2 == 0 ? 0 : 21);
                }
                label.setPos(this.getX() + labelX, this.getY() + (counter / 2 == 0 ? 0 : 21));
                counter++;
            }
        } else if (labels.size() < 7) {
            setWidth(62);
            setHeight(41);
            int counter = 0;
            for (TechTreeLabel label : labels) {
                int labelX;
                if (counter < 3) {
                    labelX = this.getX() + 21 * counter;
                } else {
                    if (labels.size() < 6) {
                        labelX = this.getX() + 11 + 21 * (counter - 3);
                    } else {
                        labelX = this.getX() + 21 * (counter - 3);
                    }
                }
                label.setPos(labelX, this.getY() + 21 * (counter / 3));
                counter++;
            }
        } else {
            setWidth(62);
            setHeight(62);
            int counter = 0;
            for (TechTreeLabel  label : labels) {
                int labelX;
                if (labels.size() == 7) {
                    if (counter < 2 || counter > 4) {
                        labelX = ((counter % 2 == 0) ? 0 : 21) + 11 + this.getX();
                        label.setPos(labelX, (counter < 2 ? 0 : 42) + this.getY());
                    } else {
                        labelX = (counter - 2) * 21 + this.getX();
                        label.setPos(labelX, this.getY() + 21);
                    }
                } else {
                    if (labels.size() == 8 && (counter > 5)) {
                        labelX = ((counter % 2 == 1) ? 0 : 21) + 11 + this.getX();
                    } else {
                        labelX = (counter % 3) * 21 + this.getX();
                    }
                    label.setPos(labelX, this.getY() + (counter / 3) * 21);
                }
                counter++;
            }
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        labels.forEach(label -> {
            label.visible = this.visible;
            label.render(guiGraphics, mouseX, mouseY, partial);
        });
    }



    public @Nullable TechTreeLabel getChosenLabel(double mouseX, double mouseY) {
        for (TechTreeLabel label : labels) {
            if (label.isMouseOver(mouseX, mouseY))
                return label;
        }
        return null;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        TechTreeLabel label = getChosenLabel(pMouseX, pMouseY);
        if (label == null) return;
        this.onClick.click(label, pMouseX, pMouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
