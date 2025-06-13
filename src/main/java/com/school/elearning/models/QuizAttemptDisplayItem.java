package com.school.elearning.models;

import java.time.LocalDateTime;

public class QuizAttemptDisplayItem {
    private int id;
    private int userId;
    private int quizId;
    private String quizTitle;
    private double score;
    private int timeTakenSeconds;
    private LocalDateTime attemptTimestamp;

    public QuizAttemptDisplayItem(int id, int userId, int quizId, String quizTitle, double score, int timeTakenSeconds, LocalDateTime attemptTimestamp) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.timeTakenSeconds = timeTakenSeconds;
        this.attemptTimestamp = attemptTimestamp;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getQuizId() { return quizId; }
    public String getQuizTitle() { return quizTitle; }
    public double getScore() { return score; }
    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public LocalDateTime getAttemptTimestamp() { return attemptTimestamp; }
    
    // Optionally, add setters if needed, though typically DTOs for display are immutable after creation.
} 