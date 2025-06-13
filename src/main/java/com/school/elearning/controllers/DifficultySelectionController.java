package com.school.elearning.controllers;

import java.io.IOException;
import java.util.List;

import com.school.elearning.models.DifficultyLevel;
import com.school.elearning.models.Quiz;
import com.school.elearning.models.User;
import com.school.elearning.services.QuizAttemptService;
import com.school.elearning.services.QuizService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DifficultySelectionController {

    @FXML private VBox difficultySelectionRoot;
    @FXML private Label subjectTitleLabel;
    @FXML private Label statusLabel;
    @FXML private Button easyButton;
    @FXML private Button mediumButton;
    @FXML private Button hardButton;

    private String currentUsername;
    private String selectedSubject;
    private QuizService quizService;
    private QuizAttemptService quizAttemptService;
    private AnchorPane mainContentArea; // To load quiz interface into dashboard content area OR replace scene

    public void initialize() {
        quizService = new QuizService();
        quizAttemptService = new QuizAttemptService();

        // Set user data to buttons to identify which difficulty was clicked
        easyButton.setUserData(DifficultyLevel.EASY);
        mediumButton.setUserData(DifficultyLevel.MEDIUM);
        hardButton.setUserData(DifficultyLevel.HARD);
    }

    public void initializeData(String username, String subject, AnchorPane mainContentArea) {
        this.currentUsername = username;
        this.selectedSubject = subject;
        this.mainContentArea = mainContentArea;
        subjectTitleLabel.setText("Selected Subject: " + subject);
        updateDifficultyButtonStates();
    }

    private void updateDifficultyButtonStates() {
        User currentUser = quizAttemptService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            statusLabel.setText("Error: Could not verify user.");
            easyButton.setDisable(true);
            mediumButton.setDisable(true);
            hardButton.setDisable(true);
            return;
        }
        int userId = currentUser.getId();

        // Easy is always enabled
        easyButton.setDisable(false);
        easyButton.setTooltip(null);
        easyButton.setStyle("-fx-font-size: 16px; -fx-background-color: #90EE90;");

        // Check for Medium unlock
        double bestEasyScore = quizAttemptService.getBestScoreForSubjectDifficulty(userId, selectedSubject, DifficultyLevel.EASY);
        boolean mediumUnlocked = bestEasyScore >= 50.0;
        mediumButton.setDisable(!mediumUnlocked);
        if (mediumUnlocked) {
            mediumButton.setTooltip(null);
            mediumButton.setStyle("-fx-font-size: 16px; -fx-background-color: #FFD700;");
        } else {
            mediumButton.setTooltip(new Tooltip("Complete 'Easy' with at least 50% to unlock."));
            mediumButton.setStyle("-fx-font-size: 16px; -fx-background-color: #FFD700; -fx-opacity: 0.5;");
        }

        // Check for Hard unlock (only if Medium is unlocked)
        boolean hardUnlocked = false;
        if (mediumUnlocked) {
            double bestMediumScore = quizAttemptService.getBestScoreForSubjectDifficulty(userId, selectedSubject, DifficultyLevel.MEDIUM);
            hardUnlocked = bestMediumScore >= 75.0;
        }
        hardButton.setDisable(!hardUnlocked);
        if (hardUnlocked) {
            hardButton.setTooltip(null);
            hardButton.setStyle("-fx-font-size: 16px; -fx-background-color: #FF6347;");
        } else {
            if (!mediumUnlocked) {
                 hardButton.setTooltip(new Tooltip("Complete 'Easy' with at least 50% to unlock 'Medium' first."));
            } else {
                 hardButton.setTooltip(new Tooltip("Complete 'Medium' with at least 75% to unlock."));
            }
            hardButton.setStyle("-fx-font-size: 16px; -fx-background-color: #FF6347; -fx-opacity: 0.5;");
        }
    }

    @FXML
    void handleDifficultySelected(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        DifficultyLevel selectedDifficulty = (DifficultyLevel) clickedButton.getUserData();
        statusLabel.setText("");

        List<Quiz> quizzes = quizService.getQuizzesBySubjectAndDifficulty(selectedSubject, selectedDifficulty);

        if (quizzes.isEmpty()) {
            statusLabel.setText("No quiz found for " + selectedSubject + " - " + selectedDifficulty.getDisplayName() + ". Please check data.");
            return;
        }

        // For now, let's take the first quiz found for that subject/difficulty.
        // Future enhancement: If multiple quizzes, list them for selection.
        Quiz quizToStart = quizzes.get(0);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz_interface.fxml"));
            Parent quizInterfaceRoot = loader.load();

            QuizInterfaceController quizInterfaceController = loader.getController();
            quizInterfaceController.initializeQuiz(quizToStart.getId(), this.currentUsername);

            // Replace the entire scene of the primary stage for the quiz interface
            Stage primaryStage = (Stage) difficultySelectionRoot.getScene().getWindow(); 
            if (primaryStage == null) { // Fallback if root not on scene yet (should not happen here)
                 primaryStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            }

            primaryStage.setScene(new Scene(quizInterfaceRoot));
            primaryStage.setTitle("Quiz: " + quizToStart.getTitle() + " - " + this.currentUsername);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading quiz interface: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load quiz. Check FXML path or controller setup: " + e.getMessage());
        }
    }
} 