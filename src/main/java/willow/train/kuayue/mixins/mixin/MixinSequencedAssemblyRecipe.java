package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(SequencedAssemblyRecipe.class)
public class MixinSequencedAssemblyRecipe {

    @Inject(method = "appliesTo", at = @At("RETURN"), cancellable = true, remap = false)
    private void kuayue$DoAppliesTo(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (input == null || !input.hasTag()) return;
        if (!input.getTag().contains("SequencedAssembly")) return;
        if (!kuayue$VerifyAssemblyData(input.getTag().getCompound("SequencedAssembly"))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "advance", at = @At("RETURN"), remap = false)
    private void kuayue$DoAdvance(ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack == null || !stack.hasTag()) return;
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains("SequencedAssembly")) return;
        kuayue$FillAssemblyData(nbt.getCompound("SequencedAssembly"));
    }

    @Unique
    public void kuayue$FillAssemblyData(CompoundTag nbt) {
        SequencedAssemblyRecipe self = kuayue$Self();
        int hash = 0;
        for (ProcessingOutput output : self.resultPool) {
            hash += kuayue$hashProcessingOutput(output);
        }
        nbt.putInt("hash", hash);
    }

    @Unique
    public boolean kuayue$VerifyAssemblyData(CompoundTag nbt) {
        if (nbt.contains("hash")) {
            SequencedAssemblyRecipe self = kuayue$Self();
            int hash = 0;
            for (ProcessingOutput output : self.resultPool) {
                hash += kuayue$hashProcessingOutput(output);
            }
            return nbt.getInt("hash") == hash;
        }
        return true;
    }

    @Unique
    public int kuayue$hashProcessingOutput(ProcessingOutput output) {
        return Objects.hash(output.getStack().getItem(), output.getChance());
    }

    @Unique
    public SequencedAssemblyRecipe kuayue$Self() {
        return (SequencedAssemblyRecipe) (Object) this;
    }
}
