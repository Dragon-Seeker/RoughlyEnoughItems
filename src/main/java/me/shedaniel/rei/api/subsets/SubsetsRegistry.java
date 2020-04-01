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

package me.shedaniel.rei.api.subsets;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApiStatus.Experimental
public interface SubsetsRegistry {
    SubsetsRegistry INSTANCE = new SubsetsRegistryImpl();
    
    /**
     * Gets all paths an entry is in, note that this is a really slow call as it looks through all paths.
     */
    @NotNull
    List<String> getEntryPaths(@NotNull EntryStack stack);
    
    @Nullable
    Set<EntryStack> getPathEntries(@NotNull String path);
    
    @NotNull
    Set<EntryStack> getOrCreatePathEntries(@NotNull String path);
    
    @NotNull
    Set<String> getPaths();
    
    void registerPathEntry(@NotNull String path, @NotNull EntryStack stack);
    
    void registerPathEntries(@NotNull String path, @NotNull Collection<EntryStack> stacks);
    
    default void registerPathEntries(@NotNull String path, @NotNull EntryStack... stacks) {
        registerPathEntries(path, Arrays.asList(stacks));
    }
}
