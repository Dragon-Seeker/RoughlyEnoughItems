/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.jeicompat;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.hooks.forge.FluidStackHooksForge;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReason;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import me.shedaniel.rei.jeicompat.wrap.*;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;

public class JEIPluginDetector {
    private static final Renderer EMPTY_RENDERER = new Renderer() {
        @Override
        public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            
        }
        
        @Override
        public int getZ() {
            return 0;
        }
        
        @Override
        public void setZ(int z) {
            
        }
    };
    
    public static void detect(BiConsumer<Class<?>, BiConsumer<List<String>, Supplier<?>>> annotationScanner, Consumer<REIPluginProvider> pluginAdder) {
        annotationScanner.accept(JeiPlugin.class, (modIds, plugin) -> {
            Supplier<JEIPluginWrapper> value = () -> new JEIPluginWrapper(modIds, (IModPlugin) plugin.get());
            pluginAdder.accept(new JEIPluginProvider(modIds, value));
        });
    }
    
    public static RuntimeException TODO() {
        return new UnsupportedOperationException("This operation has not been implemented yet!");
    }
    
    public static RuntimeException WILL_NOT_BE_IMPLEMENTED() {
        return new UnsupportedOperationException("This operation will not be implemented in REI's JEI Compatibility Layer!");
    }
    
    public static Renderer wrapDrawable(IDrawable drawable) {
        if (drawable == null) return emptyRenderer();
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                matrices.pushPose();
                matrices.translate(0, 0, getZ());
                drawable.draw(matrices, bounds.x, bounds.y);
                matrices.popPose();
            }
        };
    }
    
    public static Renderer emptyRenderer() {
        return EMPTY_RENDERER;
    }
    
    public static IRecipeManagerPlugin wrapDefaultRecipeManagerPlugin() {
        return new IRecipeManagerPlugin() {
            @Override
            @NotNull
            public <V> List<ResourceLocation> getRecipeCategoryUids(@NotNull IFocus<V> focus) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <T, V> List<T> getRecipes(@NotNull IRecipeCategory<T> recipeCategory, @NotNull IFocus<V> focus) {
                throw TODO();
            }
            
            @Override
            @NotNull
            public <T> List<T> getRecipes(@NotNull IRecipeCategory<T> recipeCategory) {
                CategoryIdentifier<Display> categoryId = wrapCategoryId(recipeCategory.getUid());
                return wrapRecipes(categoryId, false);
            }
        };
    }
    
    public static <T> List<T> wrapRecipes(CategoryIdentifier<?> id, boolean checkVisible) {
        return wrapRecipes(CategoryRegistry.getInstance().get(id).getCategory(), DisplayRegistry.getInstance().get(id), checkVisible);
    }
    
    public static <A extends Display, T> List<T> wrapRecipes(DisplayCategory<?> category, List<A> displays, boolean checkVisible) {
        boolean isWrappedCategory = category instanceof JEIWrappedCategory;
        if (checkVisible && CategoryRegistry.getInstance().isCategoryInvisible(category)) return new ArrayList<>();
        if (isWrappedCategory) {
            return CollectionUtils.filterAndMap(displays, display -> !checkVisible || DisplayRegistry.getInstance().isDisplayVisible(display),
                    display -> ((JEIWrappedDisplay<T>) display).getBackingRecipe());
        } else if (checkVisible) {
            return (List<T>) CollectionUtils.filterAndMap(displays, display -> DisplayRegistry.getInstance().isDisplayVisible(display),
                    display -> DisplayRegistry.getInstance().getDisplayOrigin(display));
        } else {
            return (List<T>) CollectionUtils.map(displays, display -> DisplayRegistry.getInstance().getDisplayOrigin(display));
        }
    }
    
    public static <A extends Display> Object wrapRecipe(A display) {
        boolean isWrappedCategory = display instanceof JEIWrappedDisplay;
        if (isWrappedCategory) {
            return ((JEIWrappedDisplay<?>) display).getBackingRecipe();
        } else {
            return MoreObjects.firstNonNull(DisplayRegistry.getInstance().getDisplayOrigin(display), display);
        }
    }
    
    public static <T> Collection<Display> createDisplayFrom(T object) {
        return DisplayRegistry.getInstance().tryFillDisplay(object);
    }
    
    public static IRecipeCategory<?> unwrapCategory(DisplayCategory<?> category) {
        if (category instanceof JEIWrappedCategory) {
            return ((JEIWrappedCategory<?>) category).getBackingCategory();
        }
        return new JEIUnwrappedCategory<>(category);
    }
    
    public static <T> T unwrap(EntryStack<T> stack) {
        T value = stack.getValue();
        if (value instanceof me.shedaniel.architectury.fluid.FluidStack) {
            return (T) FluidStackHooksForge.toForge((me.shedaniel.architectury.fluid.FluidStack) value);
        }
        return value;
    }
    
    public static UidContext wrapContext(ComparisonContext context) {
        return context == ComparisonContext.FUZZY ? UidContext.Recipe : UidContext.Ingredient;
    }
    
    public static ComparisonContext wrapContext(UidContext context) {
        return context == UidContext.Recipe ? ComparisonContext.FUZZY : ComparisonContext.EXACT;
    }
    
    public static final Map<ResourceLocation, CategoryIdentifier<?>> CATEGORY_ID_MAP = new HashMap<>();
    
    static {
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "crafting"), BuiltinPlugin.CRAFTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "stonecutting"), BuiltinPlugin.STONE_CUTTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "furnace"), BuiltinPlugin.SMELTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "smoking"), BuiltinPlugin.SMOKING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "blasting"), BuiltinPlugin.BLASTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "campfire"), BuiltinPlugin.CAMPFIRE);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "brewing"), BuiltinPlugin.BREWING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "anvil"), BuiltinPlugin.ANVIL);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "smithing"), BuiltinPlugin.SMITHING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "compostable"), BuiltinPlugin.COMPOSTING);
        CATEGORY_ID_MAP.put(new ResourceLocation("minecraft", "information"), BuiltinPlugin.INFO);
    }
    
    public static <T extends Display> CategoryIdentifier<T> wrapCategoryId(ResourceLocation id) {
        CategoryIdentifier<?> identifier = CATEGORY_ID_MAP.get(id);
        if (identifier != null) return (CategoryIdentifier<T>) identifier;
        return CategoryIdentifier.of(id);
    }
    
    public static <T> EntryStack<T> wrap(IIngredientType<T> type, T stack) {
        return wrap(wrapEntryDefinition(type), stack);
    }
    
    public static <T> EntryStack<T> wrap(EntryDefinition<T> definition, T stack) {
        if (stack == null) return EntryStack.empty().cast();
        if (definition.getType() == VanillaEntryTypes.FLUID)
            return EntryStack.of(definition, (T) FluidStackHooksForge.fromForge((FluidStack) stack));
        return EntryStack.of(definition, stack);
    }
    
    public static <T> EntryIngredient wrapList(IIngredientType<T> type, List<T> stack) {
        return wrapList(wrapEntryDefinition(type), stack);
    }
    
    public static <T> EntryIngredient wrapList(EntryDefinition<T> definition, List<T> stack) {
        if (definition.getType() == VanillaEntryTypes.FLUID)
            return EntryIngredients.of(definition, CollectionUtils.filterAndMap(stack, Predicates.notNull(), s -> (T) FluidStackHooksForge.fromForge((FluidStack) s)));
        return EntryIngredients.of(definition, stack);
    }
    
    public static <T> EntryDefinition<T> wrapEntryDefinition(IIngredientType<T> type) {
        if (type.getIngredientClass() == FluidStack.class) {
            return VanillaEntryTypes.FLUID.getDefinition().cast();
        }
        for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
            if (Objects.equals(definition.getValueType(), type.getIngredientClass())) {
                return definition.cast();
            }
        }
        throw new IllegalArgumentException("Unknown JEI Ingredient Type! " + type.getIngredientClass().getName());
    }
    
    public static EntryStack<?> wrap(Object stack) {
        return wrap(findEntryDefinition(stack).cast(), stack);
    }
    
    public static EntryDefinition<?> findEntryDefinition(Object stack) {
        if (stack instanceof ItemStack) {
            return VanillaEntryTypes.ITEM.getDefinition();
        } else if (stack instanceof FluidStack) {
            return VanillaEntryTypes.FLUID.getDefinition();
        }
        for (EntryDefinition<?> definition : EntryTypeRegistry.getInstance().values()) {
            if (definition.getValueType().isInstance(stack)) {
                return definition.cast();
            }
        }
        throw new IllegalArgumentException("Failed to find EntryDefinition of " + stack + "!");
    }
    
    public static IRecipeCategoryRegistration wrapCategoryRegistration(CategoryRegistry registry, Consumer<JEIWrappedCategory<?>> added) {
        return new IRecipeCategoryRegistration() {
            @Override
            @NotNull
            public IJeiHelpers getJeiHelpers() {
                return JEIJeiHelpers.INSTANCE;
            }
            
            @Override
            public void addRecipeCategories(@NotNull IRecipeCategory<?> @NotNull ... categories) {
                for (IRecipeCategory<?> category : categories) {
                    JEIWrappedCategory<?> wrappedCategory = new JEIWrappedCategory<>(category);
                    registry.add(wrappedCategory);
                    added.accept(wrappedCategory);
                }
            }
        };
    }
    
    public static class JEIPluginProvider implements REIPluginProvider<REIClientPlugin> {
        private final List<String> modIds;
        public final Supplier<JEIPluginWrapper> supplier;
        public JEIPluginWrapper wrapper;
        
        public JEIPluginProvider(List<String> modIds, Supplier<JEIPluginWrapper> supplier) {
            this.modIds = modIds;
            this.supplier = supplier;
        }
        
        @Override
        public String getPluginProviderName() {
            if (wrapper != null) {
                return wrapper.getPluginProviderName();
            }
            
            return "JEI Plugin [" + String.join(", ", modIds) + "]";
        }
        
        @Override
        public Collection<REIClientPlugin> provide() {
            if (ConfigObject.getInstance().isJEICompatibilityLayerEnabled()) {
                if (wrapper != null) {
                    return Collections.singletonList(wrapper);
                } else {
                    return Collections.singletonList(wrapper = supplier.get());
                }
            }
            
            return Collections.emptyList();
        }
        
        @Override
        public Class<REIClientPlugin> getPluginProviderClass() {
            return REIClientPlugin.class;
        }
    }
    
    public static class JEIPluginWrapper implements REIClientPlugin {
        public final List<String> modIds;
        public final boolean mainThread;
        public final IModPlugin backingPlugin;
        
        public final Map<DisplayCategory<?>, List<Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>>>> categories = new HashMap<>();
        public final List<Runnable> post = new ArrayList<>();
        
        public JEIPluginWrapper(List<String> modIds, IModPlugin backingPlugin) {
            this.modIds = modIds;
            this.backingPlugin = backingPlugin;
            // JER why are you reloading twice
            this.mainThread = modIds.contains("jeresources");
        }
        
        @Override
        public void registerEntryTypes(EntryTypeRegistry registry) {
            backingPlugin.registerIngredients(new JEIModIngredientRegistration(this, registry));
        }
        
        @Override
        public void registerItemComparators(ItemComparatorRegistry registry) {
            backingPlugin.registerItemSubtypes(JEISubtypeRegistration.INSTANCE);
        }
        
        @Override
        public void registerFluidComparators(FluidComparatorRegistry registry) {
            backingPlugin.registerFluidSubtypes(JEISubtypeRegistration.INSTANCE);
        }
        
        @Override
        public void registerCategories(CategoryRegistry registry) {
            this.categories.clear();
            backingPlugin.registerCategories(wrapCategoryRegistration(registry, category -> {
                categories.put(category, new ArrayList<>());
                
                DisplayRegistry.getInstance().registerFiller((Class<Object>) category.getRecipeClass(), (o, reason) ->
                                ((JEIWrappedCategory<Object>) category).handlesRecipe(o) && !reason.has(DisplayAdditionReason.RECIPE_MANAGER),
                        recipe -> {
                            return new JEIWrappedDisplay<>((JEIWrappedCategory<Object>) category, recipe);
                        });
                DisplayRegistry.getInstance().registerFiller(JEIWrappedDisplay.class, display -> display.getCategoryIdentifier().getIdentifier().equals(category.getIdentifier()), Function.identity());
                
                post.add(() -> {
                    if (Recipe.class.isAssignableFrom(category.getRecipeClass())) {
                        DisplaySerializerRegistry.getInstance().register(category.getCategoryIdentifier(), new DisplaySerializer<JEIWrappedDisplay<?>>() {
                            @Override
                            public CompoundTag save(CompoundTag tag, JEIWrappedDisplay<?> display) {
                                Recipe<?> recipe = (Recipe<?>) display.getBackingRecipe();
                                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                                ((RecipeSerializer<Recipe<?>>) recipe.getSerializer()).toNetwork(buf, recipe);
                                tag.putString("serializer", Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer()).toString());
                                tag.putString("data", Base64.encodeBase64String(buf.array()));
                                tag.putString("id", recipe.getId().toString());
                                return tag;
                            }
                            
                            @Override
                            public JEIWrappedDisplay<?> read(CompoundTag tag) {
                                RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.get(new ResourceLocation(tag.getString("serializer")));
                                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copiedBuffer(Base64.decodeBase64(tag.getString("data"))));
                                Recipe<?> recipe = serializer.fromNetwork(new ResourceLocation(tag.getString("id")), buf);
                                return new JEIWrappedDisplay<>((JEIWrappedCategory<? super Recipe<?>>) category, recipe);
                            }
                            
                            @Override
                            public boolean isPersistent() {
                                return false;
                            }
                        });
                    } else {
                        DisplaySerializerRegistry.getInstance().registerNotSerializable(category.getCategoryIdentifier());
                    }
                });
            }));
            backingPlugin.registerRecipeCatalysts(JEIRecipeCatalystRegistration.INSTANCE);
            backingPlugin.registerVanillaCategoryExtensions(new JEIVanillaCategoryExtensionRegistration(this));
            if (!registry.getVisibilityPredicates().contains(JEIRecipeManager.INSTANCE.categoryPredicate)) {
                registry.registerVisibilityPredicate(JEIRecipeManager.INSTANCE.categoryPredicate);
            }
        }
        
        @Override
        public void registerDisplays(DisplayRegistry registry) {
            backingPlugin.registerRecipes(new JEIRecipeRegistration(post));
            backingPlugin.registerAdvanced(JEIAdvancedRegistration.INSTANCE);
            if (!registry.getVisibilityPredicates().contains(JEIRecipeManager.INSTANCE.displayPredicate)) {
                registry.registerVisibilityPredicate(JEIRecipeManager.INSTANCE.displayPredicate);
            }
        }
        
        @Override
        public void registerScreens(ScreenRegistry registry) {
            backingPlugin.registerGuiHandlers(JEIGuiHandlerRegistration.INSTANCE);
        }
        
        @Override
        public void registerTransferHandlers(TransferHandlerRegistry registry) {
            backingPlugin.registerRecipeTransferHandlers(new JEIRecipeTransferRegistration(post::add));
        }
        
        @Override
        public void postRegister() {
            for (Map.Entry<DisplayCategory<?>, List<Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>>>> entry : categories.entrySet()) {
                DisplayCategory<?> category = entry.getKey();
                for (Triple<Class<?>, Predicate<Object>, Function<Object, IRecipeCategoryExtension>> pair : entry.getValue()) {
//                    DisplayRegistry.getInstance().registerFiller(pair.getLeft(), pair.getMiddle(), );
                }
            }
            backingPlugin.onRuntimeAvailable(JEIJeiRuntime.INSTANCE);
            for (Runnable runnable : post) {
                runnable.run();
            }
            post.clear();
        }
        
        @Override
        public String getPluginProviderName() {
            return "JEI Plugin [" + backingPlugin.getPluginUid() + "]";
        }
        
        @Override
        public boolean shouldBeForcefullyDoneOnMainThread(Reloadable<?> reloadable) {
            return mainThread;
        }
    }
}
