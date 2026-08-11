package com.japicraft;

import com.japicraft.registry.CommandRegistry;
import com.japicraft.registry.EventRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Snowball;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Deceiv extends JavaPlugin {
    public static final String PLUGIN_ID = "deceiv";
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        CommandRegistry.register(this);
        EventRegistry.register(this);
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClasses(Snowball.class, Interaction.class).forEach(Entity::remove));
    }
}
