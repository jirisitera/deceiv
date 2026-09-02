package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.game.GameInstance;
import com.japicraft.game.Role;
import com.japicraft.player.MoodManager;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.entity.Player;

public class InterfaceUtilities {
    public static final Component CLOSE_BUTTON = Component.text("Close Menu");
    public static final Key TRANSITION_FONT = Key.key(Deceiv.PLUGIN_ID, "transition");
    public static final String TRANSITION_BACK_SPACE = "/";
    public static final String TRANSITION_LOADING = "=";
    private static final int TRANSITION_FADE = 40;
    private static final int TRANSITION_STAY = 100;
    private static final int TRANSITION_FADE_HALF = TRANSITION_FADE / 2;
    private static final int TRANSITION_STAY_HALF = TRANSITION_STAY / 2;
    private static final Component TRANSITION_LOADING_COMPONENT = Component.text(TRANSITION_LOADING + TRANSITION_BACK_SPACE).font(TRANSITION_FONT);
    private static final Component ROLE_COMPONENT = Component.text(" | Role: ").color(ColorUtilities.WHITE);
    private static final Component UNKNOWN_COMPONENT = Component.text("???").color(ColorUtilities.GOLD);
    private static final Title BASE_TRANSITION_TITLE = Title.title(TRANSITION_LOADING_COMPONENT, Component.empty(), TRANSITION_FADE, TRANSITION_STAY, TRANSITION_FADE);

    public static void showTransition(Player player, Role role) {
        player.showTitle(BASE_TRANSITION_TITLE);

        Component playerHead = InterfaceUtilities.getHeadDisplay(player).append(ROLE_COMPONENT);
        EntityScheduler scheduler = player.getScheduler();

        scheduler.runDelayed(Deceiv.getPlugin(), _ -> {
            // after partly transitioned, show unknown role text
            player.sendTitlePart(TitlePart.SUBTITLE, playerHead.append(UNKNOWN_COMPONENT));
        }, null, TRANSITION_FADE_HALF);

        scheduler.runDelayed(Deceiv.getPlugin(), _ -> {
            // during transition, reveal role and change background color
            player.showTitle(Title.title(TRANSITION_LOADING_COMPONENT.color(role.getColor()), playerHead.append(role.getDisplayName()), 0, TRANSITION_STAY, TRANSITION_FADE));
        }, null, TRANSITION_FADE_HALF + TRANSITION_STAY_HALF);

        scheduler.runDelayed(Deceiv.getPlugin(), _ -> {
            // right before the end of transition, remove role reveal text
            player.clearTitle();
        }, null, TRANSITION_FADE + TRANSITION_STAY + TRANSITION_FADE_HALF);
    }

    public static void scheduleActionBar(Player player, GameInstance gameInstance) {
        player.getScheduler().runAtFixedRate(Deceiv.getPlugin(), task -> {
            if (!gameInstance.isPresent(player)) {
                task.cancel();
                return;
            }
            Component display;
            if (gameInstance.hasRole(player)) {
                display = MoodManager.SPRITE_SHEET.color(gameInstance.getMood(player).getIdentifier());
            } else {
                display = InterfaceUtilities.getHeadDisplay(player)
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Murderer: " + gameInstance.getRelativeChance(player, Role.MURDERER) + "%").color(Role.MURDERER.getColor()))
                    .append(Component.text(" | ").color(ColorUtilities.GRAY))
                    .append(Component.text("Detective: " + gameInstance.getRelativeChance(player, Role.DETECTIVE) + "%").color(Role.DETECTIVE.getColor()));
            }
            player.sendActionBar(display);
        }, null, 1, 30);
    }

    public static void hideActionBar(Audience audience) {
        audience.sendActionBar(Component.empty());
    }

    private static Component getHeadDisplay(Player player) {
        return Component.object(ObjectContents.playerHead(player.getUniqueId()));
    }
}
