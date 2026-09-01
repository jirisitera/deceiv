package com.japicraft.item;

import com.japicraft.Deceiv;
import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.game.Role;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerItemRelation;
import com.japicraft.player.PlayerUtilities;
import com.japicraft.sound.SoundType;
import com.japicraft.sound.SoundUtilities;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import io.papermc.paper.datacomponent.item.UseEffects;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RevolverManager implements AbstractItemHandler {
    private static final double SHOT_RANGE = 15.0;
    private static final double SHOT_THICKNESS = 0.2;
    private static final int COOLDOWN = 200;
    private static final float SHOT_HEARING_DISTANCE = 100.0F;
    private static final int WINDUP_DURATION = 20;
    private static final double BULLET_LINE_SPACING = 0.5;
    private final ItemWindUpService itemWindUpService;

    public RevolverManager(ItemWindUpService itemWindUpService) {
        this.itemWindUpService = itemWindUpService;

        UniqueItem.REVOLVER.setAbility(new ItemAbility(getShootAction()));
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
            .canSprint(false)
            .speedMultiplier(1.0F)
            .build());
        item.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
            .type(SwingAnimation.Animation.STAB)
            .duration(20)
            .build());
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Revolver").color(Role.DETECTIVE.getColor()));
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, UniqueItem.REVOLVER.getModel()));

        PlayerInventory inventory = player.getInventory();
        inventory.setHeldItemSlot(0);
        inventory.setItem(1, item);
    }

    @Override
    public boolean compare(ItemStack item) {
        return UniqueItem.REVOLVER.compare(item);
    }

    @Override
    public void handleInteract(Player player, Action action, ItemStack item) {
        if (action.isRightClick()) {
            itemWindUpService.requestHeld(player, UniqueItem.REVOLVER, item, WINDUP_DURATION);
        }
    }

    private Consumer<PlayerItemRelation> getShootAction() {
        return relation -> {
            Player player = relation.player();
            // calculate hit
            Location location = player.getEyeLocation();
            RayTraceResult hit = location.getWorld().rayTrace(
                location, location.getDirection(), RevolverManager.SHOT_RANGE,
                FluidCollisionMode.NEVER, true, RevolverManager.SHOT_THICKNESS, filter(player)
            );
            // apply effects
            SoundUtilities.playAsPlayerWithDistance(player, SoundType.REVOLVER_SHOOT, 1.0F, RevolverManager.SHOT_HEARING_DISTANCE);
            ItemUtilities.applyItemCooldown(player, relation.item(), RevolverManager.COOLDOWN);
            drawBulletLine(player);
            applyRecoil(player);
            // register hit
            if (hit == null) {
                return;
            }
            if (hit.getHitEntity() instanceof Player victim && !PlayerUtilities.isPlayerInvulnerable(victim)) {
                Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(victim, player, AnimationManager.DEATH_REVOLVER));
            } else if (hit.getHitBlock() instanceof Block block) {
                World world = block.getWorld();
                world.spawnParticle(Particle.BLOCK, hit.getHitPosition().toLocation(world), 30, 0.0, 0.0, 0.0, block.getBlockData());
            }
        };
    }

    private Predicate<Entity> filter(Player shooter) {
        return entity -> entity instanceof Player player && player != shooter && !PlayerUtilities.isPlayerInvulnerable(player);
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
