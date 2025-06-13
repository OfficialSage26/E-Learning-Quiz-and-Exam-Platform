package com.school.elearning.models;

public enum QuestionType {
    MULTIPLE_CHOICE("Multiple Choice"),
    TRUE_FALSE("True/False"),
    SHORT_ANSWER("Short Answer");
    // Add more types like FILL_IN_THE_BLANK, MATCHING as needed later

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    // Optional: a method to get enum from string
    public static QuestionType fromString(String text) {
        for (QuestionType qt : QuestionType.values()) {
            if (qt.displayName.equalsIgnoreCase(text) || qt.name().equalsIgnoreCase(text)) {
                return qt;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found for QuestionType");
    }
} 