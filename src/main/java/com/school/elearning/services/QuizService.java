package com.school.elearning.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.school.elearning.database.DatabaseManager;
import com.school.elearning.models.AnswerOption;
import com.school.elearning.models.DifficultyLevel;
import com.school.elearning.models.Question;
import com.school.elearning.models.QuestionType;
import com.school.elearning.models.Quiz;

public class QuizService {

    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT id, title, subject, difficulty_level, time_limit_minutes FROM quizzes";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String subject = rs.getString("subject");
                DifficultyLevel difficulty = DifficultyLevel.valueOf(rs.getString("difficulty_level").toUpperCase());
                int timeLimit = rs.getInt("time_limit_minutes");
                quizzes.add(new Quiz(id, title, subject, difficulty, timeLimit));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all quizzes: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing difficulty level from database: " + e.getMessage());
            e.printStackTrace();
        }
        return quizzes;
    }

    public Quiz getQuizWithDetails(int quizId) {
        Quiz quiz = null;
        String quizSql = "SELECT id, title, subject, difficulty_level, time_limit_minutes FROM quizzes WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) { // Keep connection open for subsequent calls
            try (PreparedStatement quizPstmt = conn.prepareStatement(quizSql)) {
                quizPstmt.setInt(1, quizId);
                try (ResultSet quizRs = quizPstmt.executeQuery()) {
                    if (quizRs.next()) {
                        String title = quizRs.getString("title");
                        String subject = quizRs.getString("subject");
                        DifficultyLevel difficulty = DifficultyLevel.valueOf(quizRs.getString("difficulty_level").toUpperCase());
                        int timeLimit = quizRs.getInt("time_limit_minutes");
                        quiz = new Quiz(quizId, title, subject, difficulty, timeLimit);
                        // Now fetch questions and their options using the same connection
                        quiz.setQuestions(getQuestionsForQuiz(conn, quizId));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching quiz with details (quizId: " + quizId + "): " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing difficulty level for quizId " + quizId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return quiz;
    }

    private List<Question> getQuestionsForQuiz(Connection conn, int quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String questionSql = "SELECT id, question_text, question_type, image_path FROM questions WHERE quiz_id = ?";

        try (PreparedStatement questionPstmt = conn.prepareStatement(questionSql)) {
            questionPstmt.setInt(1, quizId);
            try (ResultSet questionRs = questionPstmt.executeQuery()) {
                while (questionRs.next()) {
                    int questionId = questionRs.getInt("id");
                    String questionText = questionRs.getString("question_text");
                    QuestionType type = QuestionType.valueOf(questionRs.getString("question_type").toUpperCase());
                    String imagePath = questionRs.getString("image_path");

                    Question question = new Question(questionId, quizId, questionText, type, imagePath);
                    question.setAnswerOptions(getAnswerOptionsForQuestion(conn, questionId));
                    questions.add(question);
                }
            }
        }
        return questions;
    }

    private List<AnswerOption> getAnswerOptionsForQuestion(Connection conn, int questionId) throws SQLException {
        List<AnswerOption> options = new ArrayList<>();
        String optionSql = "SELECT id, option_text, is_correct FROM answer_options WHERE question_id = ?";

        try (PreparedStatement optionPstmt = conn.prepareStatement(optionSql)) {
            optionPstmt.setInt(1, questionId);
            try (ResultSet optionRs = optionPstmt.executeQuery()) {
                while (optionRs.next()) {
                    int optionId = optionRs.getInt("id");
                    String optionText = optionRs.getString("option_text");
                    boolean isCorrect = optionRs.getBoolean("is_correct");
                    options.add(new AnswerOption(optionId, questionId, optionText, isCorrect));
                }
            }
        }
        return options;
    }

    public List<String> getSubjects() {
        List<String> subjects = new ArrayList<>();
        String sql = "SELECT DISTINCT subject FROM quizzes ORDER BY subject ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                subjects.add(rs.getString("subject"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subjects: " + e.getMessage());
            e.printStackTrace();
        }
        return subjects;
    }

    public List<Quiz> getQuizzesBySubjectAndDifficulty(String subject, DifficultyLevel difficulty) {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT id, title, subject, difficulty_level, time_limit_minutes FROM quizzes WHERE subject = ? AND difficulty_level = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            pstmt.setString(2, difficulty.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                Quiz quiz = getQuizWithDetails(id);
                if (quiz != null) {
                    quizzes.add(quiz);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching quizzes by subject and difficulty: " + e.getMessage());
            e.printStackTrace();
        }
        return quizzes;
    }

    // Placeholder for getting user score on a quiz - to be developed for progress tracking
    public double getUserBestScore(int userId, int quizId) {
        // TODO: Implement database query to get best score
        System.out.println("Fetching best score for user " + userId + " on quiz " + quizId + " - Not implemented yet.");
        return 0.0; // Placeholder
    }
} 