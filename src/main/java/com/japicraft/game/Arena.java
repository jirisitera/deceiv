package com.japicraft.game;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Arena {
    private final List<Player> activePlayers = new ArrayList<>();
    private final ArenaLimits arenaLimits;

    public Arena(ArenaLimits arenaLimits) {
        this.arenaLimits = arenaLimits;
    }

    public boolean join(Player player) {
        if (activePlayers.size() >= arenaLimits.maxPlayerCount() || activePlayers.contains(player)) {
            return false;
        }
        activePlayers.add(player);
        return true;
    }

    public void leave(Player player) {
        activePlayers.remove(player);
    }

    public boolean isPresent(Player player) {
        return activePlayers.contains(player);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(activePlayers);
    }

    public Map<Role, Integer> getRoleLimits() {
        return arenaLimits.roleLimits();
    }

    public int getRequiredPlayerCount() {
        return arenaLimits.requiredPlayerCount();
    }
}
