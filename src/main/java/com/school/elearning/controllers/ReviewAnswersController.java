package com.school.elearning.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school.elearning.models.AnswerOption;
import com.school.elearning.models.Question;
import com.school.elearning.models.QuestionType;
import com.school.elearning.models.Quiz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ReviewAnswersController {

    @FXML private Label quizTitleLabel;
    @FXML private ScrollPane reviewScrollPane;
    @FXML private VBox questionReviewVBox;
    @FXML private Button backToQuizButton;
    @FXML private Button submitQuizFromReviewButton;

    private Quiz quiz;
    private List<Question> questions;
    private Map<Integer, Object> studentAnswers;
    private QuizInterfaceController quizInterfaceController;
    private Stage primaryStage;
    private Scene previousScene;
    private int lastQuestionIndexBeforeReview;

    public void initializeData(Quiz quiz, List<Question> questions, Map<Integer, Object> studentAnswers,
                               QuizInterfaceController quizInterfaceController, Stage stage, Scene previousScene, int currentQuestionIndex) {
        this.quiz = quiz;
        this.questions = questions;
        this.studentAnswers = studentAnswers;
        this.quizInterfaceController = quizInterfaceController;
        this.primaryStage = stage;
        this.previousScene = previousScene;
        this.lastQuestionIndexBeforeReview = currentQuestionIndex;

        quizTitleLabel.setText("Review Answers: " + quiz.getTitle());
        populateReview();
    }

    private void populateReview() {
        questionReviewVBox.getChildren().clear();
        int questionNumber = 1;
        for (Question q : questions) {
            VBox questionBox = new VBox(5);
            questionBox.setStyle("-fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 0 0 1 0;");

            Label qTextLabel = new Label("Q" + questionNumber + ": " + q.getQuestionText());
            qTextLabel.setWrapText(true);
            qTextLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Object studentRawAnswer = studentAnswers.get(q.getId());
            String studentAnswerDisplay = "Not Answered";

            if (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE || q.getQuestionType() == QuestionType.TRUE_FALSE) {
                if (studentRawAnswer instanceof Integer) {
                    Integer selectedOptionId = (Integer) studentRawAnswer;
                    Optional<AnswerOption> selectedOptionOpt = q.getAnswerOptions().stream()
                            .filter(opt -> opt.getId() == selectedOptionId)
                            .findFirst();
                    studentAnswerDisplay = selectedOptionOpt.map(AnswerOption::getOptionText).orElse("Invalid Option");
                }
            } else if (q.getQuestionType() == QuestionType.SHORT_ANSWER) {
                if (studentRawAnswer instanceof String && !((String) studentRawAnswer).trim().isEmpty()) {
                    studentAnswerDisplay = (String) studentRawAnswer;
                }
            }

            Label studentAnsTextLabel = new Label("Your Answer: " + studentAnswerDisplay);
            studentAnsTextLabel.setWrapText(true);
            
            Button jumpToQuestionButton = new Button("Edit Answer for Q" + questionNumber);
            int qIdx = questionNumber -1;
            jumpToQuestionButton.setOnAction(e -> {
                quizInterfaceController.resumeQuizFromReviewAndGoToQuestion(qIdx);
                primaryStage.setScene(previousScene);
            });

            questionBox.getChildren().addAll(qTextLabel, studentAnsTextLabel, jumpToQuestionButton);
            questionReviewVBox.getChildren().add(questionBox);
            questionNumber++;
        }
    }

    @FXML
    void handleBackToQuizAction(ActionEvent event) {
        quizInterfaceController.resumeQuizFromReviewAndGoToQuestion(this.lastQuestionIndexBeforeReview); // Resume at the question index where review was initiated
        primaryStage.setScene(previousScene);
        primaryStage.setTitle("Quiz: " + quiz.getTitle() + " - " + quizInterfaceController.getCurrentUsername());

    }

    @FXML
    void handleSubmitQuizFromReviewAction(ActionEvent event) {
        // Reuse the submit logic from QuizInterfaceController
        // The QuizInterfaceController's handleSubmitQuizAction already handles confirmation dialogs etc.
        quizInterfaceController.handleSubmitQuizAction(event); 
        // The QuizInterfaceController will then navigate to the results screen, so no need to change scene here.
    }
} 