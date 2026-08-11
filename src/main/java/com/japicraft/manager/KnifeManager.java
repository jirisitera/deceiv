package com.japicraft.manager;

import com.japicraft.Deceiv;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import io.papermc.paper.datacomponent.item.UseEffects;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KnifeManager {
    public static final String MODEL = "knife";
    private static final String THROW_ANIMATION = "spin";
    private static final double RADIUS = 1.5;
    private static final int MAX_WARMUP_TICKS = 10;
    private static final int MAX_THROW_DURATION = 100;
    private static final int THROW_COOLDOWN = 200;
    private static final int STAB_COOLDOWN = 1200;
    private static final TextColor READY_COLOR = TextColor.color(175, 8, 8);
    private static final String PREPARE_SOUND = "knife.prepare";
    private static final String THROW_SOUND = "knife.throw";
    private static final String STAB_SOUND = "knife.stab";

    public static void give(Player player) {
        ItemStack item = ItemStack.of(Material.ECHO_SHARD);
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
            .animation(ItemUseAnimation.TRIDENT)
            .hasConsumeParticles(false)
            .sound(ItemManager.EMPTY_KEY)
            .consumeSeconds(999.0F)
            .build());
        item.setData(DataComponentTypes.USE_EFFECTS, UseEffects.useEffects()
            .canSprint(true)
            .speedMultiplier(1.0F)
            .build());
        item.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
            .duration(7)
            .build());
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Knife").color(NamedTextColor.WHITE));
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, KnifeManager.MODEL));
        player.give(item);
    }

    public static void handleInteract(Player player, Action action, ItemStack item) {
        if (action.isLeftClick()) {
            // knife throw ability
            SoundManager.playAsPlayerNearby(player, THROW_SOUND, 1.0F);
            ItemManager.applyItemCooldown(player, item, KnifeManager.THROW_COOLDOWN);
            KnifeManager.throwKnife(player);
        } else if (action.isRightClick()) {
            // animation lock
            if (PlayerManager.isPlayerAnimationLocked(player)) return;
            PlayerManager.lockPlayerAnimation(player);
            // knife kill aura ability
            SoundManager.playAsPlayerNearby(player, KnifeManager.PREPARE_SOUND, 1.0F);
            PlayerManager.showInitialItemWarmup(player, KnifeManager.MAX_WARMUP_TICKS);
            // start kill aura loop task
            player.getScheduler().runAtFixedRate(Deceiv.plugin, KnifeManager.createKillAura(player, item, new AtomicInteger()), null, 1, 1);
        }
    }

    private static void throwKnife(Player player) {
        Snowball projectile = player.launchProjectile(Snowball.class);
        // ensure projectile itself is invisible
        projectile.setItem(ItemStack.of(Material.AIR));
        // attached model will follow this rotation
        projectile.setRotation(player.getYaw(), player.getPitch());
        // attach model to projectile
        BetterModel.model(KnifeManager.MODEL)
            .map(renderer -> renderer.getOrCreate(BukkitAdapter.adapt(projectile)))
            .ifPresent(tracker -> tracker.animate(KnifeManager.THROW_ANIMATION));
        projectile.getScheduler().runDelayed(Deceiv.plugin, _ -> {
            if (projectile.isValid()) {
                projectile.remove();
            }
        }, null, KnifeManager.MAX_THROW_DURATION);
    }

    private static Consumer<ScheduledTask> createKillAura(Player player, ItemStack item, AtomicInteger warmupCounter) {
        return task -> {
            Key model = player.getActiveItem().getData(DataComponentTypes.ITEM_MODEL);
            if (model == null || !model.value().equals(KnifeManager.MODEL)) {
                // knife is not equipped anymore
                PlayerManager.unlockPlayerAnimation(player);
                PlayerManager.clearActionBar(player);
                task.cancel();
                return;
            }
            boolean isItemWarmedUp = PlayerManager.isItemWarmedUp(warmupCounter, KnifeManager.MAX_WARMUP_TICKS);
            PlayerManager.showItemWarmup(player, warmupCounter, KnifeManager.MAX_WARMUP_TICKS, isItemWarmedUp, KnifeManager.READY_COLOR, KnifeManager.PREPARE_SOUND, 2.0F);
            if (!isItemWarmedUp) {
                return;
            }
            for (Player victim : player.getLocation().getNearbyPlayers(KnifeManager.RADIUS, 0.25)) {
                if (victim == player || PlayerManager.isPlayerInvulnerable(victim)) {
                    // cannot kill self
                    continue;
                }
                PlayerManager.kill(victim, player);
                SoundManager.playAsPlayerNearby(player, KnifeManager.STAB_SOUND, 1.0F);
                // put item on cooldown
                ItemManager.applyItemCooldown(player, item, KnifeManager.STAB_COOLDOWN);
                player.clearActiveItem();
                // end task
                PlayerManager.unlockPlayerAnimation(player);
                PlayerManager.clearActionBar(player);
                task.cancel();
                return;
            }
        };
    }
}
