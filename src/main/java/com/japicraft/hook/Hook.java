package com.japicraft.hook;

import org.bukkit.event.Listener;

public enum Hook {
    BETTER_MODEL("BetterModel", "Advanced animation capabilities", BetterModelHook.class);

    private final String name;
    private final String purpose;
    private final Class<? extends Listener> listener;
    private boolean available;

    Hook(String name, String purpose, Class<? extends Listener> listener) {
        this.name = name;
        this.purpose = purpose;
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public String getPurpose() {
        return purpose;
    }

    public Class<? extends Listener> getListener() {
        return listener;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
