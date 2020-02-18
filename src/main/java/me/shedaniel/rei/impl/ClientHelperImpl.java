/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.FakeModifierKeyCodeAdder;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public class ClientHelperImpl implements ClientHelper, ClientModInitializer {
    
    public static ClientHelperImpl instance;
    @ApiStatus.Internal public final Lazy<Boolean> isYog = new Lazy<>(() -> {
        try {
            if (MinecraftClient.getInstance().getSession().getProfile().getId().equals(UUID.fromString("f9546389-9415-4358-9c29-2c26b25bff5b")))
                return true;
        } catch (Throwable ignored) {
        }
        return false;
    });
    @ApiStatus.Internal public final Lazy<Boolean> ok = new Lazy<>(() -> {
        try {
            if (isYog.get())
                return true;
            LocalDateTime now = LocalDateTime.now();
            return now.getMonthValue() == 4 && now.getDayOfMonth() == 1;
        } catch (Throwable ignored) {
        }
        return false;
    });
    private final Map<String, String> modNameCache = Maps.newHashMap();
    
    @Override
    public String getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.isEmpty())
            return "";
        return Formatting.BLUE.toString() + Formatting.ITALIC.toString() + mod;
    }
    
    @Override
    public String getFormattedModFromIdentifier(Identifier identifier) {
        String mod = getModFromIdentifier(identifier);
        if (mod.isEmpty())
            return "";
        return Formatting.BLUE.toString() + Formatting.ITALIC.toString() + mod;
    }
    
    @Override
    public String getModFromItem(Item item) {
        if (item.equals(Items.AIR))
            return "";
        return getModFromIdentifier(Registry.ITEM.getId(item));
    }
    
    @Override
    public String getModFromModId(String modid) {
        if (modid == null)
            return "";
        String any = modNameCache.getOrDefault(modid, null);
        if (any != null)
            return any;
        String s = FabricLoader.getInstance().getModContainer(modid).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modid);
        modNameCache.put(modid, s);
        return s;
    }
    
    @Override
    public boolean isCheating() {
        return ConfigObject.getInstance().isCheating();
    }
    
    @Override
    public void setCheating(boolean cheating) {
        ConfigObject.getInstance().setCheating(cheating);
        ConfigManager.getInstance().saveConfig();
    }
    
    @Override
    public void sendDeletePacket() {
        if (ScreenHelper.getLastContainerScreen() instanceof CreativeInventoryScreen) {
            MinecraftClient.getInstance().player.inventory.setCursorStack(ItemStack.EMPTY);
            return;
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()));
    }
    
    @Override
    public boolean tryCheatingEntry(EntryStack entry) {
        if (entry.getType() != EntryStack.Type.ITEM)
            return false;
        ItemStack cheatedStack = entry.getItemStack().copy();
        if (RoughlyEnoughItemsCore.canUsePackets()) {
            try {
                ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()).writeItemStack(cheatedStack));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Identifier identifier = entry.getIdentifier().orElse(null);
            if (identifier == null)
                return false;
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().asString() : "";
            String og = cheatedStack.getCount() == 1 ? ConfigObject.getInstance().getGiveCommand().replaceAll(" \\{count}", "") : ConfigObject.getInstance().getGiveCommand();
            String madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                MinecraftClient.getInstance().player.addChatMessage(new TranslatableText("text.rei.too_long_nbt"), false);
            }
            MinecraftClient.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    @Override
    public boolean executeRecipeKeyBind(EntryStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getRecipesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map, stack);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeUsageKeyBind(EntryStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getUsagesFor(stack);
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map, stack);
        return map.keySet().size() > 0;
    }
    
    @Override
    public List<ItemStack> getInventoryItemsTypes() {
        List<ItemStack> inventoryStacks = new ArrayList<>(MinecraftClient.getInstance().player.inventory.main);
        inventoryStacks.addAll(MinecraftClient.getInstance().player.inventory.armor);
        inventoryStacks.addAll(MinecraftClient.getInstance().player.inventory.offHand);
        return inventoryStacks;
    }
    
    @Override
    public boolean executeViewAllRecipesKeyBind() {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = RecipeHelper.getInstance().getAllRecipes();
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeViewAllRecipesFromCategory(Identifier category) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        RecipeCategory<?> recipeCategory = CollectionUtils.findFirstOrNull(RecipeHelper.getInstance().getAllCategories(), c -> c.getIdentifier().equals(category));
        if (recipeCategory == null)
            return false;
        map.put(recipeCategory, RecipeHelper.getInstance().getAllRecipesFromCategory(recipeCategory));
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public boolean executeViewAllRecipesFromCategories(List<Identifier> categories) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        for (Identifier category : categories) {
            RecipeCategory<?> recipeCategory = CollectionUtils.findFirstOrNull(RecipeHelper.getInstance().getAllCategories(), c -> c.getIdentifier().equals(category));
            if (recipeCategory == null)
                continue;
            map.put(recipeCategory, RecipeHelper.getInstance().getAllRecipesFromCategory(recipeCategory));
        }
        if (map.keySet().size() > 0)
            openRecipeViewingScreen(map);
        return map.keySet().size() > 0;
    }
    
    @Override
    public void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
        openRecipeViewingScreen(map, null);
    }
    
    public void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map, EntryStack notice) {
        Screen screen;
        if (ConfigObject.getInstance().getRecipeScreenType() == RecipeScreenType.VILLAGER) {
            screen = new VillagerRecipeViewingScreen(map);
            if (notice != null)
                ((VillagerRecipeViewingScreen) screen).addMainStackToNotice(notice);
        } else if (ConfigObject.getInstance().getRecipeScreenType() == RecipeScreenType.UNSET) {
            screen = new PreRecipeViewingScreen(ScreenHelper.getLastContainerScreen(), RecipeScreenType.UNSET, true, original -> {
                ConfigObject.getInstance().setRecipeScreenType(original ? RecipeScreenType.ORIGINAL : RecipeScreenType.VILLAGER);
                ConfigManager.getInstance().saveConfig();
                openRecipeViewingScreen(map, notice);
            });
        } else {
            screen = new RecipeViewingScreen(map);
            if (notice != null)
                ((RecipeViewingScreen) screen).addMainStackToNotice(notice);
        }
        ScreenHelper.storeRecipeScreen(MinecraftClient.getInstance().currentScreen);
        MinecraftClient.getInstance().openScreen(screen);
    }
    
    @Override
    public void onInitializeClient() {
        ClientHelperImpl.instance = this;
        registerFabricKeyBinds();
        modNameCache.put("minecraft", "Minecraft");
        modNameCache.put("c", "Global");
        modNameCache.put("global", "Global");
    }
    
    @Override
    public void registerFabricKeyBinds() {
        String category = "key.rei.category";
        if (!FabricLoader.getInstance().isModLoaded("amecs")) {
            try {
                ConfigObjectImpl.General general = ConfigObject.getInstance().getGeneral();
                ConfigObjectImpl.General instance = general.getClass().getConstructor().newInstance();
                for (Field declaredField : general.getClass().getDeclaredFields()) {
                    if (declaredField.getType() == ModifierKeyCode.class && !declaredField.isAnnotationPresent(ConfigEntry.Gui.Excluded.class)) {
                        declaredField.setAccessible(true);
                        FakeModifierKeyCodeAdder.INSTANCE.registerModifierKeyCode(category, "config.roughlyenoughitems." + declaredField.getName(), () -> {
                            try {
                                ModifierKeyCode code = (ModifierKeyCode) declaredField.get(general);
                                return code == null ? ModifierKeyCode.unknown() : code;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, () -> {
                            try {
                                return (ModifierKeyCode) declaredField.get(instance);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }, keyCode -> {
                            try {
                                declaredField.set(general, keyCode);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                KeyBindingRegistryImpl.INSTANCE.addCategory(category);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    
}
