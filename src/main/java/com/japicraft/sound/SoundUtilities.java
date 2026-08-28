package com.japicraft.sound;

import com.japicraft.Deceiv;
import com.japicraft.item.UniqueItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;

public final class SoundUtilities {
    public static void playAsPlayerNearby(Player player, @Subst("default") String soundName, float volume, float pitch) {
        player.getWorld().playSound(Sound.sound(Key.key(Deceiv.PLUGIN_ID, soundName), Sound.Source.PLAYER, volume, pitch), player);
    }

    public static void playAsPlayerNearby(Player player, SoundType type, float volume, float pitch) {
        playAsPlayerNearby(player, type.getPath(), volume, pitch);
    }

    public static void playAsPlayerNearby(Player player, SoundType type) {
        playAsPlayerNearby(player, type.getPath(), 1.0F, 1.0F);
    }

    public static void playAsPlayerWithDistance(Player player, SoundType type, float pitch, float blockRadius) {
        playAsPlayerNearby(player, type, blockRadius / 16.0F, pitch);
    }

    public static void playAsPlayerNearbyWithItem(Player player, UniqueItem unique, SoundType type) {
        playAsPlayerNearby(player, unique.getModel() + type.getPath(), 1.0F, 1.0F);
    }
}
