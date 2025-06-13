package com.school.elearning.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school.elearning.database.DatabaseManager;
import com.school.elearning.models.AnswerOption;
import com.school.elearning.models.DifficultyLevel;
import com.school.elearning.models.Question;
import com.school.elearning.models.QuestionType;
import com.school.elearning.models.Quiz;
import com.school.elearning.models.QuizAttemptDisplayItem;
import com.school.elearning.models.User;

public class QuizAttemptService {

    public int saveQuizAttempt(User user, Quiz quiz, Map<Integer, Object> studentAnswers, List<Question> questions, double score, int timeTakenSeconds) {
        String insertAttemptSql = "INSERT INTO quiz_attempts (user_id, quiz_id, score, time_taken_seconds, attempt_timestamp) VALUES (?, ?, ?, ?, ?)";
        String insertStudentAnswerSql = "INSERT INTO student_answers (quiz_attempt_id, question_id, selected_answer_option_id, short_answer_text, is_correct) VALUES (?, ?, ?, ?, ?)";
        int quizAttemptId = -1;

        if (user == null || user.getId() <= 0) {
            System.err.println("Cannot save quiz attempt: Invalid user data.");
            return -1; // Or throw an exception
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into quiz_attempts
            try (PreparedStatement pstmtAttempt = conn.prepareStatement(insertAttemptSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtAttempt.setInt(1, user.getId());
                pstmtAttempt.setInt(2, quiz.getId());
                pstmtAttempt.setDouble(3, score);
                pstmtAttempt.setInt(4, timeTakenSeconds);
                pstmtAttempt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmtAttempt.executeUpdate();

                try (ResultSet generatedKeys = pstmtAttempt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        quizAttemptId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating quiz attempt failed, no ID obtained.");
                    }
                }
            }

            // 2. Insert into student_answers
            try (PreparedStatement pstmtStudentAnswer = conn.prepareStatement(insertStudentAnswerSql)) {
                for (Question q : questions) {
                    Object answer = studentAnswers.get(q.getId());
                    boolean currentAnswerIsCorrect = false;
                    Integer selectedOptionId = null;
                    String shortAnswerText = null;

                    if (answer != null) {
                        List<AnswerOption> options = q.getAnswerOptions() != null ? q.getAnswerOptions() : new java.util.ArrayList<>();
                        if (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE || q.getQuestionType() == QuestionType.TRUE_FALSE) {
                            selectedOptionId = (Integer) answer;
                            Optional<AnswerOption> correctOption = options.stream().filter(AnswerOption::isCorrect).findFirst();
                            if (correctOption.isPresent() && correctOption.get().getId() == selectedOptionId) {
                                currentAnswerIsCorrect = true;
                            }
                        } else if (q.getQuestionType() == QuestionType.SHORT_ANSWER) {
                            shortAnswerText = ((String) answer).trim();
                            Optional<AnswerOption> correctOption = options.stream()
                                    .filter(opt -> opt.isCorrect() && opt.getOptionText() != null)
                                    .findFirst();
                            if (correctOption.isPresent() && correctOption.get().getOptionText().equalsIgnoreCase(shortAnswerText)) {
                                currentAnswerIsCorrect = true;
                            } else if (correctOption.isEmpty() && options.isEmpty() && shortAnswerText.isEmpty()) {
                                // If no correct answer is defined and student provided no answer,
                                // consider it "correct" in the sense that it's not wrong.
                                // Or handle as per specific quiz rules (e.g. needs a non-empty correct answer).
                                // For now, this means an empty short answer to a question without a defined correct one is not marked wrong here.
                                // The scoring logic in QuizInterfaceController should primarily determine points.
                                // This 'is_correct' is for feedback.
                            }
                        }
                    }

                    pstmtStudentAnswer.setInt(1, quizAttemptId);
                    pstmtStudentAnswer.setInt(2, q.getId());
                    if (selectedOptionId != null) {
                        pstmtStudentAnswer.setInt(3, selectedOptionId);
                    } else {
                        pstmtStudentAnswer.setNull(3, Types.INTEGER);
                    }
                    pstmtStudentAnswer.setString(4, shortAnswerText);
                    pstmtStudentAnswer.setBoolean(5, currentAnswerIsCorrect);
                    pstmtStudentAnswer.addBatch();
                }
                pstmtStudentAnswer.executeBatch();
            }

            conn.commit(); // Commit transaction
            System.out.println("Quiz attempt saved successfully for user ID: " + user.getId() + ", quiz ID: " + quiz.getId() + ", attempt ID: " + quizAttemptId);

        } catch (SQLException e) {
            System.err.println("Error saving quiz attempt to database: " + e.getMessage());
            e.printStackTrace();
            // Consider rolling back if conn was not null and auto-commit was false
            quizAttemptId = -1; // Indicate failure
             // Rollback in case of error:
            // Connection conn = DatabaseManager.getConnection(); // Re-obtain or ensure it's accessible
            // if (conn != null) {
            //     try {
            //         conn.rollback();
            //     } catch (SQLException ex) {
            //         System.err.println("Error rolling back transaction: " + ex.getMessage());
            //     }
            // }
        }
        return quizAttemptId;
    }

    // Helper method to get User by username (can be expanded or moved to UserService)
    // This is needed if QuizInterfaceController only has username
    public User getUserByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), username);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Or throw an exception
    }

    public List<QuizAttemptDisplayItem> getQuizAttemptsForUser(int userId) {
        List<QuizAttemptDisplayItem> attempts = new ArrayList<>();
        String sql = "SELECT qa.id, qa.quiz_id, qa.score, qa.time_taken_seconds, qa.attempt_timestamp, q.title AS quiz_title " +
                     "FROM quiz_attempts qa " +
                     "JOIN quizzes q ON qa.quiz_id = q.id " +
                     "WHERE qa.user_id = ? ORDER BY qa.attempt_timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attempts.add(new QuizAttemptDisplayItem(
                    rs.getInt("id"),
                    userId,
                    rs.getInt("quiz_id"),
                    rs.getString("quiz_title"),
                    rs.getDouble("score"),
                    rs.getInt("time_taken_seconds"),
                    rs.getTimestamp("attempt_timestamp").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching quiz attempts for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return attempts;
    }

    public double getBestScoreForSubjectDifficulty(int userId, String subject, DifficultyLevel difficulty) {
        double bestScore = 0.0;
        String sql = "SELECT MAX(qa.score) AS max_score " +
                     "FROM quiz_attempts qa " +
                     "JOIN quizzes q ON qa.quiz_id = q.id " +
                     "WHERE qa.user_id = ? AND q.subject = ? AND q.difficulty_level = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, subject);
            pstmt.setString(3, difficulty.name()); // Assumes DifficultyLevel enum has a name() method returning "EASY", "MEDIUM", "HARD"
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bestScore = rs.getDouble("max_score");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching best score for user " + userId + ", subject " + subject + ", difficulty " + difficulty + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Catch potential NPE if difficulty is null, though unlikely here
            System.err.println("An unexpected error occurred while fetching best score: " + e.getMessage());
            e.printStackTrace();
        }
        return bestScore;
    }
} 