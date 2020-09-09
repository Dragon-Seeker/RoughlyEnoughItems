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

import me.shedaniel.rei.impl.Internals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public interface ConfigManager {
    
    /**
     * @return the instance of {@link me.shedaniel.rei.api.ConfigManager}
     */
    @NotNull
    static ConfigManager getInstance() {
        return Internals.getConfigManager();
    }
    
    /**
     * Saves the config.
     */
    void saveConfig();
    
    /**
     * Gets if craftable only filter is enabled
     *
     * @return whether craftable only filter is enabled
     */
    boolean isCraftableOnlyEnabled();
    
    /**
     * Toggles the craftable only filter
     */
    void toggleCraftableOnly();
    
    /**
     * Opens the config screen
     *
     * @param parent the screen shown before
     */
    default void openConfigScreen(Screen parent) {
        Minecraft.getInstance().setScreen(getConfigScreen(parent));
    }
    
    /**
     * Gets the config screen
     *
     * @param parent the screen shown before
     * @return the config screen
     */
    Screen getConfigScreen(Screen parent);
    
    ConfigObject getConfig();
    
}
