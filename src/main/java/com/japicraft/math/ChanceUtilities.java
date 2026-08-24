package com.japicraft.math;

import com.japicraft.game.Arena;
import com.japicraft.game.Role;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

public class ChanceUtilities {
    private static final int CHANCE_BASE = 100;
    private static final int CHANCE_STEP = 5;

    public static int getRoleChance(Player player, Role role) {
        return role.getChanceKey()
            .map(key -> Math.max(1, player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, CHANCE_BASE)))
            .orElse(0);
    }

    public static void clearRoleChance(Player player, Role role) {
        ChanceUtilities.adjustRoleChance(role, key -> player.getPersistentDataContainer().remove(key));
    }

    public static void increaseRoleChance(Player player, Role role) {
        ChanceUtilities.adjustRoleChance(role, key -> player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, ChanceUtilities.getRoleChance(player, role) + CHANCE_STEP));
    }

    public static void decreaseRoleChance(Player player, Role role) {
        ChanceUtilities.adjustRoleChance(role, key -> player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, ChanceUtilities.getRoleChance(player, role) - CHANCE_STEP));
    }

    public static int getTotalArenaWeights(Arena arena, Role role) {
        int totalChanceWeight = 0;
        for (Player player : arena.getPlayers()) {
            totalChanceWeight += ChanceUtilities.getRoleChance(player, role);
        }
        if (totalChanceWeight <= 0) {
            totalChanceWeight = 1;
        }
        return totalChanceWeight;
    }

    public static double getRelativeRoleChance(Player player, Role role, int totalChanceWeight) {
        if (totalChanceWeight == 0) {
            return 0.0;
        }
        return ((double) ChanceUtilities.getRoleChance(player, role) / totalChanceWeight) * 100.0;
    }

    private static void adjustRoleChance(Role role, Consumer<NamespacedKey> action) {
        role.getChanceKey().ifPresent(action);
    }
}
