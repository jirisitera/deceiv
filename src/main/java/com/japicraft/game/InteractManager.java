package com.japicraft.game;

import com.japicraft.container.GameContainer;
import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.item.DaggerManager;
import com.japicraft.item.ItemManager;
import com.japicraft.item.ItemUtilities;
import com.japicraft.item.RevolverManager;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerUtilities;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InteractManager implements Listener {
    private final GameContainer gameContainer;
    private final AnimationManager animationManager;
    private final ItemManager itemManager;

    public InteractManager(GameContainer gameContainer, AnimationManager animationManager, ItemManager itemManager) {
        this.gameContainer = gameContainer;
        this.animationManager = animationManager;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction body)) return;
        inspectBody(event.getPlayer(), body);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player victim) {
            event.setCancelled(true);
            if (ItemUtilities.isHoldingItem(attacker.getInventory(), RevolverManager.MODEL)) {
                PlayerUtilities.applyKnockback(attacker, victim);
            }
        } else if (entity instanceof Interaction body) {
            attacker.sendMessage(Component.text("I probably shouldn't be touching a corpse..."));
            PersistentDataContainer container = body.getPersistentDataContainer();
            int nextHealth = container.getOrDefault(animationManager.deadBodyHealthKey, PersistentDataType.INTEGER, AnimationManager.DEAD_BODY_MAX_HEALTH) - 1;
            if (nextHealth <= 0) {
                body.remove();
            } else {
                container.set(animationManager.deadBodyHealthKey, PersistentDataType.INTEGER, nextHealth);
            }
        }
    }

    public void inspectBody(Player player, Interaction body) {
        if (gameContainer.getGameInstance(player).getPlayerRole(player) != Role.DETECTIVE) {
            player.sendMessage(Component.text("A dead body?! How could have that happened?"));
            return;
        }
        PersistentDataContainer container = body.getPersistentDataContainer();
        // check if body is valid
        String name = container.get(animationManager.deadBodyNameKey, PersistentDataType.STRING);
        if (name == null) {
            return;
        }
        // get body data
        long timestamp = container.getOrDefault(animationManager.deadBodyTimestampKey, PersistentDataType.LONG, -1L);
        String reason = container.get(animationManager.deadBodyReasonKey, PersistentDataType.STRING);
        int health = container.getOrDefault(animationManager.deadBodyHealthKey, PersistentDataType.INTEGER, -1);
        // calculate variables
        long age = (body.getWorld().getGameTime() - timestamp) / 1200L;

        player.sendMessage(Component.text("Dead body of player '" + name + "'. Detective's Analysis:")
            .appendNewline()
            .append(Component.text("- Cause of death: " + reason + "."))
            .appendNewline()
            .append(Component.text("- Time of death: " + age + " minutes ago."))
            .appendNewline()
            .append(Component.text("- Will be destroyed in : " + health + " hits."))
        );
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball) || snowball.getItem().getType() != Material.AIR) {
            return;
        }
        event.setCancelled(true);
        if (event.getHitEntity() instanceof Player victim && snowball.getShooter() instanceof Player player && victim != player && !PlayerUtilities.isPlayerInvulnerable(victim)) {
            Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(victim, player, AnimationManager.DEATH_THROWING_DAGGER));
        } else if (event.getHitBlock() instanceof Block block) {
            snowball.getWorld().spawnParticle(Particle.BLOCK, snowball.getLocation(), 100, 0.5, 0.5, 0.5, block.getBlockData());
        }
        snowball.remove();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (PlayerUtilities.isPlayerInvulnerable(player)) {
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
            case DaggerManager.MODEL:
                itemManager.handleDaggerInteract(player, action, item);
                break;
            case RevolverManager.MODEL:
                itemManager.handleRevolverInteract(player, action, item);
                break;
        }
    }
}
