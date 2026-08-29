package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.game.GameInstance;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;

public class CountdownInterface {
    private static final int GAME_LENGTH_SECONDS = 600;
    private final BossBar displayBar = BossBar.bossBar(Component.empty(), 1.0F, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_20);
    private AtomicInteger counter;

    public void show(Audience audience) {
        displayBar.addViewer(audience);
    }

    public void hide(Audience audience) {
        displayBar.removeViewer(audience);
    }

    public void update(int currentTicks) {
        String minutes = String.format("%02d", currentTicks / 60);
        String seconds = String.format("%02d", currentTicks % 60);
        displayBar.name(Component.text("Time Left: " + minutes + ":" + seconds));
        displayBar.progress(currentTicks / 600.0F);
    }

    public void initialize(Deceiv plugin, GameInstance gameInstance) {
        counter = new AtomicInteger(CountdownInterface.GAME_LENGTH_SECONDS);
        update(CountdownInterface.GAME_LENGTH_SECONDS);
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            int currentTicks = counter.getAndDecrement();
            if (currentTicks <= 0) {
                gameInstance.endRound();
                task.cancel();
            } else {
                update(currentTicks);
            }
        }, 20, 20);
    }

    public void delete() {
        counter.set(-1);
    }
}
