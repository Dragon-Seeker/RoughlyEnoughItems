package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import org.jetbrains.annotations.ApiStatus;

/**
 * The IIngredientFilter is JEI's filter that can be set by players or controlled by mods.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getIngredientFilter()}.
 */
public interface IIngredientFilter {
    /**
     * Set the search filter string for the ingredient list.
     */
    void setFilterText(String filterText);
    
    /**
     * @return the current search filter string for the ingredient list
     */
    String getFilterText();
    
    /**
     * @return a list containing all ingredients that match the current filter.
     * To get all the ingredients known to JEI, see {@link IIngredientManager#getAllIngredients(IIngredientType)}.
     */
    ImmutableList<Object> getFilteredIngredients();
    
    @ApiStatus.Internal
    <V> boolean isIngredientVisible(V ingredient);
    
    @ApiStatus.Internal
    <V> boolean isIngredientVisible(V ingredient, IIngredientHelper<V> ingredientHelper);
}
