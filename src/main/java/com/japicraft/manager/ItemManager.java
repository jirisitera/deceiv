package com.japicraft.manager;

import net.kyori.adventure.key.Key;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
    public static final Key EMPTY_KEY = Key.key("intentionally_empty");

    public static void applyItemCooldown(Player player, ItemStack item, int cooldown) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        player.setCooldown(item, cooldown);
    }
}
