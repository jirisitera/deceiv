package com.japicraft.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record PlayerItemRelation(Player player, ItemStack item) {
}
