package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;

import java.util.ArrayList;
import java.util.List;

public class NodeTooltip extends AbstractWidget {

    private ClientTechTreeNode node;
    private MutableComponent title;
    private final List<Component> descriptions;
    private ItemStack icon;
    public static final int maxWidth = 150, boarderWidth = 1,
            boarderColor = 0xffffffff,
            backgroundColor = 0x80000000,
            fontColor = 0xffffffff;


    public NodeTooltip(ClientTechTreeNode node) {
        super(0, 0, 0, 0, Component.empty());
        this.node = node;
        descriptions = new ArrayList<>();
    }

    public ClientTechTreeNode getNode() {
        return node;
    }

    public void updateNode() {
        this.title = Component.translatable(node.getName()).withStyle(Style.EMPTY.withBold(true));
        this.icon = node.getLogo();
        Component component = Component.translatable(node.getDescription());
        Font font = Minecraft.getInstance().font;
        this.descriptions.clear();
        this.descriptions.addAll(lineFeed(component, font, maxWidth));
        if (descriptions.size() > 1) {
            setWidth(maxWidth + 6);
        } else {
            if (descriptions.size() < 1) {
                setWidth(font.width(title) + 22);
            } else {
                setWidth(Math.max(font.width(title) + 22,
                        font.width(descriptions.get(0)) + 22));
            }
        }
        setHeight(getContentHeight(this.descriptions.size(), font.lineHeight));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @OnlyIn(Dist.CLIENT)
    public static List<Component> lineFeed(Component component, Font font, int maxWidth) {
        String descriptionText = component.getString();
        List<Component> result = new ArrayList<>();
        while (true) {
            String inner = font.plainSubstrByWidth(descriptionText, maxWidth);
            descriptionText = descriptionText.substring(inner.length());
            result.add(Component.literal(inner));
            if (descriptionText.length() < 1) break;
        }
        return result;
    }

    public static int getContentHeight(int lines, int lineHeight) {
        return 2 * boarderWidth + 20 + (lines + 1) * lineHeight;
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partial) {
        Font font = Minecraft.getInstance().font;
        renderRectangle(poseStack);
        int baseX = this.x + 5;
        int baseY = this.y + 2 * boarderWidth + 16;
        fill(poseStack, baseX, baseY,
                this.x + this.width - 5, baseY + boarderWidth, boarderColor);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 400.0D);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.renderAndDecorateItem(icon, this.x + boarderWidth + 1, this.y + boarderWidth + 1);
        Matrix4f matrix4f = poseStack.last().pose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(title, this.x + 16 + 2 * boarderWidth + 1,
                this.y + boarderWidth + (16f - font.lineHeight) / 2, fontColor,
                false, matrix4f, buffer, false,
                0, 15728880);
        for (int i = 0; i < descriptions.size(); i++) {
            Component component = descriptions.get(i);
            font.drawInBatch(component, baseX,
                    baseY + boarderWidth + 1 + i * font.lineHeight, fontColor,
                    false, matrix4f, buffer, false,
                    0, 15728880);
        }
        buffer.endBatch();
        poseStack.popPose();
    }

    private void renderRectangle(PoseStack poseStack) {
        fill(poseStack, x, y, x + width, y + boarderWidth, boarderColor);
        fill(poseStack, x, y, x + boarderWidth, y + height - boarderWidth, boarderColor);
        fill(poseStack, x + width - boarderWidth, y, x + width, y + height - boarderWidth, boarderColor);
        fill(poseStack, x, y + height - boarderWidth, x + width, y + height, boarderColor);
        fill(poseStack, x + boarderWidth, y + boarderWidth, x + width - boarderWidth,
                y + height - boarderWidth, backgroundColor);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {

    }
}
