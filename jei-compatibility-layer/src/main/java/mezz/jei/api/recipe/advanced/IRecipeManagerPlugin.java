package mezz.jei.api.recipe.advanced;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IAdvancedRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * {@link IRecipeManagerPlugin}s are used by the {@link IRecipeManager} to look up recipes.
 * JEI has its own internal plugin, which uses information from {@link IRecipeCategory} to look up recipes.
 * Implementing your own Recipe Registry Plugin offers total control of lookups, but it must be fast.
 * <p>
 * Add your plugin with {@link IAdvancedRegistration#addRecipeManagerPlugin(IRecipeManagerPlugin)}
 */
public interface IRecipeManagerPlugin {
    /**
     * Returns a list of Recipe Types offered for the focus.
     *
     * @since 9.5.0
     */
    default <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        return List.of();
    }
    
    /**
     * Returns a list of Recipes in the recipeCategory that have the focus.
     * This is used internally by JEI to implement {@link IRecipeManager#getRecipes(IRecipeCategory, List, boolean)}.
     */
    <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus);
    
    /**
     * Returns a list of all Recipes in the recipeCategory.
     * This is used internally by JEI to implement {@link IRecipeManager#getRecipes(IRecipeCategory, List, boolean)}.
     */
    <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory);
    
    /**
     * Returns a list of Recipe Categories offered for the focus.
     * This is used internally by JEI to implement {@link IRecipeManager#getRecipeCategories(IFocus, boolean)}.
     *
     * @deprecated use {@link #getRecipeTypes(IFocus)} instead.
     */
    @Deprecated(forRemoval = true, since = "9.5.0")
    <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus);
}
