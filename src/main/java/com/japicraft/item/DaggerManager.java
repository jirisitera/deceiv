package com.japicraft.item;

import com.japicraft.Deceiv;
import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.event.PlayerFinishUsingItemEvent;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Consumer;

public class DaggerManager implements AbstractItemHandler {
    private static final int STAB_DURATION = 10;
    private static final int STAB_COOLDOWN = 1200;
    private static final double STAB_RADIUS = 1.5;

    private static final int THROW_DURATION = 5;
    private static final int THROW_COOLDOWN = 200;
    private static final int MAX_THROW_DURATION = 100;

    private final ItemWindUpService itemWindUpService;
    private final AnimationManager animationManager;

    public DaggerManager(ItemWindUpService itemWindUpService, AnimationManager animationManager) {
        this.itemWindUpService = itemWindUpService;
        this.animationManager = animationManager;

        UniqueItem.DAGGER.setAbility(new ItemAbility(getStabAction()));
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
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, UniqueItem.DAGGER.getModel()));
        // give item safely, as to not reveal the murderer
        PlayerInventory inventory = player.getInventory();
        inventory.setHeldItemSlot(0);
        inventory.setItem(4, item);
    }

    @Override
    public boolean compare(ItemStack item) {
        return UniqueItem.DAGGER.compare(item);
    }

    @Override
    public void handleInteract(Player player, Action action, ItemStack item) {
        if (action.isLeftClick()) {
            itemWindUpService.requestDelayed(player, UniqueItem.DAGGER, THROW_DURATION, THROW_COOLDOWN, item, SoundType.DAGGER_THROW, getThrowAction());
        } else if (action.isRightClick()) {
            itemWindUpService.requestContinuous(player, UniqueItem.DAGGER, item, STAB_DURATION);
        }
    }

    private Consumer<Player> getThrowAction() {
        return player -> {
            Snowball projectile = player.launchProjectile(Snowball.class);
            // ensure projectile itself is invisible
            projectile.setItem(ItemStack.of(Material.AIR));
            // attached model will follow this rotation
            projectile.setRotation(player.getYaw(), player.getPitch());
            // attach model to projectile
            animationManager.playDaggerThrowAnimation(projectile);
            // remove the projectile when it lives too long
            projectile.getScheduler().runDelayed(Deceiv.getPlugin(), _ -> {
                if (projectile.isValid()) {
                    projectile.remove();
                }
            }, null, DaggerManager.MAX_THROW_DURATION);
        };
    }

    private Consumer<PlayerItemRelation> getStabAction() {
        return relation -> {
            Player player = relation.player();
            ItemStack item = relation.item();
            for (Player victim : player.getLocation().getNearbyPlayers(DaggerManager.STAB_RADIUS, 0.25)) {
                if (victim == player || PlayerUtilities.isPlayerInvulnerable(victim)) {
                    // cannot kill self
                    continue;
                }
                // apply effects
                SoundUtilities.playAsPlayerNearby(player, SoundType.DAGGER_STAB);
                ItemUtilities.applyItemCooldown(player, item, DaggerManager.STAB_COOLDOWN);
                // end action
                Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(victim, player, AnimationManager.DEATH_DAGGER));
                Bukkit.getPluginManager().callEvent(new PlayerFinishUsingItemEvent(player));
                player.clearActiveItem();
                break;
            }
        };
    }
}
