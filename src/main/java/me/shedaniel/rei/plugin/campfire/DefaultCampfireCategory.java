/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.campfire;

import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeArrowWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultCampfireCategory implements RecipeCategory<DefaultCampfireDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CAMPFIRE;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.CAMPFIRE);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.campfire");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultCampfireDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);
        final double cookingTime = recipeDisplaySupplier.get().getCookTime();
        DecimalFormat df = new DecimalFormat("###.##");
        List<Widget> widgets = new LinkedList<>(Collections.singletonList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 177, 82, 34);
                int height = 14 - MathHelper.ceil(System.currentTimeMillis() / 250d % 14d);
                blit(startPoint.x + 2, startPoint.y + 31 + (3 - height), 82, 77 + (14 - height), 14, height);
                String text = I18n.translate("category.rei.campfire.time", df.format(cookingTime / 20d));
                int length = MinecraftClient.getInstance().textRenderer.getStringWidth(text);
                MinecraftClient.getInstance().textRenderer.draw(text, bounds.x + bounds.width - length - 5, bounds.y + 5, ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 0xFF404040);
            }
        }));
        widgets.add(RecipeArrowWidget.create(new Point(startPoint.x + 24, startPoint.y + 8), true).time(cookingTime * 50));
        widgets.add(EntryWidget.create(startPoint.x + 1, startPoint.y + 1).entries(recipeDisplaySupplier.get().getInputEntries().get(0)).markIsInput());
        widgets.add(EntryWidget.create(startPoint.x + 61, startPoint.y + 9).entries(recipeDisplaySupplier.get().getOutputEntries()).noBackground().markIsOutput());
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 49;
    }
}
