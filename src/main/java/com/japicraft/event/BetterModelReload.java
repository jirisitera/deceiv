package com.japicraft.event;

import com.japicraft.Deceiv;
import kr.toxicity.model.api.bukkit.event.BetterModelBukkitEvent;
import kr.toxicity.model.api.data.ModelAsset;
import kr.toxicity.model.api.event.ModelAssetsEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.io.InputStream;

public class BetterModelReload implements Listener {
    @EventHandler
    public void onBetterModelReload(BetterModelBukkitEvent bukkitEvent) throws IOException {
        ModelAssetsEvent event = bukkitEvent.as(ModelAssetsEvent.class);
        InputStream in = Deceiv.plugin.getResource("knife.bbmodel");
        if (event != null && in != null) {
            event.addAsset(ModelAsset.of("knife", in.readAllBytes()));
        }
    }
}
