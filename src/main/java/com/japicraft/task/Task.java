package com.japicraft.task;

public enum Task {
    EAT("Eat something, yummy preferably."),
    SLEEP("Rest in a bed for a while."),
    DRINK("Have a drink, on the house!");
    private final String assignment;

    Task(String assignment) {
        this.assignment = assignment;
    }

    public String getAssignment() {
        return assignment;
    }
}
