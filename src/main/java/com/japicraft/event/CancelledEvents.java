package com.japicraft.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class CancelledEvents implements Listener {
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ClickType click = event.getClick();
        if (click == ClickType.DROP || click == ClickType.CONTROL_DROP || click == ClickType.WINDOW_BORDER_LEFT || click == ClickType.WINDOW_BORDER_RIGHT) {
            event.setCancelled(true);
        }
    }
}
