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

package me.shedaniel.rei.api.client.registry.transfer;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.display.Display;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Handler for display transfer, only executed on the client, implementations of this handler should sync recipes to the server to transfer recipes themselves.
 * <p>
 * REI provides a {@link TransferHandler} that handles displays that are provided with {@link me.shedaniel.rei.api.common.transfer.info.MenuInfo},
 * as a lightweight and simple way to implement recipe transfers.
 *
 * @see TransferHandlerRegistry
 * @see me.shedaniel.rei.api.common.transfer.info.MenuInfo
 */
@Environment(EnvType.CLIENT)
public interface TransferHandler extends Comparable<TransferHandler> {
    /**
     * @return the priority of this handler, higher priorities will be called first.
     */
    default double getPriority() {
        return 0d;
    }
    
    Result handle(Context context);
    
    @Override
    default int compareTo(TransferHandler o) {
        return Double.compare(getPriority(), o.getPriority());
    }
    
    @Deprecated
    @Environment(EnvType.CLIENT)
    @Nullable
    @ApiStatus.ScheduledForRemoval
    default TransferHandlerErrorRenderer provideErrorRenderer(Context context, Object data) {
        return null;
    }
    
    @ApiStatus.NonExtendable
    interface Result {
        /**
         * Creates a successful result, no further handlers will be called.
         */
        static Result createSuccessful() {
            return new ResultImpl().color(0x00000000);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         * This will also mark the handler as not applicable.
         */
        static Result createNotApplicable() {
            return new ResultImpl(false).color(0x00000000);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         *
         * @param error The error itself
         */
        static Result createFailed(Component error) {
            return new ResultImpl(error, 0x67ff0000);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         * The special color will be applied if this is the last handler.
         *
         * @param error The error itself
         * @param color A special color for the button
         */
        static Result createFailedCustomButtonColor(Component error, int color) {
            return createFailed(error).color(color);
        }
        
        /**
         * Forces this handler to be the last handler, no further handlers will be called.
         */
        default Result blocksFurtherHandling() {
            return blocksFurtherHandling(true);
        }
        
        /**
         * Forces this handler to be the last handler, no further handlers will be called.
         */
        Result blocksFurtherHandling(boolean returnsToScreen);
        
        /**
         * @return the color in which the button should be displayed in.
         */
        int getColor();
        
        /**
         * Sets the color in which the button should be displayed in.
         */
        Result color(int color);
        
        /**
         * Sets the error data, to be passed to {@link TransferHandler#provideErrorRenderer(Context, Object)}.
         */
        @Deprecated
        Result errorRenderer(Object data);
        
        Result renderer(TransferHandlerRenderer renderer);
        
        @ApiStatus.Experimental
        Result overrideTooltipRenderer(BiConsumer<Point, TooltipSink> renderer);
        
        /**
         * @return whether this handler has successfully handled the transfer.
         */
        boolean isSuccessful();
        
        /**
         * @return whether this handler should be the last handler.
         */
        boolean isBlocking();
        
        /**
         * Applicable if {@link #isSuccessful()} is true.
         *
         * @return whether to return to the previous screen rather than staying open
         */
        boolean isReturningToScreen();
        
        /**
         * @return whether the handler is applicable.
         */
        boolean isApplicable();
        
        /**
         * Applicable if {@link #isSuccessful()} is false.
         *
         * @return the error message
         */
        Component getError();
        
        @Environment(EnvType.CLIENT)
        @ApiStatus.Internal
        TransferHandlerRenderer getRenderer(TransferHandler handler, Context context);
        
        @Environment(EnvType.CLIENT)
        @ApiStatus.Internal
        BiConsumer<Point, TooltipSink> getTooltipRenderer();
        
        @ApiStatus.Internal
        void fillTooltip(List<Component> components);
        
        @FunctionalInterface
        interface TooltipSink {
            void accept(Tooltip tooltip);
        }
    }
    
    @ApiStatus.NonExtendable
    interface Context {
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        static Context create(boolean actuallyCrafting, @Nullable AbstractContainerScreen<?> containerScreen, Display display) {
            return create(actuallyCrafting, Screen.hasShiftDown(), containerScreen, display);
        }
        
        static Context create(boolean actuallyCrafting, boolean stackedCrafting, @Nullable AbstractContainerScreen<?> containerScreen, Display display) {
            return new ContextImpl(actuallyCrafting, stackedCrafting, containerScreen, () -> display);
        }
        
        default Minecraft getMinecraft() {
            return Minecraft.getInstance();
        }
        
        /**
         * Returns whether we should actually move the items.
         *
         * @return whether we should actually move the items.
         */
        boolean isActuallyCrafting();
        
        boolean isStackedCrafting();
        
        Display getDisplay();
        
        @Nullable
        AbstractContainerScreen<?> getContainerScreen();
        
        @Nullable
        default AbstractContainerMenu getMenu() {
            return getContainerScreen() == null ? null : getContainerScreen().getMenu();
        }
    }
    
    @ApiStatus.Internal
    final class ResultImpl implements Result {
        private boolean successful, applicable, returningToScreen, blocking;
        private Component error;
        private Object errorRenderer;
        private BiConsumer<Point, TooltipSink> tooltipRenderer;
        private int color;
        
        private ResultImpl() {
            this(true, true);
        }
        
        public ResultImpl(boolean applicable) {
            this(false, applicable);
        }
        
        public ResultImpl(boolean successful, boolean applicable) {
            this.successful = successful;
            this.applicable = applicable;
        }
        
        public ResultImpl(Component error, int color) {
            this.successful = false;
            this.applicable = true;
            this.error = error;
            this.color = color;
        }
        
        @Override
        public Result blocksFurtherHandling(boolean returningToScreen) {
            this.blocking = true;
            this.returningToScreen = returningToScreen;
            return this;
        }
        
        @Override
        public int getColor() {
            return color;
        }
        
        @Override
        public TransferHandler.Result color(int color) {
            this.color = color;
            return this;
        }
        
        @Override
        public Result errorRenderer(Object data) {
            this.errorRenderer = data;
            return this;
        }
        
        @Override
        public Result renderer(TransferHandlerRenderer renderer) {
            this.errorRenderer = renderer;
            return this;
        }
        
        @Override
        public Result overrideTooltipRenderer(BiConsumer<Point, TooltipSink> renderer) {
            this.tooltipRenderer = renderer;
            return this;
        }
        
        @Override
        public boolean isSuccessful() {
            return successful;
        }
        
        @Override
        public boolean isBlocking() {
            return successful || blocking;
        }
        
        @Override
        public boolean isApplicable() {
            return applicable;
        }
        
        @Override
        public boolean isReturningToScreen() {
            return returningToScreen;
        }
        
        @Override
        public Component getError() {
            return error;
        }
        
        @Override
        public TransferHandlerRenderer getRenderer(TransferHandler handler, Context context) {
            if (errorRenderer == null) return null;
            if (errorRenderer instanceof TransferHandlerRenderer) return (TransferHandlerRenderer) errorRenderer;
            if (isSuccessful()) return null;
            TransferHandlerErrorRenderer renderer = handler.provideErrorRenderer(context, this.errorRenderer);
            return renderer == null ? null : renderer.asNew();
        }
        
        @Override
        public BiConsumer<Point, TooltipSink> getTooltipRenderer() {
            return tooltipRenderer;
        }
        
        @Override
        public void fillTooltip(List<Component> components) {
            if (!isSuccessful() && isApplicable()) {
                components.add(getError());
            }
        }
    }
    
    @ApiStatus.Internal
    final class ContextImpl implements Context {
        private boolean actuallyCrafting;
        private boolean stackedCrafting;
        private AbstractContainerScreen<?> containerScreen;
        private Supplier<Display> recipeDisplaySupplier;
        
        private ContextImpl(boolean actuallyCrafting, boolean stackedCrafting, AbstractContainerScreen<?> containerScreen, Supplier<Display> recipeDisplaySupplier) {
            this.actuallyCrafting = actuallyCrafting;
            this.stackedCrafting = stackedCrafting;
            this.containerScreen = containerScreen;
            this.recipeDisplaySupplier = recipeDisplaySupplier;
        }
        
        @Override
        public boolean isActuallyCrafting() {
            return actuallyCrafting;
        }
        
        @Override
        public boolean isStackedCrafting() {
            return stackedCrafting;
        }
        
        @Override
        public AbstractContainerScreen<?> getContainerScreen() {
            return containerScreen;
        }
        
        @Override
        public Display getDisplay() {
            return recipeDisplaySupplier.get();
        }
    }
}
