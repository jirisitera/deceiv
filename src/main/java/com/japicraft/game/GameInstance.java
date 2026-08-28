package com.japicraft.game;

import com.japicraft.Deceiv;
import com.japicraft.player.Mood;
import com.japicraft.player.MoodManager;
import com.japicraft.ui.ActionBarManager;
import com.japicraft.ui.CountdownInterface;
import com.japicraft.ui.InterfaceUtilities;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GameInstance implements Listener {
    private final Deceiv plugin;
    private final Arena arena;
    private final CountdownInterface countdownInterface;
    private final RoleManager roleManager;
    private final MoodManager moodManager;
    private boolean isRoundInProgress = false;

    public GameInstance(Deceiv plugin, ArenaLimits arenaLimits) {
        this.plugin = plugin;
        this.arena = new Arena(arenaLimits);
        this.countdownInterface = new CountdownInterface();
        this.moodManager = new MoodManager();
        this.roleManager = new RoleManager();
    }

    public int getRequiredPlayerCount() {
        return arena.getRequiredPlayerCount();
    }

    @Nullable
    public Role getPlayerRole(Player player) {
        return roleManager.getRole(player);
    }

    public boolean isPresent(Player player) {
        return arena.isPresent(player);
    }

    public Mood getMood(Player player) {
        return moodManager.get(player);
    }

    public List<Player> getPlayers() {
        return arena.getPlayers();
    }

    public boolean addPlayer(Player player) {
        boolean state = arena.join(player);
        countdownInterface.show(player);
        ActionBarManager.schedule(player, plugin, this);
        moodManager.set(player, Mood.CALM);
        return state;
    }

    public int getRelativeChance(Player player, Role role) {
        return roleManager.getRelativeChance(player, role);
    }

    public boolean hasRole(Player player) {
        return roleManager.hasRole(player);
    }

    public void removePlayer(Player player) {
        arena.leave(player);
        countdownInterface.hide(player);
        ActionBarManager.clear(player);
    }

    public void delete() {
        endRound();
        arena.delete();
    }

    public void recalculateArenaChances() {
        roleManager.recalculateRoleChances(arena);
    }

    public boolean startRound() {
        if (getPlayers().size() < getRequiredPlayerCount()) {
            return false;
        }
        isRoundInProgress = true;

        roleManager.pickRoles(arena);
        countdownInterface.initialize(plugin, this);

        for (Player player : getPlayers()) {
            InterfaceUtilities.showTransition(player, roleManager.getRole(player), plugin);
        }
        return true;
    }

    public boolean isRoundInProgress() {
        return isRoundInProgress;
    }

    public boolean endRound() {
        countdownInterface.hide(Audience.audience(getPlayers()));
        ActionBarManager.clear(Audience.audience(getPlayers()));
        // only perform if there is an actual round in progress
        if (!isRoundInProgress()) {
            return false;
        }
        isRoundInProgress = false;
        roleManager.clearRoles();
        return true;
    }
}
