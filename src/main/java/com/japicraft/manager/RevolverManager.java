package com.japicraft.manager;

import com.japicraft.Deceiv;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import io.papermc.paper.datacomponent.item.UseEffects;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RevolverManager {
    public static final String MODEL = "revolver";
    private static final int SHOT_COOLDOWN = 200;
    private static final double SHOT_RANGE = 10.0;
    private static final double SHOT_THICKNESS = 0.2;
    private static final float SHOT_HEARING_DISTANCE = 100.0F;
    private static final int MAX_WARMUP_TICKS = 20;
    private static final double BULLET_LINE_SPACING = 0.5;
    private static final TextColor READY_COLOR = TextColor.color(15, 180, 215);
    private static final String PREPARE_SOUND = "revolver.prepare";
    private static final String READY_SOUND = "revolver.ready";
    private static final String SHOOT_SOUND = "revolver.shoot";

    public static void give(Player player) {
        ItemStack item = ItemStack.of(Material.NAUTILUS_SHELL);
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
            .animation(ItemUseAnimation.CROSSBOW)
            .hasConsumeParticles(false)
            .sound(ItemManager.EMPTY_KEY)
            .consumeSeconds(999.0F)
            .build());
        item.setData(DataComponentTypes.USE_EFFECTS, UseEffects.useEffects()
            .speedMultiplier(0.5F)
            .build());
        item.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
            .type(SwingAnimation.Animation.STAB)
            .duration(20)
            .build());
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Revolver").color(NamedTextColor.WHITE));
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, RevolverManager.MODEL));
        player.give(item);
    }

    public static void handleInteract(Player player, Action action, ItemStack item) {
        if (action.isRightClick()) {
            // animation lock
            if (PlayerManager.isPlayerAnimationLocked(player)) return;
            PlayerManager.lockPlayerAnimation(player);
            // revolver shoot ability
            SoundManager.playAsPlayerNearby(player, RevolverManager.PREPARE_SOUND, 1.0F);
            PlayerManager.showInitialItemWarmup(player, RevolverManager.MAX_WARMUP_TICKS);
            // start aim loop task
            player.getScheduler().runAtFixedRate(Deceiv.plugin, RevolverManager.createAimLoop(player, item, new AtomicInteger()), null, 1, 1);
        }
    }

    private static Consumer<ScheduledTask> createAimLoop(Player player, ItemStack item, AtomicInteger warmupCounter) {
        return task -> {
            boolean isItemWarmedUp = PlayerManager.isItemWarmedUp(warmupCounter, RevolverManager.MAX_WARMUP_TICKS);
            if (!player.hasActiveItem()) {
                if (isItemWarmedUp && RevolverManager.hasRevolverItem(player.getInventory())) {
                    ItemManager.applyItemCooldown(player, item, RevolverManager.SHOT_COOLDOWN);
                    RevolverManager.shoot(player);
                }
                // end task
                PlayerManager.clearActionBar(player);
                PlayerManager.unlockPlayerAnimation(player);
                task.cancel();
                return;
            }
            // warmup timer announcement
            PlayerManager.showItemWarmup(player, warmupCounter, MAX_WARMUP_TICKS, isItemWarmedUp, RevolverManager.READY_COLOR, RevolverManager.READY_SOUND, 1.0F);
            // draw aim
            Location location = player.getEyeLocation();
            location.add(location.getDirection().multiply(SHOT_RANGE));
            player.spawnParticle(Particle.CRIT, location, 1, 0, 0, 0, 0);
        };
    }

    private static Predicate<Entity> filter(Player shooter) {
        return entity -> entity instanceof Player player && player != shooter && !PlayerManager.isPlayerInvulnerable(player);
    }

    private static void shoot(Player player) {
        // calculate hit
        Location location = player.getEyeLocation();
        RayTraceResult hit = location.getWorld().rayTrace(
            location, location.getDirection(), RevolverManager.SHOT_RANGE,
            FluidCollisionMode.NEVER, true, RevolverManager.SHOT_THICKNESS, RevolverManager.filter(player)
        );
        // apply effects
        RevolverManager.drawBulletLine(player);
        RevolverManager.applyRecoil(player);
        SoundManager.playAsPlayerWithDistance(player, RevolverManager.SHOOT_SOUND, 1.0F, RevolverManager.SHOT_HEARING_DISTANCE);
        if (hit == null) {
            return;
        }
        // register hit
        if (hit.getHitEntity() instanceof Player victim && !PlayerManager.isPlayerInvulnerable(victim)) {
            PlayerManager.kill(victim, player);
        } else if (hit.getHitBlock() instanceof Block block) {
            World world = block.getWorld();
            world.spawnParticle(Particle.BLOCK, hit.getHitPosition().toLocation(world), 30, 0.0, 0.0, 0.0, block.getBlockData());
        }
    }

    private static boolean hasRevolverItem(PlayerInventory inventory) {
        return RevolverManager.isRevolverItem(inventory.getItemInMainHand()) || RevolverManager.isRevolverItem(inventory.getItemInOffHand());
    }

    private static boolean isRevolverItem(ItemStack item) {
        Key model = item.getData(DataComponentTypes.ITEM_MODEL);
        return model != null && model.value().equals(RevolverManager.MODEL);
    }

    private static void drawBulletLine(Player player) {
        Location location = player.getEyeLocation();
        Vector step = location.getDirection().multiply(RevolverManager.BULLET_LINE_SPACING);
        for (double i = 0; i < SHOT_RANGE; i += RevolverManager.BULLET_LINE_SPACING) {
            location.add(step);
            player.getWorld().spawnParticle(Particle.SMOKE, location, 3, 0, 0, 0, 0);
        }
    }

    private static void applyRecoil(Player player) {
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
