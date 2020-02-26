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

package me.shedaniel.rei.gui;

import me.shedaniel.rei.api.RecipeHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConfigReloadingScreen extends Screen {
    
    private Screen parent;
    
    public ConfigReloadingScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        if (!RecipeHelper.getInstance().arePluginsLoading())
            minecraft.openScreen(parent);
        this.drawCenteredString(this.font, I18n.translate("text.rei.config.is.reloading"), this.width / 2, this.height / 2 - 50, 16777215);
        String string_3;
        switch ((int) (Util.getMeasuringTimeMs() / 300L % 4L)) {
            case 0:
            default:
                string_3 = "O o o";
                break;
            case 1:
            case 3:
                string_3 = "o O o";
                break;
            case 2:
                string_3 = "o o O";
        }
        this.drawCenteredString(this.font, string_3, this.width / 2, this.height / 2 - 41, 8421504);
        super.render(int_1, int_2, float_1);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
