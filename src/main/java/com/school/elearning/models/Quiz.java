package com.school.elearning.models;

import java.util.List;

public class Quiz {
    private int id;
    private String title;
    private String subject;
    // private int subjectId; // We'll add subjects later
    private DifficultyLevel difficultyLevel;
    private int timeLimitMinutes; // in minutes
    private List<Question> questions; // To hold questions related to this quiz

    // Constructor for basic quiz info (e.g., when listing quizzes)
    public Quiz(int id, String title, String subject, DifficultyLevel difficultyLevel, int timeLimitMinutes) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.difficultyLevel = difficultyLevel;
        this.timeLimitMinutes = timeLimitMinutes;
    }

    // Constructor that includes questions (e.g., when taking a quiz)
    public Quiz(int id, String title, String subject, DifficultyLevel difficultyLevel, int timeLimitMinutes, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.difficultyLevel = difficultyLevel;
        this.timeLimitMinutes = timeLimitMinutes;
        this.questions = questions;
    }
    
    // Default constructor (if needed by frameworks or for other use cases)
    public Quiz() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                 "id=" + id +
                 ", title='" + title + '\'' +
                 ", subject='" + subject + '\'' +
                 ", difficultyLevel=" + difficultyLevel +
                 ", timeLimitMinutes=" + timeLimitMinutes +
                 '}';
    }
} 