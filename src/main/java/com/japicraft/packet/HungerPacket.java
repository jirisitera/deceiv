package com.japicraft.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import org.bukkit.entity.Player;

public class HungerPacket {
    public static void showVisualHungerEffect(Player player) {
        WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(player.getEntityId(), PotionTypes.HUNGER, 0, -1, (byte) 0);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
