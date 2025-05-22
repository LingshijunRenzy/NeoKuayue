package willow.train.kuayue.systems.tech_tree.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import org.joml.Matrix4f;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;

import java.util.ArrayList;
import java.util.List;

public class Tooltip extends AbstractWidget {

    private MutableComponent title;

    @Getter
    private final String identifier;
    private final List<Component> descriptions;
    private final ItemStack icon;

    public static final int maxWidth = 150, boarderWidth = 1,
            boarderColor = 0xffffffff,
            backgroundColor = 0x80000000,
            fontColor = 0xffffffff;


    public static Tooltip fromNode(ClientTechTreeNode node) {
        return new Tooltip(node.location.toString(), node.getName(),
                node.getLogo(), node.getDescription());
    }

    public static Tooltip fromGroup(ClientTechTreeGroup group) {
        return new Tooltip(group.getId().toString(), group.getTitleKey(),
                group.getIcon(), group.getDescriptionKey());
    }

    public Tooltip(String identifier, String titleKey, ItemStack icon, String descriptionKey) {
        super(0, 0, 0, 0, Component.empty());
        this.identifier = identifier;
        this.title = Component.translatable(titleKey);
        this.descriptions = new ArrayList<>();
        this.icon = icon;
        Component component = Component.translatable(descriptionKey);
        Font font = Minecraft.getInstance().font;
        this.descriptions.addAll(lineFeed(component, font, maxWidth));
        if (descriptions.size() > 1) {
            setWidth(maxWidth + 6);
        } else {
            if (descriptions.isEmpty()) {
                setWidth(font.width(title) + 22);
            } else {
                setWidth(Math.max(font.width(title) + 22,
                        font.width(descriptions.get(0)) + 22));
            }
        }
        setHeight(getContentHeight(this.descriptions.size(), font.lineHeight));
        title = Component.literal(font.substrByWidth(title, this.width - 22).getString());
    }

    public void setX(int x) {
        super.setX(x);
    }

    public void setY(int y) {
        super.setY(y);
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
            if (descriptionText.isEmpty()) break;
        }
        return result;
    }

    public static int getContentHeight(int lines, int lineHeight) {
        return 2 * boarderWidth + 20 + (lines + 1) * lineHeight;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        Font font = Minecraft.getInstance().font;
        renderRectangle(graphics);
        int baseX = this.getX() + 5;
        int baseY = this.getY() + 2 * boarderWidth + 16;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0D, 0.0D, 400.0D);
        graphics.fill(baseX, baseY,
                this.getX() + this.width - 5, baseY + boarderWidth, boarderColor);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.renderAndDecorateItem(icon, this.getX() + boarderWidth + 1, this.getY() + boarderWidth + 1);
        Matrix4f matrix4f = graphics.pose().last().pose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(title, this.getX() + 16 + 2 * boarderWidth + 1,
                this.getY() + boarderWidth + (16f - font.lineHeight) / 2, fontColor,
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
        graphics.pose().popPose();
    }

    private void renderRectangle(GuiGraphics graphics) {

        graphics.fill(this.getX(), this.getY(), this.getX() + width, this.getY() + boarderWidth, boarderColor);
        graphics.fill(this.getX(), this.getY(), this.getX() + boarderWidth, this.getY() + height - boarderWidth, boarderColor);
        graphics.fill(this.getX() + width - boarderWidth, this.getY(), this.getX() + width, this.getY() + height - boarderWidth, boarderColor);
        graphics.fill(this.getX(), this.getY() + height - boarderWidth, this.getX() + width, this.getY() + height, boarderColor);
        graphics.fill(this.getX() + boarderWidth, this.getY() + boarderWidth, this.getX() + width - boarderWidth,
                this.getY() + height - boarderWidth, backgroundColor);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tooltip tooltip)) return false;
        return this.identifier.equals(tooltip.identifier);
    }

    public boolean is(Object obj) {
        if (!(obj instanceof ClientTechTreeGroup) &&
                !(obj instanceof ClientTechTreeNode)) return false;
        if (obj instanceof ClientTechTreeGroup group)
            return this.identifier.equals(group.getId().toString());
        if (obj instanceof ClientTechTreeNode node)
            return this.identifier.equals(node.location.toString());
        return false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {

    }
}
