/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin.common.displays.crafting;

import dev.architectury.injectables.annotations.ExpectPlatform;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class DefaultCraftingDisplay<C extends Recipe<?>> extends BasicDisplay implements SimpleGridMenuDisplay {
    protected Optional<C> recipe;
    
    public DefaultCraftingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<C> recipe) {
        this(inputs, outputs, recipe.map(Recipe::getId), recipe);
    }
    
    public DefaultCraftingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, Optional<C> recipe) {
        super(inputs, outputs, location);
        this.recipe = recipe;
    }
    
    private static final List<CraftingRecipeSizeProvider<?>> SIZE_PROVIDER = new ArrayList<>();
    
    static {
        registerPlatformSizeProvider();
    }
    
    /**
     * Registers a size provider for crafting recipes.
     * This is not reloadable, please statically register your provider, and
     * do not repeatedly register it.
     *
     * @param sizeProvider the provider to register
     * @param <R>          the recipe type
     */
    public static <R extends Recipe<?>> void registerSizeProvider(CraftingRecipeSizeProvider<R> sizeProvider) {
        SIZE_PROVIDER.add(0, sizeProvider);
    }
    
    @Nullable
    public static DefaultCraftingDisplay<?> of(Recipe<?> recipe) {
        if (recipe instanceof ShapelessRecipe) {
            return new DefaultShapelessDisplay((ShapelessRecipe) recipe);
        } else if (recipe instanceof ShapedRecipe) {
            return new DefaultShapedDisplay((ShapedRecipe) recipe);
        } else if (!recipe.isSpecial()) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (CraftingRecipeSizeProvider<?> pair : SIZE_PROVIDER) {
                CraftingRecipeSizeProvider.Size size = ((CraftingRecipeSizeProvider<Recipe<?>>) pair).getSize(recipe);
                
                if (size != null) {
                    return new DefaultCustomShapedDisplay(recipe, EntryIngredients.ofIngredients(recipe.getIngredients()),
                            Collections.singletonList(EntryIngredients.of(recipe.getResultItem())),
                            size.getWidth(), size.getHeight());
                }
            }
            
            return new DefaultCustomDisplay(recipe, EntryIngredients.ofIngredients(recipe.getIngredients()),
                    Collections.singletonList(EntryIngredients.of(recipe.getResultItem())));
        }
        
        return null;
    }
    
    @ExpectPlatform
    private static void registerPlatformSizeProvider() {
        throw new AssertionError();
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.CRAFTING;
    }
    
    public Optional<C> getOptionalRecipe() {
        return recipe;
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return getOptionalRecipe().map(Recipe::getId);
    }
    
    public <T extends AbstractContainerMenu> List<List<ItemStack>> getOrganisedInputEntries(SimpleGridMenuInfo<T, DefaultCraftingDisplay<?>> menuInfo, T container) {
        return CollectionUtils.map(getOrganisedInputEntries(menuInfo.getCraftingWidth(container), menuInfo.getCraftingHeight(container)), ingredient ->
                CollectionUtils.<EntryStack<?>, ItemStack>filterAndMap(ingredient, stack -> stack.getType() == VanillaEntryTypes.ITEM,
                        EntryStack::castValue));
    }
    
    public <T extends AbstractContainerMenu> List<EntryIngredient> getOrganisedInputEntries(int menuWidth, int menuHeight) {
        List<EntryIngredient> list = new ArrayList<>(menuWidth * menuHeight);
        for (int i = 0; i < menuWidth * menuHeight; i++) {
            list.add(EntryIngredient.empty());
        }
        for (int i = 0; i < getInputEntries().size(); i++) {
            list.set(getSlotWithSize(this, i, menuWidth), getInputEntries().get(i));
        }
        return list;
    }
    
    public boolean isShapeless() {
        return false;
    }
    
    public static int getSlotWithSize(DefaultCraftingDisplay<?> display, int index, int craftingGridWidth) {
        return getSlotWithSize(display.getInputWidth(), index, craftingGridWidth);
    }
    
    public static int getSlotWithSize(int recipeWidth, int index, int craftingGridWidth) {
        int x = index % recipeWidth;
        int y = (index - x) / recipeWidth;
        return craftingGridWidth * y + x;
    }
    
    public static BasicDisplay.Serializer<DefaultCraftingDisplay<?>> serializer() {
        return BasicDisplay.Serializer.<DefaultCraftingDisplay<?>>ofSimple(DefaultCustomDisplay::simple)
                .inputProvider(display -> display.getOrganisedInputEntries(3, 3));
    }
    
    @Override
    public List<InputIngredient<EntryStack<?>>> getInputIngredients(MenuSerializationContext<?, ?, ?> context, MenuInfo<?, ?> info, boolean fill) {
        int craftingWidth = 3, craftingHeight = 3;
        
        if (info instanceof SimpleGridMenuInfo && fill) {
            craftingWidth = ((SimpleGridMenuInfo<AbstractContainerMenu, ?>) info).getCraftingWidth(context.getMenu());
            craftingHeight = ((SimpleGridMenuInfo<AbstractContainerMenu, ?>) info).getCraftingHeight(context.getMenu());
        }
        
        return getInputIngredients(craftingWidth, craftingHeight);
    }
    
    public List<InputIngredient<EntryStack<?>>> getInputIngredients(int craftingWidth, int craftingHeight) {
        int inputWidth = Math.max(3, getInputWidth());
        int inputHeight = Math.max(3, getInputHeight());
        
        InputIngredient<EntryStack<?>>[][] grid = new InputIngredient[Math.max(inputWidth, craftingWidth)][Math.max(inputHeight, craftingHeight)];
        
        List<EntryIngredient> inputEntries = getInputEntries();
        for (int i = 0; i < inputEntries.size(); i++) {
            grid[i % getInputWidth()][i / getInputWidth()] = InputIngredient.of(getSlotWithSize(getInputWidth(), i, craftingWidth), inputEntries.get(i));
        }
        
        List<InputIngredient<EntryStack<?>>> list = new ArrayList<>(craftingWidth * craftingHeight);
        for (int i = 0, n = craftingWidth * craftingHeight; i < n; i++) {
            list.add(InputIngredient.empty(i));
        }
        
        for (int x = 0; x < craftingWidth; x++) {
            for (int y = 0; y < craftingHeight; y++) {
                if (grid[x][y] != null) {
                    int index = craftingWidth * y + x;
                    list.set(index, grid[x][y]);
                }
            }
        }
        
        return list;
    }
}
