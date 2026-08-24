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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RevolverManager {
    public static final String MODEL = "revolver";
    private static final int SHOT_COOLDOWN = 200;
    private static final double SHOT_RANGE = 15.0;
    private static final double SHOT_THICKNESS = 0.2;
    private static final float SHOT_HEARING_DISTANCE = 100.0F;
    private static final int MAX_WARMUP_TICKS = 20;
    private static final double BULLET_LINE_SPACING = 0.5;
    private static final String PREPARE_SOUND = "revolver.prepare";
    private static final String READY_SOUND = "revolver.ready";
    private static final String SHOOT_SOUND = "revolver.shoot";
    private final Deceiv plugin;
    private final AnimationManager animationManager;

    public RevolverManager(Deceiv plugin, AnimationManager animationManager) {
        this.plugin = plugin;
        this.animationManager = animationManager;
    }

    public static void give(Player player) {
        ItemStack item = ItemStack.of(Material.NAUTILUS_SHELL);
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
            .animation(ItemUseAnimation.CROSSBOW)
            .hasConsumeParticles(false)
            .sound(ItemUtilities.EMPTY_KEY)
            .consumeSeconds(999.0F)
            .build());
        item.setData(DataComponentTypes.USE_EFFECTS, UseEffects.useEffects()
            .speedMultiplier(0.5F)
            .build());
        item.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
            .type(SwingAnimation.Animation.STAB)
            .duration(20)
            .build());
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Revolver").color(Role.DETECTIVE.getColor()));
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, RevolverManager.MODEL));
        player.give(item);
    }

    public void handleInteract(Player player, Action action, ItemStack item) {
        if (action.isRightClick()) {
            // animation lock
            if (animationManager.isPlayerAnimationLocked(player)) return;
            animationManager.lockPlayerAnimation(player);
            // revolver shoot ability
            SoundUtilities.playAsPlayerNearby(player, RevolverManager.PREPARE_SOUND, 1.0F);
            animationManager.showInitialItemWarmup(player, RevolverManager.MAX_WARMUP_TICKS);
            // start aim loop task
            player.getScheduler().runAtFixedRate(plugin, createAimLoop(player, item, new AtomicInteger()), null, 1, 1);
        }
    }

    private Consumer<ScheduledTask> createAimLoop(Player player, ItemStack item, AtomicInteger warmupCounter) {
        return task -> {
            boolean isItemWarmedUp = animationManager.isItemWarmedUp(warmupCounter, RevolverManager.MAX_WARMUP_TICKS);
            if (player.hasActiveItem()) {
                animationManager.showAndIncrementItemWarmup(player, warmupCounter, MAX_WARMUP_TICKS, isItemWarmedUp, Role.DETECTIVE.getColor(), RevolverManager.READY_SOUND, 1.0F);
                drawAim(player);
                return;
            }
            if (isItemWarmedUp && ItemUtilities.isHoldingItem(player.getInventory(), RevolverManager.MODEL)) {
                ItemUtilities.applyItemCooldown(player, item, RevolverManager.SHOT_COOLDOWN);
                shoot(player);
            }
            animationManager.endRepeatingAnimationTask(task, player);
        };
    }

    private Predicate<Entity> filter(Player shooter) {
        return entity -> entity instanceof Player player && player != shooter && !PlayerUtilities.isPlayerInvulnerable(player);
    }

    private void shoot(Player player) {
        // calculate hit
        Location location = player.getEyeLocation();
        RayTraceResult hit = location.getWorld().rayTrace(
            location, location.getDirection(), RevolverManager.SHOT_RANGE,
            FluidCollisionMode.NEVER, true, RevolverManager.SHOT_THICKNESS, filter(player)
        );
        // apply effects
        drawBulletLine(player);
        applyRecoil(player);
        SoundUtilities.playAsPlayerWithDistance(player, RevolverManager.SHOOT_SOUND, 1.0F, RevolverManager.SHOT_HEARING_DISTANCE);
        if (hit == null) {
            return;
        }
        // register hit
        if (hit.getHitEntity() instanceof Player victim && !PlayerUtilities.isPlayerInvulnerable(victim)) {
            Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(victim, player, AnimationManager.DEATH_REVOLVER));
        } else if (hit.getHitBlock() instanceof Block block) {
            World world = block.getWorld();
            world.spawnParticle(Particle.BLOCK, hit.getHitPosition().toLocation(world), 30, 0.0, 0.0, 0.0, block.getBlockData());
        }
    }

    private void drawAim(Player player) {
        Location location = player.getEyeLocation();
        location.add(location.getDirection().multiply(RevolverManager.SHOT_RANGE));
        player.spawnParticle(Particle.CRIT, location, 1, 0, 0, 0, 0);
    }

    private void drawBulletLine(Player player) {
        Location location = player.getEyeLocation();
        Vector step = location.getDirection().multiply(RevolverManager.BULLET_LINE_SPACING);
        for (double i = 0; i < RevolverManager.SHOT_RANGE; i += RevolverManager.BULLET_LINE_SPACING) {
            location.add(step);
            player.getWorld().spawnParticle(Particle.SMOKE, location, 3, 0, 0, 0, 0);
        }
    }

    private void applyRecoil(Player player) {
        // apply recoil force
        Vector recoil = player.getLocation().getDirection();
        recoil.multiply(-0.5);
        recoil.setY(0.25);
        player.setVelocity(recoil);
        // force the player to look upward
        float pitch = player.getPitch() - 10;
        if (pitch < -90.0f) pitch = -90.0f;
        player.setRotation(player.getYaw(), pitch);
    }
}
