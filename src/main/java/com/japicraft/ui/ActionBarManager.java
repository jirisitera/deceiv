package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.game.GameInstance;
import com.japicraft.game.Role;
import com.japicraft.player.MoodManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.entity.Player;

public class ActionBarManager {
    public static void schedule(Player player, Deceiv plugin, GameInstance gameInstance) {
        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (!gameInstance.isPresent(player)) {
                task.cancel();
                return;
            }
            Component actionBarDisplay;
            if (gameInstance.hasRole(player)) {
                actionBarDisplay = Component.text(gameInstance.getMood(player).getModel()).font(MoodManager.FONT);
            } else {
                actionBarDisplay = Component.object(ObjectContents.playerHead(player.getUniqueId()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Murderer: " + gameInstance.getRelativeChance(player, Role.MURDERER) + "%").color(Role.MURDERER.getColor()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Detective: " + gameInstance.getRelativeChance(player, Role.DETECTIVE) + "%").color(Role.DETECTIVE.getColor()));
            }
            player.sendActionBar(actionBarDisplay);
        }, null, 1, 30);
    }

    public static void clear(Audience audience) {
        audience.sendActionBar(Component.empty());
    }

}
