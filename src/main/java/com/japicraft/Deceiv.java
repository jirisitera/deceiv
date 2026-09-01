package com.japicraft;

import com.japicraft.registry.PacketRegistry;
import com.japicraft.registry.PluginRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Snowball;
import org.bukkit.plugin.java.JavaPlugin;

public class Deceiv extends JavaPlugin {
    public static final String PLUGIN_ID = "deceiv";
    public static Deceiv plugin;
    private static ComponentLogger logger;

    public static Deceiv getPlugin() {
        return plugin;
    }

    public static ComponentLogger logger() {
        return logger;
    }

    @Override
    public void onLoad() {
        plugin = this;
        logger = getComponentLogger();
        new PacketRegistry().initialize();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        new PluginRegistry().initialize();
    }

    @Override
    public void onDisable() {
        // clean up dangling temporary entities
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClasses(Snowball.class, Interaction.class).forEach(Entity::remove));
    }
}
