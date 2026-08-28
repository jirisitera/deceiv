package com.japicraft.player;

import com.japicraft.Deceiv;
import com.japicraft.hook.BetterModelHook;
import com.japicraft.hook.Hook;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;

public class AnimationManager {
    public static final String DEATH_THROWING_DAGGER = "Skewered";
    public static final String DEATH_DAGGER = "Stabbed";
    public static final String DEATH_REVOLVER = "Shot";
    public static final int CORPSE_MAX_HEALTH = 5;
    private static final String CORPSE_NAME = "corpse_name";
    public static final NamespacedKey CORPSE_NAME_KEY = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.CORPSE_NAME);
    private static final String CORPSE_TIMESTAMP = "corpse_timestamp";
    public static final NamespacedKey CORPSE_TIMESTAMP_KEY = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.CORPSE_TIMESTAMP);
    private static final String CORPSE_REASON = "corpse_reason";
    public static final NamespacedKey CORPSE_REASON_KEY = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.CORPSE_REASON);
    private static final String CORPSE_HEALTH = "corpse_health";
    public static final NamespacedKey CORPSE_HEALTH_KEY = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.CORPSE_HEALTH);
    private final HashSet<Player> animationLockedPlayers = new HashSet<>();

    public boolean isPlayerAnimationLocked(Player player) {
        return animationLockedPlayers.contains(player);
    }

    public void lockPlayerAnimation(Player player) {
        animationLockedPlayers.add(player);
    }

    public void unlockPlayerAnimation(Player player) {
        animationLockedPlayers.remove(player);
    }

    public void playPlayerDeathAnimation(Player player, String reason) {
        // create dead player body marker
        World world = player.getWorld();
        Interaction corpse = world.spawn(PlayerUtilities.groundLocation(player.getLocation()), Interaction.class);
        corpse.setInteractionHeight(0.25F);
        corpse.setInteractionWidth(1.25F);
        corpse.setResponsive(true);
        corpse.getPersistentDataContainer().set(CORPSE_NAME_KEY, PersistentDataType.STRING, player.getName());
        corpse.getPersistentDataContainer().set(CORPSE_TIMESTAMP_KEY, PersistentDataType.LONG, world.getGameTime());
        corpse.getPersistentDataContainer().set(CORPSE_REASON_KEY, PersistentDataType.STRING, reason);
        corpse.getPersistentDataContainer().set(CORPSE_HEALTH_KEY, PersistentDataType.INTEGER, AnimationManager.CORPSE_MAX_HEALTH);
        // attach model and animate body
        if (Hook.BETTER_MODEL.isAvailable()) {
            BetterModelHook.playDeathAnimation(player, corpse);
        }
    }

    public void playDaggerThrowAnimation(Entity projectile) {
        if (Hook.BETTER_MODEL.isAvailable()) {
            BetterModelHook.playDaggerThrowAnimation(projectile);
        }
    }

    public void playPlayerRollAnimation(Player player) {
        if (Hook.BETTER_MODEL.isAvailable()) {
            if (isPlayerAnimationLocked(player)) {
                return;
            }
            lockPlayerAnimation(player);
            BetterModelHook.playRollAnimation(player).thenRun(() -> unlockPlayerAnimation(player));
        }
    }
}
