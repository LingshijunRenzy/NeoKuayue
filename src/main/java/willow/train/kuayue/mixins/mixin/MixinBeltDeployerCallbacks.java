package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import willow.train.kuayue.systems.tech_tree.recipes.BlueprintDeployRecipe;

import java.util.List;

@Mixin(BeltDeployerCallbacks.class)
public class MixinBeltDeployerCallbacks {

    @Inject(
            method = "activate",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;convertToAndLeaveHeld(Ljava/util/List;Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;)Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;",shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private static void afterRecipeApplyOn(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, DeployerBlockEntity blockEntity, Recipe<?> recipe, CallbackInfo ci, List<TransportedItemStack> collect) {
        if(recipe instanceof BlueprintDeployRecipe blueprintDeployRecipe) {
            ItemStack heldItem = blockEntity.getPlayer().getMainHandItem();
            ItemStack resultItem = collect.get(0).stack;
            collect.get(0).stack = blueprintDeployRecipe.replaceResults(resultItem, heldItem);
        }
    }
}
