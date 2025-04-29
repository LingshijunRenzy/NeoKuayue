package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import willow.train.kuayue.block.bogey.carriage.ISingleBogeyBlockEntity;

@Mixin(value = AbstractBogeyBlockEntity.class,remap = false)
public class SnrCompatibilityMixinAbstractBogeyBlockEntity {
    @Inject(method="setBogeyStyle", at = @At("HEAD"), cancellable = true)
    public void onSetBogeyStyle(BogeyStyle style, CallbackInfo ci) {
        if (this instanceof ISingleBogeyBlockEntity isbe && !isbe.isBogeyStyleValid(style)) {
            ci.cancel();
            return;
        }
    }
}
