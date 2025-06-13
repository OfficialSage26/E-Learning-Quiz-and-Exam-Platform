package com.school.elearning.controllers;

import java.util.List;

import com.school.elearning.models.DifficultyLevel;
import com.school.elearning.services.LeaderboardService;
import com.school.elearning.services.LeaderboardService.LeaderboardEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class LeaderboardViewController {

    @FXML private ComboBox<String> subjectComboBox;
    @FXML private ComboBox<DifficultyLevel> difficultyComboBox;
    @FXML private Button refreshButton;
    @FXML private TableView<LeaderboardEntry> leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, String> rankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> usernameColumn;
    @FXML private TableColumn<LeaderboardEntry, String> subjectColumn;
    @FXML private TableColumn<LeaderboardEntry, String> difficultyColumn;
    @FXML private TableColumn<LeaderboardEntry, String> scoreColumn;
    @FXML private TableColumn<LeaderboardEntry, String> timeColumn;
    @FXML private TableColumn<LeaderboardEntry, String> quizTitleColumn;

    private LeaderboardService leaderboardService;
    private String currentUsername;
    private ObservableList<LeaderboardEntry> leaderboardEntries = FXCollections.observableArrayList();

    public void initialize() {
        leaderboardService = new LeaderboardService();
        
        // Initialize difficulty combo box
        difficultyComboBox.setItems(FXCollections.observableArrayList(DifficultyLevel.values()));
        
        // Initialize subject combo box
        List<String> subjects = leaderboardService.getAllSubjects();
        subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
        
        // Set up table columns
        rankColumn.setCellValueFactory(cellData -> {
            int index = leaderboardTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubject()));
        difficultyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDifficulty()));
        scoreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f%%", cellData.getValue().getScore())));
        timeColumn.setCellValueFactory(cellData -> {
            int totalSeconds = cellData.getValue().getTimeTakenSeconds();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return new SimpleStringProperty(String.format("%02d:%02d", minutes, seconds));
        });
        quizTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuizTitle()));
        
        // Set up table
        leaderboardTable.setItems(leaderboardEntries);
        
        // Load initial data
        refreshLeaderboard();
    }

    public void initializeData(String username) {
        this.currentUsername = username;
        refreshLeaderboard();
    }

    @FXML
    protected void handleRefreshAction() {
        refreshLeaderboard();
    }

    private void refreshLeaderboard() {
        String selectedSubject = subjectComboBox.getValue();
        DifficultyLevel selectedDifficulty = difficultyComboBox.getValue();
        
        List<LeaderboardEntry> entries;
        if (selectedSubject != null && selectedDifficulty != null) {
            // Filter by both subject and difficulty
            entries = leaderboardService.getSubjectLeaderboard(selectedSubject, 100);
            entries.removeIf(entry -> !entry.getDifficulty().equals(selectedDifficulty.name()));
        } else if (selectedSubject != null) {
            // Filter by subject only
            entries = leaderboardService.getSubjectLeaderboard(selectedSubject, 100);
        } else if (selectedDifficulty != null) {
            // Filter by difficulty only
            entries = leaderboardService.getDifficultyLeaderboard(selectedDifficulty, 100);
        } else {
            // Show global leaderboard
            entries = leaderboardService.getGlobalLeaderboard(100);
        }
        
        leaderboardEntries.setAll(entries);
    }
} 