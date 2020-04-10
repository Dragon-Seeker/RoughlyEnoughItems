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

import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Lazy;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.net.URISyntaxException;

@ApiStatus.Internal
public class FailedToLoadScreen extends Screen {
    public static final Lazy<FailedToLoadScreen> INSTANCE = new Lazy<>(FailedToLoadScreen::new);
    private AbstractButtonWidget buttonExit;
    private StringEntryListWidget listWidget;
    
    private FailedToLoadScreen() {
        super(new LiteralText("REI has failed to init"));
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    protected void init() {
        children.add(listWidget = new StringEntryListWidget(minecraft, width, height, 32, height - 32));
        listWidget.max = 80;
        listWidget.creditsClearEntries();
        listWidget.creditsAddEntry(new EmptyItem());
        for (Pair<String, String> pair : RoughlyEnoughItemsState.getFailedToLoad()) {
            listWidget.creditsAddEntry(new TextItem(pair.getLeft()));
            if (pair.getRight() != null)
                listWidget.creditsAddEntry(this.new LinkItem(pair.getRight()));
            for (int i = 0; i < 2; i++) {
                listWidget.creditsAddEntry(new EmptyItem());
            }
        }
        for (StringItem child : listWidget.children()) {
            listWidget.max = Math.max(listWidget.max, child.getWidth());
        }
        children.add(buttonExit = new ButtonWidget(width / 2 - 100, height - 26, 200, 20, "Exit", button -> {
            MinecraftClient.getInstance().scheduleStop();
        }));
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        return listWidget.mouseScrolled(double_1, double_2, double_3) || super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        this.listWidget.render(int_1, int_2, float_1);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
        this.buttonExit.render(int_1, int_2, float_1);
    }
    
    private static class StringEntryListWidget extends DynamicNewSmoothScrollingEntryListWidget<StringItem> {
        private boolean inFocus;
        private int max = 80;
        
        public StringEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY) {
            super(client, width, height, startY, endY, DrawableHelper.BACKGROUND_LOCATION);
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            if (!this.inFocus && this.getItemCount() == 0) {
                return false;
            } else {
                this.inFocus = !this.inFocus;
                if (this.inFocus && this.getFocused() == null && this.getItemCount() > 0) {
                    this.moveSelection(1);
                } else if (this.inFocus && this.getFocused() != null) {
                    this.moveSelection(0);
                }
                
                return this.inFocus;
            }
        }
        
        public void creditsClearEntries() {
            clearItems();
        }
        
        private StringItem rei_getEntry(int int_1) {
            return this.children().get(int_1);
        }
        
        public void creditsAddEntry(StringItem entry) {
            addItem(entry);
        }
        
        @Override
        public int getItemWidth() {
            return max;
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 40;
        }
    }
    
    private abstract static class StringItem extends DynamicNewSmoothScrollingEntryListWidget.Entry<StringItem> {
        public abstract int getWidth();
    }
    
    private static class EmptyItem extends StringItem {
        @Override
        public void render(int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, float v) {
        
        }
        
        @Override
        public int getItemHeight() {
            return 5;
        }
        
        @Override
        public int getWidth() {
            return 0;
        }
    }
    
    private static class TextItem extends StringItem {
        private String text;
        
        public TextItem(Text textComponent) {
            this(textComponent.asFormattedString());
        }
        
        public TextItem(String text) {
            this.text = text;
        }
        
        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(text, x + 5, y, -1);
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
        
        @Override
        public int getWidth() {
            return MinecraftClient.getInstance().textRenderer.getStringWidth(text) + 10;
        }
    }
    
    private class LinkItem extends StringItem {
        private String text;
        private boolean contains;
        
        public LinkItem(Text textComponent) {
            this(textComponent.asFormattedString());
        }
        
        public LinkItem(String text) {
            this.text = text;
        }
        
        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            contains = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;
            if (contains) {
                FailedToLoadScreen.this.renderTooltip("Click to open link.", mouseX, mouseY);
                MinecraftClient.getInstance().textRenderer.drawWithShadow("§n" + text, x + 5, y, 0xff1fc3ff);
            } else {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(text, x + 5, y, 0xff1fc3ff);
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
        
        @Override
        public int getWidth() {
            return MinecraftClient.getInstance().textRenderer.getStringWidth(text) + 10;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (contains && button == 0) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                try {
                    Util.getOperatingSystem().open(new URI(text));
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
