package com.school.elearning.controllers;

import java.io.IOException;
import java.util.List;

import com.school.elearning.services.QuizService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane; // Assuming mainContentArea in Dashboard is AnchorPane
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class SubjectSelectionController {

    @FXML private VBox subjectSelectionRoot;
    @FXML private TilePane subjectTilePane;
    @FXML private Label statusLabel;

    private QuizService quizService;
    private String currentUsername;
    private AnchorPane mainContentArea; // To load next FXML into dashboard's content area

    public void initialize() {
        quizService = new QuizService();
        subjectTilePane.setPrefTileWidth(180);
        subjectTilePane.setPrefTileHeight(120);
    }

    public void initializeData(String username, AnchorPane mainContentArea) {
        this.currentUsername = username;
        this.mainContentArea = mainContentArea; // Store reference to dashboard's content area
        loadSubjects();
    }

    private void loadSubjects() {
        List<String> subjects = quizService.getSubjects();
        subjectTilePane.getChildren().clear();

        if (subjects.isEmpty()) {
            statusLabel.setText("No subjects found. Please add quizzes with subjects.");
            return;
        }

        int numSubjects = subjects.size();
        if (numSubjects > 0) {
            int numColumns = (int) Math.ceil(Math.sqrt(numSubjects));
            subjectTilePane.setPrefColumns(numColumns);
        }

        for (String subjectName : subjects) {
            Button subjectButton = new Button(subjectName);
            subjectButton.setPrefSize(180, 120);
            subjectButton.setStyle("-fx-font-size: 14px; -fx-background-color: #ADD8E6; -fx-border-color: #00008B; -fx-border-width: 1px; -fx-background-radius: 10; -fx-border-radius: 10;");
            subjectButton.setTextAlignment(TextAlignment.CENTER);
            subjectButton.setWrapText(true);
            subjectButton.setOnAction(event -> handleSubjectSelected(subjectName));
            subjectTilePane.getChildren().add(subjectButton);
        }
    }

    private void handleSubjectSelected(String subjectName) {
        statusLabel.setText("");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/difficulty_selection.fxml"));
            Node difficultySelectionNode = loader.load();

            DifficultySelectionController difficultyController = loader.getController();
            difficultyController.initializeData(currentUsername, subjectName, mainContentArea);
            
            // Load the difficulty selection into the mainContentArea of the dashboard
            if (mainContentArea != null) {
                mainContentArea.getChildren().setAll(difficultySelectionNode);
            } else {
                System.err.println("Error: mainContentArea is null in SubjectSelectionController.");
                statusLabel.setText("Error: Could not navigate to difficulty selection.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading difficulty selection screen: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load difficulty selection. FXML path or controller issue.");
        }
    }
} 