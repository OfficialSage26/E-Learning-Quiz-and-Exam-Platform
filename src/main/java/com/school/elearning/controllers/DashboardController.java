package com.school.elearning.controllers;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button startQuizButton;

    @FXML
    private Button viewProgressButton;

    @FXML
    private Button viewLeaderboardsButton;
    
    @FXML
    private Button userGuideButton; 

    @FXML
    private AnchorPane mainContentArea; // To load other FXMLs into

    private String currentUsername;

    public void initialize() {
        // Initialization code can go here if needed when the FXML is loaded
        // For example, load a default view into mainContentArea
        System.out.println("Dashboard Initialized.");
    }

    // Method to receive data from LoginController
    public void initializeData(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("Welcome, " + username + "!");
        // You can now use this.currentUsername for other operations if needed
    }

    @FXML
    protected void handleLogoutButtonAction(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find login.fxml");
                // Optionally show an alert to the user
                return;
            }
            Parent loginRoot = FXMLLoader.load(fxmlUrl);
            Scene loginScene = new Scene(loginRoot);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Student Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally show an error message to the user
        }
    }

    @FXML
    protected void handleStartQuizAction(ActionEvent event) {
        System.out.println("Start New Quiz button clicked by " + currentUsername);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/subject_selection.fxml"));
            Node subjectSelectionNode = loader.load();

            SubjectSelectionController subjectSelectionController = loader.getController();
            if (subjectSelectionController != null) {
                subjectSelectionController.initializeData(this.currentUsername, this.mainContentArea);
            } else {
                System.err.println("Failed to get SubjectSelectionController instance.");
                mainContentArea.getChildren().setAll(new Label("Error: Could not load subject selection controller."));
            }
            mainContentArea.getChildren().setAll(subjectSelectionNode);
        } catch (IOException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Error loading subject selection screen: " + e.getMessage()));
        } catch (NullPointerException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Failed to load subject selection. FXML path or controller issue."));
        }
    }

    @FXML
    protected void handleViewProgressAction(ActionEvent event) {
        System.out.println("View My Progress button clicked by " + currentUsername);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/progress_view.fxml"));
            Node progressViewNode = loader.load();

            ProgressViewController progressViewController = loader.getController();
            if (progressViewController != null) {
                progressViewController.initializeData(this.currentUsername);
            } else {
                System.err.println("Failed to get ProgressViewController instance.");
            }
            mainContentArea.getChildren().setAll(progressViewNode);
        } catch (IOException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Error loading progress screen: " + e.getMessage()));
        } catch (NullPointerException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Failed to load progress screen. FXML path or controller issue."));
        }
    }

    @FXML
    protected void handleViewLeaderboardsAction(ActionEvent event) {
        System.out.println("View Leaderboards button clicked by " + currentUsername);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/leaderboard_view.fxml"));
            Node leaderboardViewNode = loader.load();

            LeaderboardViewController leaderboardViewController = loader.getController();
            if (leaderboardViewController != null) {
                leaderboardViewController.initializeData(this.currentUsername);
            } else {
                System.err.println("Failed to get LeaderboardViewController instance.");
            }
            mainContentArea.getChildren().setAll(leaderboardViewNode);
        } catch (IOException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Error loading leaderboards screen: " + e.getMessage()));
        } catch (NullPointerException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Failed to load leaderboards screen. FXML path or controller issue."));
        }
    }
    
    @FXML
    protected void handleUserGuideAction(ActionEvent event) {
        System.out.println("User Guide button clicked by " + currentUsername);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_guide.fxml"));
            Node userGuideNode = loader.load();

            UserGuideController userGuideController = loader.getController();
            if (userGuideController != null) {
                userGuideController.initializeData(this.currentUsername);
            } else {
                System.err.println("Failed to get UserGuideController instance.");
            }
            mainContentArea.getChildren().setAll(userGuideNode);
        } catch (IOException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Error loading user guide: " + e.getMessage()));
        } catch (NullPointerException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Failed to load user guide. FXML path or controller issue."));
        }
    }

    // Helper method to load FXML content into the mainContentArea
    private void loadContent(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("Cannot load FXML file: " + fxmlPath + ". Make sure it's in your resources/fxml folder.");
                mainContentArea.getChildren().setAll(new Label("Error: Content not found (" + fxmlPath + ")"));
                return;
            }
            Node content = FXMLLoader.load(fxmlUrl);
            mainContentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
            mainContentArea.getChildren().setAll(new Label("Error loading content from " + fxmlPath));
        }
    }
} 