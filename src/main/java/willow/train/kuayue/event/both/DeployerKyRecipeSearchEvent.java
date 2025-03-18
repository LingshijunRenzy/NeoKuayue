package willow.train.kuayue.event.both;

import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import willow.train.kuayue.initial.recipe.AllRecipes;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeployerKyRecipeSearchEvent {

    @SubscribeEvent
    public static void addKyRecipeType(DeployerRecipeSearchEvent event) {
        Level level = event.getBlockEntity().getLevel();
        if (level == null) return;
        event.addRecipe(() -> level.getRecipeManager().getRecipeFor(
                AllRecipes.blueprintRecipe.getRecipeType(), event.getInventory(), level),
                25);
    }
}
