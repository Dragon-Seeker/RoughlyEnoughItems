package mezz.jei.api.recipe;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.Optional;

/**
 * The current search focus.
 * Set by the player when they look up the recipe. The ingredient being looked up is the focus.
 * This class is immutable, the value and mode do not change.
 * <p>
 * Create a focus with {@link IFocusFactory#createFocus(RecipeIngredientRole, IIngredientType, Object)}.
 * <p>
 * Use a null IFocus to signify no focus, like in the case of looking up categories of recipes.
 */
public interface IFocus<V> {
    /**
     * The ingredient that is being focused on.
     *
     * @since 9.3.0
     */
    ITypedIngredient<V> getTypedValue();
    
    /**
     * The focused recipe ingredient role.
     *
     * @since 9.3.0
     */
    RecipeIngredientRole getRole();
    
    /**
     * @return this focus if it matches the given ingredient type.
     * This is useful when handling a wildcard generic instance of `IFocus<?>`.
     *
     * @since 9.4.0
     */
    <T> Optional<IFocus<T>> checkedCast(IIngredientType<T> ingredientType);
    
    /**
     * The focus mode.
     * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
     * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
     *
     * @deprecated Use {@link RecipeIngredientRole} instead.
     */
    @Deprecated(forRemoval = true, since = "9.3.0")
    enum Mode {
        INPUT,
        OUTPUT;
        
        /**
         * Convert this legacy {@link IFocus} {@link Mode} into a {@link RecipeIngredientRole}.
         *
         * @since 9.3.0
         */
        public RecipeIngredientRole toRole() {
            return switch (this) {
                case INPUT -> RecipeIngredientRole.INPUT;
                case OUTPUT -> RecipeIngredientRole.OUTPUT;
            };
        }
    }
    
    /**
     * The ingredient that is being focused on.
     *
     * @deprecated use {@link #getTypedValue()} instead.
     */
    @Deprecated(forRemoval = true, since = "9.3.0")
    default V getValue() {
        return getTypedValue().getIngredient();
    }
    
    /**
     * The focus mode.
     * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
     * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
     *
     * @deprecated Use {@link #getRole()} instead.
     */
    @Deprecated(forRemoval = true, since = "9.3.0")
    Mode getMode();
}
