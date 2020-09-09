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

package me.shedaniel.rei.plugin.composting;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DefaultCompostingDisplay implements RecipeDisplay {
    private List<List<EntryStack>> inputs;
    private Object2FloatMap<IItemProvider> inputMap;
    private List<List<EntryStack>> output;
    private int page;
    
    public DefaultCompostingDisplay(int page, List<Object2FloatMap.Entry<IItemProvider>> inputs, Object2FloatMap<IItemProvider> map, ItemStack output) {
        this.page = page;
        {
            List<EntryStack>[] result = new List[inputs.size()];
            int i = 0;
            for (Object2FloatMap.Entry<IItemProvider> entry : inputs) {
                result[i] = Collections.singletonList(EntryStack.create(entry.getKey()));
                i++;
            }
            this.inputs = Arrays.asList(result);
        }
        this.inputMap = map;
        this.output = Collections.singletonList(Collections.singletonList(EntryStack.create(output)));
    }
    
    public int getPage() {
        return page;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return inputs;
    }
    
    public Object2FloatMap<IItemProvider> getInputMap() {
        return inputMap;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return output;
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.COMPOSTING;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return inputs;
    }
}
