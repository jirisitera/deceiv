package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

public class NametagManager implements Listener {
    private static final double MAX_DISTANCE = 5;
    private static final double MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;
    private static final double CROSSHAIR_CHECK = 0.95;
    private final AnimationManager animationManager;

    public NametagManager(AnimationManager animationManager) {
        this.animationManager = animationManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getScheduler().runAtFixedRate(Deceiv.getPlugin(), task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }
            if (animationManager.isPlayerAnimationLocked(player)) {
                return;
            }
            Player target = getClosestTarget(player);
            if (target == null) {
                player.clearTitle();
            } else {
                player.showTitle(Title.title(Component.empty(), Component.text(target.getName()), 5, 20, 5));
            }
        }, null, 20, 20);
    }

    public Player getClosestTarget(Player player) {
        Location location = player.getEyeLocation();
        Vector locationVector = location.toVector();
        Vector direction = location.getDirection();

        double closestDistance = MAX_DISTANCE_SQUARED;
        Player closestTarget = null;

        for (Entity entity : location.getNearbyPlayers(MAX_DISTANCE)) {
            if (!(entity instanceof Player target) || !target.isValid() || PlayerUtilities.isPlayerInvulnerable(player) || PlayerUtilities.isPlayerInvulnerable(target) || player == target) {
                continue;
            }
            Location targetLocation = target.getEyeLocation();
            if (targetLocation.toVector().subtract(locationVector).normalize().dot(direction) <= CROSSHAIR_CHECK) {
                // not looking towards target
                continue;
            }
            double distanceSquared = location.distanceSquared(targetLocation);
            if (distanceSquared >= closestDistance || !player.hasLineOfSight(target)) {
                // not a viable candidate
                continue;
            }
            closestTarget = target;
            closestDistance = distanceSquared;
        }
        return closestTarget;
    }
}
