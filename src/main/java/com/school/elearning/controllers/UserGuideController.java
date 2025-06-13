package com.school.elearning.controllers;

public class UserGuideController {

    public void initialize() {
        System.out.println("UserGuideController initialized.");
        // Future: Could load guide content dynamically or handle interactions.
    }
    
    // Method to be called by DashboardController if data needs to be passed (e.g. username for personalized tips - unlikely for a generic guide)
    public void initializeData(String username) {
        System.out.println("User Guide accessed by: " + username);
    }
} 