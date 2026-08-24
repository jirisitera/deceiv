package com.japicraft.game;

import com.japicraft.Deceiv;
import com.japicraft.ui.ColorUtilities;
import com.japicraft.ui.CountdownInterface;
import com.japicraft.ui.InterfaceUtilities;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class GameManager implements Listener {
    private final Deceiv plugin;
    private final RoleManager roleManager;
    private final CountdownInterface countdownInterface;
    private final Arena arena;
    private boolean isRoundInProgress = false;

    public GameManager(Deceiv plugin, ArenaLimits arenaLimits) {
        this.plugin = plugin;
        this.countdownInterface = new CountdownInterface();
        this.roleManager = new RoleManager();
        this.arena = new Arena(arenaLimits);
    }

    public int getRequiredPlayerCount() {
        return arena.getRequiredPlayerCount();
    }

    public Role getPlayerRole(Player player) {
        return roleManager.getRole(player);
    }

    public List<Player> getPlayers() {
        return arena.getPlayers();
    }

    public boolean addPlayer(Player player) {
        boolean state = arena.join(player);
        countdownInterface.show(player);
        scheduleActionBar(player);
        return state;
    }

    public void removePlayer(Player player) {
        countdownInterface.hide(player);
        InterfaceUtilities.clearActionBar(player);
        arena.leave(player);
    }

    public void delete() {
        endRound();
        InterfaceUtilities.clearActionBar(Audience.audience(arena.getPlayers()));
        countdownInterface.hide(Audience.audience(getPlayers()));
        arena.delete();
    }

    public void recalculateArenaChances() {
        roleManager.recalculateRoleChances(arena);
    }

    public void scheduleActionBar(Player player) {
        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (!player.isOnline() || !arena.isInGame(player)) {
                task.cancel();
                return;
            }
            Component actionBarDisplay;
            if (roleManager.hasRole(player)) {
                actionBarDisplay = Component.text("You are a ").append(roleManager.getRole(player).getDisplayName());
            } else {
                actionBarDisplay = Component.object(ObjectContents.playerHead(player.getUniqueId()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Murderer: " + roleManager.getRelativeChance(player, Role.MURDERER) + "%").color(Role.MURDERER.getColor()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Detective: " + roleManager.getRelativeChance(player, Role.DETECTIVE) + "%").color(Role.DETECTIVE.getColor()));
            }
            player.sendActionBar(actionBarDisplay);
        }, null, 1, 30);
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
        if (!isRoundInProgress()) {
            return false;
        }
        isRoundInProgress = false;
        roleManager.clearRoles();
        countdownInterface.hide(Audience.audience(getPlayers()));
        return true;
    }
}
