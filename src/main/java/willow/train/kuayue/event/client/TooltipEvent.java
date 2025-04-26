package willow.train.kuayue.event.client;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        if (!tag.contains("node")) return;
        try {
            NodeLocation nodeLoc = new NodeLocation(tag.getString("node"));
            ClientTechTreeManager manager = ClientTechTreeManager.getInstance();
            ClientTechTreeNode node = manager.getNode(nodeLoc);
            if (node == null) return;
            MutableComponent component = Component.literal("-> ")
                    .append(Component.translatable(node.getName()))
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
            event.getToolTip().add(component);
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public static void onItemTooltipCheckItemType(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        ResourceLocation itemRegistryName = ForgeRegistries.ITEMS.getKey(item);
        if (itemRegistryName != null) {
            String itemPath = itemRegistryName.getPath();
            ItemTooltipConfig.TooltipInfo tooltipInfo = ItemTooltipConfig.getTooltipInfo(itemPath);
            if (tooltipInfo != null) {
                try {
                    event.getToolTip().add(tooltipInfo.createTooltipComponent());
                } catch (Exception ignored) {}
            }
        }
    }
}