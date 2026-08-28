package com.japicraft.command;

import com.japicraft.game.GameInstance;
import org.bukkit.entity.Player;

public record PlayerInstanceRelation(Player player, GameInstance instance) {
}
