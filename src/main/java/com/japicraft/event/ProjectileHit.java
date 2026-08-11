package com.japicraft.event;

import com.japicraft.manager.PlayerManager;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHit implements Listener {
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball) || snowball.getItem().getType() != Material.AIR) {
            return;
        }
        event.setCancelled(true);
        if (event.getHitEntity() instanceof Player victim && snowball.getShooter() instanceof Player player && victim != player && !PlayerManager.isPlayerInvulnerable(victim)) {
            PlayerManager.kill(victim, player);
        } else if (event.getHitBlock() instanceof Block block) {
            snowball.getWorld().spawnParticle(Particle.BLOCK, snowball.getLocation(), 100, 0.5, 0.5, 0.5, block.getBlockData());
        }
        snowball.remove();
    }
}
