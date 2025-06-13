package com.school.elearning.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.school.elearning.database.DatabaseManager;

public class AuthService {

    public boolean register(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            System.err.println("Password hashing failed.");
            return false;
        }

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            // Common error: UNIQUE constraint failed (username already exists)
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE") || e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Registration failed: Username '" + username + "' already exists.");
            } else {
                System.err.println("Error registering user: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                String providedHashedPassword = hashPassword(password);
                
                // Debugging output (remove in production)
                // System.out.println("Stored hash: " + storedHashedPassword);
                // System.out.println("Provided hash: " + providedHashedPassword);

                return storedHashedPassword.equals(providedHashedPassword);
            } else {
                return false; // User not found
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
} 