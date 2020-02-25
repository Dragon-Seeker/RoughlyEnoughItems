/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.brewing;

import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultBrewingCategory implements RecipeCategory<DefaultBrewingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.BREWING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.BREWING_STAND);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.brewing");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultBrewingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        final DefaultBrewingDisplay recipeDisplay = recipeDisplaySupplier.get();
        Point startPoint = new Point(bounds.getCenterX() - 52, bounds.getCenterY() - 29);
        List<Widget> widgets = new LinkedList<>(Collections.singletonList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 108, 103, 59);
                int width = MathHelper.ceil(System.currentTimeMillis() / 250d % 18d);
                blit(startPoint.x + 44, startPoint.y + 28, 103, 163, width, 4);
            }
        }));
        widgets.add(EntryWidget.create(startPoint.x + 1, startPoint.y + 1).entry(EntryStack.create(Items.BLAZE_POWDER)).noBackground().markIsInput());
        widgets.add(EntryWidget.create(startPoint.x + 40, startPoint.y + 1).entries(recipeDisplay.getInputEntries().get(0)).noBackground().markIsInput());
        widgets.add(EntryWidget.create(startPoint.x + 63, startPoint.y + 1).entries(recipeDisplay.getInputEntries().get(1)).noBackground().markIsInput());
        widgets.add(EntryWidget.create(startPoint.x + 40, startPoint.y + 35).entries(recipeDisplay.getOutput(0)).noBackground().markIsOutput());
        widgets.add(EntryWidget.create(startPoint.x + 63, startPoint.y + 42).entries(recipeDisplay.getOutput(1)).noBackground().markIsOutput());
        widgets.add(EntryWidget.create(startPoint.x + 86, startPoint.y + 35).entries(recipeDisplay.getOutput(2)).noBackground().markIsOutput());
        return widgets;
    }
    
}
