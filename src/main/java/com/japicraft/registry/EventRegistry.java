package com.japicraft.registry;

import com.japicraft.event.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class EventRegistry {
    public static void register(JavaPlugin plugin) {
        List<Listener> events = List.of(
            new AsyncPlayerPreLogin(),
            new BetterModelReload(),
            new EntityDamageByEntity(),
            new PlayerDropItem(),
            new PlayerInteract(),
            new PlayerInteractAtEntity(),
            new PlayerSwapHandItems(),
            new ProjectileHit()
        );
        for (Listener listener : events) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }
}
