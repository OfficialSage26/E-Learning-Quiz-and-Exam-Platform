package com.school.elearning.models;

public class StudentAnswer {
    private int id;
    private int quizAttemptId; // Foreign key to QuizAttempt
    private int questionId;    // Foreign key to Question
    private Integer selectedAnswerOptionId; // Foreign key to AnswerOption (nullable for short answers)
    private String shortAnswerText;      // For QuestionType.SHORT_ANSWER (nullable otherwise)
    private boolean isCorrect;         // Was the provided answer correct?

    // Constructor
    public StudentAnswer(int id, int quizAttemptId, int questionId, Integer selectedAnswerOptionId, String shortAnswerText, boolean isCorrect) {
        this.id = id;
        this.quizAttemptId = quizAttemptId;
        this.questionId = questionId;
        this.selectedAnswerOptionId = selectedAnswerOptionId;
        this.shortAnswerText = shortAnswerText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizAttemptId() {
        return quizAttemptId;
    }

    public void setQuizAttemptId(int quizAttemptId) {
        this.quizAttemptId = quizAttemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedAnswerOptionId() {
        return selectedAnswerOptionId;
    }

    public void setSelectedAnswerOptionId(Integer selectedAnswerOptionId) {
        this.selectedAnswerOptionId = selectedAnswerOptionId;
    }

    public String getShortAnswerText() {
        return shortAnswerText;
    }

    public void setShortAnswerText(String shortAnswerText) {
        this.shortAnswerText = shortAnswerText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return "StudentAnswer{" +
               "id=" + id +
               ", quizAttemptId=" + quizAttemptId +
               ", questionId=" + questionId +
               ", selectedAnswerOptionId=" + selectedAnswerOptionId +
               ", shortAnswerText='" + shortAnswerText + '\'' +
               ", isCorrect=" + isCorrect +
               '}';
    }
} 