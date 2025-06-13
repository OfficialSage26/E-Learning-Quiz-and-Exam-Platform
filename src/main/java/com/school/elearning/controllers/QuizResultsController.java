package com.school.elearning.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school.elearning.MainApp;
import com.school.elearning.models.AnswerOption;
import com.school.elearning.models.Question;
import com.school.elearning.models.QuestionType;
import com.school.elearning.models.Quiz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class QuizResultsController {

    @FXML private Label quizTitleLabel;
    @FXML private Label overallScoreLabel;
    @FXML private Label questionsStatusLabel;
    @FXML private Label timeTakenLabel;
    @FXML private ScrollPane resultsScrollPane;
    @FXML private VBox questionResultsVBox;
    @FXML private Button backToDashboardButton;

    private String currentUsername;
    private Quiz currentQuiz;

    public void initialize() {
        // Initialization if needed
    }

    // Updated signature
    public void initializeData(Quiz quiz, Map<Integer, Object> studentAnswers, List<Question> questions, String username, double score, int timeTakenSeconds) {
        this.currentUsername = username;
        this.currentQuiz = quiz;

        quizTitleLabel.setText("Quiz Results: " + quiz.getTitle());
        overallScoreLabel.setText(String.format("Overall Score: %.2f%%", score));
        if (score < 50) {
            overallScoreLabel.setTextFill(Color.RED);
        } else if (score < 75) {
            overallScoreLabel.setTextFill(Color.ORANGE);
        } else {
            overallScoreLabel.setTextFill(Color.GREEN);
        }

        int minutes = timeTakenSeconds / 60;
        int seconds = timeTakenSeconds % 60;
        timeTakenLabel.setText(String.format("Time Taken: %02d:%02d", minutes, seconds));

        long answeredCount = studentAnswers.values().stream().filter(ans -> ans != null && (!(ans instanceof String) || !((String)ans).isEmpty())).count();
        long correctCount = 0; // Will be calculated below

        questionResultsVBox.getChildren().clear();
        int questionNumber = 1;

        for (Question q : questions) {
            VBox questionBox = new VBox(5);
            questionBox.setStyle("-fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 0 0 1 0;"); // Bottom border

            Label qTextLabel = new Label("Q" + questionNumber + ": " + q.getQuestionText());
            qTextLabel.setWrapText(true);
            qTextLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Object studentRawAnswer = studentAnswers.get(q.getId());
            String studentAnswerDisplay = "Not Answered";
            boolean wasCorrect = false;

            Label studentAnsLabel = new Label();
            Label correctAnswerLabel = new Label();
            correctAnswerLabel.setWrapText(true);
            studentAnsLabel.setWrapText(true);

            if (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE || q.getQuestionType() == QuestionType.TRUE_FALSE) {
                Optional<AnswerOption> correctAnswerOpt = q.getAnswerOptions().stream().filter(AnswerOption::isCorrect).findFirst();
                String correctAnsText = correctAnswerOpt.map(AnswerOption::getOptionText).orElse("N/A");
                correctAnswerLabel.setText("Correct Answer: " + correctAnsText);

                if (studentRawAnswer instanceof Integer) {
                    Integer selectedOptionId = (Integer) studentRawAnswer;
                    Optional<AnswerOption> selectedOptionOpt = q.getAnswerOptions().stream().filter(opt -> opt.getId() == selectedOptionId).findFirst();
                    studentAnswerDisplay = selectedOptionOpt.map(AnswerOption::getOptionText).orElse("Invalid Option Selected");
                    if (correctAnswerOpt.isPresent() && selectedOptionId.equals(correctAnswerOpt.get().getId())) {
                        wasCorrect = true;
                    }
                }
            } else if (q.getQuestionType() == QuestionType.SHORT_ANSWER) {
                Optional<AnswerOption> correctAnswerOpt = q.getAnswerOptions().stream().filter(AnswerOption::isCorrect).findFirst();
                String correctAnsText = correctAnswerOpt.map(AnswerOption::getOptionText).orElse("N/A (No correct answer defined for short answer)");
                correctAnswerLabel.setText("Correct Answer: " + correctAnsText);

                if (studentRawAnswer instanceof String && !((String) studentRawAnswer).trim().isEmpty()) {
                    studentAnswerDisplay = (String) studentRawAnswer;
                    if (correctAnswerOpt.isPresent() && studentAnswerDisplay.trim().equalsIgnoreCase(correctAnswerOpt.get().getOptionText())) {
                        wasCorrect = true;
                    }
                }
            }
            
            if(wasCorrect) correctCount++;

            studentAnsLabel.setText("Your Answer: " + studentAnswerDisplay);

            Label resultLabel = new Label(wasCorrect ? "Correct" : "Incorrect");
            resultLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
            resultLabel.setTextFill(wasCorrect ? Color.GREEN : Color.RED);
            
            questionBox.getChildren().addAll(qTextLabel, studentAnsLabel, correctAnswerLabel, resultLabel);
            questionResultsVBox.getChildren().add(questionBox);
            questionNumber++;
        }
        questionsStatusLabel.setText(String.format("Answered: %d/%d | Correct: %d/%d", answeredCount, questions.size(), correctCount, questions.size()));
    }

    @FXML
    protected void handleBackToDashboardAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.initializeData(this.currentUsername); // Pass username back

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(dashboardRoot);
            // String css = MainApp.class.getResource("/css/style.css").toExternalForm();
            // scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.setTitle("Student Dashboard - " + this.currentUsername);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error, e.g., show an alert
            if(quizTitleLabel != null) { // Check if label is available before using
                quizTitleLabel.setText("Error loading dashboard: " + e.getMessage());
            }
        }  catch (NullPointerException e) {
             e.printStackTrace();
             if(quizTitleLabel != null) {
                quizTitleLabel.setText("Failed to load dashboard. FXML missing or controller issue: " + e.getMessage());
            }
        }
    }
} 