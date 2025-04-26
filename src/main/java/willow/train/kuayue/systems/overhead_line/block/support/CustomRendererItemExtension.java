package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Supplier;

public class CustomRendererItemExtension implements IClientItemExtensions {
    private final Supplier<Supplier<BlockEntityWithoutLevelRenderer>> renderer;

    public CustomRendererItemExtension(Supplier<Supplier<BlockEntityWithoutLevelRenderer>> renderer) {
        this.renderer = renderer;
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return this.renderer.get().get();
    }
}
