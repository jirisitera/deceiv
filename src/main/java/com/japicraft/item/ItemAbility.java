package com.japicraft.item;

import com.japicraft.player.PlayerItemRelation;

import java.util.function.Consumer;

public record ItemAbility(Consumer<PlayerItemRelation> action) {
}
