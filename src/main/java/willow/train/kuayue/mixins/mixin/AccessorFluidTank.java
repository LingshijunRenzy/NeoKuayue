package willow.train.kuayue.mixins.mixin;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidTank.class)
public interface AccessorFluidTank {
    @Accessor("fluid")
    void setFluid(FluidStack fluid);
}
