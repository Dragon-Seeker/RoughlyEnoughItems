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

package me.shedaniel.rei.jeicompat.wrap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.utils.value.Value;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoProvider;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationProviderContext;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.transfer.JEIRecipeTransferData;
import me.shedaniel.rei.jeicompat.transfer.JEITransferMenuInfo;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeTransferRegistration implements IRecipeTransferRegistration {
    private final Consumer<Runnable> post;
    
    public JEIRecipeTransferRegistration(Consumer<Runnable> post) {
        this.post = post;
    }
    
    @Override
    @NotNull
    public IJeiHelpers getJeiHelpers() {
        return JEIJeiHelpers.INSTANCE;
    }
    
    @Override
    @NotNull
    public IRecipeTransferHandlerHelper getTransferHelper() {
        return JEIRecipeTransferHandlerHelper.INSTANCE;
    }
    
    @Override
    public <C extends AbstractContainerMenu> void addRecipeTransferHandler(Class<C> containerClass, ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        addRecipeTransferHandler(new IRecipeTransferInfo<C, Object>() {
            @Override
            public Class<C> getContainerClass() {
                return containerClass;
            }
            
            @Override
            public Class<Object> getRecipeClass() {
                return Object.class;
            }
            
            @Override
            public ResourceLocation getRecipeCategoryUid() {
                return recipeCategoryUid;
            }
            
            @Override
            public boolean canHandle(C container, Object recipe) {
                return getContainerClass().isInstance(container);
            }
            
            @Override
            public List<net.minecraft.world.inventory.Slot> getRecipeSlots(C container, Object recipe) {
                return IntStream.range(recipeSlotStart, recipeSlotStart + recipeSlotCount)
                        .mapToObj(container::getSlot)
                        .collect(Collectors.toList());
            }
            
            @Override
            public List<net.minecraft.world.inventory.Slot> getInventorySlots(C container, Object recipe) {
                return IntStream.range(inventorySlotStart, inventorySlotStart + inventorySlotCount)
                        .mapToObj(container::getSlot)
                        .collect(Collectors.toList());
            }
        });
    }
    
    @Override
    public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> info) {
        post.accept(() -> {
            MenuInfoRegistry.getInstance().register(info.getRecipeCategoryUid().categoryId(), info.getContainerClass(),
                    new MenuInfoProvider<C, Display>() {
                        @Override
                        public Optional<MenuInfo<C, Display>> provideClient(Display display, C menu) {
                            return Optional.of(new JEITransferMenuInfo<>(new JEIRecipeTransferData<>(info, menu, (R) display.jeiValue())));
                        }
                        
                        @Override
                        public Optional<MenuInfo<C, Display>> provide(CategoryIdentifier<Display> display, C menu, MenuSerializationProviderContext<C, ?, Display> context, CompoundTag networkTag) {
                            return Optional.of(new JEITransferMenuInfo<>(JEIRecipeTransferData.read(menu, networkTag)));
                        }
                        
                        @Override
                        public Optional<MenuInfo<C, Display>> provide(CategoryIdentifier<Display> categoryId, Class<C> menuClass) {
                            throw new UnsupportedOperationException();
                        }
                    });
        });
    }
    
    @Override
    public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, ResourceLocation recipeCategoryUid) {
        TransferHandlerRegistry.getInstance().register(context -> {
            if (recipeTransferHandler.getContainerClass().isInstance(context.getMenu())) {
                Display display = context.getDisplay();
                if (recipeCategoryUid == null || display.getCategoryIdentifier().getIdentifier().equals(recipeCategoryUid)) {
                    IRecipeLayout layout;
                    Value<IDrawable> background = new Value<IDrawable>() {
                        @Override
                        public void accept(IDrawable iDrawable) {
                        }
                        
                        @Override
                        public IDrawable get() {
                            return JEIGuiHelper.INSTANCE.createBlankDrawable(0, 0);
                        }
                    };
                    if (display instanceof JEIWrappedDisplay) {
                        layout = ((JEIWrappedDisplay<Object>) display).getBackingCategory().createLayout((JEIWrappedDisplay<Object>) display, background);
                    } else {
                        DisplayCategory<Display> category = CategoryRegistry.getInstance().get(display.getCategoryIdentifier().cast()).getCategory();
                        DisplayCategoryView<Display> categoryView = CategoryRegistry.getInstance().get(display.getCategoryIdentifier().cast()).getView(display);
                        layout = new JEIWrappingRecipeLayout<>(category, background);
                        List<Widget> widgets = categoryView.setupDisplay(display, new Rectangle(0, 0, category.getDisplayWidth(display), category.getDisplayHeight()));
                        addToLayout(layout, widgets, 4, 4);
                    }
                    if (context.isActuallyCrafting()) {
                        context.getMinecraft().setScreen(context.getContainerScreen());
                    }
                    IRecipeTransferError error = ((IRecipeTransferHandler<AbstractContainerMenu, Object>) recipeTransferHandler).transferRecipe(context.getMenu(), context.getDisplay().jeiValue(), layout, context.getMinecraft().player, context.isStackedCrafting(), context.isActuallyCrafting());
                    if (error == null) {
                        return TransferHandler.Result.createSuccessful();
                    } else if (error instanceof IRecipeTransferError) {
                        IRecipeTransferError.Type type = error.getType();
                        if (type == IRecipeTransferError.Type.INTERNAL) {
                            return TransferHandler.Result.createNotApplicable();
                        }
                        TransferHandler.Result result = type == IRecipeTransferError.Type.COSMETIC ? TransferHandler.Result.createSuccessful()
                                : TransferHandler.Result.createFailed(error instanceof JEIRecipeTransferError ? ((JEIRecipeTransferError) error).getText() : new TextComponent(""));
                        
                        if (error instanceof JEIRecipeTransferError) {
                            JEIRecipeTransferError transferError = (JEIRecipeTransferError) error;
                            IntArrayList redSlots = transferError.getRedSlots();
                            if (redSlots == null) redSlots = new IntArrayList();
                            return result.renderer(TransferHandlerRenderer.forRedSlots(redSlots));
                        } else {
                            return result
                                    .overrideTooltipRenderer((point, tooltipSink) -> {})
                                    .renderer((matrices, mouseX, mouseY, delta, widgets, bounds, d) -> {
                                        error.showError(matrices, mouseX, mouseY, layout, bounds.x + 4, bounds.y + 4);
                                    });
                        }
                    }
                }
            }
            return TransferHandler.Result.createNotApplicable();
        });
    }
    
    private void addToLayout(IRecipeLayout layout, List<Widget> entries, int xOffset, int yOffset) {
        Map<Boolean, List<Pair<Slot, Multimap<EntryType<?>, EntryStack<?>>>>> groups = new HashMap<>();
        for (Widget widget : entries) {
            if (widget instanceof Slot) {
                Multimap<EntryType<?>, EntryStack<?>> group = HashMultimap.create();
                List<EntryStack<?>> ingredient = ((Slot) widget).getEntries();
                for (EntryStack<?> stack : ingredient) {
                    group.put(stack.getType(), stack);
                }
                groups.computeIfAbsent(((Slot) widget).getNoticeMark() != Slot.OUTPUT, $ -> new ArrayList<>())
                        .add(Pair.of((Slot) widget, group));
            }
        }
        for (Map.Entry<Boolean, List<Pair<Slot, Multimap<EntryType<?>, EntryStack<?>>>>> entry : groups.entrySet()) {
            entry.getValue().stream().map(Pair::getRight).map(Multimap::keys).flatMap(Collection::stream)
                    .distinct().forEach(type -> {
                        IGuiIngredientGroup<Object> group = layout.getIngredientsGroup((IIngredientType<Object>) type.getDefinition().jeiType());
                        int[] i = new int[]{getNextId(group.getGuiIngredients().keySet())};
                        for (Pair<Slot, Multimap<EntryType<?>, EntryStack<?>>> pair : entry.getValue()) {
                            Slot slot = pair.getLeft();
                            Collection<EntryStack<?>> stacks = pair.getRight().get(type);
                            group.set(i[0], CollectionUtils.map(stacks, JEIPluginDetector::jeiValue));
                            group.init(i[0], entry.getKey(), slot.getBounds().x - xOffset, slot.getBounds().y - yOffset);
                            i[0]++;
                        }
                    });
        }
    }
    
    private int getNextId(Set<Integer> keys) {
        for (int i = 0; ; i++) {
            if (!keys.contains(i)) {
                return i;
            }
        }
    }
    
    @Override
    public <C extends AbstractContainerMenu, R> void addUniversalRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler) {
        addRecipeTransferHandler(recipeTransferHandler, null);
    }
}
