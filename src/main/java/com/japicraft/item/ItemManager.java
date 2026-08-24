package com.japicraft.item;

import com.japicraft.Deceiv;
import com.japicraft.player.AnimationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
    private final DaggerManager daggerManager;
    private final RevolverManager revolverManager;

    public ItemManager(Deceiv plugin, AnimationManager animationManager) {
        this.daggerManager = new DaggerManager(plugin, animationManager);
        this.revolverManager = new RevolverManager(plugin, animationManager);
    }

    public void handleDaggerInteract(Player player, Action action, ItemStack item) {
        daggerManager.handleInteract(player, action, item);
    }

    public void handleRevolverInteract(Player player, Action action, ItemStack item) {
        revolverManager.handleInteract(player, action, item);
    }
}
