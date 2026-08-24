package com.japicraft.player;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PlayerUtilities {
    public static final String PLAYER_MODEL = "player";

    public static boolean isPlayerInvulnerable(Player player) {
        return player.getGameMode().equals(GameMode.SPECTATOR) || player.hasPotionEffect(PotionEffectType.RESISTANCE);
    }

    public static Location groundLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return location;
        }
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        int step = world.getBlockAt(blockX, blockY, blockZ).isSolid() ? 1 : -1;
        int newY = blockY;
        // find the ground point
        while (newY >= world.getMinHeight() && newY < world.getMaxHeight()) {
            if (world.getBlockAt(blockX, newY, blockZ).isSolid() && !world.getBlockAt(blockX, newY + 1, blockZ).isSolid()) {
                if (newY != blockY) location.setY(newY + 1.0);
                break;
            }
            newY += step;
        }
        return location;
    }

    public static void applyKnockback(Player attacker, Player victim) {
        Vector direction = victim.getLocation().toVector().subtract(attacker.getLocation().toVector());
        victim.knockback(1.2, direction.getX(), direction.getZ());
        // apply slight upward force
        Vector newVelocity = victim.getVelocity();
        newVelocity.setY(newVelocity.getY() + 0.2);
        victim.setVelocity(newVelocity);
    }

    public static String getRawProfileTextures(PlayerProfile profile) {
        return profile.getProperties().stream()
            .filter(property -> property.getName().equals("textures"))
            .map(ProfileProperty::getValue)
            .findFirst()
            .orElse(null);
    }
}
