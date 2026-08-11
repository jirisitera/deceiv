package com.japicraft.manager;

import com.japicraft.Deceiv;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;

public class SoundManager {
    public static void playAsPlayerNearby(Player player, @Subst("default") String soundName, float pitch) {
        player.getWorld().playSound(Sound.sound(Key.key(Deceiv.PLUGIN_ID, soundName), Sound.Source.PLAYER, 1.0F, pitch), player);
    }

    public static void playAsPlayerWithDistance(Player player, @Subst("default") String soundName, float pitch, float blockRadius) {
        player.getWorld().playSound(Sound.sound(Key.key(Deceiv.PLUGIN_ID, soundName), Sound.Source.PLAYER, blockRadius / 16.0F, pitch), player);
    }
}
