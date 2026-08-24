package com.japicraft.game;

import java.util.Map;

public record ArenaLimits(int requiredPlayerCount, int maxPlayerCount, Map<Role, Integer> roleLimits) {
    public ArenaLimits {
        roleLimits = Map.copyOf(roleLimits);
    }
}
