package com.school.elearning.models;

import java.util.List;

public class Question {
    private int id;
    private int quizId;
    private String questionText;
    private QuestionType questionType;
    private String imagePath; // Optional, path to an image file
    private List<AnswerOption> answerOptions; // For multiple choice / true-false
    // For short answer, the correct answer could be a special AnswerOption or a direct field

    public Question(int id, int quizId, String questionText, QuestionType questionType, String imagePath) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    @Override
    public String toString() {
        return "Question{" +
               "id=" + id +
               ", quizId=" + quizId +
               ", questionText='" + questionText + '\'' +
               ", questionType=" + questionType +
               (imagePath != null ? ", imagePath='" + imagePath + '\'' : "") +
               '}';
    }
} 