package com.school.elearning.models;

public enum DifficultyLevel {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    // Optional: a method to get enum from string, useful for database retrieval
    public static DifficultyLevel fromString(String text) {
        for (DifficultyLevel b : DifficultyLevel.values()) {
            if (b.displayName.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
} 