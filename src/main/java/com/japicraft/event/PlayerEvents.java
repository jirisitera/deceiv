package com.japicraft.event;

import com.japicraft.container.GameContainer;
import com.japicraft.game.GameManager;
import com.japicraft.game.Role;
import com.japicraft.player.AnimationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerEvents implements Listener {
    private final GameContainer gameContainer;
    private final AnimationManager animationManager;

    public PlayerEvents(GameContainer gameContainer, AnimationManager animationManager) {
        this.gameContainer = gameContainer;
        this.animationManager = animationManager;
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (event.getMainHandItem().getType() == Material.AIR && event.getOffHandItem().getType() == Material.AIR) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            // give speed
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 2, false, false, false));
            // roll animation
            animationManager.playPlayerRollAnimation(player);
        }
    }

    @EventHandler
    public void onPlayerElimination(PlayerEliminateEvent event) {
        Player victim = event.getVictim();
        Player killer = event.getKiller();
        // get game instance
        GameManager gameManager = gameContainer.getGameInstance(victim);
        if (!gameManager.equals(gameContainer.getGameInstance(killer))) {
            return;
        }
        // remove player from game
        victim.setGameMode(GameMode.SPECTATOR);
        // play death animation
        animationManager.playPlayerDeathAnimation(victim, event.getReason());
        // give kill credit
        killer.sendMessage(Component.text("Killed ").append(victim.name()));
        // check if the game is supposed to end
        boolean noMurderers = true;
        boolean onlyMurderers = true;
        for (Player player : gameManager.getPlayers()) {
            if (gameManager.getPlayerRole(player).equals(Role.MURDERER)) {
                noMurderers = false;
            } else {
                onlyMurderers = false;
            }
        }
        if (noMurderers || onlyMurderers) {
            gameManager.endRound();
        }
    }
}
