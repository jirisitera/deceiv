package com.japicraft.event;

import com.japicraft.manager.PlayerManager;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerSwapHandItems implements Listener {
    @EventHandler
    public void onPlayerSwap(PlayerSwapHandItemsEvent event) {
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();
        if (mainHand.getType() == Material.AIR && offHand.getType() == Material.AIR) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            // give speed
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 2, false, false, false));
            // animation lock
            if (PlayerManager.isPlayerAnimationLocked(player)) return;
            PlayerManager.lockPlayerAnimation(player);
            // run the animation on the player
            BetterModel.limb(PlayerManager.MODEL)
                .map(renderer -> renderer.getOrCreate(BukkitAdapter.adapt(player)))
                .ifPresent(tracker -> tracker.animate(PlayerManager.ROLL_ANIMATION, AnimationModifier.DEFAULT, () -> {
                    PlayerManager.unlockPlayerAnimation(player);
                    tracker.close();
                }));
        }
    }
}
