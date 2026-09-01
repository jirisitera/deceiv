package com.japicraft.game;

import com.japicraft.player.Mood;
import com.japicraft.player.MoodManager;
import com.japicraft.ui.ActionBarManager;
import com.japicraft.ui.InfoBoardManager;
import com.japicraft.ui.InterfaceUtilities;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GameInstance implements Listener {
    private final Arena arena;
    private final RoleManager roleManager;
    private final MoodManager moodManager;
    private final InfoBoardManager infoBoardManager;
    private boolean isRoundInProgress = false;

    public GameInstance(ArenaLimits arenaLimits) {
        this.roleManager = new RoleManager();
        this.arena = new Arena(arenaLimits);
        this.moodManager = new MoodManager(this);
        this.infoBoardManager = new InfoBoardManager(this);
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
        ActionBarManager.schedule(player, this);
        infoBoardManager.initialize();
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
        infoBoardManager.hide(player);
        ActionBarManager.clear(player);
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

        for (Player player : getPlayers()) {
            InterfaceUtilities.showTransition(player, roleManager.getRole(player));

            moodManager.set(player, Mood.CALM);
            moodManager.scheduleMoodController(player);

            infoBoardManager.show(player);
        }
        return true;
    }

    public boolean isRoundInProgress() {
        return isRoundInProgress;
    }

    public boolean endRound() {
        ActionBarManager.clear(Audience.audience(getPlayers()));
        for (Player player : getPlayers()) {
            infoBoardManager.hide(player);
        }
        infoBoardManager.remove();
        roleManager.clearRoles();
        // only perform if there is an actual round in progress
        if (!isRoundInProgress()) {
            return false;
        }
        isRoundInProgress = false;
        return true;
    }
}
