package com.japicraft.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerEliminateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player victim;
    private final Player killer;
    private final String reason;

    public PlayerEliminateEvent(Player victim, Player killer, String reason) {
        this.victim = victim;
        this.killer = killer;
        this.reason = reason;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getVictim() {
        return victim;
    }

    public Player getKiller() {
        return killer;
    }

    public String getReason() {
        return reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
