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

package me.shedaniel.rei.impl;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.filtering.FilteringContextImpl;
import me.shedaniel.rei.impl.filtering.FilteringContextType;
import me.shedaniel.rei.impl.filtering.FilteringRule;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class EntryRegistryImpl implements EntryRegistry {
    
    private final List<EntryStack> preFilteredList = Lists.newCopyOnWriteArrayList();
    private final List<EntryStack> entries = Lists.newCopyOnWriteArrayList();
    @Nullable
    private List<AmountIgnoredEntryStackWrapper> reloadingRegistry;
    private boolean reloading;
    
    private static EntryStack findFirstOrNullEqualsEntryIgnoreAmount(Collection<EntryStack> list, EntryStack obj) {
        for (EntryStack t : list) {
            if (t.equalsIgnoreAmount(obj))
                return t;
        }
        return null;
    }
    
    public void finishReload() {
        reloading = false;
        preFilteredList.clear();
        reloadingRegistry.removeIf(AmountIgnoredEntryStackWrapper::isEmpty);
        entries.clear();
        entries.addAll(CollectionUtils.map(reloadingRegistry, AmountIgnoredEntryStackWrapper::unwrap));
        reloadingRegistry = null;
    }
    
    @Override
    @NotNull
    public Stream<EntryStack> getEntryStacks() {
        return entries.stream();
    }
    
    @Override
    @NotNull
    public List<EntryStack> getPreFilteredList() {
        return preFilteredList;
    }
    
    @Override
    @ApiStatus.Experimental
    public void refilter() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        FilteringContextImpl context = new FilteringContextImpl(entries);
        List<FilteringRule<?>> rules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
        Stopwatch innerStopwatch = Stopwatch.createStarted();
        for (int i = rules.size() - 1; i >= 0; i--) {
            innerStopwatch.reset().start();
            FilteringRule<?> rule = rules.get(i);
            context.handleResult(rule.processFilteredStacks(context));
            RoughlyEnoughItemsCore.LOGGER.debug("Refiltered rule [%s] in %s.", FilteringRule.REGISTRY.getKey(rule).toString(), innerStopwatch.stop().toString());
        }
        
        Set<AmountIgnoredEntryStackWrapper> hiddenStacks = context.stacks.get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            preFilteredList.clear();
            preFilteredList.addAll(entries);
        } else {
            preFilteredList.clear();
            preFilteredList.addAll(entries.parallelStream()
                    .map(AmountIgnoredEntryStackWrapper::new)
                    .filter(not(hiddenStacks::contains))
                    .map(AmountIgnoredEntryStackWrapper::unwrap)
                    .collect(Collectors.toList()));
        }
        
        RoughlyEnoughItemsCore.LOGGER.debug("Refiltered %d entries with %d rules in %s.", entries.size() - preFilteredList.size(), rules.size(), stopwatch.stop().toString());
    }
    
    static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>) target.negate();
    }
    
    public void resetToReloadStart() {
        entries.clear();
        if (reloadingRegistry != null)
            reloadingRegistry.clear();
        reloadingRegistry = Lists.newArrayListWithCapacity(Registry.ITEM.keySet().size() + 100);
        preFilteredList.clear();
        reloading = true;
    }
    
    @NotNull
    @Override
    public List<ItemStack> appendStacksForItem(@NotNull Item item) {
        NonNullList<ItemStack> list = NonNullList.create();
        item.fillItemCategory(item.getItemCategory(), list);
        if (list.isEmpty())
            return Collections.singletonList(item.getDefaultInstance());
        return list;
    }
    
    @NotNull
    @Override
    public ItemStack[] getAllStacksFromItem(@NotNull Item item) {
        List<ItemStack> list = appendStacksForItem(item);
        ItemStack[] array = list.toArray(new ItemStack[0]);
        Arrays.sort(array, (a, b) -> ItemStack.matches(a, b) ? 0 : 1);
        return array;
    }
    
    @Override
    public void registerEntryAfter(@Nullable EntryStack afterEntry, @NotNull EntryStack stack) {
        if (reloading) {
            int index = afterEntry != null ? reloadingRegistry.lastIndexOf(new AmountIgnoredEntryStackWrapper(afterEntry)) : -1;
            if (index >= 0) {
                reloadingRegistry.add(index, new AmountIgnoredEntryStackWrapper(stack));
            } else reloadingRegistry.add(new AmountIgnoredEntryStackWrapper(stack));
        } else {
            if (afterEntry != null) {
                int index = entries.lastIndexOf(afterEntry);
                entries.add(index, stack);
            } else entries.add(stack);
        }
    }
    
    @Override
    public void registerEntriesAfter(@Nullable EntryStack afterEntry, @NotNull Collection<@NotNull ? extends EntryStack> stacks) {
        if (reloading) {
            int index = afterEntry != null ? reloadingRegistry.lastIndexOf(new AmountIgnoredEntryStackWrapper(afterEntry)) : -1;
            if (index >= 0) {
                reloadingRegistry.addAll(index, CollectionUtils.map(stacks, AmountIgnoredEntryStackWrapper::new));
            } else reloadingRegistry.addAll(CollectionUtils.map(stacks, AmountIgnoredEntryStackWrapper::new));
        } else {
            if (afterEntry != null) {
                int index = entries.lastIndexOf(afterEntry);
                entries.addAll(index, stacks);
            } else entries.addAll(stacks);
        }
    }
    
    @Override
    public boolean alreadyContain(EntryStack stack) {
        if (reloading) {
            return reloadingRegistry.parallelStream().anyMatch(s -> s.unwrap().equalsAll(stack));
        }
        return entries.parallelStream().anyMatch(s -> s.equalsAll(stack));
    }
    
    @Override
    public void removeEntry(EntryStack stack) {
        if (reloading) {
            reloadingRegistry.remove(new AmountIgnoredEntryStackWrapper(stack));
        } else {
            entries.remove(stack);
        }
    }
    
    @Override
    public void removeEntryIf(Predicate<EntryStack> stackPredicate) {
        if (reloading) {
            reloadingRegistry.removeIf(wrapper -> stackPredicate.test(wrapper.unwrap()));
        } else {
            entries.removeIf(stackPredicate);
        }
    }
}
