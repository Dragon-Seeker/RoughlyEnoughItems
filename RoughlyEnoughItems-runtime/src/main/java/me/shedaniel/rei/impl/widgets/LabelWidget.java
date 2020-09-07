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

package me.shedaniel.rei.impl.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.clothconfig2.forge.api.LazyResettable;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Label;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LabelWidget extends Label {
    
    private boolean focused = false;
    private boolean clickable = false;
    private int horizontalAlignment = Label.CENTER;
    private boolean hasShadow = true;
    private boolean focusable = true;
    private int color = REIHelper.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : -1;
    private int hoveredColor = REIHelper.getInstance().isDarkThemeEnabled() ? -1 : 0xFF66FFCC;
    @NotNull private Point point;
    @Nullable private Function<Label, @Nullable String> tooltip;
    @Nullable private Consumer<Label> onClick;
    @Nullable private BiConsumer<MatrixStack, Label> onRender;
    @NotNull private ITextProperties text;
    @NotNull
    private final LazyResettable<IReorderingProcessor> orderedText = new LazyResettable<>(() -> LanguageMap.getInstance().getVisualOrder(getMessage()));
    
    public LabelWidget(@NotNull Point point, @NotNull ITextProperties text) {
        Objects.requireNonNull(this.point = point);
        Objects.requireNonNull(this.text = text);
    }
    
    @Override
    public final boolean isClickable() {
        return clickable;
    }
    
    @Override
    public final void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    
    @Nullable
    @Override
    public final Consumer<Label> getOnClick() {
        return onClick;
    }
    
    @Override
    public final void setOnClick(@Nullable Consumer<Label> onClick) {
        this.onClick = onClick;
    }
    
    @Nullable
    @Override
    public final BiConsumer<MatrixStack, Label> getOnRender() {
        return onRender;
    }
    
    @Override
    public final void setOnRender(@Nullable BiConsumer<MatrixStack, Label> onRender) {
        this.onRender = onRender;
    }
    
    @Override
    public final boolean isFocusable() {
        return focusable;
    }
    
    @Override
    public final void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }
    
    @Override
    @Nullable
    public final String getTooltip() {
        if (tooltip == null)
            return null;
        return tooltip.apply(this);
    }
    
    @Override
    public final void setTooltip(@Nullable Function<Label, @Nullable String> tooltip) {
        this.tooltip = tooltip;
    }
    
    @Override
    public final int getHorizontalAlignment() {
        return horizontalAlignment;
    }
    
    @Override
    public final void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }
    
    @Override
    public final boolean hasShadow() {
        return hasShadow;
    }
    
    @Override
    public final void setShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }
    
    @Override
    public final int getColor() {
        return color;
    }
    
    @Override
    public final void setColor(int color) {
        this.color = color;
    }
    
    @Override
    public final int getHoveredColor() {
        return hoveredColor;
    }
    
    @Override
    public final void setHoveredColor(int hoveredColor) {
        this.hoveredColor = hoveredColor;
    }
    
    @Override
    public final @NotNull Point getPoint() {
        return point;
    }
    
    @Override
    public final void setPoint(@NotNull Point point) {
        this.point = Objects.requireNonNull(point);
    }
    
    @Override
    public ITextProperties getMessage() {
        return text;
    }
    
    @Override
    public void setMessage(@NotNull ITextProperties message) {
        this.text = Objects.requireNonNull(message);
        this.orderedText.reset();
    }
    
    @NotNull
    @Override
    public final Rectangle getBounds() {
        int width = font.width(text);
        Point point = getPoint();
        if (getHorizontalAlignment() == LEFT_ALIGNED)
            return new Rectangle(point.x - 1, point.y - 5, width + 2, 14);
        if (getHorizontalAlignment() == RIGHT_ALIGNED)
            return new Rectangle(point.x - width - 1, point.y - 5, width + 2, 14);
        return new Rectangle(point.x - width / 2 - 1, point.y - 5, width + 2, 14);
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (getOnRender() != null)
            getOnRender().accept(matrices, this);
        int color = getColor();
        if (isClickable() && isHovered(mouseX, mouseY))
            color = getHoveredColor();
        Point pos = getPoint();
        int width = font.width(orderedText.get());
        switch (getHorizontalAlignment()) {
            case LEFT_ALIGNED:
                if (hasShadow())
                    font.drawShadow(matrices, orderedText.get(), pos.x, pos.y, color);
                else
                    font.draw(matrices, orderedText.get(), pos.x, pos.y, color);
                break;
            case RIGHT_ALIGNED:
                if (hasShadow())
                    font.drawShadow(matrices, orderedText.get(), pos.x - width, pos.y, color);
                else
                    font.draw(matrices, orderedText.get(), pos.x - width, pos.y, color);
                break;
            case CENTER:
            default:
                if (hasShadow())
                    font.drawShadow(matrices, orderedText.get(), pos.x - width / 2f, pos.y, color);
                else
                    font.draw(matrices, orderedText.get(), pos.x - width / 2f, pos.y, color);
                break;
        }
        if (isHovered(mouseX, mouseY)) {
            String tooltip = getTooltip();
            if (tooltip != null) {
                if (!focused && containsMouse(mouseX, mouseY))
                    Tooltip.create(Stream.of(tooltip.split("\n")).map(StringTextComponent::new).collect(Collectors.toList())).queue();
                else if (focused)
                    Tooltip.create(point, Stream.of(tooltip.split("\n")).map(StringTextComponent::new).collect(Collectors.toList())).queue();
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isClickable() && containsMouse(mouseX, mouseY)) {
            Widgets.produceClickSound();
            if (onClick != null)
                onClick.accept(this);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!isClickable() || !isFocusable() || !focused)
            return false;
        if (int_1 != 257 && int_1 != 32 && int_1 != 335)
            return false;
        Widgets.produceClickSound();
        if (onClick != null)
            onClick.accept(this);
        return true;
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!isClickable() || !isFocusable())
            return false;
        this.focused = !this.focused;
        return true;
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return containsMouse(mouseX, mouseY) || focused;
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return Collections.emptyList();
    }
}
