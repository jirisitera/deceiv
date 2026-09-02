package com.japicraft.sound;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HeartbeatManager {
    private void sendSample(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
    }
}
