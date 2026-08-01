package com.japicraft;

import com.japicraft.registry.CommandRegistry;
import com.japicraft.registry.EventRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Deceiv extends JavaPlugin {
    public static Plugin plugin;
    public static PluginManager pluginManager;
    public static BukkitScheduler scheduler;
    public static final String PLUGIN_ID = "deceiv";

    @Override
    public void onEnable() {
        plugin = this;
        pluginManager = Bukkit.getPluginManager();
        scheduler = Bukkit.getScheduler();
        CommandRegistry.register(this);
        EventRegistry.register(this);
    }
}
