package com.school.elearning.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.school.elearning.database.DatabaseManager;
import com.school.elearning.models.DifficultyLevel;

public class LeaderboardService {
    
    public static class LeaderboardEntry {
        private final String username;
        private final String subject;
        private final String difficulty;
        private final double score;
        private final int timeTakenSeconds;
        private final String quizTitle;
        
        public LeaderboardEntry(String username, String subject, String difficulty, double score, int timeTakenSeconds, String quizTitle) {
            this.username = username;
            this.subject = subject;
            this.difficulty = difficulty;
            this.score = score;
            this.timeTakenSeconds = timeTakenSeconds;
            this.quizTitle = quizTitle;
        }
        
        public String getUsername() { return username; }
        public String getSubject() { return subject; }
        public String getDifficulty() { return difficulty; }
        public double getScore() { return score; }
        public int getTimeTakenSeconds() { return timeTakenSeconds; }
        public String getQuizTitle() { return quizTitle; }
    }
    
    public List<LeaderboardEntry> getGlobalLeaderboard(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = "SELECT u.username, q.subject, q.difficulty_level, qa.score, qa.time_taken_seconds, q.title as quiz_title " +
                    "FROM quiz_attempts qa " +
                    "JOIN users u ON qa.user_id = u.id " +
                    "JOIN quizzes q ON qa.quiz_id = q.id " +
                    "ORDER BY qa.score DESC, qa.time_taken_seconds ASC " +
                    "LIMIT ?";
                    
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                    rs.getString("username"),
                    rs.getString("subject"),
                    rs.getString("difficulty_level"),
                    rs.getDouble("score"),
                    rs.getInt("time_taken_seconds"),
                    rs.getString("quiz_title")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching global leaderboard: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }
    
    public List<LeaderboardEntry> getSubjectLeaderboard(String subject, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = "SELECT u.username, q.subject, q.difficulty_level, qa.score, qa.time_taken_seconds, q.title as quiz_title " +
                    "FROM quiz_attempts qa " +
                    "JOIN users u ON qa.user_id = u.id " +
                    "JOIN quizzes q ON qa.quiz_id = q.id " +
                    "WHERE q.subject = ? " +
                    "ORDER BY qa.score DESC, qa.time_taken_seconds ASC " +
                    "LIMIT ?";
                    
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                    rs.getString("username"),
                    rs.getString("subject"),
                    rs.getString("difficulty_level"),
                    rs.getDouble("score"),
                    rs.getInt("time_taken_seconds"),
                    rs.getString("quiz_title")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subject leaderboard: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }
    
    public List<LeaderboardEntry> getDifficultyLeaderboard(DifficultyLevel difficulty, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = "SELECT u.username, q.subject, q.difficulty_level, qa.score, qa.time_taken_seconds, q.title as quiz_title " +
                    "FROM quiz_attempts qa " +
                    "JOIN users u ON qa.user_id = u.id " +
                    "JOIN quizzes q ON qa.quiz_id = q.id " +
                    "WHERE q.difficulty_level = ? " +
                    "ORDER BY qa.score DESC, qa.time_taken_seconds ASC " +
                    "LIMIT ?";
                    
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, difficulty.name());
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                    rs.getString("username"),
                    rs.getString("subject"),
                    rs.getString("difficulty_level"),
                    rs.getDouble("score"),
                    rs.getInt("time_taken_seconds"),
                    rs.getString("quiz_title")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching difficulty leaderboard: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }
    
    public List<String> getAllSubjects() {
        List<String> subjects = new ArrayList<>();
        String sql = "SELECT DISTINCT subject FROM quizzes ORDER BY subject";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(rs.getString("subject"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subjects: " + e.getMessage());
            e.printStackTrace();
        }
        return subjects;
    }
} 