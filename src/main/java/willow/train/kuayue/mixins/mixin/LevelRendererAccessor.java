package willow.train.kuayue.mixins.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor()
    public ClientLevel getLevel();
}
