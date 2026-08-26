package com.japicraft.player;

public enum Mood {
    EAT("Eat something, yummy preferably."),
    NERVOUS("Rest in a bed for a while."),
    TERRIFIED("Have a drink, on the house!");
    private final String assignment;

    Mood(String assignment) {
        this.assignment = assignment;
    }

    public String getAssignment() {
        return assignment;
    }
}
