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
    public static void schedule(Player player, GameInstance gameInstance) {
        player.getScheduler().runAtFixedRate(Deceiv.getPlugin(), task -> {
            if (!gameInstance.isPresent(player)) {
                task.cancel();
                return;
            }
            Component display;
            if (gameInstance.hasRole(player)) {
                display = MoodManager.SPRITE_SHEET.color(gameInstance.getMood(player).getIdentifier());
            } else {
                display = Component.object(ObjectContents.playerHead(player.getUniqueId()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Murderer: " + gameInstance.getRelativeChance(player, Role.MURDERER) + "%").color(Role.MURDERER.getColor()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Detective: " + gameInstance.getRelativeChance(player, Role.DETECTIVE) + "%").color(Role.DETECTIVE.getColor()));
            }
            player.sendActionBar(display);
        }, null, 1, 30);
    }

    public static void clear(Audience audience) {
        audience.sendActionBar(Component.empty());
    }

}
