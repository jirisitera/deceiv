package com.japicraft.packet;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PacketService implements Listener {
    private static final String TEAM_NAME = "global";
    private final Team globalTeam;

    public PacketService() {
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = mainBoard.getTeam(TEAM_NAME);
        if (team == null) {
            team = mainBoard.registerNewTeam(TEAM_NAME);
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        this.globalTeam = team;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        }
        HungerPacket.showVisualHungerEffect(player);

        globalTeam.addEntry(player.getName());
    }


    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent event) {
        HungerPacket.showVisualHungerEffect(event.getPlayer());
    }
}
