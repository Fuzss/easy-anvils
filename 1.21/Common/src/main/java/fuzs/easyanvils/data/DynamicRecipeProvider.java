package fuzs.easyanvils.data;

import fuzs.puzzleslib.api.data.v2.AbstractRecipeProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class DynamicRecipeProvider extends AbstractRecipeProvider {

    public DynamicRecipeProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.NAME_TAG)
                .define('#', Items.STRING)
                .define('X', Items.PAPER)
                .pattern("  #")
                .pattern(" X ")
                .pattern("X  ")
                .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                .save(recipeOutput);
    }
}
