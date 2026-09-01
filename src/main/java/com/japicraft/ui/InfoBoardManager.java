package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.game.GameInstance;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InfoBoardManager {
    private static final int GAME_LENGTH_SECONDS = 600;
    private final HashMap<Player, BossBar> displays = new HashMap<>();
    private final AtomicInteger timeTracker = new AtomicInteger(InfoBoardManager.GAME_LENGTH_SECONDS);
    private final GameInstance gameInstance;
    private ScheduledTask task;

    public InfoBoardManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void initialize() {
        if (task != null) {
            return;
        }
        // initialize first time
        for (Player player : gameInstance.getPlayers()) {
            displays.put(player, BossBar.bossBar(Component.empty(), 1.0F, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS));
            update(player, InfoBoardManager.GAME_LENGTH_SECONDS);
        }
        // continuously update
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(Deceiv.getPlugin(), _ -> {
            int time = getTime();

            for (Player player : gameInstance.getPlayers()) {
                update(player, time);
            }

        }, 20, 20);
    }

    public void update(Player player, int time) {
        BossBar display = displays.get(player);
        if (display == null) {
            return;
        }
        String minutes = String.format("%02d", time / 60);
        String seconds = String.format("%02d", time % 60);
        display.name(Component.text("Time Left: " + minutes + ":" + seconds));
    }

    public int getTime() {
        int time = timeTracker.getAndDecrement();
        if (time <= 0) {
            gameInstance.endRound();
        }
        return time;
    }

    public void show(Player player) {
        displays.get(player).addViewer(player);
    }

    public void hide(Player player) {
        displays.get(player).removeViewer(player);
    }

    public void remove() {
        if (task != null) {
            task.cancel();
        }
        displays.clear();
    }
}
