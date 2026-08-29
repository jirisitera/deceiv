package com.japicraft.player;

import com.japicraft.Deceiv;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MoodManager {
    public static final Key FONT = Key.key(Deceiv.PLUGIN_ID, "mood");
    private final Map<Player, Mood> moods = new HashMap<>();

    public void set(Player player, Mood mood) {
        moods.put(player, mood);
    }

    public Mood get(Player player) {
        return moods.get(player);
    }

    public void scheduleMoodController(Player player) {
        
    }
}
