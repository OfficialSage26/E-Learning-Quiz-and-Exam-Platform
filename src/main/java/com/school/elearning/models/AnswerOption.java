package com.school.elearning.models;

public class AnswerOption {
    private int id;
    private int questionId; // Foreign key to Question
    private String optionText;
    private boolean isCorrect; // True if this is the correct option

    public AnswerOption(int id, int questionId, String optionText, boolean isCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return "AnswerOption{" +
               "id=" + id +
               ", questionId=" + questionId +
               ", optionText='" + optionText + '\'' +
               ", isCorrect=" + isCorrect +
               '}';
    }
} 