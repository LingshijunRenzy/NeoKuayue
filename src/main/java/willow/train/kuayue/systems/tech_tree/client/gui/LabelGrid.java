package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LabelGrid extends AbstractWidget {

    private List<TechTreeLabel> labels;
    public LabelGrid(int pX, int pY, @NotNull List<TechTreeLabel> labels) {
        super(pX, pY, 0, 0, Component.empty());
        if (labels.size() < 10){
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

    public void setWidthAndHeight() {
        if (labels.size() < 2) {
            setWidth(20);
            setHeight(20);
            for (TechTreeLabel label : labels) {
                label.setPos(this.x, this.y);
            }
        } else if (labels.size() == 2) {
            setWidth(41);
            setHeight(20);
            int counter = 0;
            for (TechTreeLabel label : labels) {
                label.setPos(this.x + (counter == 0 ? 0 : 21), this.y);
                counter++;
            }
        } else if (labels.size() < 5) {
            setWidth(41);
            setHeight(41);
            int counter = 0;
            for (TechTreeLabel label : labels) {
                int labelX;
                if (labels.size() == 3 && counter == 2) {
                    labelX = this.x + 41 / 2;
                } else {
                    labelX = this.x + (counter % 2 == 0 ? 0 : 21);
                }
                label.setPos(this.x + labelX, this.y + (counter / 2 == 0 ? 0 : 21));
                counter++;
            }
        } else if (labels.size() < 7) {
            setWidth(62);
            setHeight(41);
            int counter = 0;
            for (TechTreeLabel label : labels) {
                int labelX;
                if (counter < 3) {
                    labelX = this.x + 21 * counter;
                } else {
                    if (labels.size() < 6) {
                        labelX = this.x + 11 + 21 * (counter - 3);
                    } else {
                        labelX = this.x + 21 * (counter - 3);
                    }
                }
                label.setPos(labelX, this.y + 21 * (counter / 3));
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
                        labelX = ((counter % 2 == 0) ? 0 : 21) + 11 + this.x;
                        label.setPos(labelX, (counter < 2 ? 0 : 42) + this.y);
                    } else {
                        labelX = (counter - 2) * 21 + this.x;
                        label.setPos(labelX, this.y + 21);
                    }
                } else {
                    if (labels.size() == 8 && (counter > 5)) {
                        labelX = ((counter % 2 == 1) ? 0 : 21) + 11 + this.x;
                    } else {
                        labelX = (counter % 3) * 21 + this.x;
                    }
                    label.setPos(labelX, this.y + (counter / 3) * 21);
                }
                counter++;
            }
        }
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partial) {
        labels.forEach(label -> {
            label.visible = this.visible;
            render(poseStack, mouseX, mouseY, partial);
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
    public void updateNarration(NarrationElementOutput output) {

    }
}
