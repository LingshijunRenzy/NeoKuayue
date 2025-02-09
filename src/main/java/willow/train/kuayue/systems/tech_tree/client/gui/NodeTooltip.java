package willow.train.kuayue.systems.tech_tree.client.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class NodeTooltip extends AbstractWidget {

    private ClientTechTreeNode node;
    private Component title;
    private final List<Component> discriptions;
    private ItemStack icon;
    public NodeTooltip(ClientTechTreeNode node) {
        super(0, 0, 0, 0, Component.empty());
        this.node = node;
        discriptions = new ArrayList<>();
    }

    private void updateNode() {
        this.title = Component.translatable(node.getName());
        this.icon = node.getLogo();
        Component component = Component.translatable(node.getDescription());
        lineFeed(component);
        this.discriptions.add(component);
    }

    public static void lineFeed(Component component) {
        String descriptionText = component.getString();

    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
