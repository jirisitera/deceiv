package com.japicraft.game;

import com.japicraft.item.DaggerManager;
import com.japicraft.item.RevolverManager;
import com.japicraft.math.ChanceUtilities;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class RoleManager {
    private final Map<UUID, Role> assignedRoles = new HashMap<>();
    private final Map<Role, Integer> totalRoleWeights = new HashMap<>();

    public static void clearChances(Player player) {
        RoleManager.performForSpecialRoles(role -> ChanceUtilities.clearRoleChance(player, role));
    }

    public static void performForSpecialRoles(Consumer<Role> action) {
        for (Role role : Role.values()) {
            if (role.isSpecial()) {
                action.accept(role);
            }
        }
    }

    public void recalculateRoleChances(Arena arena) {
        RoleManager.performForSpecialRoles(role -> totalRoleWeights.put(role, ChanceUtilities.getTotalArenaWeights(arena, role)));
    }

    public int getRelativeChance(Player player, Role role) {
        return (int) ChanceUtilities.getRelativeRoleChance(player, role, totalRoleWeights.get(role));
    }

    public void assignRole(Player player, Role role) {
        assignedRoles.put(player.getUniqueId(), role);
    }

    public Role getRole(Player player) {
        return assignedRoles.getOrDefault(player.getUniqueId(), Role.INNOCENT);
    }

    public boolean hasRole(Player player) {
        return assignedRoles.containsKey(player.getUniqueId());
    }

    public void clearRoles() {
        assignedRoles.clear();
    }

    public void pickRoles(Arena arena) {
        List<Player> lobby = new ArrayList<>(arena.getPlayers());
        Collections.shuffle(lobby);
        List<Player> availablePool = new ArrayList<>(lobby);
        RoleManager.performForSpecialRoles(role -> pickSpecialRole(availablePool, role, arena.getRoleLimits().get(role)));
        for (Player player : availablePool) {
            assignRole(player, Role.INNOCENT);
        }
    }

    public void pickSpecialRole(List<Player> availablePool, Role role, int maxRoleCount) {
        List<Player> unpickedPlayers = new ArrayList<>(availablePool);
        for (int i = 0; i < maxRoleCount; i++) {
            int totalWeight = 0;
            for (Player current : availablePool) totalWeight += ChanceUtilities.getRoleChance(current, role);
            if (totalWeight <= 0) totalWeight = 1;

            int randomDraw = ThreadLocalRandom.current().nextInt(totalWeight);
            int currentCount = 0;
            Player chosen = null;
            for (Player current : availablePool) {
                currentCount += ChanceUtilities.getRoleChance(current, role);
                if (currentCount > randomDraw) {
                    chosen = current;
                    break;
                }
            }
            if (chosen == null) chosen = availablePool.getFirst();

            assignSpecialRole(chosen, role);
            // player now has a role, remove them from pools
            availablePool.remove(chosen);
            unpickedPlayers.remove(chosen);
        }
        // up the chances of anyone who didn't get a special role
        for (Player current : unpickedPlayers) ChanceUtilities.increaseRoleChance(current, role);
    }

    private void assignSpecialRole(Player player, Role role) {
        assignRole(player, role);
        switch (role) {
            case MURDERER -> DaggerManager.give(player);
            case DETECTIVE -> RevolverManager.give(player);
        }
        ChanceUtilities.decreaseRoleChance(player, role);
    }

}
