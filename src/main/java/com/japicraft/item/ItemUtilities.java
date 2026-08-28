package com.japicraft.item;

import net.kyori.adventure.key.Key;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ItemUtilities {
    public static final Key EMPTY_KEY = Key.key("intentionally_empty");

    public static void applyItemCooldown(Player player, ItemStack item, int cooldown) {
        if (player.getGameMode() == GameMode.CREATIVE || ItemUtilities.isOnCooldown(player, item)) {
            return;
        }
        player.setCooldown(item, cooldown);
    }

    public static boolean isOnCooldown(Player player, ItemStack item) {
        return player.getCooldown(item) > 0;
    }

    public static boolean isHoldingItem(PlayerInventory inventory, UniqueItem unique) {
        return unique.compare(inventory.getItemInMainHand()) || unique.compare(inventory.getItemInOffHand());
    }
}
