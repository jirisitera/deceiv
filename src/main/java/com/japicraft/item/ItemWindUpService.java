package com.japicraft.item;

import com.japicraft.Deceiv;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerItemRelation;
import com.japicraft.sound.SoundType;
import com.japicraft.sound.SoundUtilities;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ItemWindUpService {
    private static final Component PROGRESS = Component.text("|").font(Key.key(Deceiv.PLUGIN_ID, "windup")).shadowColor(ShadowColor.none());
    private static final Component READY = Component.text("-").font(Key.key(Deceiv.PLUGIN_ID, "windup")).shadowColor(ShadowColor.none());
    private static final int MAX_DURATION = 999 * Ticks.TICKS_PER_SECOND;
    private static final int MIN_DURATION = 5;
    private final Deceiv plugin;
    private final AnimationManager animationManager;
    private final Map<Player, ScheduledTask> latestRequest = new HashMap<>();
    private final Map<Player, UniqueItem> ready = new HashMap<>();

    public ItemWindUpService(Deceiv plugin, AnimationManager animationManager) {
        this.plugin = plugin;
        this.animationManager = animationManager;
    }

    public UniqueItem getReadyItem(Player player) {
        return ready.get(player);
    }

    public void setReady(Player player, UniqueItem unique) {
        ready.put(player, unique);
        showReadyIndicator(player, unique, MAX_DURATION);
    }

    public void clear(Player player) {
        ready.remove(player);
        hideIndicator(player);
        animationManager.unlockPlayerAnimation(player);
        latestRequest.remove(player);
    }

    public void hideIndicator(Player player) {
        player.showTitle(Title.title(Component.empty(), Component.empty()));
    }

    private void showPrepareIndicator(Player player, int duration) {
        player.showTitle(Title.title(Component.empty(), ItemWindUpService.PROGRESS, duration, duration, 0));
    }

    public void showReadyIndicator(Player player, UniqueItem unique, int duration) {
        player.showTitle(Title.title(Component.empty(), ItemWindUpService.READY.color(unique.getRole().getColor()), 0, duration, 0));
    }

    private boolean passLock(Player player, int duration) {
        if (animationManager.isPlayerAnimationLocked(player)) {
            return true;
        }
        animationManager.lockPlayerAnimation(player);
        showPrepareIndicator(player, duration);
        return false;
    }

    private boolean hasTaskBeenReplaced(ScheduledTask task, Player player) {
        ScheduledTask latest = latestRequest.get(player);
        return latest == null || latest != task;
    }

    public void processContinuousRequest(Player player, UniqueItem unique, ItemStack item, int duration, Runnable action) {
        if (passLock(player, duration)) {
            return;
        }
        SoundUtilities.playAsPlayerNearbyWithItem(player, unique, SoundType.ITEM_ACTION_PREPARE);
        latestRequest.put(player, player.getScheduler().runDelayed(plugin, task -> {
            if (hasTaskBeenReplaced(task, player) || !player.hasActiveItem() || ItemUtilities.isOnCooldown(player, item)) {
                return;
            }
            SoundUtilities.playAsPlayerNearbyWithItem(player, unique, SoundType.ITEM_ACTION_READY);
            action.run();
        }, null, duration));
    }

    public void requestContinuous(Player player, UniqueItem unique, ItemStack item, int duration) {
        processContinuousRequest(player, unique, item, duration, () -> {
            showReadyIndicator(player, unique, MAX_DURATION);
            player.getScheduler().runAtFixedRate(plugin, task -> {
                if (!player.hasActiveItem() || ItemUtilities.isOnCooldown(player, item)) {
                    task.cancel();
                    return;
                }
                unique.getAbility().action().accept(new PlayerItemRelation(player, item));
            }, null, 1, 4);
        });
    }

    public void requestHeld(Player player, UniqueItem unique, ItemStack item, int duration) {
        processContinuousRequest(player, unique, item, duration, () -> setReady(player, unique));
    }

    public void requestDelayed(Player player, UniqueItem unique, int duration, int cooldown, ItemStack item, SoundType sound, Consumer<Player> action) {
        if (passLock(player, duration)) {
            return;
        }
        player.getScheduler().runDelayed(plugin, _ -> {
            if (!ItemUtilities.isHoldingItem(player.getInventory(), unique) || ItemUtilities.isOnCooldown(player, item)) {
                return;
            }
            ItemUtilities.applyItemCooldown(player, item, cooldown);
            SoundUtilities.playAsPlayerNearby(player, sound);
            showReadyIndicator(player, unique, MIN_DURATION);
            action.accept(player);
            animationManager.unlockPlayerAnimation(player);
        }, null, duration);
    }
}
