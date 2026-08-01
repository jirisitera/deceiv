package com.japicraft.event;

import com.japicraft.Deceiv;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.platform.PlatformEntity;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class PlayerInteract implements Listener {
    private static final String ANIMATION_THROW = "spin";
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getCooldown(item) > 0) {
            return;
        }
        Action action = event.getAction();
        if (action.isRightClick()) {
            player.setCooldown(item, 20);
            player.sendMessage("Right clicked!");
            // create guiding entity
            Location location = player.getEyeLocation();
            Vector velocity = location.getDirection().normalize().multiply(0.5);
            ItemDisplay marker = location.getWorld().spawn(location, ItemDisplay.class);
            // attach model to entity
            PlatformEntity attachTarget = BukkitAdapter.adapt(marker);
            BetterModel.model("knife")
                .map(renderer -> renderer.getOrCreate(attachTarget))
                .ifPresent(tracker -> tracker.animate(ANIMATION_THROW));
            // simulate gravity
            Deceiv.scheduler.runTaskTimer(Deceiv.plugin, task -> {
                if (!marker.isValid() || marker.getTicksLived() > 100) {
                    marker.remove();
                    BetterModel.registry(attachTarget).ifPresent(EntityTrackerRegistry::close);
                    task.cancel();
                    return;
                }
                RayTraceResult hit = location.getWorld().rayTrace(
                    location, velocity, velocity.length(),
                    FluidCollisionMode.NEVER, true, 0.2,
                    entity -> entity != player && entity != marker
                );
                if (hit != null) {
                    if (hit.getHitEntity() != null) {
                        player.sendMessage("You hit an entity: " + hit.getHitEntity().getName());
                    } else if (hit.getHitBlock() != null) {
                        player.sendMessage("You hit a block!");
                    }
                    marker.remove();
                    BetterModel.registry(attachTarget).ifPresent(EntityTrackerRegistry::close);
                    task.cancel();
                    return;
                }
                velocity.setY(velocity.getY() - 0.01);
                location.add(velocity);
                marker.teleport(location);
            }, 0, 1);
        } else if (action.isLeftClick()) {
            player.sendMessage("Left clicked!");
        }
    }
}
