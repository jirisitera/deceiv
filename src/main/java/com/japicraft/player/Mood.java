package com.japicraft.player;

import com.japicraft.ui.ColorUtilities;
import net.kyori.adventure.text.format.TextColor;

public enum Mood {
    CALM(1, 0, "No danger around at all."),
    NERVOUS(2, 1, "A possibility of dangerous presence."),
    TERRIFIED(3, 2, "Life threatening danger close by!"),
    POISONED(4, 3, "Poisoned.");
    private static final int EFFECT_GROUP = 2;
    private final TextColor identifier;
    private final int severity;
    private final String description;

    Mood(int effectType, int severity, String description) {
        this.identifier = ColorUtilities.getEffectIdentifier(Mood.EFFECT_GROUP, effectType);
        this.severity = severity;
        this.description = description;
    }

    public TextColor getIdentifier() {
        return identifier;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }
}
