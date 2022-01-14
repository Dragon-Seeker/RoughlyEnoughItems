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

package me.shedaniel.rei.jeicompat.wrap;

import com.google.common.collect.Lists;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeRegistration implements IRecipeRegistration {
    private final List<Runnable> post;
    
    public JEIRecipeRegistration(List<Runnable> post) {
        this.post = post;
    }
    
    @Override
    @NotNull
    public IJeiHelpers getJeiHelpers() {
        return JEIJeiHelpers.INSTANCE;
    }
    
    @Override
    @NotNull
    public IIngredientManager getIngredientManager() {
        return JEIIngredientManager.INSTANCE;
    }
    
    @Override
    @NotNull
    public IVanillaRecipeFactory getVanillaRecipeFactory() {
        return JEIVanillaRecipeFactory.INSTANCE;
    }
    
    @Override
    public void addRecipes(@NotNull Collection<?> recipes, @NotNull ResourceLocation categoryId) {
        post.add(() -> {
            CategoryIdentifier<Display> categoryIdentifier = CategoryIdentifier.of(categoryId);
            DisplayRegistry registry = DisplayRegistry.getInstance();
            if (recipes instanceof List<?> && recipes.size() >= 100) {
                addRecipesOptimized((List<Object>) recipes, categoryIdentifier, registry);
                return;
            }
            
            for (Object recipe : recipes) {
                Collection<Display> displays = registry.tryFillDisplay(recipe);
                for (Display display : displays) {
                    if (Objects.equals(display.getCategoryIdentifier(), categoryIdentifier)) {
                        registry.add(display, recipe);
                    }
                }
            }
        });
    }
    
    private void addRecipesOptimized(List<Object> recipes, @NotNull CategoryIdentifier<?> categoryId, DisplayRegistry registry) {
        List<CompletableFuture<List<Collection<Display>>>> completableFutures = Lists.newArrayList();
        Function<Object, Collection<Display>> tryFillDisplay = registry::tryFillDisplay;
        CollectionUtils.partition(recipes, 50).forEach(list -> {
            completableFutures.add(CompletableFuture.supplyAsync(() -> {
                return CollectionUtils.map(list, tryFillDisplay);
            }));
        });
        
        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(120, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        int i = 0;
        
        for (CompletableFuture<List<Collection<Display>>> future : completableFutures) {
            List<Collection<Display>> displayCollection = future.getNow(null);
            
            if (displayCollection != null) {
                int j = 0;
                
                for (Collection<Display> displays : displayCollection) {
                    Object origin = recipes.get(i * 50 + j);
                    
                    for (Display display : displays) {
                        if (Objects.equals(display.getCategoryIdentifier(), categoryId)) {
                            registry.add(display, origin);
                        }
                    }
                }
            }
            
            i++;
        }
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull T ingredient, @NotNull IIngredientType<T> ingredientType, @NotNull String @NotNull ... descriptionKeys) {
        EntryStack<T> stack = ingredient.unwrapStack(ingredientType);
        BuiltinClientPlugin.getInstance().registerInformation(stack, stack.asFormattedText(), components -> {
            for (String key : descriptionKeys) {
                components.add(new TranslatableComponent(key));
            }
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull T ingredient, @NotNull IIngredientType<T> ingredientType, @NotNull Component @NotNull ... descriptionComponents) {
        EntryStack<T> stack = ingredient.unwrapStack(ingredientType);
        BuiltinClientPlugin.getInstance().registerInformation(stack, stack.asFormattedText(), components -> {
            Collections.addAll(components, descriptionComponents);
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull List<T> ingredients, @NotNull IIngredientType<T> ingredientType, @NotNull String @NotNull ... descriptionKeys) {
        EntryIngredient ingredient = ingredientType.unwrapList(ingredients);
        BuiltinClientPlugin.getInstance().registerInformation(ingredient, ImmutableTextComponent.EMPTY, components -> {
            for (String key : descriptionKeys) {
                components.add(new TranslatableComponent(key));
            }
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull List<T> ingredients, @NotNull IIngredientType<T> ingredientType, @NotNull Component @NotNull ... descriptionComponents) {
        EntryIngredient ingredient = ingredientType.unwrapList(ingredients);
        BuiltinClientPlugin.getInstance().registerInformation(ingredient, ImmutableTextComponent.EMPTY, components -> {
            Collections.addAll(components, descriptionComponents);
            return components;
        });
    }
}
