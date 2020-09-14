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
import me.shedaniel.rei.impl.NetworkingManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class RoughlyEnoughItemsInit {
    @ApiStatus.Internal public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    
    public RoughlyEnoughItemsInit() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> RoughlyEnoughItemsCore::new);
        NetworkingManager.init();
        RoughlyEnoughItemsNetwork.onInitialize();
    }
    
    public static <T> void scanAnnotation(Type annotationType, Consumer<T> consumer) {
        List<T> instances = Lists.newArrayList();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotation : data.getAnnotations()) {
                if (annotationType.equals(annotation.getAnnotationType())) {
                    try {
                        T instance = (T) Class.forName(annotation.getMemberName()).getDeclaredConstructor().newInstance();
                        instances.add(instance);
                    } catch (Throwable throwable) {
                        LOGGER.error("Failed to load plugin: " + annotation.getMemberName(), throwable);
                    }
                }
            }
        }
        
        for (T instance : instances) {
            consumer.accept(instance);
        }
    }
}