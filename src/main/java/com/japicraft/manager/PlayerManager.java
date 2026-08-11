package com.japicraft.manager;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.profile.ModelProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerManager {
    public static final String MODEL = "player";
    public static final String ROLL_ANIMATION = "roll";
    private static final String DEATH_ANIMATION = "death";
    private static final HashSet<Player> animationLockedPlayers = new HashSet<>();
    private static final String PROGRESS_BAR_INDICATOR = "|";
    private static final int PROGRESS_BAR_SIZE = 40;
    private static final AnimationModifier HOLD_ON_LAST = AnimationModifier.builder().type(AnimationIterator.Type.HOLD_ON_LAST).build();

    public static boolean isPlayerAnimationLocked(Player player) {
        return PlayerManager.animationLockedPlayers.contains(player);
    }

    public static void lockPlayerAnimation(Player player) {
        PlayerManager.animationLockedPlayers.add(player);
    }

    public static boolean isPlayerInvulnerable(Player player) {
        return player.getGameMode().equals(GameMode.SPECTATOR) || player.hasPotionEffect(PotionEffectType.RESISTANCE);
    }

    public static void unlockPlayerAnimation(Player player) {
        PlayerManager.animationLockedPlayers.remove(player);
    }

    public static void clearActionBar(Player player) {
        player.sendActionBar(Component.empty());
    }

    public static void inspectBody(Player player, Interaction body) {
        Component name = body.customName();
        if (name != null) {
            player.sendMessage(name);
        } else {
            player.sendMessage("Dead body of an unknown player.");
        }
    }

    public static void kill(Player victim, Player killer) {
        // ground location
        Location deathLocation = PlayerManager.groundLocation(victim.getLocation());
        // remove player from game
        victim.setGameMode(GameMode.SPECTATOR);
        // create dead player body marker
        Interaction body = victim.getWorld().spawn(deathLocation, Interaction.class, interaction -> {
            interaction.setInteractionHeight(0.25F);
            interaction.setInteractionWidth(1.25F);
            interaction.setResponsive(true);
        });
        // attach model and animate body
        BetterModel.limb(PlayerManager.MODEL)
            .map(renderer -> renderer.getOrCreate(BukkitAdapter.adapt(body), ModelProfile.of(BukkitAdapter.adapt(victim))))
            .ifPresent(tracker -> tracker.animate(PlayerManager.DEATH_ANIMATION, PlayerManager.HOLD_ON_LAST));
        // give kill credit
        killer.sendMessage(Component.text("Killed ").append(victim.name()));
    }

    private static Location groundLocation(Location location) {
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

    public static boolean isItemWarmedUp(AtomicInteger counter, int maxTicks) {
        return counter.get() > maxTicks;
    }

    public static void showInitialItemWarmup(Player player, int maxTicks) {
        int progressBarScale = PlayerManager.PROGRESS_BAR_SIZE / maxTicks;
        player.sendActionBar(Component.text(PlayerManager.PROGRESS_BAR_INDICATOR.repeat(maxTicks * progressBarScale)).color(NamedTextColor.DARK_GRAY));
    }

    public static void showItemWarmup(Player player, AtomicInteger counter, int maxTicks, boolean isItemWarmedUp, TextColor readyColor, String readySound, float readyPitch) {
        int progressBarScale = PlayerManager.PROGRESS_BAR_SIZE / maxTicks;
        if (isItemWarmedUp) {
            player.sendActionBar(Component.text(PlayerManager.PROGRESS_BAR_INDICATOR.repeat(maxTicks * progressBarScale)).color(readyColor));
        } else {
            int currentTicks = counter.getAndIncrement();
            if (currentTicks == maxTicks) {
                SoundManager.playAsPlayerNearby(player, readySound, readyPitch);
            }
            player.sendActionBar(Component.text(PlayerManager.PROGRESS_BAR_INDICATOR.repeat(currentTicks * progressBarScale)).color(NamedTextColor.WHITE)
                .append(Component.text(PlayerManager.PROGRESS_BAR_INDICATOR.repeat((maxTicks - currentTicks) * progressBarScale)).color(NamedTextColor.DARK_GRAY))
            );
        }
    }
}
