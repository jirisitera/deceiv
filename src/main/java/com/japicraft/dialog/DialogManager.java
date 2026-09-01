package com.japicraft.dialog;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DialogManager implements Listener {
    @EventHandler
    void onPlayerCustomClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection gameConnection)) {
            return;
        }
        Player player = gameConnection.getPlayer();
        switch (event.getIdentifier().value()) {
            case "purchase_grenade": {
                player.sendMessage(Component.text("Bought a grenade!"));
                break;
            }
            case "purchase_body_bag": {
                player.sendMessage(Component.text("Bought a body bag!"));
                break;
            }
        }
    }
}
