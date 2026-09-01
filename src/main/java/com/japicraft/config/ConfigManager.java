package com.japicraft.config;

import com.japicraft.Deceiv;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public static void reload() {
        Deceiv.getPlugin().reloadConfig();
    }

    public static FileConfiguration getConfig() {
        return Deceiv.getPlugin().getConfig();
    }

}
