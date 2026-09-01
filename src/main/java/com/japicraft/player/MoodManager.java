package com.japicraft.player;

import com.japicraft.Deceiv;
import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.game.GameInstance;
import com.japicraft.game.Role;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class MoodManager {
    public static final Component SPRITE_SHEET = Component.text("|").font(Key.key(Deceiv.PLUGIN_ID, "mood"));
    private static final int MAX_SAFE_SEVERITY = 1;
    private final Map<Player, Mood> moods = new HashMap<>();
    private final GameInstance gameInstance;

    public MoodManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void set(Player player, Mood mood) {
        moods.put(player, mood);
    }

    public Mood get(Player player) {
        return moods.get(player);
    }

    public void scheduleMoodController(Player player) {
        player.getScheduler().runAtFixedRate(Deceiv.getPlugin(), task -> {
            if (!gameInstance.isPresent(player)) {
                task.cancel();
                return;
            }
            if (get(player).getSeverity() > MoodManager.MAX_SAFE_SEVERITY) {
                return;
            }
            player.getLocation().getNearbyPlayers(5).forEach(target -> {
                Role role = gameInstance.getPlayerRole(target);
                if (role == null) {
                    return;
                }
                if (role.equals(Role.MURDERER) || role.equals(Role.DETECTIVE)) {
                    set(player, Mood.NERVOUS);
                } else {
                    set(player, Mood.CALM);
                }
            });
        }, null, 40, 40);
    }

    @EventHandler
    public void onPlayerElimination(PlayerEliminateEvent event) {
        event.getVictim().getLocation().getNearbyPlayers(10).forEach(player -> set(player, Mood.TERRIFIED));
    }
}
