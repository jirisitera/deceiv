package com.japicraft.event;

import com.japicraft.manager.KnifeManager;
import com.japicraft.manager.PlayerManager;
import com.japicraft.manager.RevolverManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (PlayerManager.isPlayerInvulnerable(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Key model = item.getData(DataComponentTypes.ITEM_MODEL);
        if (model == null) {
            return;
        }
        if (player.getCooldown(item) > 0) {
            return;
        }
        Action action = event.getAction();
        String modelValue = model.value();
        switch (modelValue) {
            case KnifeManager.MODEL:
                KnifeManager.handleInteract(player, action, item);
                break;
            case RevolverManager.MODEL:
                RevolverManager.handleInteract(player, action, item);
                break;
        }
    }
}
