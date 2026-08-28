package com.japicraft.container;

import com.japicraft.Deceiv;
import com.japicraft.game.ArenaLimits;
import com.japicraft.game.GameInstance;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameContainer {
    private final Map<Key, GameInstance> instances = new HashMap<>();
    private final Map<Player, GameInstance> playerTracker = new HashMap<>();
    private final Deceiv plugin;

    public GameContainer(Deceiv plugin) {
        this.plugin = plugin;
    }

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
        GameInstance instance = new GameInstance(plugin, arenaLimits);
        instances.put(key, instance);
        return true;
    }

    public boolean deleteGame(Key key) {
        GameInstance instance = getGameInstance(key);
        if (instance == null) {
            return false;
        }
        instance.delete();
        instances.remove(key);
        return true;
    }
}
