package willow.train.kuayue.initial.recipe;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import willow.train.kuayue.block.recipe.BlueprintBlock;
import willow.train.kuayue.block.recipe.BlueprintBlockEntity;
import willow.train.kuayue.block.recipe.BlueprintMenu;
import willow.train.kuayue.block.recipe.BlueprintScreen;
import willow.train.kuayue.initial.AllElements;

public class AllRecipeBlock {

    public static final BlockReg<BlueprintBlock> BLUEPRINT_TABLE =
            new BlockReg<BlueprintBlock>("blueprint_table")
                    .blockType(BlueprintBlock::new)
                    .materialColor(MapColor.WOOD)
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .addProperty(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .addProperty(properties -> properties.strength(1.0f, 1.5f))
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueMainTab)
                    .withBlockEntity("blueprint_table_be", BlueprintBlockEntity::new)
                    .withMenu("blueprint_table_menu", BlueprintMenu::new, () -> BlueprintScreen::new)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}

}
