package com.school.elearning.models;

public class User {
    private int id;
    private String username;
    // We might add more fields like email, registrationDate later if needed from DB

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // Setters - typically ID might be database-assigned and username unique,
    // so setters might be limited or not present depending on usage.
    // For now, providing them for flexibility.
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               '}';
    }
} 