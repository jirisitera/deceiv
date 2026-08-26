package com.japicraft;

import com.japicraft.registry.PacketRegistry;
import com.japicraft.registry.PluginRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Snowball;
import org.bukkit.plugin.java.JavaPlugin;

public class Deceiv extends JavaPlugin {
    public static final String PLUGIN_ID = "deceiv";

    @Override
    public void onLoad() {
        new PacketRegistry().initialize();
    }

    @Override
    public void onEnable() {
        new PluginRegistry(this).initialize();
    }

    @Override
    public void onDisable() {
        // clean up dangling temporary entities
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClasses(Snowball.class, Interaction.class).forEach(Entity::remove));
    }
}
