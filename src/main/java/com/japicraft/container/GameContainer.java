package com.japicraft.container;

import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.game.ArenaLimits;
import com.japicraft.game.GameInstance;
import com.japicraft.game.Role;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameContainer implements Listener {
    private final Map<Key, GameInstance> instances = new HashMap<>();
    private final Map<Player, GameInstance> playerTracker = new HashMap<>();

    public boolean addPlayerToGame(Player player, GameInstance game) {
        if (playerTracker.containsKey(player)) {
            return false;
        }
        playerTracker.put(player, game);
        return game.addPlayer(player);
    }

    public Set<Key> getInstanceKeys() {
        return instances.keySet();
    }

    public boolean removePlayerFromGame(Player player, GameInstance instance) {
        GameInstance compareInstance = playerTracker.get(player);
        if (!instance.equals(compareInstance)) {
            return false;
        }
        playerTracker.remove(player);
        instance.removePlayer(player);
        return true;
    }

    @Nullable
    public GameInstance getGameInstance(Player player) {
        return playerTracker.get(player);
    }

    @Nullable
    public GameInstance getGameInstance(Key key) {
        return instances.get(key);
    }

    public boolean createGame(Key key, ArenaLimits arenaLimits) {
        if (instances.containsKey(key)) {
            return false;
        }
        GameInstance instance = new GameInstance(arenaLimits);
        instances.put(key, instance);
        return true;
    }

    public boolean deleteGame(Key key) {
        GameInstance instance = getGameInstance(key);
        if (instance == null) {
            return false;
        }
        instance.endRound();
        instance.getPlayers().forEach(player -> removePlayerFromGame(player, instance));
        instances.remove(key);
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameInstance gameInstance = getGameInstance(player);
        if (gameInstance == null) {
            return;
        }
        removePlayerFromGame(player, gameInstance);
    }

    @EventHandler
    public void onPlayerElimination(PlayerEliminateEvent event) {
        // get game instance
        GameInstance gameInstance = getGameInstance(event.getVictim());
        if (gameInstance == null || !gameInstance.equals(getGameInstance(event.getKiller()))) {
            return;
        }

        // give kill credit
        event.getKiller().sendMessage(Component.text("Killed ").append(event.getVictim().name()));

        // check if the game is supposed to end
        boolean noMurderers = true;
        boolean onlyMurderers = true;
        for (Player player : gameInstance.getPlayers()) {
            Role role = gameInstance.getPlayerRole(player);
            if (role == null) {
                continue;
            }
            if (role.equals(Role.MURDERER)) {
                noMurderers = false;
            } else {
                onlyMurderers = false;
            }
        }
        if (noMurderers || onlyMurderers) {
            gameInstance.endRound();
        }
    }
}
