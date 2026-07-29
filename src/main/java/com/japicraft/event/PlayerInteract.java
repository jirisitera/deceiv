package com.japicraft.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getCooldown(item) > 0) {
            return;
        }
        Action action = event.getAction();
        if (action.isRightClick()) {
            player.sendMessage("Right clicked!");
        } else if (action.isLeftClick()) {
            player.sendMessage("Left clicked!");
        }
    }
}
