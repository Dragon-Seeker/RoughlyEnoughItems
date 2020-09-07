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

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DefaultCustomDisplay implements DefaultCraftingDisplay {
    
    private List<List<EntryStack>> input;
    private List<EntryStack> output;
    private IRecipe<?> possibleRecipe;
    private int width, height;
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output, IRecipe<?> possibleRecipe) {
        this(possibleRecipe, CollectionUtils.map(input, EntryStack::ofItemStacks), EntryStack.ofItemStacks(output));
    }
    
    public DefaultCustomDisplay(IRecipe<?> possibleRecipe, List<List<EntryStack>> input, List<EntryStack> output) {
        this.input = input;
        this.output = output;
        this.possibleRecipe = possibleRecipe;
        List<Boolean> row = Lists.newArrayList(false, false, false);
        List<Boolean> column = Lists.newArrayList(false, false, false);
        for (int i = 0; i < 9; i++)
            if (i < this.input.size()) {
                List<EntryStack> stacks = this.input.get(i);
                if (stacks.stream().anyMatch(stack -> !stack.isEmpty())) {
                    row.set((i - (i % 3)) / 3, true);
                    column.set(i % 3, true);
                }
            }
        this.width = (int) column.stream().filter(Boolean::booleanValue).count();
        this.height = (int) row.stream().filter(Boolean::booleanValue).count();
    }
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output) {
        this(input, output, null);
    }
    
    protected Optional<IRecipe<?>> getRecipe() {
        return Optional.ofNullable(possibleRecipe);
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getRecipeLocation() {
        return getRecipe().map(IRecipe::getId);
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
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public Optional<IRecipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(possibleRecipe);
    }
    
}
