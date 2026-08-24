package com.japicraft.player;

import com.japicraft.Deceiv;
import com.japicraft.hook.BetterModelHook;
import com.japicraft.hook.Hook;
import com.japicraft.sound.SoundUtilities;
import com.japicraft.ui.ColorUtilities;
import com.japicraft.ui.InterfaceUtilities;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class AnimationManager {
    public static final String DEATH_THROWING_DAGGER = "Skewered";
    public static final String DEATH_DAGGER = "Stabbed";
    public static final String DEATH_REVOLVER = "Shot";
    public static final int DEAD_BODY_MAX_HEALTH = 5;
    private static final String DEAD_BODY_NAME = "dead_body_name";
    private static final String DEAD_BODY_TIMESTAMP = "dead_body_timestamp";
    private static final String DEAD_BODY_REASON = "dead_body_reason";
    private static final String DEAD_BODY_HEALTH = "dead_body_health";
    private static final String PROGRESS_BAR_INDICATOR = "|";
    private static final int PROGRESS_BAR_SIZE = 40;
    public final NamespacedKey deadBodyHealthKey = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.DEAD_BODY_HEALTH);
    public final NamespacedKey deadBodyNameKey = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.DEAD_BODY_NAME);
    public final NamespacedKey deadBodyTimestampKey = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.DEAD_BODY_TIMESTAMP);
    public final NamespacedKey deadBodyReasonKey = new NamespacedKey(Deceiv.PLUGIN_ID, AnimationManager.DEAD_BODY_REASON);
    private final HashSet<Player> animationLockedPlayers = new HashSet<>();

    public void endRepeatingAnimationTask(ScheduledTask task, Player player) {
        InterfaceUtilities.clearActionBar(player);
        unlockPlayerAnimation(player);
        task.cancel();
    }

    public void playPlayerDeathAnimation(Player player, String reason) {
        // create dead player body marker
        World world = player.getWorld();
        Interaction body = world.spawn(PlayerUtilities.groundLocation(player.getLocation()), Interaction.class);
        body.setInteractionHeight(0.25F);
        body.setInteractionWidth(1.25F);
        body.setResponsive(true);
        body.getPersistentDataContainer().set(deadBodyNameKey, PersistentDataType.STRING, player.getName());
        body.getPersistentDataContainer().set(deadBodyTimestampKey, PersistentDataType.LONG, world.getGameTime());
        body.getPersistentDataContainer().set(deadBodyReasonKey, PersistentDataType.STRING, reason);
        body.getPersistentDataContainer().set(deadBodyHealthKey, PersistentDataType.INTEGER, AnimationManager.DEAD_BODY_MAX_HEALTH);
        // attach model and animate body
        if (Hook.BETTER_MODEL.isAvailable()) {
            BetterModelHook.playDeathAnimation(player, body);
        }
    }

    public void playProjectileThrowAnimation(Entity projectile) {
        if (Hook.BETTER_MODEL.isAvailable()) {
            BetterModelHook.playThrowAnimation(projectile);
        }
    }

    public void playPlayerRollAnimation(Player player) {
        if (Hook.BETTER_MODEL.isAvailable()) {
            BetterModelHook.playRollAnimation(player, this);
        }
    }

    public boolean isPlayerAnimationLocked(Player player) {
        return animationLockedPlayers.contains(player);
    }

    public void lockPlayerAnimation(Player player) {
        animationLockedPlayers.add(player);
    }

    public void unlockPlayerAnimation(Player player) {
        animationLockedPlayers.remove(player);
    }

    public boolean isItemWarmedUp(AtomicInteger counter, int maxTicks) {
        return counter.get() > maxTicks;
    }

    public void showInitialItemWarmup(Player player, int maxTicks) {
        int progressBarScale = AnimationManager.PROGRESS_BAR_SIZE / maxTicks;
        player.sendActionBar(Component.text(AnimationManager.PROGRESS_BAR_INDICATOR.repeat(maxTicks * progressBarScale)).color(ColorUtilities.GRAY));
    }

    public void showAndIncrementItemWarmup(Player player, AtomicInteger counter, int maxTicks, boolean isItemWarmedUp, TextColor readyColor, String readySound, float readyPitch) {
        int progressBarScale = AnimationManager.PROGRESS_BAR_SIZE / maxTicks;
        if (isItemWarmedUp) {
            player.sendActionBar(Component.text(AnimationManager.PROGRESS_BAR_INDICATOR.repeat(maxTicks * progressBarScale)).color(readyColor));
        } else {
            int currentTicks = counter.getAndIncrement();
            if (currentTicks == maxTicks) {
                SoundUtilities.playAsPlayerNearby(player, readySound, readyPitch);
            }
            player.sendActionBar(Component.text(AnimationManager.PROGRESS_BAR_INDICATOR.repeat(currentTicks * progressBarScale)).color(ColorUtilities.WHITE)
                .append(Component.text(AnimationManager.PROGRESS_BAR_INDICATOR.repeat((maxTicks - currentTicks) * progressBarScale)).color(ColorUtilities.GRAY))
            );
        }
    }
}
