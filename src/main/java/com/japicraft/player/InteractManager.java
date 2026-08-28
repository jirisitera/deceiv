package com.japicraft.player;

import com.japicraft.container.GameContainer;
import com.japicraft.event.PlayerEliminateEvent;
import com.japicraft.game.GameInstance;
import com.japicraft.game.Role;
import com.japicraft.item.ItemUtilities;
import com.japicraft.item.UniqueItem;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InteractManager implements Listener {
    private final GameContainer gameContainer;

    public InteractManager(GameContainer gameContainer) {
        this.gameContainer = gameContainer;
    }

    @EventHandler
    public void onPlayerInspectCorpse(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction corpse)) {
            return;
        }
        Player player = event.getPlayer();
        GameInstance gameInstance = gameContainer.getGameInstance(player);
        if (gameInstance == null) {
            return;
        }
        if (gameInstance.getPlayerRole(player) != Role.DETECTIVE) {
            player.sendMessage(Component.text("A dead body?! How could have that happened?"));
            return;
        }
        PersistentDataContainer container = corpse.getPersistentDataContainer();
        // check if body is valid
        String name = container.get(AnimationManager.CORPSE_NAME_KEY, PersistentDataType.STRING);
        if (name == null) {
            return;
        }
        // get body data
        long timestamp = container.getOrDefault(AnimationManager.CORPSE_TIMESTAMP_KEY, PersistentDataType.LONG, -1L);
        String reason = container.get(AnimationManager.CORPSE_REASON_KEY, PersistentDataType.STRING);
        int health = container.getOrDefault(AnimationManager.CORPSE_HEALTH_KEY, PersistentDataType.INTEGER, -1);
        // calculate variables
        long age = (corpse.getWorld().getGameTime() - timestamp) / 1200L;
        // print dead body report
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
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player victim) {
            // knockback ability for detectives
            if (ItemUtilities.isHoldingItem(attacker.getInventory(), UniqueItem.REVOLVER)) {
                PlayerUtilities.applyKnockback(attacker, victim);
            }
            // disable any PvP damage
            event.setCancelled(true);
        } else if (entity instanceof Interaction corpse) {
            // corpse destroying feature
            attacker.sendMessage(Component.text("I probably shouldn't be touching a corpse..."));
            PersistentDataContainer container = corpse.getPersistentDataContainer();
            int nextHealth = container.getOrDefault(AnimationManager.CORPSE_HEALTH_KEY, PersistentDataType.INTEGER, AnimationManager.CORPSE_MAX_HEALTH) - 1;
            if (nextHealth <= 0) {
                corpse.remove();
            } else {
                container.set(AnimationManager.CORPSE_HEALTH_KEY, PersistentDataType.INTEGER, nextHealth);
            }
        }
    }

    @EventHandler
    public void onThrowingDaggerHit(ProjectileHitEvent event) {
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
}
