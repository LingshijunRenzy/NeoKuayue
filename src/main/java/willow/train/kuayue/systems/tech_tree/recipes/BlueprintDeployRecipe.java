package willow.train.kuayue.systems.tech_tree.recipes;

import com.google.gson.JsonObject;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.items.wrapper.RecipeWrapper;
 import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.initial.recipe.AllRecipes;
import willow.train.kuayue.systems.tech_tree.NodeLocation;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class BlueprintDeployRecipe extends DeployerApplicationRecipe {

    private NodeLocation node;
    private String nodePatternSource;
    private Predicate<NodeLocation> matcher;
    private boolean pasteNodeToResult;

    public BlueprintDeployRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(params);
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level p_77569_2_) {
        return super.matches(inv, p_77569_2_) && heldItemHasNode(inv.getItem(1));
    }


    private boolean heldItemHasNode(ItemStack stack) {
        if (!stack.hasTag()) return false;

        CompoundTag tag = stack.getTag();
        if(tag == null || !tag.contains("node")) return false;

        String nodeStr = tag.getString("node");
        try {
            NodeLocation node = new NodeLocation(nodeStr);
            if(matcher == null) compileMatcher();
            return matcher.test(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ItemStack assemble(RecipeWrapper inv) {
        ItemStack result = super.assemble(inv);
        if (!pasteNodeToResult) {
            return result;
        }
        ItemStack held = inv.getItem(1);
        if(held.hasTag() && held.getTag().contains("node")) {
            String nodeStr = held.getTag().getString("node");
            CompoundTag nbt = result.getOrCreateTag();
            nbt.putString("node", nodeStr);
        }
        return result;
    }

    @Override
    public @NotNull List<ItemStack> rollResults() {
        return rollResults(this.getRollableResults());
    }

    @Override
    public @NotNull List<ItemStack> rollResults(@NotNull List<ProcessingOutput> rollableResults) {
        List<ItemStack> results = super.rollResults(rollableResults);
        if (!pasteNodeToResult) {
            return results;
        }

        ItemStack held = this.ingredients.get(1).getItems()[0];
        if(!held.hasTag() || !held.getTag().contains("node")) {
            return results;
        }
        String nodeStr = held.getTag().getString("node");

        for(ProcessingOutput output : rollableResults) {
            ItemStack stack = output.getStack();
            CompoundTag nbt = stack.getOrCreateTag();
            nbt.putString("node", nodeStr);
        }
        return results;
    }

    @Override
    public boolean shouldKeepHeldItem() {
        return true;
    }

    @Override
    public void readAdditional(JsonObject json) {
        super.readAdditional(json);
        this.nodePatternSource = json.get("node").getAsString();
        this.pasteNodeToResult = json.has("paste_node_to_result") &&
                json.get("paste_node_to_result").getAsBoolean();
        compileMatcher();
    }

    @Override
    public void writeAdditional(JsonObject json) {
        super.writeAdditional(json);
        json.addProperty("node", this.nodePatternSource);
        json.addProperty("paste_node_to_result", this.pasteNodeToResult);
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        super.readAdditional(buffer);
        this.nodePatternSource = buffer.readUtf();
        this.pasteNodeToResult = buffer.readBoolean();
        compileMatcher();
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        super.writeAdditional(buffer);
        buffer.writeUtf(this.nodePatternSource);
        buffer.writeBoolean(this.pasteNodeToResult);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AllRecipes.blueprintSerializer;
    }

    private void compileMatcher() {
        if(nodePatternSource == null || nodePatternSource.isEmpty()) {
            matcher = loc -> false;
            return;
        }

        int firstColon = nodePatternSource.indexOf(':');
        if(firstColon == -1) {
            matcher = loc -> false;
            return;
        }

        String namespace = nodePatternSource.substring(0, firstColon);
        String pathPattern = nodePatternSource.substring(firstColon + 1);

        int pathSplit = findPathSplitIndex(pathPattern);

        if (pathSplit == -1) {
            matcher = loc -> false;
            return;
        }

        String group = pathPattern.substring(0, pathSplit);
        String namePattern = pathPattern.substring(pathSplit + 1);

        String regex = "^" +
                convertToRegex(namespace) + ":" +
                convertToRegex(group) + "\\." +
                convertToRegex(namePattern) + "$";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        matcher = loc -> pattern.matcher(loc.toString()).matches();
    }

    private int findPathSplitIndex(String pathPattern) {
        if(pathPattern == null || pathPattern.isEmpty()) {
            return -1;
        }

        if(pathPattern.startsWith("[")) {
            int bracketCount = 0;
            for (int i = 0; i < pathPattern.length(); i++) {
                char c = pathPattern.charAt(i);
                if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;
                if (bracketCount == 0 && i + 1 < pathPattern.length() && pathPattern.charAt(i + 1) == '.') {
                    return i + 1;
                }
            }
        }

        return pathPattern.indexOf(".");
    }

    private String convertToRegex(String pattern) {
        if(pattern.startsWith("[") && pattern.endsWith("]")) {
            return pattern.substring(1, pattern.length() - 1);
        } else {
            return Pattern.quote(pattern);
        }
    }
}
