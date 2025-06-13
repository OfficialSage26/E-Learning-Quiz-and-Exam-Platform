package com.school.elearning.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.school.elearning.models.QuizAttemptDisplayItem;
import com.school.elearning.models.User;
import com.school.elearning.services.QuizAttemptService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ProgressViewController {

    @FXML private Label summaryLabel;
    @FXML private TableView<QuizAttemptDisplayItem> progressTableView;
    @FXML private TableColumn<QuizAttemptDisplayItem, String> quizTitleColumn;
    @FXML private TableColumn<QuizAttemptDisplayItem, String> scoreColumn;
    @FXML private TableColumn<QuizAttemptDisplayItem, String> timeTakenColumn;
    @FXML private TableColumn<QuizAttemptDisplayItem, String> dateColumn;
    @FXML private Label noAttemptsLabel;

    private QuizAttemptService quizAttemptService;
    private User currentUser; // Store the full User object if needed for more info

    public void initialize() {
        quizAttemptService = new QuizAttemptService();
        System.out.println("ProgressViewController initialized.");
        setupTableColumns();
        // Ensure table is hidden initially if noAttemptsLabel is to be shown first
        progressTableView.setVisible(false);
        progressTableView.setManaged(false);
        noAttemptsLabel.setVisible(true);
        noAttemptsLabel.setManaged(true);
    }

    private void setupTableColumns() {
        quizTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuizTitle()));
        scoreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f%%", cellData.getValue().getScore())));
        timeTakenColumn.setCellValueFactory(cellData -> {
            int totalSeconds = cellData.getValue().getTimeTakenSeconds();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return new SimpleStringProperty(String.format("%02d:%02d", minutes, seconds));
        });
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getAttemptTimestamp();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new SimpleStringProperty(timestamp.format(formatter));
        });
    }

    public void initializeData(String username) {
        // To get User ID, we'll use the getUserByUsername method from QuizAttemptService
        // (Ideally, a dedicated UserService would provide this more cleanly)
        this.currentUser = quizAttemptService.getUserByUsername(username);

        if (this.currentUser == null) {
            summaryLabel.setText("Could not load progress: User not found.");
            noAttemptsLabel.setText("Error: User details not available.");
            progressTableView.setVisible(false);
            progressTableView.setManaged(false);
            noAttemptsLabel.setVisible(true);
            noAttemptsLabel.setManaged(true);
            return;
        }

        List<QuizAttemptDisplayItem> attempts = quizAttemptService.getQuizAttemptsForUser(currentUser.getId());

        if (attempts.isEmpty()) {
            summaryLabel.setText(username + ", you haven't attempted any quizzes yet.");
            progressTableView.setVisible(false);
            progressTableView.setManaged(false);
            noAttemptsLabel.setVisible(true);
            noAttemptsLabel.setManaged(true);
            noAttemptsLabel.setText("No quiz attempts found. Start a quiz to see your progress here!");
        } else {
            ObservableList<QuizAttemptDisplayItem> displayableAttempts = FXCollections.observableArrayList(attempts);
            progressTableView.setItems(displayableAttempts);
            progressTableView.setVisible(true);
            progressTableView.setManaged(true);
            noAttemptsLabel.setVisible(false);
            noAttemptsLabel.setManaged(false);

            // Calculate summary statistics
            double totalScore = 0;
            for (QuizAttemptDisplayItem item : attempts) {
                totalScore += item.getScore();
            }
            double averageScore = totalScore / attempts.size();
            summaryLabel.setText(String.format("%s's Progress: Total Quizzes Attempted: %d | Average Score: %.2f%%", 
                                   username, attempts.size(), averageScore));
        }
    }
} 