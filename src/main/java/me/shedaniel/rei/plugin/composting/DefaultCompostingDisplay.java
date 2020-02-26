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

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultCompostingDisplay implements RecipeDisplay {
    
    private List<EntryStack> order, allItems;
    private Map<ItemConvertible, Float> inputMap;
    private List<EntryStack> output;
    private int page;
    
    public DefaultCompostingDisplay(int page, List<ItemConvertible> order, Map<ItemConvertible, Float> inputMap, List<ItemConvertible> allItems, ItemStack[] output) {
        this.page = page;
        this.order = order.stream().map(EntryStack::create).collect(Collectors.toList());
        this.inputMap = inputMap;
        this.output = Arrays.stream(output).map(EntryStack::create).collect(Collectors.toList());
        this.allItems = allItems.stream().map(EntryStack::create).collect(Collectors.toList());
    }
    
    public int getPage() {
        return page;
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        List<List<EntryStack>> lists = new ArrayList<>();
        for (EntryStack allItem : allItems) {
            lists.add(Collections.singletonList(allItem));
        }
        return lists;
    }
    
    public Map<ItemConvertible, Float> getInputMap() {
        return inputMap;
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.COMPOSTING;
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return Collections.singletonList(allItems);
    }
    
    public List<EntryStack> getItemsByOrder() {
        return order;
    }
    
}
