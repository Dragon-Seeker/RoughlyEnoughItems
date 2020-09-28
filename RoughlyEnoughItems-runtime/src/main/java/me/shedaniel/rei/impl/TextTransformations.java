package me.shedaniel.rei.impl;

import me.shedaniel.math.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TextTransformations {
    public static IReorderingProcessor applyRainbow(IReorderingProcessor sequence, int x, int y) {
        int[] combinedX = {x};
        return sink -> sequence.accept((charIndex, style, codePoint) -> {
            if (charIndex == 0) combinedX[0] = x;
            int rgb = Color.HSBtoRGB(((Util.getMillis() - combinedX[0] * 10 - y * 10) % 2000) / 2000F, 0.8F, 0.95F);
            combinedX[0] += Minecraft.getInstance().font.getSplitter().widthProvider.getWidth(codePoint, style);
            return sink.accept(charIndex, style.withColor(net.minecraft.util.text.Color.fromRgb(rgb)), codePoint);
        });
    }
}
