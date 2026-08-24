package com.japicraft.container;

import com.japicraft.Deceiv;
import com.japicraft.game.ArenaLimits;
import com.japicraft.game.GameManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameContainer {
    private final Map<Key, GameManager> instances = new HashMap<>();
    private final Map<UUID, GameManager> playerTracker = new HashMap<>();
    private final Deceiv plugin;

    public GameContainer(Deceiv plugin) {
        this.plugin = plugin;
    }

    public boolean addPlayerToGame(Player player, GameManager game) {
        if (playerTracker.containsKey(player.getUniqueId())) {
            return false;
        }
        playerTracker.put(player.getUniqueId(), game);
        return game.addPlayer(player);
    }

    public Set<Key> getInstanceKeys() {
        return instances.keySet();
    }

    public boolean removePlayerFromGame(Player player, GameManager instance) {
        GameManager compareInstance = playerTracker.get(player.getUniqueId());
        if (!instance.equals(compareInstance)) {
            return false;
        }
        playerTracker.remove(player.getUniqueId());
        instance.removePlayer(player);
        return true;
    }

    public GameManager getGameInstance(Player player) {
        return playerTracker.get(player.getUniqueId());
    }

    public GameManager getGameInstance(Key key) {
        return instances.get(key);
    }

    public boolean createGame(Key key, ArenaLimits arenaLimits) {
        if (instances.containsKey(key)) {
            return false;
        }
        GameManager instance = new GameManager(plugin, arenaLimits);
        instances.put(key, instance);
        return true;
    }

    public boolean deleteGame(Key key) {
        GameManager instance = getGameInstance(key);
        if (instance == null) {
            return false;
        }
        instance.delete();
        instances.remove(key);
        return true;
    }
}
