package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CarriageContraption.class)
public interface AccessorCarriageContraption {

    @Accessor(value = "secondBogeyPos")
    public void setSecondBogeyPos(BlockPos secondBogeyPos);
}
