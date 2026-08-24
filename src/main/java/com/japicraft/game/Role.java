package com.japicraft.game;

import com.japicraft.Deceiv;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum Role {
    INNOCENT("Innocent", TextColor.color(20, 165, 50), null),
    MURDERER("Murderer", TextColor.color(225, 65, 15), "murderer_chance"),
    DETECTIVE("Detective", TextColor.color(35, 145, 245), "detective_chance");
    private final String name;
    private final TextColor color;
    private final NamespacedKey specialChanceKey;

    Role(String name, TextColor color, @Nullable String specialChanceKey) {
        this.name = name;
        this.color = color;
        this.specialChanceKey = specialChanceKey != null ? new NamespacedKey(Deceiv.PLUGIN_ID, specialChanceKey) : null;
    }

    public String getName() {
        return name;
    }

    public TextColor getColor() {
        return color;
    }

    public Optional<NamespacedKey> getChanceKey() {
        return Optional.ofNullable(specialChanceKey);
    }

    public Component getDisplayName() {
        return Component.text(name).color(color);
    }

    public boolean isSpecial() {
        return specialChanceKey != null;
    }
}
