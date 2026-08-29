package com.japicraft.item;

import com.japicraft.Deceiv;
import com.japicraft.event.PlayerFinishUsingItemEvent;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerItemRelation;
import com.japicraft.player.PlayerUtilities;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemManager implements Listener {
    private static final int DROP_COOLDOWN = 20;
    private final List<ItemHandler> handlers = new ArrayList<>();
    private final AnimationManager animationManager;
    private final ItemWindUpService itemWindUpService;

    public ItemManager(Deceiv plugin, AnimationManager animationManager) {
        this.animationManager = animationManager;
        this.itemWindUpService = new ItemWindUpService(plugin, animationManager);

        registerHandler(new DaggerManager(itemWindUpService, plugin, animationManager));
        registerHandler(new RevolverManager(itemWindUpService));
    }

    public void registerHandler(ItemHandler handler) {
        this.handlers.add(handler);
    }

    @EventHandler
    public void onPlayerInteractWithItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (PlayerUtilities.isPlayerInvulnerable(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || ItemUtilities.isOnCooldown(player, item)) {
            return;
        }
        for (ItemHandler handler : handlers) {
            if (handler.compare(item)) {
                handler.handleInteract(player, event.getAction(), item);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerStopUsingItem(PlayerStopUsingItemEvent event) {
        Player player = event.getPlayer();
        if (!animationManager.isPlayerAnimationLocked(player)) {
            return;
        }
        UniqueItem unique = itemWindUpService.getReadyItem(player);
        ItemStack item = event.getItem();
        itemWindUpService.clear(player);
        // run ability action if applicable
        if (unique == null || !ItemUtilities.isHoldingItem(player.getInventory(), unique) || ItemUtilities.isOnCooldown(player, item)) {
            return;
        }
        unique.getAbility().action().accept(new PlayerItemRelation(player, item));
    }

    @EventHandler
    public void onPlayerChangeHeldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!animationManager.isPlayerAnimationLocked(player)) {
            return;
        }
        itemWindUpService.clear(player);
        applyUniqueDropCooldown(player, player.getInventory().getItem(event.getPreviousSlot()));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!animationManager.isPlayerAnimationLocked(player)) {
            return;
        }
        itemWindUpService.clear(player);
        applyUniqueDropCooldown(player, event.getItemDrop().getItemStack());
    }

    @EventHandler
    public void onPlayerFinishUsingItem(PlayerFinishUsingItemEvent event) {
        itemWindUpService.clear(event.getPlayer());
    }

    private void applyUniqueDropCooldown(Player player, ItemStack item) {
        // usable items get a brief cooldown when action is canceled
        if (UniqueItem.fromItem(item) == null) {
            return;
        }
        ItemUtilities.applyItemCooldown(player, item, ItemManager.DROP_COOLDOWN);
    }
}
