package com.japicraft.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    private static final Map<UUID, Role> activeRoles = new HashMap<>();

    public static void assignRole(Player player, Role role) {
        activeRoles.put(player.getUniqueId(), role);
    }

    public static Role getRole(Player player) {
        return activeRoles.getOrDefault(player.getUniqueId(), Role.INNOCENT);
    }

    public static void startRound() {
        // TODO: implement round start logic
    }

    public static void endRound() {
        activeRoles.clear();
    }

    public enum Role {
        INNOCENT,
        MURDERER,
        DETECTIVE
    }
}
