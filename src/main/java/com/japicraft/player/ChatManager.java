package com.japicraft.player;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class ChatManager implements Listener {
    private static final double CHAT_RADIUS = 48.0;
    private static final double SQUARED_RADIUS = CHAT_RADIUS * CHAT_RADIUS;

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        event.viewers().removeIf(audience -> {
            if (audience instanceof Player receiver) {
                if (!sender.getWorld().equals(receiver.getWorld())) {
                    return true;
                }
                return sender.getLocation().distanceSquared(receiver.getLocation()) > SQUARED_RADIUS;
            }
            return false;
        });
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlineMode() || event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        PlayerProfile profile = event.getPlayerProfile();
        profile.removeProperty("textures");
        String value = "ewogICJ0aW1lc3RhbXAiIDogMTc4NjMyMDI5NjY4OSwKICAicHJvZmlsZUlkIiA6ICJlMmQwMjg1N2YyNDY0NDUwYmJmODY5NDc3YjI3MDkzMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJqYXBpY3JhZnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ3NzM3Yzg3NjgwYzQyMWY2OTBlZGY5ZGI3YzhiNDVkZWUyNjQ0ODgxYTJjZTI5YTZlNTY0OTQ5OGJjODhjNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM1Nzk5NjhjNjRjMTcxOTc0MGZkOGMyYTQ1MTQ2MTg3OWIyMzgwMDI1NzRmY2U0OGY3ZDFhN2MzNmExYzdkNCIKICAgIH0KICB9Cn0=";
        String signature = "vAj2nxwmY2dThIlcCpTgrORHiJHU7KyH7kJ/dJBNY4faEF04VSFshCxFGmWF8dWbH8so1EgrFX4AR97vmJEXqqrHVjjAnQmwKUnxMC7qTEduaZ4LCTSdYt3C4s0teHvY2fFrNj3Gxb3WC/czNT+ne2j7rOx+afdUXizeKrS1YXxuCkjFYiH6XdrGVYjV/V+eWVHECjuot1aiESnYKF1++hP8m9ABdKpNlGpyHvbuipSzvlcUpeBUy5l4tqma2bC8anFiF/Zwm9V8hzpsxhlvB6Z98iUjazsJxMtSzFsUctB+G1qIwzhbwRGZIneVvne7VIgBVBMnlnjYDbp9Vhou9PCBZEn0XGp8WzzCnLm+tRSYnhVSbyzOoqhCA19zBDwjj+GiZfIpVI/4QZ1UX4D6t4cjzD3RRVQJBBFHiCibqLV5x2VQXnWDy2NNBbeDPnrie0+Up8Aecf/UHhQ7OVGnLFBRYwjjUqQqRF4F0yvVdMviRGX5YtbqhGLQ7st96vod8VJuUpOwIFxkuJFWEDhLoxyLaYf7YZbAmrPmJkimsuPYe5PBl50m3DLAh1DrkwLrjN2snKoJEF9HJnE5WU5CoTTSMSAA04fpMU7fqB/umfJ2JFmVe9vPLehgCAV7Lkx7UDN0Ww5Oi+2nPixNX6M5CqOeA79/jaSqQ/MNZPTKrJo=";
        profile.setProperty(new ProfileProperty("textures", value, signature));
        event.setPlayerProfile(profile);
    }
}
