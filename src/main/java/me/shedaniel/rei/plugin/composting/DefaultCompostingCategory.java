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

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultCompostingCategory implements RecipeCategory<DefaultCompostingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.COMPOSTING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.COMPOSTER);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.composting");
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultCompostingDisplay recipe) {
        return new RecipeEntry() {
            @Override
            public int getHeight() {
                return 10 + MinecraftClient.getInstance().textRenderer.fontHeight;
            }
            
            @Nullable
            @Override
            public QueuedTooltip getTooltip(int mouseX, int mouseY) {
                return null;
            }
            
            @Override
            public void render(Rectangle rectangle, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().textRenderer.draw(I18n.translate("text.rei.composting.page", recipe.getPage() + 1), rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCompostingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        List<Widget> widgets = Lists.newArrayList();
        Point startingPoint = new Point(bounds.x + bounds.width - 55, bounds.y + 110);
        widgets.add(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                fillGradient(getBounds().x, getBounds().y, getBounds().getMaxX(), getBounds().getMaxY(), getInnerColor(), getInnerColor());
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                this.blit(startingPoint.x, startingPoint.y + 3, 28, 221, 55, 26);
            }
        });
        List<EntryStack> stacks = new LinkedList<>(recipeDisplaySupplier.get().getItemsByOrder());
        int i = 0;
        for (int y = 0; y < 6; y++)
            for (int x = 0; x < 8; x++) {
                int finalI = i;
                EntryStack entryStack = stacks.size() > i ? stacks.get(finalI) : EntryStack.empty();
                if (entryStack.getType() != EntryStack.Type.EMPTY)
                    for (Map.Entry<ItemConvertible, Float> entry : recipeDisplaySupplier.get().getInputMap().entrySet()) {
                        if (entry.getKey().asItem().equals(entryStack.getItem())) {
                            entryStack = entryStack.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(I18n.translate("text.rei.composting.chance", MathHelper.fastFloor(entry.getValue() * 100))));
                            break;
                        }
                    }
                widgets.add(EntryWidget.create(bounds.getCenterX() - 72 + x * 18, bounds.y + 3 + y * 18).entry(entryStack).markIsInput());
                i++;
            }
        widgets.add(EntryWidget.create(startingPoint.x + 33, startingPoint.y + 8).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground().markIsOutput());
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @Override
    public int getFixedRecipesPerPage() {
        return 1;
    }
}