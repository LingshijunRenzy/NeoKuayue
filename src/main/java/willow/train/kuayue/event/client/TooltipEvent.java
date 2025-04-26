package willow.train.kuayue.event.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag()) return;
        net.minecraft.nbt.CompoundTag tag = stack.getTag();
        if (!tag.contains("node")) return;
        try {
            willow.train.kuayue.systems.tech_tree.NodeLocation nodeLoc = new willow.train.kuayue.systems.tech_tree.NodeLocation(tag.getString("node"));
            willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager manager = willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager.getInstance();
            willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode node = manager.getNode(nodeLoc);
            if (node == null) return;
            net.minecraft.network.chat.MutableComponent component = net.minecraft.network.chat.Component.literal("-> ")
                    .append(net.minecraft.network.chat.Component.translatable(node.getName()))
                    .setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.ChatFormatting.GOLD));
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