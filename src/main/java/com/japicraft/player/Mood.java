package com.japicraft.player;

public enum Mood {
    CALM("1", "No danger around at all."),
    NERVOUS("2", "A possibility of dangerous presence."),
    TERRIFIED("3", "Life threatening danger close by!");
    private final String model;
    private final String description;

    Mood(String model, String description) {
        this.model = model;
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public String getDescription() {
        return description;
    }
}
