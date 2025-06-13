package com.school.elearning.models;

import java.time.LocalDateTime;
import java.util.List;

public class QuizAttempt {
    private int id;
    private int userId; // Foreign key to User table
    private int quizId; // Foreign key to Quiz table
    private double score; // Can be percentage or raw score
    private int timeTakenSeconds;
    private LocalDateTime attemptTimestamp;
    private List<StudentAnswer> studentAnswers; // List of answers given in this attempt

    // Constructor without studentAnswers, as they might be added later
    public QuizAttempt(int id, int userId, int quizId, double score, int timeTakenSeconds, LocalDateTime attemptTimestamp) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.timeTakenSeconds = timeTakenSeconds;
        this.attemptTimestamp = attemptTimestamp;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(int timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public LocalDateTime getAttemptTimestamp() {
        return attemptTimestamp;
    }

    public void setAttemptTimestamp(LocalDateTime attemptTimestamp) {
        this.attemptTimestamp = attemptTimestamp;
    }

    public List<StudentAnswer> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(List<StudentAnswer> studentAnswers) {
        this.studentAnswers = studentAnswers;
    }

    @Override
    public String toString() {
        return "QuizAttempt{" +
               "id=" + id +
               ", userId=" + userId +
               ", quizId=" + quizId +
               ", score=" + score +
               ", timeTakenSeconds=" + timeTakenSeconds +
               ", attemptTimestamp=" + attemptTimestamp +
               '}';
    }
} 