package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.game.Role;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
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
        EntityScheduler scheduler = player.getScheduler();
        Component playerHead = Component.object(ObjectContents.playerHead(player.getUniqueId()));
        // show base title
        player.showTitle(BASE_TRANSITION_TITLE);
        // after partly transitioned, show unknown role text
        scheduler.runDelayed(Deceiv.getPlugin(), _ -> player.sendTitlePart(TitlePart.SUBTITLE, playerHead.append(ROLE_COMPONENT).append(UNKNOWN_COMPONENT)), null, TRANSITION_FADE_HALF);
        // during transition, reveal role and change background color
        scheduler.runDelayed(Deceiv.getPlugin(), _ -> player.showTitle(buildRevealTitle(role, playerHead)), null, TRANSITION_FADE_HALF + TRANSITION_STAY_HALF);
        // right before the end of transition, remove role reveal text
        scheduler.runDelayed(Deceiv.getPlugin(), _ -> player.sendTitlePart(TitlePart.SUBTITLE, Component.empty()), null, TRANSITION_FADE + TRANSITION_STAY + TRANSITION_FADE_HALF);
    }

    private static Title buildRevealTitle(Role role, Component playerHead) {
        return Title.title(TRANSITION_LOADING_COMPONENT.color(role.getColor()), playerHead.append(ROLE_COMPONENT).append(role.getDisplayName()), 0, TRANSITION_STAY, TRANSITION_FADE);
    }
}
