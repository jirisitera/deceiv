package com.japicraft.registry;

import com.japicraft.Deceiv;
import com.japicraft.event.PlayerInteract;
import org.bukkit.plugin.java.JavaPlugin;

public class EventRegistry {
    public static void register(JavaPlugin plugin) {
        Deceiv.pluginManager.registerEvents(new PlayerInteract(), plugin);
    }
}
