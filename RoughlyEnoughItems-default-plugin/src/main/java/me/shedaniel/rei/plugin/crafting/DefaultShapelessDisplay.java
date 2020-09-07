/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.plugin.crafting;

import me.shedaniel.rei.api.EntryStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DefaultShapelessDisplay implements DefaultCraftingDisplay {
    
    private ShapelessRecipe display;
    private List<List<EntryStack>> input;
    private List<EntryStack> output;
    
    public DefaultShapelessDisplay(ShapelessRecipe recipe) {
        this.display = recipe;
        this.input = EntryStack.ofIngredients(recipe.getIngredients());
        this.output = Collections.singletonList(EntryStack.create(recipe.getResultItem()));
    }
    
    @Override
    public Optional<IRecipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(display);
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getRecipeLocation() {
        return Optional.ofNullable(display).map(ShapelessRecipe::getId);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return input;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return input;
    }
    
    @Override
    public int getWidth() {
        if (display.getIngredients().size() > 4)
            return 3;
        return 2;
    }
    
    @Override
    public int getHeight() {
        if (display.getIngredients().size() > 4)
            return 3;
        return 2;
    }
    
}
