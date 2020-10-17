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

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.gui.modules.Menu;
import me.shedaniel.rei.gui.modules.entries.GameModeMenuEntry;
import me.shedaniel.rei.gui.modules.entries.WeatherMenuEntry;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.*;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ContainerScreenOverlay extends WidgetWithBounds implements REIOverlay {
    
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<Tooltip> TOOLTIPS = Lists.newArrayList();
    private static final List<Runnable> AFTER_RENDER = Lists.newArrayList();
    private static final EntryListWidget ENTRY_LIST_WIDGET = new EntryListWidget();
    private static FavoritesListWidget favoritesListWidget = null;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReInit = false;
    private int tooltipWidth;
    private int tooltipHeight;
    private List<IReorderingProcessor> tooltipLines;
    public final TriConsumer<MatrixStack, Point, Float> renderTooltipCallback = (matrices, mouse, aFloat) -> {
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        matrices.pushPose();
        matrices.translate(0, 0, 999);
        int x = mouse.x;
        int y = mouse.y;
        this.fillGradient(matrices, x - 3, y - 4, x + tooltipWidth + 3, y - 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, y + tooltipHeight + 3, x + tooltipWidth + 3, y + tooltipHeight + 4, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, y - 3, x + tooltipWidth + 3, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 4, y - 3, x - 3, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(matrices, x + tooltipWidth + 3, y - 3, x + tooltipWidth + 4, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, y - 3 + 1, x - 3 + 1, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(matrices, x + tooltipWidth + 2, y - 3 + 1, x + tooltipWidth + 3, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(matrices, x - 3, y - 3, x + tooltipWidth + 3, y - 3 + 1, 1347420415, 1347420415);
        this.fillGradient(matrices, x - 3, y + tooltipHeight + 2, x + tooltipWidth + 3, y + tooltipHeight + 3, 1344798847, 1344798847);
        int currentY = y;
        IRenderTypeBuffer.Impl immediate = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        Matrix4f matrix = matrices.last().pose();
        for (int lineIndex = 0; lineIndex < tooltipLines.size(); lineIndex++) {
            font.drawInBatch(tooltipLines.get(lineIndex), x, currentY, -1, true, matrix, immediate, false, 0, 15728880);
            currentY += lineIndex == 0 ? 12 : 10;
        }
        immediate.endBatch();
        matrices.popPose();
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    };
    private Rectangle bounds;
    private MainWindow window;
    private Button leftButton, rightButton;
    @ApiStatus.Experimental
    private Rectangle subsetsButtonBounds;
    @ApiStatus.Experimental
    @Nullable
    private Menu subsetsMenu = null;
    private Widget wrappedSubsetsMenu = null;
    
    @Nullable
    private Menu weatherMenu = null;
    private Widget wrappedWeatherMenu = null;
    private boolean renderWeatherMenu = false;
    private Button weatherButton = null;
    
    @Nullable
    private Menu gameModeMenu = null;
    private Widget wrappedGameModeMenu = null;
    private boolean renderGameModeMenu = false;
    private Button gameModeButton = null;
    
    public static EntryListWidget getEntryListWidget() {
        return ENTRY_LIST_WIDGET;
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return favoritesListWidget;
    }
    
    @ApiStatus.Experimental
    @Nullable
    public Menu getSubsetsMenu() {
        return subsetsMenu;
    }
    
    public void removeWeatherMenu() {
        this.renderWeatherMenu = false;
        Widget tmpWeatherMenu = wrappedWeatherMenu;
        AFTER_RENDER.add(() -> this.widgets.remove(tmpWeatherMenu));
        this.weatherMenu = null;
        this.wrappedWeatherMenu = null;
    }
    
    public void removeGameModeMenu() {
        this.renderGameModeMenu = false;
        Widget tmpGameModeMenu = wrappedGameModeMenu;
        AFTER_RENDER.add(() -> this.widgets.remove(tmpGameModeMenu));
        this.gameModeMenu = null;
        this.wrappedGameModeMenu = null;
    }
    
    @Override
    public void queueReloadOverlay() {
        shouldReInit = true;
    }
    
    public void init(boolean useless) {
        init();
    }
    
    public void init() {
        this.shouldReInit = false;
        //Update Variables
        this.children().clear();
        this.wrappedSubsetsMenu = null;
        this.subsetsMenu = null;
        this.weatherMenu = null;
        this.renderWeatherMenu = false;
        this.weatherButton = null;
        this.window = Minecraft.getInstance().getWindow();
        this.bounds = DisplayHelper.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), Minecraft.getInstance().screen);
        widgets.add(ENTRY_LIST_WIDGET);
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null)
                favoritesListWidget = new FavoritesListWidget();
            widgets.add(favoritesListWidget);
        }
        ENTRY_LIST_WIDGET.updateArea(ScreenHelper.getSearchField() == null ? "" : null);
        if (ScreenHelper.getSearchField() == null) {
            ScreenHelper.setSearchField(new OverlaySearchField(0, 0, 0, 0));
        }
        ScreenHelper.getSearchField().getBounds().setBounds(getSearchFieldArea());
        this.widgets.add(ScreenHelper.getSearchField());
        ScreenHelper.getSearchField().setChangedListener(s -> ENTRY_LIST_WIDGET.updateSearch(s, false));
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(leftButton = Widgets.createButton(new Rectangle(bounds.x, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslationTextComponent("text.rei.left_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.previousPage();
                        if (ENTRY_LIST_WIDGET.getPage() < 0)
                            ENTRY_LIST_WIDGET.setPage(ENTRY_LIST_WIDGET.getTotalPages() - 1);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.get("text.rei.previous_page"))
                    .focusable(false));
            widgets.add(rightButton = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 18, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslationTextComponent("text.rei.right_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.nextPage();
                        if (ENTRY_LIST_WIDGET.getPage() >= ENTRY_LIST_WIDGET.getTotalPages())
                            ENTRY_LIST_WIDGET.setPage(0);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.get("text.rei.next_page"))
                    .focusable(false));
        }
        
        final Rectangle configButtonArea = getConfigButtonArea();
        Widget tmp;
        widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.mergeWidgets(
                Widgets.createButton(configButtonArea, NarratorChatListener.NO_TITLE)
                        .onClick(button -> {
                            if (Screen.hasShiftDown()) {
                                ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                                return;
                            }
                            ConfigManager.getInstance().openConfigScreen(REIHelper.getInstance().getPreviousContainerScreen());
                        })
                        .onRender((matrices, button) -> {
                            if (ClientHelper.getInstance().isCheating() && RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                button.setTint(RoughlyEnoughItemsCore.hasPermissionToUsePackets() ? 721354752 : 1476440063);
                            } else {
                                button.removeTint();
                            }
                        })
                        .focusable(false)
                        .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                        .tooltipSupplier(button -> {
                            String tooltips = I18n.get("text.rei.config_tooltip");
                            tooltips += "\n  ";
                            if (!ClientHelper.getInstance().isCheating())
                                tooltips += "\n" + I18n.get("text.rei.cheating_disabled");
                            else if (!RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                if (minecraft.gameMode.hasInfiniteItems())
                                    tooltips += "\n" + I18n.get("text.rei.cheating_limited_creative_enabled");
                                else tooltips += "\n" + I18n.get("text.rei.cheating_enabled_no_perms");
                            } else if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                                tooltips += "\n" + I18n.get("text.rei.cheating_enabled");
                            else
                                tooltips += "\n" + I18n.get("text.rei.cheating_limited_enabled");
                            return tooltips;
                        }),
                Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    helper.setBlitOffset(helper.getBlitOffset() + 1);
                    Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
                    helper.blit(matrices, configButtonArea.x + 3, configButtonArea.y + 3, 0, 0, 14, 14);
                })
                )
        ));
        tmp.setZ(600);
        if (ConfigObject.getInstance().doesShowUtilsButtons()) {
            widgets.add(gameModeButton = Widgets.createButton(ConfigObject.getInstance().isLowerConfigButton() ? new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 10, 20, 20) : new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 55 : 35, 10, 20, 20), NarratorChatListener.NO_TITLE)
                    .onRender((matrices, button) -> {
                        boolean tmpRender = renderGameModeMenu;
                        renderGameModeMenu = !renderWeatherMenu && (button.isFocused() || button.containsMouse(PointHelper.ofMouse()) || (wrappedGameModeMenu != null && wrappedGameModeMenu.containsMouse(PointHelper.ofMouse())));
                        if (tmpRender != renderGameModeMenu) {
                            if (renderGameModeMenu) {
                                this.gameModeMenu = new Menu(new Point(button.getBounds().x, button.getBounds().getMaxY()),
                                        CollectionUtils.filterAndMap(Arrays.asList(GameType.values()), mode -> mode != GameType.NOT_SET, GameModeMenuEntry::new));
                                if (ConfigObject.getInstance().isLeftHandSidePanel())
                                    this.gameModeMenu.menuStartPoint.x -= this.gameModeMenu.getBounds().width - this.gameModeButton.getBounds().width;
                                this.wrappedGameModeMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(gameModeMenu), 0, 0, 600);
                                AFTER_RENDER.add(() -> this.widgets.add(wrappedGameModeMenu));
                            } else {
                                removeGameModeMenu();
                            }
                        }
                        button.setText(new StringTextComponent(getGameModeShortText(getCurrentGameMode())));
                    })
                    .focusable(false)
                    .tooltipLine(I18n.get("text.rei.gamemode_button.tooltip.all"))
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            widgets.add(weatherButton = Widgets.createButton(new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 35, 20, 20), NarratorChatListener.NO_TITLE)
                    .onRender((matrices, button) -> {
                        boolean tmpRender = renderWeatherMenu;
                        renderWeatherMenu = !renderGameModeMenu && (button.isFocused() || button.containsMouse(PointHelper.ofMouse()) || (wrappedWeatherMenu != null && wrappedWeatherMenu.containsMouse(PointHelper.ofMouse())));
                        if (tmpRender != renderWeatherMenu) {
                            if (renderWeatherMenu) {
                                this.weatherMenu = new Menu(new Point(button.getBounds().x, button.getBounds().getMaxY()),
                                        CollectionUtils.map(Weather.values(), WeatherMenuEntry::new));
                                if (ConfigObject.getInstance().isLeftHandSidePanel())
                                    this.weatherMenu.menuStartPoint.x -= this.weatherMenu.getBounds().width - this.weatherButton.getBounds().width;
                                this.wrappedWeatherMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(weatherMenu), 0, 0, 400);
                                AFTER_RENDER.add(() -> this.widgets.add(wrappedWeatherMenu));
                            } else {
                                removeWeatherMenu();
                            }
                        }
                    })
                    .tooltipLine(I18n.get("text.rei.weather_button.tooltip.all"))
                    .focusable(false)
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                helper.blit(matrices, weatherButton.getBounds().x + 3, weatherButton.getBounds().y + 3, getCurrentWeather().getId() * 14, 14, 14, 14);
            }));
        }
        subsetsButtonBounds = getSubsetsButtonBounds();
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            widgets.add(InternalWidgets.wrapLateRenderable(InternalWidgets.wrapTranslate(Widgets.createButton(subsetsButtonBounds, ClientHelperImpl.getInstance().isAprilFools.get() ? new TranslationTextComponent("text.rei.tiny_potato") : new TranslationTextComponent("text.rei.subsets"))
                    .onClick(button -> {
                        if (subsetsMenu == null) {
                            wrappedSubsetsMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(this.subsetsMenu = Menu.createSubsetsMenuFromRegistry(new Point(this.subsetsButtonBounds.x, this.subsetsButtonBounds.getMaxY()))), 0, 0, 400);
                            this.widgets.add(this.wrappedSubsetsMenu);
                        } else {
                            this.widgets.remove(this.wrappedSubsetsMenu);
                            this.subsetsMenu = null;
                            this.wrappedSubsetsMenu = null;
                        }
                    }), 0, 0, 600)));
        }
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(Widgets.createClickableLabel(new Point(bounds.x + (bounds.width / 2), bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10), NarratorChatListener.NO_TITLE, label -> {
                ENTRY_LIST_WIDGET.setPage(0);
                ENTRY_LIST_WIDGET.updateEntriesPosition();
            }).tooltipLine(I18n.get("text.rei.go_back_first_page")).focusable(false).onRender((matrices, label) -> {
                label.setClickable(ENTRY_LIST_WIDGET.getTotalPages() > 1);
                label.setText(new StringTextComponent(String.format("%s/%s", ENTRY_LIST_WIDGET.getPage() + 1, Math.max(ENTRY_LIST_WIDGET.getTotalPages(), 1))));
            }).rainbow(new Random().nextFloat() < 1.0E-4D || ClientHelperImpl.getInstance().isAprilFools.get()));
        }
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            Rectangle area = getCraftableToggleArea();
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
            this.widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.mergeWidgets(
                    Widgets.createButton(area, NarratorChatListener.NO_TITLE)
                            .focusable(false)
                            .onClick(button -> {
                                ConfigManager.getInstance().toggleCraftableOnly();
                                ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
                            })
                            .onRender((matrices, button) -> button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 939579655 : 956235776))
                            .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                            .tooltipSupplier(button -> I18n.get(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all")),
                    Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                        Vector4f vector = new Vector4f(area.x + 2, area.y + 2, helper.getBlitOffset() - 10, 1.0F);
                        vector.transform(matrices.last().pose());
                        itemRenderer.blitOffset = vector.z();
                        itemRenderer.renderGuiItem(icon, (int) vector.x(), (int) vector.y());
                        itemRenderer.blitOffset = 0.0F;
                    }))
            ));
            tmp.setZ(600);
        }
    }
    
    private Rectangle getSubsetsButtonBounds() {
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            if (Minecraft.getInstance().screen instanceof RecipeViewingScreen) {
                RecipeViewingScreen widget = (RecipeViewingScreen) Minecraft.getInstance().screen;
                return new Rectangle(widget.getBounds().x, 3, widget.getBounds().width, 18);
            }
            if (Minecraft.getInstance().screen instanceof VillagerRecipeViewingScreen) {
                VillagerRecipeViewingScreen widget = (VillagerRecipeViewingScreen) Minecraft.getInstance().screen;
                return new Rectangle(widget.bounds.x, 3, widget.bounds.width, 18);
            }
            return new Rectangle(REIHelper.getInstance().getPreviousContainerScreen().getGuiLeft(), 3, REIHelper.getInstance().getPreviousContainerScreen().getXSize(), 18);
        }
        return null;
    }
    
    private Weather getNextWeather() {
        try {
            Weather current = getCurrentWeather();
            int next = current.getId() + 1;
            if (next >= 3)
                next = 0;
            return Weather.byId(next);
        } catch (Exception e) {
            return Weather.CLEAR;
        }
    }
    
    private Weather getCurrentWeather() {
        ClientWorld world = Minecraft.getInstance().level;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getLevelData().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private String getGameModeShortText(GameType gameMode) {
        return I18n.get("text.rei.short_gamemode." + gameMode.getName());
    }
    
    private String getGameModeText(GameType gameMode) {
        return I18n.get("selectWorld.gameMode." + gameMode.getName());
    }
    
    private GameType getNextGameMode(boolean reverse) {
        try {
            GameType current = getCurrentGameMode();
            int next = current.getId() + 1;
            if (reverse)
                next -= 2;
            if (next > 3)
                next = 0;
            if (next < 0)
                next = 3;
            return GameType.byId(next);
        } catch (Exception e) {
            return GameType.NOT_SET;
        }
    }
    
    private GameType getCurrentGameMode() {
        return Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId()).getGameMode();
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1;
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) widthRemoved += 22;
        if (ConfigObject.getInstance().isLowerConfigButton()) widthRemoved += 22;
        SearchFieldLocation searchFieldLocation = ScreenHelper.getContextualSearchFieldLocation();
        switch (searchFieldLocation) {
            case TOP_SIDE:
                return getTopSideSearchFieldArea(widthRemoved);
            case BOTTOM_SIDE:
                return getBottomSideSearchFieldArea(widthRemoved);
            default:
            case CENTER: {
                for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(Minecraft.getInstance().screen.getClass())) {
                    if (decider instanceof DisplayHelper.DisplayBoundsProvider) {
                        Rectangle containerBounds = ((DisplayHelper.DisplayBoundsProvider<Screen>) decider).getScreenBounds(Minecraft.getInstance().screen);
                        return getBottomCenterSearchFieldArea(containerBounds, widthRemoved);
                    }
                }
                return new Rectangle();
            }
        }
    }
    
    private Rectangle getTopSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, 4, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, window.getGuiScaledHeight() - 22, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomCenterSearchFieldArea(Rectangle containerBounds, int widthRemoved) {
        return new Rectangle(containerBounds.x, window.getGuiScaledHeight() - 22, containerBounds.width - widthRemoved, 18);
    }
    
    private Rectangle getCraftableToggleArea() {
        Rectangle area = getSearchFieldArea();
        area.setLocation(area.x + area.width + 4, area.y - 1);
        area.setSize(20, 20);
        return area;
    }
    
    private Rectangle getConfigButtonArea() {
        if (ConfigObject.getInstance().isLowerConfigButton()) {
            Rectangle area = getSearchFieldArea();
            area.setLocation(area.x + area.width + (ConfigObject.getInstance().isCraftableFilterEnabled() ? 26 : 4), area.y - 1);
            area.setSize(20, 20);
            return area;
        }
        return new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 10, 20, 20);
    }
    
    private String getCheatModeText() {
        return I18n.get(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (shouldReInit) {
            ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
            init();
        } else {
            for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(minecraft.screen.getClass())) {
                if (decider != null && decider.shouldRecalculateArea(ConfigObject.getInstance().getDisplayPanelLocation(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (ConfigManager.getInstance().isCraftableOnlyEnabled()) {
            Set<EntryStack> currentStacks = ClientHelperImpl.getInstance()._getInventoryItemsTypes();
            if (!currentStacks.equals(ScreenHelper.inventoryStacks)) {
                ScreenHelper.inventoryStacks = currentStacks;
                ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
            }
        }
        if (OverlaySearchField.isHighlighting) {
            matrices.pushPose();
            matrices.translate(0, 0, 200f);
            if (Minecraft.getInstance().screen instanceof ContainerScreen) {
                ContainerScreen<?> containerScreen = (ContainerScreen<?>) Minecraft.getInstance().screen;
                int x = containerScreen.getGuiLeft(), y = containerScreen.getGuiTop();
                for (Slot slot : containerScreen.getMenu().slots)
                    if (!slot.hasItem() || !ENTRY_LIST_WIDGET.canLastSearchTermsBeAppliedTo(EntryStack.create(slot.getItem())))
                        fillGradient(matrices, x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, -601874400, -601874400);
            }
            matrices.popPose();
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderWidgets(matrices, mouseX, mouseY, delta);
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            List<ResourceLocation> categories = null;
            Screen screen = Minecraft.getInstance().screen;
            ClickAreaHandler.ClickAreaContext context = new ClickAreaHandler.ClickAreaContext<Screen>() {
                @Override
                public Screen getScreen() {
                    return screen;
                }
                
                @Override
                public Point getMousePosition() {
                    return new Point(mouseX, mouseY);
                }
            };
            for (Map.Entry<Class<? extends Screen>, ClickAreaHandler<?>> area : ((RecipeHelperImpl) RecipeHelper.getInstance()).getClickAreas().entries()) {
                if (area.getKey().equals(screen.getClass())) {
                    ClickAreaHandler.Result result = area.getValue().handle(context);
                    if (result.isSuccessful()) {
                        if (categories == null) {
                            categories = result.getCategories().collect(Collectors.toList());
                        } else categories.addAll(result.getCategories().collect(Collectors.toList()));
                    }
                }
            }
            if (categories != null && !categories.isEmpty()) {
                String collect = CollectionUtils.mapAndJoinToString(categories, identifier -> RecipeHelper.getInstance().getCategory(identifier).getCategoryName(), ", ");
                Tooltip.create(new TranslationTextComponent("text.rei.view_recipes_for", collect)).queue();
            }
        }
    }
    
    public void lateRender(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (ScreenHelper.isOverlayVisible()) {
            ScreenHelper.getSearchField().laterRender(matrices, mouseX, mouseY, delta);
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && wrappedSubsetsMenu != widget && wrappedWeatherMenu != widget && wrappedGameModeMenu != widget)
                    widget.render(matrices, mouseX, mouseY, delta);
            }
        }
        if (wrappedWeatherMenu != null) {
            if (wrappedWeatherMenu.containsMouse(mouseX, mouseY)) {
                TOOLTIPS.clear();
            }
            wrappedWeatherMenu.render(matrices, mouseX, mouseY, delta);
        } else if (wrappedGameModeMenu != null) {
            if (wrappedGameModeMenu.containsMouse(mouseX, mouseY)) {
                TOOLTIPS.clear();
            }
            wrappedGameModeMenu.render(matrices, mouseX, mouseY, delta);
        }
        if (wrappedSubsetsMenu != null) {
            TOOLTIPS.clear();
            wrappedSubsetsMenu.render(matrices, mouseX, mouseY, delta);
        }
        Screen currentScreen = Minecraft.getInstance().screen;
        if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated)
            for (Tooltip tooltip : TOOLTIPS) {
                if (tooltip != null)
                    renderTooltip(matrices, tooltip);
            }
        for (Runnable runnable : AFTER_RENDER) {
            runnable.run();
        }
        TOOLTIPS.clear();
        AFTER_RENDER.clear();
    }
    
    public void renderTooltip(MatrixStack matrices, Tooltip tooltip) {
        renderTooltip(matrices, tooltip.getText(), tooltip.getX(), tooltip.getY());
    }
    
    public void renderTooltip(MatrixStack matrices, List<ITextComponent> lines, int mouseX, int mouseY) {
        if (lines.isEmpty())
            return;
        List<IReorderingProcessor> orderedTexts = CollectionUtils.map(lines, ITextComponent::getVisualOrderText);
        renderTooltipInner(matrices, orderedTexts, mouseX, mouseY);
    }
    
    public void renderTooltipInner(MatrixStack matrices, List<IReorderingProcessor> lines, int mouseX, int mouseY) {
        if (lines.isEmpty())
            return;
        matrices.pushPose();
        matrices.translate(0, 0, 500);
        minecraft.screen.renderTooltip(matrices, lines, mouseX, mouseY);
        matrices.popPose();
    }
    
    public void addTooltip(@Nullable Tooltip tooltip) {
        if (tooltip != null)
            TOOLTIPS.add(tooltip);
    }
    
    public void renderWidgets(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            leftButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
            rightButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
        }
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (wrappedSubsetsMenu != null && wrappedSubsetsMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (wrappedWeatherMenu != null && wrappedWeatherMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (wrappedGameModeMenu != null && wrappedGameModeMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (isInside(PointHelper.ofMouse())) {
            if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
                if (amount > 0 && leftButton.isEnabled())
                    leftButton.onClick();
                else if (amount < 0 && rightButton.isEnabled())
                    rightButton.onClick();
                else
                    return false;
                return true;
            } else if (ENTRY_LIST_WIDGET.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        if (isNotInExclusionZones(PointHelper.getMouseX(), PointHelper.getMouseY())) {
            if (favoritesListWidget != null && favoritesListWidget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        for (Widget widget : widgets)
            if (widget != ENTRY_LIST_WIDGET && (favoritesListWidget == null || widget != favoritesListWidget)
                && (wrappedSubsetsMenu == null || widget != wrappedSubsetsMenu)
                && (wrappedWeatherMenu == null || widget != wrappedWeatherMenu)
                && (wrappedGameModeMenu == null || widget != wrappedGameModeMenu)
                && widget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ScreenHelper.isOverlayVisible()) {
            if (ScreenHelper.getSearchField().keyPressed(keyCode, scanCode, modifiers))
                return true;
            for (IGuiEventListener listener : widgets)
                if (listener != ScreenHelper.getSearchField() && listener.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesKey(keyCode, scanCode)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        EntryStack stack = RecipeHelper.getInstance().getScreenFocusedStack(Minecraft.getInstance().screen);
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(keyCode, scanCode)) {
                return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addRecipesFor(stack).setOutputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(keyCode, scanCode)) {
                return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addUsagesFor(stack).setInputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                stack.setAmount(127);
                if (!CollectionUtils.anyMatchEqualsEntryIgnoreAmount(ConfigObject.getInstance().getFavorites(), stack))
                    ConfigObject.getInstance().getFavorites().add(stack);
                ConfigManager.getInstance().saveConfig();
                FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
                if (favoritesListWidget != null)
                    favoritesListWidget.updateSearch(ContainerScreenOverlay.getEntryListWidget(), ScreenHelper.getSearchField().getText());
                return true;
            }
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            ScreenHelper.getSearchField().setFocused(true);
            setFocused(ScreenHelper.getSearchField());
            ScreenHelper.getSearchField().keybindFocusTime = System.currentTimeMillis();
            ScreenHelper.getSearchField().keybindFocusKey = keyCode;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ScreenHelper.getSearchField().charTyped(char_1, int_1))
            return true;
        for (IGuiEventListener listener : widgets)
            if (listener != ScreenHelper.getSearchField() && listener.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ConfigObject.getInstance().getHideKeybind().matchesMouse(button)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        EntryStack stack = RecipeHelper.getInstance().getScreenFocusedStack(Minecraft.getInstance().screen);
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesMouse(button)) {
                return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addRecipesFor(stack).setOutputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesMouse(button)) {
                return ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addUsagesFor(stack).setInputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
                stack.setAmount(127);
                if (!CollectionUtils.anyMatchEqualsEntryIgnoreAmount(ConfigObject.getInstance().getFavorites(), stack))
                    ConfigObject.getInstance().getFavorites().add(stack);
                ConfigManager.getInstance().saveConfig();
                FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
                if (favoritesListWidget != null)
                    favoritesListWidget.updateSearch(ContainerScreenOverlay.getEntryListWidget(), ScreenHelper.getSearchField().getText());
                return true;
            }
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (wrappedSubsetsMenu != null && wrappedSubsetsMenu.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(wrappedSubsetsMenu);
            if (button == 0)
                this.setDragging(true);
            ScreenHelper.getSearchField().setFocused(false);
            return true;
        }
        if (wrappedWeatherMenu != null) {
            if (wrappedWeatherMenu.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(wrappedWeatherMenu);
                if (button == 0)
                    this.setDragging(true);
                ScreenHelper.getSearchField().setFocused(false);
                return true;
            } else if (!wrappedWeatherMenu.containsMouse(mouseX, mouseY) && !weatherButton.containsMouse(mouseX, mouseY)) {
                removeWeatherMenu();
            }
        }
        if (wrappedGameModeMenu != null) {
            if (wrappedGameModeMenu.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(wrappedGameModeMenu);
                if (button == 0)
                    this.setDragging(true);
                ScreenHelper.getSearchField().setFocused(false);
                return true;
            } else if (!wrappedGameModeMenu.containsMouse(mouseX, mouseY) && !gameModeButton.containsMouse(mouseX, mouseY)) {
                removeGameModeMenu();
            }
        }
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            List<ResourceLocation> categories = null;
            Screen screen = Minecraft.getInstance().screen;
            ClickAreaHandler.ClickAreaContext context = new ClickAreaHandler.ClickAreaContext<Screen>() {
                @Override
                public Screen getScreen() {
                    return screen;
                }
                
                @Override
                public Point getMousePosition() {
                    return new Point(mouseX, mouseY);
                }
            };
            for (Map.Entry<Class<? extends Screen>, ClickAreaHandler<?>> area : ((RecipeHelperImpl) RecipeHelper.getInstance()).getClickAreas().entries()) {
                if (area.getKey().equals(screen.getClass())) {
                    ClickAreaHandler.Result result = area.getValue().handle(context);
                    if (result.isSuccessful()) {
                        if (categories == null) {
                            categories = result.getCategories().collect(Collectors.toList());
                        } else categories.addAll(result.getCategories().collect(Collectors.toList()));
                    }
                }
            }
            if (categories != null && !categories.isEmpty()) {
                ClientHelper.getInstance().openView(ClientHelper.ViewSearchBuilder.builder().addCategories(categories).fillPreferredOpenedCategory());
                Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        for (IGuiEventListener element : widgets)
            if (element != wrappedSubsetsMenu && element != wrappedWeatherMenu && element != wrappedGameModeMenu && element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0)
                    this.setDragging(true);
                if (!(element instanceof OverlaySearchField))
                    ScreenHelper.getSearchField().setFocused(false);
                return true;
            }
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesMouse(button)) {
            ScreenHelper.getSearchField().setFocused(true);
            setFocused(ScreenHelper.getSearchField());
            ScreenHelper.getSearchField().keybindFocusTime = -1;
            ScreenHelper.getSearchField().keybindFocusKey = -1;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        return (this.getFocused() != null && this.isDragging() && int_1 == 0) && this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (OverlayDecider decider : DisplayHelper.getInstance().getSortedOverlayDeciders(Minecraft.getInstance().screen.getClass())) {
            ActionResultType in = decider.isInZone(mouseX, mouseY);
            if (in != ActionResultType.PASS)
                return in == ActionResultType.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
}
