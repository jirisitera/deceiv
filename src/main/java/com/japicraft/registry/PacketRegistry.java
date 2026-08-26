package com.japicraft.registry;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.japicraft.packet.HardcorePacket;

import java.util.ArrayList;
import java.util.Collection;

public class PacketRegistry {
    public void initialize() {
        ArrayList<PacketListenerAbstract> events = new ArrayList<>();

        events.add(new HardcorePacket());

        registerEvents(events);
    }

    public void registerEvents(Collection<PacketListenerAbstract> events) {
        for (PacketListenerAbstract event : events) {
            PacketEvents.getAPI().getEventManager().registerListener(event);
        }
    }
}
