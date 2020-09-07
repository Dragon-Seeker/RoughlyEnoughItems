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

package me.shedaniel.rei.impl.filtering;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.AmountIgnoredEntryStackWrapper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
@ApiStatus.Internal
@ApiStatus.Experimental
public interface FilteringResult {
    static FilteringResult create() {
        return create(Lists.newArrayList(), Lists.newArrayList());
    }
    
    static FilteringResult create(List<EntryStack> hiddenStacks, List<EntryStack> shownStacks) {
        return new FilteringResultImpl(hiddenStacks, shownStacks);
    }
    
    Set<AmountIgnoredEntryStackWrapper> getHiddenStacks();
    
    Set<AmountIgnoredEntryStackWrapper> getShownStacks();
    
    default FilteringResult hide(EntryStack stack) {
        getHiddenStacks().add(new AmountIgnoredEntryStackWrapper(stack));
        return this;
    }
    
    default FilteringResult hide(Collection<EntryStack> stacks) {
        getHiddenStacks().addAll(CollectionUtils.map(stacks, AmountIgnoredEntryStackWrapper::new));
        return this;
    }
    
    default FilteringResult show(EntryStack stack) {
        getShownStacks().add(new AmountIgnoredEntryStackWrapper(stack));
        return this;
    }
    
    default FilteringResult show(Collection<EntryStack> stacks) {
        getShownStacks().addAll(CollectionUtils.map(stacks, AmountIgnoredEntryStackWrapper::new));
        return this;
    }
    
    default FilteringResult hideW(AmountIgnoredEntryStackWrapper stack) {
        getHiddenStacks().add(stack);
        return this;
    }
    
    default FilteringResult hideW(Collection<AmountIgnoredEntryStackWrapper> stacks) {
        getHiddenStacks().addAll(stacks);
        return this;
    }
    
    default FilteringResult showW(AmountIgnoredEntryStackWrapper stack) {
        getShownStacks().add(stack);
        return this;
    }
    
    default FilteringResult showW(Collection<AmountIgnoredEntryStackWrapper> stacks) {
        getShownStacks().addAll(stacks);
        return this;
    }
}
