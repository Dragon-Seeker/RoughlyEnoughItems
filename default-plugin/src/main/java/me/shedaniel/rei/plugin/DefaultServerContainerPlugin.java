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

package me.shedaniel.rei.plugin;

import me.shedaniel.rei.plugin.containers.CraftingContainerInfoWrapper;
import me.shedaniel.rei.api.server.ContainerInfoHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.*;

public class DefaultServerContainerPlugin implements Runnable {
    @Override
    public void run() {
        ContainerInfoHandler.registerContainerInfo(new ResourceLocation("minecraft", "plugins/crafting"), CraftingContainerInfoWrapper.create(CraftingMenu.class));
        ContainerInfoHandler.registerContainerInfo(new ResourceLocation("minecraft", "plugins/crafting"), CraftingContainerInfoWrapper.create(InventoryMenu.class));
        ContainerInfoHandler.registerContainerInfo(new ResourceLocation("minecraft", "plugins/smelting"), CraftingContainerInfoWrapper.create(FurnaceMenu.class));
        ContainerInfoHandler.registerContainerInfo(new ResourceLocation("minecraft", "plugins/smoking"), CraftingContainerInfoWrapper.create(SmokerMenu.class));
        ContainerInfoHandler.registerContainerInfo(new ResourceLocation("minecraft", "plugins/blasting"), CraftingContainerInfoWrapper.create(BlastFurnaceMenu.class));
    }
}
