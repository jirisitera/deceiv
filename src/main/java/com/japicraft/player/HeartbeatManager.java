package com.japicraft.player;

import com.japicraft.Deceiv;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class HeartbeatManager {
    public static void calm(Player player) {
        player.sendActionBar(Component.text("1").font(Key.key(Deceiv.PLUGIN_ID, "heartbeat")));
    }
}
