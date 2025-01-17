package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Size2i;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension}
 * to have your recipe extension work as part of {@link RecipeTypes#CRAFTING} as a shapeless recipe.
 * <p>
 * For shaped recipes, override {@link #getWidth()} and {@link #getHeight()}.
 * <p>
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getCraftingCategory()}
 * and then registering it with {@link IExtendableRecipeCategory#addCategoryExtension}.
 */
public interface ICraftingCategoryExtension extends IRecipeCategoryExtension {
    /**
     * Override the default {@link IRecipeCategory} behavior.
     *
     * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
     * @since 9.4.0
     */
    default void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        // if this new method is not implemented, call the legacy method
        setRecipe(builder, craftingGridHelper, focuses.getAllFocuses());
    }
    
    /**
     * Return the registry name of the recipe here.
     * With advanced tooltips on, this will show on the output item's tooltip.
     * <p>
     * This will also show the modId when the recipe modId and output item modId do not match.
     * This lets the player know where the recipe came from.
     *
     * @return the registry name of the recipe, or null if there is none
     */
    @Nullable
    default ResourceLocation getRegistryName() {
        return null;
    }
    
    /**
     * @return the width of a shaped recipe, or 0 for a shapeless recipe
     * @since 9.3.0
     */
    default int getWidth() {
        // if not implemented, this calls the old getSize function for backward compatibility
        Size2i size = getSize();
        if (size == null) {
            return 0;
        }
        return size.width;
    }
    
    /**
     * @return the height of a shaped recipe, or 0 for a shapeless recipe
     * @since 9.3.0
     */
    default int getHeight() {
        // if not implemented, this calls the old getSize function for backward compatibility
        Size2i size = getSize();
        if (size == null) {
            return 0;
        }
        return size.height;
    }
    
    /**
     * Override the default {@link IRecipeCategory} behavior.
     *
     * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
     * @since 9.3.0
     * @deprecated use {@link #setRecipe(IRecipeLayoutBuilder, ICraftingGridHelper, IFocusGroup)}
     */
    @Deprecated(forRemoval = true, since = "9.4.0")
    default void setRecipe(
            IRecipeLayoutBuilder builder,
            ICraftingGridHelper craftingGridHelper,
            List<? extends IFocus<?>> focuses
    ) {
        
    }
    
    /**
     * @return the size of a shaped recipe, or null for a shapeless recipe
     * @deprecated Use {@link #getWidth()} and {@link #getHeight()} instead.
     */
    @Deprecated(forRemoval = true, since = "9.3.0")
    @Nullable
    default Size2i getSize() {
        return null;
    }
}
