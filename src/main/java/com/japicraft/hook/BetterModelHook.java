package com.japicraft.hook;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.japicraft.Deceiv;
import com.japicraft.item.DaggerManager;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.PlayerUtilities;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.bukkit.event.BetterModelBukkitEvent;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.ModelAsset;
import kr.toxicity.model.api.event.ModelAssetsEvent;
import kr.toxicity.model.api.profile.ModelProfile;
import kr.toxicity.model.api.profile.ModelProfileInfo;
import kr.toxicity.model.api.profile.ModelProfileSkin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.profile.PlayerTextures;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class BetterModelHook implements Listener {
    private static final String DEATH_ANIMATION = "death";
    private static final String ROLL_ANIMATION = "roll";
    private static final String THROW_ANIMATION = "spin";
    private static final AnimationModifier HOLD_ON_LAST = AnimationModifier.builder().type(AnimationIterator.Type.HOLD_ON_LAST).build();
    private final Deceiv plugin;

    public BetterModelHook(Deceiv plugin) {
        this.plugin = plugin;
    }

    public static void playDeathAnimation(Player player, Entity body) {
        BetterModel.limb(PlayerUtilities.PLAYER_MODEL)
            .map(renderer -> renderer.getOrCreate(BukkitAdapter.adapt(body), getModelProfile(player)))
            .ifPresent(tracker -> tracker.animate(BetterModelHook.DEATH_ANIMATION, BetterModelHook.HOLD_ON_LAST));
    }

    public static void playThrowAnimation(Entity projectile) {
        BetterModel.model(DaggerManager.MODEL)
            .map(renderer -> renderer.getOrCreate(BukkitAdapter.adapt(projectile)))
            .ifPresent(tracker -> tracker.animate(BetterModelHook.THROW_ANIMATION));
    }

    public static void playRollAnimation(Player player, AnimationManager animationManager) {
        if (animationManager.isPlayerAnimationLocked(player)) {
            return;
        }
        animationManager.lockPlayerAnimation(player);
        BetterModel.limb(PlayerUtilities.PLAYER_MODEL)
            .map(renderer -> renderer.getOrCreate(BukkitAdapter.adapt(player)))
            .ifPresent(tracker -> tracker.animate(BetterModelHook.ROLL_ANIMATION, AnimationModifier.DEFAULT, () -> {
                animationManager.unlockPlayerAnimation(player);
                tracker.close();
            }));
    }

    private static ModelProfile getModelProfile(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        String rawTextures = PlayerUtilities.getRawProfileTextures(profile);
        if (!profile.hasTextures() || rawTextures == null) {
            return ModelProfile.UNKNOWN;
        }
        PlayerTextures textures = player.getPlayerProfile().getTextures();
        URL skinLink = textures.getSkin();
        URL capeLink = textures.getCape();
        if (skinLink == null || capeLink == null) {
            return ModelProfile.UNKNOWN;
        }
        ModelProfileSkin skin;
        try {
            skin = new ModelProfileSkin(skinLink.toURI(), capeLink.toURI(), textures.getSkinModel().equals(PlayerTextures.SkinModel.SLIM), rawTextures);
        } catch (URISyntaxException exception) {
            return ModelProfile.UNKNOWN;
        }
        ModelProfileInfo info = new ModelProfileInfo(UUID.randomUUID(), player.getName());
        return ModelProfile.of(info, skin);
    }

    @EventHandler
    public void onModelAssetsLoad(BetterModelBukkitEvent bukkitEvent) throws IOException {
        ModelAssetsEvent event = bukkitEvent.as(ModelAssetsEvent.class);
        if (event == null) {
            return;
        }
        addAsset(event, "dagger.bbmodel", "dagger");
        addAsset(event, "player.bbmodel", "player");
    }

    private void addAsset(ModelAssetsEvent event, String path, String name) throws IOException {
        InputStream inputStream = plugin.getResource(path);
        if (inputStream == null) {
            return;
        }
        event.addAsset(ModelAsset.of(name, inputStream.readAllBytes()));
    }
}
