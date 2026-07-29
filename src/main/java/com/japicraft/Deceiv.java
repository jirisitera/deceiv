package com.japicraft;

import com.japicraft.registry.CommandRegistry;
import com.japicraft.registry.EventRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Deceiv extends JavaPlugin {
    public static Plugin plugin;
    public static PluginManager pluginManager;

    @Override
    public void onEnable() {
        plugin = this;
        pluginManager = Bukkit.getPluginManager();
        CommandRegistry.register(this);
        EventRegistry.register(this);
    }
}
