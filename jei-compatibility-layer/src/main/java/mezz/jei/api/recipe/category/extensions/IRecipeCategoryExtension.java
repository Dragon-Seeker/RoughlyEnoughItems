package mezz.jei.api.recipe.category.extensions;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/**
 * An extension to a recipe category with methods that allow JEI to make sense of it.
 * Plugins implement these for recipe categories that support it, for each type of recipe they have.
 */
public interface IRecipeCategoryExtension {
    
    /**
     * Draw additional info about the recipe.
     * Use the mouse position for things like button highlights.
     * Tooltips are handled by {@link #getTooltipStrings(double, double)}
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @see IDrawable for a simple class for drawing things.
     * @see IGuiHelper for useful functions.
     */
    default void drawInfo(int recipeWidth, int recipeHeight, PoseStack stack, double mouseX, double mouseY) {
        
    }
    
    /**
     * Get the tooltip for whatever is under the mouse.
     * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
     * <p>
     * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
     * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltipStrings(Object, IRecipeSlotsView, double, double)}
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @return tooltip strings. If there is no tooltip at this position, return an empty list.
     */
    default List<Component> getTooltipStrings(double mouseX, double mouseY) {
        return Collections.emptyList();
    }
    
    /**
     * Called when a player inputs while hovering over the recipe.
     * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @param input  the current input from the player.
     * @return true if the input was handled, false otherwise
     * @since 8.3.0
     */
    default boolean handleInput(double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE) {
            return handleClick(mouseX, mouseY, input.getValue());
        }
        return false;
    }
    
    /**
     * Called when a player clicks the recipe.
     * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
     *
     * @param mouseX      the X position of the mouse, relative to the recipe.
     * @param mouseY      the Y position of the mouse, relative to the recipe.
     * @param mouseButton the current mouse event button.
     * @return true if the click was handled, false otherwise
     * @deprecated Use {@link #handleInput(double, double, InputConstants.Key)}
     */
    @Deprecated(forRemoval = true, since = "8.3.0")
    default boolean handleClick(double mouseX, double mouseY, int mouseButton) {
        return false;
    }
    
    /**
     * Gets all the recipe's ingredients by filling out an instance of {@link IIngredients}.
     *
     * @see ICraftingCategoryExtension#setRecipe(IRecipeLayoutBuilder, ICraftingGridHelper, IFocusGroup)
     * @deprecated Subclasses of this interface should define their own methods of setting ingredients.
     */
    @Deprecated(forRemoval = true, since = "9.3.0")
    default void setIngredients(IIngredients ingredients) {
        
    }
}
