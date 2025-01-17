/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import me.shedaniel.rei.impl.client.gui.screen.WarningAndErrorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.UnaryOperator;

public class ErrorDisplayer {
    public void onInitializeClient() {
        registerGuiInit((screen) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if ((!RoughlyEnoughItemsState.getErrors().isEmpty() || !RoughlyEnoughItemsState.getWarnings().isEmpty()) && !(screen instanceof WarningAndErrorScreen)) {
                WarningAndErrorScreen warningAndErrorScreen = new WarningAndErrorScreen("initialization", RoughlyEnoughItemsState.getWarnings(), RoughlyEnoughItemsState.getErrors(), (parent) -> {
                    if (RoughlyEnoughItemsState.getErrors().isEmpty()) {
                        RoughlyEnoughItemsState.clear();
                        RoughlyEnoughItemsState.continues();
                        Minecraft.getInstance().setScreen(parent);
                    } else {
                        Minecraft.getInstance().stop();
                    }
                });
                warningAndErrorScreen.setParent(screen);
                return warningAndErrorScreen;
            }
            
            return null;
        });
    }
    
    @ExpectPlatform
    public static void registerGuiInit(UnaryOperator<Screen> consumer) {
        throw new AssertionError();
    }
}
