package com.japicraft.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.EnumSet;

public class EventCancelService implements Listener {
    private static final EnumSet<ClickType> CANCELLED_CLICK_TYPES = EnumSet.of(
        ClickType.DROP,
        ClickType.CONTROL_DROP,
        ClickType.WINDOW_BORDER_LEFT,
        ClickType.WINDOW_BORDER_RIGHT
    );
    private static final EnumSet<EntityDamageEvent.DamageCause> CANCELLED_DAMAGE_CAUSES = EnumSet.of(
        EntityDamageEvent.DamageCause.FALL,
        EntityDamageEvent.DamageCause.SUFFOCATION,
        EntityDamageEvent.DamageCause.FIRE,
        EntityDamageEvent.DamageCause.DROWNING
    );

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (CANCELLED_CLICK_TYPES.contains(event.getClick())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && CANCELLED_DAMAGE_CAUSES.contains(event.getCause())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
