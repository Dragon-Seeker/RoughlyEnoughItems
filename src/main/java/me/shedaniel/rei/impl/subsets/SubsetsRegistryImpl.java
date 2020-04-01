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

package me.shedaniel.rei.impl.subsets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Experimental
@ApiStatus.Internal
public class SubsetsRegistryImpl implements SubsetsRegistry {
    private final Map<String, Set<EntryStack>> entryPaths = Maps.newHashMap();
    
    public void reset() {
        entryPaths.clear();
    }
    
    @Override
    public @NotNull List<String> getEntryPaths(@NotNull EntryStack stack) {
        List<String> strings = null;
        for (Map.Entry<String, Set<EntryStack>> entry : entryPaths.entrySet()) {
            if (CollectionUtils.findFirstOrNullEqualsEntryIgnoreAmount(entry.getValue(), stack) != null) {
                if (strings == null)
                    strings = Lists.newArrayList();
                strings.add(entry.getKey());
            }
        }
        return strings == null ? Collections.emptyList() : strings;
    }
    
    @Override
    public void registerPathEntry(@NotNull String path, @NotNull EntryStack stack) {
        getOrCreatePathEntries(path).add(stack.copy().setting(EntryStack.Settings.CHECK_AMOUNT, EntryStack.Settings.FALSE).setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
    }
    
    @Override
    public void registerPathEntries(@NotNull String path, @NotNull Collection<EntryStack> stacks) {
        Set<EntryStack> entries = getOrCreatePathEntries(path);
        for (EntryStack stack : stacks) {
            entries.add(stack.copy().setting(EntryStack.Settings.CHECK_AMOUNT, EntryStack.Settings.FALSE).setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE));
        }
    }
    
    @Nullable
    @Override
    public Set<EntryStack> getPathEntries(@NotNull String path) {
        if (!isPathValid(path))
            throw new IllegalArgumentException("Illegal path: " + path);
        return entryPaths.get(path);
    }
    
    @Override
    public @NotNull Set<String> getPaths() {
        return entryPaths.keySet();
    }
    
    @NotNull
    @Override
    public Set<EntryStack> getOrCreatePathEntries(@NotNull String path) {
        Set<EntryStack> paths = getPathEntries(path);
        if (paths == null) {
            entryPaths.put(path, Sets.newLinkedHashSet());
            paths = Objects.requireNonNull(getPathEntries(path));
        }
        return paths;
    }
    
    private boolean isPathValid(String path) {
        String[] pathSegments = path.split("/");
        for (String pathSegment : pathSegments) {
            if (!Identifier.isValid(pathSegment))
                return false;
        }
        return true;
    }
}
