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

package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BaseBoundsHandler extends DisplayHelper.DisplayBoundsHandler<Screen> {
    
    static BaseBoundsHandler getInstance() {
        return DisplayHelper.getInstance().getBaseBoundsHandler();
    }
    
    /**
     * Gets the exclusion zones by the screen class
     *
     * @param currentScreenClass the current screen class
     * @param isOnRightSide      whether the user has set the overlay to the right
     * @return the list of exclusion zones
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide) {
        return getExclusionZones(currentScreenClass, false);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide, boolean sort) {
        return getExclusionZones(currentScreenClass, sort);
    }
    
    List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort);
    
    int supplierSize();
    
    /**
     * Register an exclusion zone
     *
     * @param screenClass the screen
     * @param supplier    the exclusion zone supplier, isOnRightSide -> the list of exclusion zones
     * @see #registerExclusionZones(Class, Supplier) for non deprecated version
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default void registerExclusionZones(Class<?> screenClass, Function<Boolean, List<Rectangle>> supplier) {
        RoughlyEnoughItemsCore.LOGGER.warn("[REI] Someone is registering exclusion zones with the deprecated method: " + supplier.getClass().getName());
        registerExclusionZones(screenClass, () -> {
            List<Rectangle> zones = Lists.newArrayList(supplier.apply(false));
            zones.addAll(supplier.apply(true));
            return zones;
        });
    }
    
    /**
     * Register an exclusion zone
     *
     * @param screenClass the screen
     * @param supplier    the exclusion zone supplier, returns the list of exclusion zones
     */
    void registerExclusionZones(Class<?> screenClass, Supplier<List<Rectangle>> supplier);
    
}
