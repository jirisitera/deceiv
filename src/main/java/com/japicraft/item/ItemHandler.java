package com.japicraft.item;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface ItemHandler {
    boolean compare(ItemStack item);

    void handleInteract(Player player, Action action, ItemStack item);
}
