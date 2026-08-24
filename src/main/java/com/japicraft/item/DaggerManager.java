package com.japicraft.item;

import com.japicraft.Deceiv;
import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.game.Role;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerUtilities;
import com.japicraft.sound.SoundUtilities;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import io.papermc.paper.datacomponent.item.UseEffects;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DaggerManager {
    public static final String MODEL = "dagger";
    private static final double RADIUS = 1.5;
    private static final int STAB_WARMUP_TICKS = 10;
    private static final int THROW_WARMUP_TICKS = 5;
    private static final int MAX_THROW_DURATION = 100;
    private static final int THROW_COOLDOWN = 200;
    private static final int STAB_COOLDOWN = 1200;
    private static final String PREPARE_SOUND = "dagger.prepare";
    private static final String THROW_SOUND = "dagger.throw";
    private static final String STAB_SOUND = "dagger.stab";
    private final Deceiv plugin;
    private final AnimationManager animationManager;

    public DaggerManager(Deceiv plugin, AnimationManager animationManager) {
        this.plugin = plugin;
        this.animationManager = animationManager;
    }

    public static void give(Player player) {
        ItemStack item = ItemStack.of(Material.ECHO_SHARD);
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
            .animation(ItemUseAnimation.TRIDENT)
            .hasConsumeParticles(false)
            .sound(ItemUtilities.EMPTY_KEY)
            .consumeSeconds(999.0F)
            .build());
        item.setData(DataComponentTypes.USE_EFFECTS, UseEffects.useEffects()
            .canSprint(true)
            .speedMultiplier(1.0F)
            .build());
        item.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
            .duration(7)
            .build());
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Dagger").color(Role.MURDERER.getColor()));
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, DaggerManager.MODEL));
        // give item safely, as to not reveal the murderer
        PlayerInventory inventory = player.getInventory();
        inventory.setHeldItemSlot(0);
        inventory.setItem(4, item);
    }

    public void handleInteract(Player player, Action action, ItemStack item) {
        if (action.isLeftClick()) {
            // animation lock
            if (animationManager.isPlayerAnimationLocked(player)) return;
            animationManager.lockPlayerAnimation(player);
            // dagger throw ability
            ItemUtilities.applyItemCooldown(player, item, DaggerManager.THROW_COOLDOWN);
            animationManager.showInitialItemWarmup(player, DaggerManager.THROW_WARMUP_TICKS);
            // start
            player.getScheduler().runAtFixedRate(plugin, createDaggerWindup(player, item, new AtomicInteger()), null, 1, 1);
        } else if (action.isRightClick()) {
            // animation lock
            if (animationManager.isPlayerAnimationLocked(player)) return;
            animationManager.lockPlayerAnimation(player);
            // dagger kill aura ability
            SoundUtilities.playAsPlayerNearby(player, DaggerManager.PREPARE_SOUND, 1.0F);
            animationManager.showInitialItemWarmup(player, DaggerManager.STAB_WARMUP_TICKS);
            // start kill aura loop task
            player.getScheduler().runAtFixedRate(plugin, createKillAura(player, item, new AtomicInteger()), null, 1, 1);
        }
    }

    private Consumer<ScheduledTask> createDaggerWindup(Player player, ItemStack item, AtomicInteger warmupCounter) {
        return task -> {
            if (!ItemUtilities.isHoldingItem(player.getInventory(), DaggerManager.MODEL)) {
                animationManager.endRepeatingAnimationTask(task, player);
                return;
            }
            boolean isItemWarmedUp = animationManager.isItemWarmedUp(warmupCounter, DaggerManager.THROW_WARMUP_TICKS);
            animationManager.showAndIncrementItemWarmup(player, warmupCounter, THROW_WARMUP_TICKS, isItemWarmedUp, Role.MURDERER.getColor(), DaggerManager.THROW_SOUND, 1.0F);
            if (isItemWarmedUp) {
                ItemUtilities.applyItemCooldown(player, item, DaggerManager.THROW_COOLDOWN);
                throwDagger(player);
                animationManager.endRepeatingAnimationTask(task, player);
            }
        };
    }

    private void throwDagger(Player player) {
        Snowball projectile = player.launchProjectile(Snowball.class);
        // ensure projectile itself is invisible
        projectile.setItem(ItemStack.of(Material.AIR));
        // attached model will follow this rotation
        projectile.setRotation(player.getYaw(), player.getPitch());
        // attach model to projectile
        animationManager.playProjectileThrowAnimation(projectile);
        // remove the projectile when it lives too long
        projectile.getScheduler().runDelayed(plugin, _ -> {
            if (projectile.isValid()) {
                projectile.remove();
            }
        }, null, DaggerManager.MAX_THROW_DURATION);
    }

    private Consumer<ScheduledTask> createKillAura(Player player, ItemStack item, AtomicInteger warmupCounter) {
        return task -> {
            if (!ItemUtilities.isItemWithModel(player.getActiveItem(), DaggerManager.MODEL)) {
                // dagger is not equipped anymore
                animationManager.endRepeatingAnimationTask(task, player);
                return;
            }
            boolean isItemWarmedUp = animationManager.isItemWarmedUp(warmupCounter, DaggerManager.STAB_WARMUP_TICKS);
            animationManager.showAndIncrementItemWarmup(player, warmupCounter, DaggerManager.STAB_WARMUP_TICKS, isItemWarmedUp, Role.MURDERER.getColor(), DaggerManager.PREPARE_SOUND, 2.0F);
            if (isItemWarmedUp) {
                for (Player victim : player.getLocation().getNearbyPlayers(DaggerManager.RADIUS, 0.25)) {
                    if (victim == player || PlayerUtilities.isPlayerInvulnerable(victim)) {
                        // cannot kill self
                        continue;
                    }
                    Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(victim, player, AnimationManager.DEATH_DAGGER));
                    SoundUtilities.playAsPlayerNearby(player, DaggerManager.STAB_SOUND, 1.0F);
                    // put item on cooldown
                    ItemUtilities.applyItemCooldown(player, item, DaggerManager.STAB_COOLDOWN);
                    player.clearActiveItem();
                    animationManager.endRepeatingAnimationTask(task, player);
                    break;
                }
            }
        };
    }
}
