package com.japicraft.ui;

import net.kyori.adventure.text.format.TextColor;

public class ColorUtilities {
    public static final int BASE_IDENTIFIER = 204;
    public static final int ENCODING_MULTIPLIER = 4;
    public static final TextColor GRAY = TextColor.color(100, 100, 100);
    public static final TextColor WHITE = TextColor.color(255, 255, 255);
    public static final TextColor GOLD = TextColor.color(255, 215, 0);

    public static TextColor getEffectIdentifier(int effectGroup, int effectType) {
        return TextColor.color(ColorUtilities.BASE_IDENTIFIER, effectGroup * ColorUtilities.ENCODING_MULTIPLIER, effectType * ColorUtilities.ENCODING_MULTIPLIER);
    }
}
