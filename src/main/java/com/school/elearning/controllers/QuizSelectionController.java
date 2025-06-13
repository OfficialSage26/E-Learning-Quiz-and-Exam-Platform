package com.school.elearning.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.school.elearning.models.DifficultyLevel;
import com.school.elearning.models.Quiz;
import com.school.elearning.services.QuizService;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class QuizSelectionController {

    @FXML
    private VBox quizSelectionRoot; // fx:id for the root pane, useful for getting the stage

    @FXML
    private TableView<QuizDisplayItem> quizzesTableView;

    @FXML
    private TableColumn<QuizDisplayItem, String> titleColumn;

    @FXML
    private TableColumn<QuizDisplayItem, String> difficultyColumn;

    @FXML
    private TableColumn<QuizDisplayItem, Integer> timeLimitColumn;

    @FXML
    private TableColumn<QuizDisplayItem, Integer> questionsCountColumn;

    @FXML
    private Button startQuizButton;

    @FXML
    private Label statusLabel;

    private QuizService quizService;
    private ObservableList<QuizDisplayItem> quizDisplayList = FXCollections.observableArrayList();
    private String currentUsername; // To store the username

    public void initialize() {
        quizService = new QuizService();

        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        difficultyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDifficultyLevel().getDisplayName()));
        timeLimitColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTimeLimitMinutes()).asObject());
        questionsCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuestionCount()).asObject());

        loadQuizzes();

        quizzesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            startQuizButton.setDisable(newSelection == null);
        });
        
        quizzesTableView.setPlaceholder(new Label("No quizzes available or failed to load."));
    }

    // Method to be called by DashboardController to pass the username
    public void initializeData(String username) {
        this.currentUsername = username;
        // You could, for example, re-filter quizzes here if they were user-specific
        // Or simply store the username for when a quiz is started.
        System.out.println("QuizSelectionController initialized with username: " + this.currentUsername);
    }

    private void loadQuizzes() {
        List<Quiz> quizzesFromDb = quizService.getAllQuizzes();
        if (quizzesFromDb == null || quizzesFromDb.isEmpty()) {
            statusLabel.setText("No quizzes found in the database.");
            quizzesTableView.setPlaceholder(new Label("No quizzes available.")); // More specific placeholder
            quizDisplayList.clear();
            return;
        }

        List<QuizDisplayItem> displayItems = quizzesFromDb.stream().map(quiz -> {
            Quiz detailedQuiz = quizService.getQuizWithDetails(quiz.getId());
            int questionCount = (detailedQuiz != null && detailedQuiz.getQuestions() != null) ? detailedQuiz.getQuestions().size() : 0;
            return new QuizDisplayItem(quiz.getId(), quiz.getTitle(), quiz.getDifficultyLevel(), quiz.getTimeLimitMinutes(), questionCount);
        }).collect(Collectors.toList());
        
        quizDisplayList.setAll(displayItems);
        quizzesTableView.setItems(quizDisplayList);
        if (displayItems.isEmpty()){
            quizzesTableView.setPlaceholder(new Label("No quizzes loaded. Check sample data or DB connection."));
        }
    }

    @FXML
    protected void handleStartSelectedQuizAction(ActionEvent event) {
        QuizDisplayItem selectedQuizItem = quizzesTableView.getSelectionModel().getSelectedItem();
        if (selectedQuizItem != null) {
            statusLabel.setText(""); 
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz_interface.fxml"));
                Parent quizInterfaceRoot = loader.load();

                QuizInterfaceController quizInterfaceController = loader.getController();
                
                // Ensure username is available. If not, it's a flow error.
                if (this.currentUsername == null || this.currentUsername.isEmpty()) {
                    System.err.println("Critical Error: Username not available in QuizSelectionController for starting quiz.");
                    statusLabel.setText("Error: User session error. Cannot start quiz.");
                    return;
                }
                
                quizInterfaceController.initializeQuiz(selectedQuizItem.getId(), this.currentUsername);

                // Get the primary stage to change its scene entirely
                Stage primaryStage = (Stage) quizSelectionRoot.getScene().getWindow(); 

                primaryStage.setScene(new Scene(quizInterfaceRoot));
                primaryStage.setTitle("Quiz: " + selectedQuizItem.getTitle() + " - " + this.currentUsername);
                primaryStage.setMinWidth(1000); // Ensure interface is visible
                primaryStage.setMinHeight(700);
                primaryStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Error loading quiz interface: " + e.getMessage());
            } catch (NullPointerException e) {
                e.printStackTrace();
                statusLabel.setText("Failed to load quiz. Check FXML path or controller setup: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Please select a quiz to start.");
        }
    }

    // Inner class for displaying in TableView, including question count
    public static class QuizDisplayItem {
        private final int id;
        private final String title;
        private final DifficultyLevel difficultyLevel;
        private final int timeLimitMinutes;
        private final int questionCount;

        public QuizDisplayItem(int id, String title, DifficultyLevel difficultyLevel, int timeLimitMinutes, int questionCount) {
            this.id = id;
            this.title = title;
            this.difficultyLevel = difficultyLevel;
            this.timeLimitMinutes = timeLimitMinutes;
            this.questionCount = questionCount;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
        public int getTimeLimitMinutes() { return timeLimitMinutes; }
        public int getQuestionCount() { return questionCount; }
    }
} 