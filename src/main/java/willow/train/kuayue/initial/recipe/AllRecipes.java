package willow.train.kuayue.initial.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import kasuga.lib.registrations.common.RecipeReg;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.tech_tree.recipes.BlueprintDeployRecipe;

public class AllRecipes {

    public static final ProcessingRecipeSerializer<BlueprintDeployRecipe>
        blueprintSerializer = new ProcessingRecipeSerializer<BlueprintDeployRecipe>(BlueprintDeployRecipe::new);
    public static final
    RecipeReg<BlueprintDeployRecipe,
            ProcessingRecipeSerializer<BlueprintDeployRecipe>>
            blueprintRecipe = new RecipeReg<BlueprintDeployRecipe,
            ProcessingRecipeSerializer<BlueprintDeployRecipe>>("blueprint_deploy")
            .withSerializer(blueprintSerializer)
            .submit(AllElements.testRegistry);

    public static void invoke(){

    }
}
