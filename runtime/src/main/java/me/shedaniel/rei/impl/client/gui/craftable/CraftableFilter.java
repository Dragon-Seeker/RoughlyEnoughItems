/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.gui.craftable;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import net.minecraft.client.Minecraft;

public class CraftableFilter {
    public static final CraftableFilter INSTANCE = new CraftableFilter();
    private boolean dirty = false;
    private LongSet invStacks = new LongOpenHashSet();
    
    public void markDirty() {
        dirty = true;
    }
    
    public boolean wasDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        
        return Minecraft.getInstance().player.containerMenu != null;
    }
    
    public void tick() {
        if (dirty) return;
        LongSet currentStacks;
        try {
            currentStacks = ClientHelperImpl.getInstance()._getInventoryItemsTypes();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            currentStacks = LongSets.EMPTY_SET;
        }
        if (!currentStacks.equals(this.invStacks)) {
            invStacks = new LongOpenHashSet(currentStacks);
            markDirty();
        }
    }
    
    public boolean matches(EntryStack<?> stack, Iterable<SlotAccessor> inputSlots) {
        if (invStacks.contains(EntryStacks.hashFuzzy(stack))) return true;
        if (stack.getType() != VanillaEntryTypes.ITEM) return false;
        for (SlotAccessor slot : inputSlots) {
            EntryStack<?> itemStack = EntryStacks.of(slot.getItemStack());
            if (!itemStack.isEmpty() && EntryStacks.equalsFuzzy(itemStack, stack)) {
                return true;
            }
        }
        return false;
    }
}
