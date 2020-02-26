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

package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.impl.*;
import me.shedaniel.rei.listeners.RecipeBookButtonWidgetHooks;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.tests.plugin.REITestPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApiStatus.Internal
public class RoughlyEnoughItemsCore implements ClientModInitializer {
    
    @ApiStatus.Internal public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelperImpl();
    private static final EntryRegistry ENTRY_REGISTRY = new EntryRegistryImpl();
    private static final DisplayHelper DISPLAY_HELPER = new DisplayHelperImpl();
    private static final Map<Identifier, REIPluginEntry> plugins = Maps.newHashMap();
    private static final ExecutorService SYNC_RECIPES = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "REI-SyncRecipes"));
    private static ConfigManager configManager;
    
    @ApiStatus.Internal
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    @ApiStatus.Internal
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    
    @ApiStatus.Internal
    public static EntryRegistry getEntryRegistry() {
        return ENTRY_REGISTRY;
    }
    
    @ApiStatus.Internal
    public static DisplayHelper getDisplayHelper() {
        return DISPLAY_HELPER;
    }
    
    /**
     * Registers a REI plugin
     *
     * @param plugin the plugin instance
     * @return the plugin itself
     * @deprecated Check REI wiki
     */
    @Deprecated
    public static REIPluginEntry registerPlugin(REIPluginEntry plugin) {
        plugins.put(plugin.getPluginIdentifier(), plugin);
        RoughlyEnoughItemsCore.LOGGER.debug("[REI] Registered plugin %s from %s", plugin.getPluginIdentifier().toString(), plugin.getClass().getSimpleName());
        return plugin;
    }
    
    public static List<REIPluginEntry> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<Identifier> getPluginIdentifier(REIPluginEntry plugin) {
        for (Identifier identifier : plugins.keySet())
            if (identifier != null && plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
    }
    
    public static boolean hasPermissionToUsePackets() {
        try {
            MinecraftClient.getInstance().getNetworkHandler().getCommandSource().hasPermissionLevel(0);
            return hasOperatorPermission() && canUsePackets();
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public static boolean hasOperatorPermission() {
        try {
            return MinecraftClient.getInstance().getNetworkHandler().getCommandSource().hasPermissionLevel(1);
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public static boolean canUsePackets() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET) && ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
    }
    
    @ApiStatus.Internal
    public static void syncRecipes(long[] lastSync) {
        if (lastSync != null) {
            if (lastSync[0] > 0 && System.currentTimeMillis() - lastSync[0] <= 5000) {
                RoughlyEnoughItemsCore.LOGGER.warn("[REI] Suppressing Sync Recipes!");
                return;
            }
            lastSync[0] = System.currentTimeMillis();
        }
        RecipeManager recipeManager = MinecraftClient.getInstance().getNetworkHandler().getRecipeManager();
        if (ConfigObject.getInstance().doesRegisterRecipesInAnotherThread()) {
            CompletableFuture.runAsync(() -> ((RecipeHelperImpl) RecipeHelper.getInstance()).recipesLoaded(recipeManager), SYNC_RECIPES);
        } else {
            ((RecipeHelperImpl) RecipeHelper.getInstance()).recipesLoaded(recipeManager);
        }
    }
    
    @ApiStatus.Internal
    public static boolean isDebugModeEnabled() {
        return System.getProperty("rei.test", "false").equals("true");
    }
    
    @Override
    public void onInitializeClient() {
        configManager = new ConfigManagerImpl();
        
        registerClothEvents();
        discoverPluginEntries();
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            //noinspection deprecation
            if (modContainer.getMetadata().containsCustomElement("roughlyenoughitems:plugins"))
                RoughlyEnoughItemsCore.LOGGER.error("[REI] REI plugin from " + modContainer.getMetadata().getId() + " is not loaded because it is too old!");
        }
        
        ClientSidePacketRegistry.INSTANCE.register(RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, (packetContext, packetByteBuf) -> {
            ItemStack stack = packetByteBuf.readItemStack();
            String player = packetByteBuf.readString(32767);
            packetContext.getPlayer().addChatMessage(new LiteralText(I18n.translate("text.rei.cheat_items").replaceAll("\\{item_name}", SearchArgument.tryGetItemStackName(stack.copy())).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)), false);
        });
        ClientSidePacketRegistry.INSTANCE.register(RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (currentScreen instanceof CraftingTableScreen) {
                RecipeBookWidget recipeBookGui = ((RecipeBookProvider) currentScreen).getRecipeBookWidget();
                RecipeBookGhostSlots ghostSlots = ((RecipeBookGuiHooks) recipeBookGui).rei_getGhostSlots();
                ghostSlots.reset();
                
                List<List<ItemStack>> input = Lists.newArrayList();
                int mapSize = packetByteBuf.readInt();
                for (int i = 0; i < mapSize; i++) {
                    List<ItemStack> list = Lists.newArrayList();
                    int count = packetByteBuf.readInt();
                    for (int j = 0; j < count; j++) {
                        list.add(packetByteBuf.readItemStack());
                    }
                    input.add(list);
                }
                
                ghostSlots.addSlot(Ingredient.ofItems(Items.STONE), 381203812, 12738291);
                CraftingTableContainer container = ((CraftingTableScreen) currentScreen).getContainer();
                for (int i = 0; i < input.size(); i++) {
                    List<ItemStack> stacks = input.get(i);
                    if (!stacks.isEmpty()) {
                        Slot slot = container.getSlot(i + container.getCraftingResultSlotIndex() + 1);
                        ghostSlots.addSlot(Ingredient.ofStacks(stacks.toArray(new ItemStack[0])), slot.xPosition, slot.yPosition);
                    }
                }
            }
        });
    }
    
    private void discoverPluginEntries() {
        for (REIPluginEntry reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins", REIPluginEntry.class)) {
            try {
                if (!REIPluginV0.class.isAssignableFrom(reiPlugin.getClass()))
                    throw new IllegalArgumentException("REI plugin is too old!");
                registerPlugin(reiPlugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Can't load REI plugins from %s: %s", reiPlugin.getClass(), e.getLocalizedMessage());
            }
        }
        for (REIPluginV0 reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins_v0", REIPluginV0.class)) {
            try {
                registerPlugin(reiPlugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Can't load REI plugins from %s: %s", reiPlugin.getClass(), e.getLocalizedMessage());
            }
        }
        
        // Test Only
        loadTestPlugins();
    }
    
    private void loadTestPlugins() {
        if (isDebugModeEnabled()) {
            registerPlugin(new REITestPlugin());
        }
    }
    
    private boolean shouldReturn(Class<?> screen) {
        for (OverlayDecider decider : DisplayHelper.getInstance().getAllOverlayDeciders()) {
            if (!decider.isHandingScreen(screen))
                continue;
            ActionResult result = decider.shouldScreenBeOverlayed(screen);
            if (result != ActionResult.PASS)
                return result == ActionResult.FAIL || ScreenHelper.getLastContainerScreenHooks() == null;
        }
        return true;
    }
    
    @SuppressWarnings("deprecation")
    private void registerClothEvents() {
        final Identifier recipeButtonTex = new Identifier("textures/gui/recipe_button.png");
        long[] lastSync = {-1};
        ClothClientHooks.SYNC_RECIPES.register((minecraftClient, recipeManager, synchronizeRecipesS2CPacket) -> syncRecipes(lastSync));
        ClothClientHooks.SCREEN_ADD_BUTTON.register((minecraftClient, screen, abstractButtonWidget) -> {
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof AbstractContainerScreen && abstractButtonWidget instanceof TexturedButtonWidget)
                if (((RecipeBookButtonWidgetHooks) abstractButtonWidget).rei_getTexture().equals(recipeButtonTex))
                    return ActionResult.FAIL;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof InventoryScreen && minecraftClient.interactionManager.hasCreativeInventory())
                return;
            if (shouldReturn(screen.getClass()))
                return;
            if (screen instanceof AbstractContainerScreen)
                ScreenHelper.setLastContainerScreen((AbstractContainerScreen<?>) screen);
            boolean alreadyAdded = false;
            for (Element element : Lists.newArrayList(screenHooks.cloth_getInputListeners()))
                if (ContainerScreenOverlay.class.isAssignableFrom(element.getClass()))
                    if (alreadyAdded)
                        screenHooks.cloth_getInputListeners().remove(element);
                    else
                        alreadyAdded = true;
            if (!alreadyAdded)
                screenHooks.cloth_getInputListeners().add(ScreenHelper.getLastOverlay(true, false));
        });
        ClothClientHooks.SCREEN_RENDER_POST.register((minecraftClient, screen, i, i1, v) -> {
            if (shouldReturn(screen.getClass()))
                return;
            ScreenHelper.getLastOverlay().render(i, i1, v);
        });
        ClothClientHooks.SCREEN_MOUSE_DRAGGED.register((minecraftClient, screen, v, v1, i, v2, v3) -> {
            if (shouldReturn(screen.getClass()))
                return ActionResult.PASS;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseDragged(v, v1, i, v2, v3))
                return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_CLICKED.register((minecraftClient, screen, v, v1, i) -> {
            if (screen instanceof CreativeInventoryScreen)
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(v, v1, i)) {
                    screen.setFocused(ScreenHelper.getLastOverlay());
                    if (i == 0)
                        screen.setDragging(true);
                    return ActionResult.SUCCESS;
                }
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_SCROLLED.register((minecraftClient, screen, v, v1, v2) -> {
            if (shouldReturn(screen.getClass()))
                return ActionResult.PASS;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseScrolled(v, v1, v2))
                return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_CHAR_TYPED.register((minecraftClient, screen, character, keyCode) -> {
            if (shouldReturn(screen.getClass()))
                return ActionResult.PASS;
            if (ScreenHelper.getLastOverlay().charTyped(character, keyCode))
                return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_LATE_RENDER.register((minecraftClient, screen, i, i1, v) -> {
            if (!ScreenHelper.isOverlayVisible())
                return;
            if (shouldReturn(screen.getClass()))
                return;
            ScreenHelper.getLastOverlay().lateRender(i, i1, v);
        });
        ClothClientHooks.SCREEN_KEY_PRESSED.register((minecraftClient, screen, i, i1, i2) -> {
            if (screen.getFocused() != null && screen.getFocused() instanceof TextFieldWidget || (screen.getFocused() instanceof RecipeBookWidget && ((RecipeBookGuiHooks) screen.getFocused()).rei_getSearchField() != null && ((RecipeBookGuiHooks) screen.getFocused()).rei_getSearchField().isFocused()))
                return ActionResult.PASS;
            if (shouldReturn(screen.getClass()))
                return ActionResult.PASS;
            if (ScreenHelper.getLastOverlay().keyPressed(i, i1, i2))
                return ActionResult.SUCCESS;
            if (screen instanceof AbstractContainerScreen && configManager.getConfig().doesDisableRecipeBook() && configManager.getConfig().doesFixTabCloseContainer())
                if (i == 258 && minecraftClient.options.keyInventory.matchesKey(i, i1)) {
                    minecraftClient.player.closeContainer();
                    return ActionResult.SUCCESS;
                }
            return ActionResult.PASS;
        });
    }
    
}
