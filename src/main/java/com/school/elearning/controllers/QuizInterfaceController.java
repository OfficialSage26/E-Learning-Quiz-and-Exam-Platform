package com.school.elearning.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school.elearning.models.AnswerOption;
import com.school.elearning.models.Question;
import com.school.elearning.models.QuestionType;
import com.school.elearning.models.Quiz;
import com.school.elearning.models.User;
import com.school.elearning.services.QuizAttemptService;
import com.school.elearning.services.QuizService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

public class QuizInterfaceController {

    @FXML private BorderPane quizInterfaceRoot;
    @FXML private Label quizTitleLabel;
    @FXML private Label questionProgressLabel;
    @FXML private Label timerLabel;
    @FXML private Label quizStatusLabel;
    @FXML private TextFlow questionTextFlow;
    @FXML private VBox answerOptionsVBox;
    @FXML private VBox questionAreaVBox; // Make sure this fx:id exists in FXML
    @FXML private FlowPane questionNavFlowPane;
    @FXML private Label markedForReviewCountLabel;
    @FXML private Button prevQuestionButton;
    @FXML private Button nextQuestionButton;
    @FXML private Button markForReviewButton;
    @FXML private Button reviewAllButton;
    @FXML private Button submitQuizButton;

    private QuizService quizService;
    private QuizAttemptService quizAttemptService;
    private User currentUserObject;
    private Quiz currentQuiz;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private String currentUsername;

    private Timeline quizTimeline;
    private int timeRemainingSeconds;

    // To store student's answers temporarily: Map<QuestionID, SelectedAnswerOptionID or TextAnswer>
    private Map<Integer, Object> studentAnswers = new HashMap<>();
    // To store marked for review status: Map<QuestionID, Boolean>
    private Map<Integer, Boolean> markedForReview = new HashMap<>();

    private double finalScore; // To store the calculated score
    private int finalTimeTakenSeconds; // To store the calculated time taken
    private Scene currentScene; // To store the current scene for returning from review

    public void initialize() {
        quizService = new QuizService();
        quizAttemptService = new QuizAttemptService();
        // Ensure buttons that depend on quiz load are initially disabled or managed
        prevQuestionButton.setDisable(true);
        nextQuestionButton.setDisable(true);
        markForReviewButton.setDisable(true);
        submitQuizButton.setDisable(true);
        reviewAllButton.setDisable(true);
    }

    public void initializeQuiz(int quizId, String username) {
        this.currentUsername = username;
        this.currentUserObject = quizAttemptService.getUserByUsername(username);
        if (this.currentUserObject == null) {
            quizStatusLabel.setText("Error: Could not verify user. Please restart.");
            quizStatusLabel.setTextFill(Color.RED);
            setInteractionDisabled(true);
            return;
        }

        this.currentQuiz = quizService.getQuizWithDetails(quizId);

        if (currentQuiz == null) {
            quizStatusLabel.setText("Error: Could not load quiz. Please go back and try again.");
            quizStatusLabel.setTextFill(Color.RED);
            // Disable all controls if quiz fails to load
            return;
        }

        this.questions = currentQuiz.getQuestions();
        if (this.questions == null || this.questions.isEmpty()) {
            quizStatusLabel.setText("Error: This quiz has no questions.");
            quizStatusLabel.setTextFill(Color.RED);
            return;
        }

        // Randomize questions
        Collections.shuffle(this.questions);
        
        // For multiple choice questions, also randomize answer options
        for (Question q : this.questions) {
            if (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE || q.getQuestionType() == QuestionType.TRUE_FALSE) {
                Collections.shuffle(q.getAnswerOptions());
            }
        }

        quizTitleLabel.setText(currentQuiz.getTitle());
        timeRemainingSeconds = currentQuiz.getTimeLimitMinutes() * 60;
        
        // Initialize answer and review maps
        questions.forEach(q -> {
            studentAnswers.put(q.getId(), null); // No answer initially
            markedForReview.put(q.getId(), false); // Not marked initially
        });

        setupQuestionNavigation();
        displayQuestion(currentQuestionIndex);
        startTimer();
        updateButtonStates();
        markForReviewButton.setDisable(false);
        submitQuizButton.setDisable(false);
        reviewAllButton.setDisable(false);

        if (quizInterfaceRoot != null && quizInterfaceRoot.getScene() != null) {
            this.currentScene = quizInterfaceRoot.getScene();
        }
    }

    private void startTimer() {
        updateTimerLabel(); // Initial display
        quizTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemainingSeconds--;
            updateTimerLabel();
            if (timeRemainingSeconds <= 0) {
                quizTimeline.stop();
                quizStatusLabel.setText("Time's up! Auto-submitting quiz...");
                quizStatusLabel.setTextFill(Color.ORANGERED);
                // Disable further interaction
                setInteractionDisabled(true);
                // Auto-submit logic
                handleSubmitQuizAction(new ActionEvent(submitQuizButton, submitQuizButton)); // Simulate event for auto-submit
            } else if (timeRemainingSeconds == 600) { // 10 minutes
                quizStatusLabel.setText("Warning: 10 minutes remaining!");
                quizStatusLabel.setTextFill(Color.ORANGE);
            } else if (timeRemainingSeconds == 300) { // 5 minutes
                quizStatusLabel.setText("Warning: 5 minutes remaining!");
                 quizStatusLabel.setTextFill(Color.ORANGE);
            } else if (timeRemainingSeconds == 60) { // 1 minute
                quizStatusLabel.setText("Warning: 1 minute remaining! Submit soon.");
                 quizStatusLabel.setTextFill(Color.ORANGERED);
            } else if (timeRemainingSeconds < 600 && timeRemainingSeconds % 120 == 0){ // Clear warning after some time if not the critical ones
                 if(!quizStatusLabel.getText().contains("minute remaining")) quizStatusLabel.setText("");
            }
        }));
        quizTimeline.setCycleCount(Timeline.INDEFINITE);
        quizTimeline.play();
    }
    
    private void setInteractionDisabled(boolean disabled) {
        if (questionAreaVBox != null) questionAreaVBox.setDisable(disabled);
        if (prevQuestionButton != null) prevQuestionButton.setDisable(disabled);
        if (nextQuestionButton != null) nextQuestionButton.setDisable(disabled);
        if (markForReviewButton != null) markForReviewButton.setDisable(disabled);
        if (reviewAllButton != null) reviewAllButton.setDisable(disabled);
        if (questionNavFlowPane != null) questionNavFlowPane.setDisable(disabled);
        // Submit button should still be clickable for manual submission unless time is up
        if (submitQuizButton != null && disabled && timeRemainingSeconds <=0) submitQuizButton.setDisable(true); 
    }

    private void updateTimerLabel() {
        int minutes = timeRemainingSeconds / 60;
        int seconds = timeRemainingSeconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        currentQuestionIndex = index;
        Question q = questions.get(index);

        questionTextFlow.getChildren().clear();
        questionTextFlow.getChildren().add(new Text(q.getQuestionText()));
        questionProgressLabel.setText(String.format("Q: %d/%d", index + 1, questions.size()));

        answerOptionsVBox.getChildren().clear();
        ToggleGroup answerGroup = new ToggleGroup();

        switch (q.getQuestionType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                for (AnswerOption option : q.getAnswerOptions()) {
                    RadioButton rb = new RadioButton(option.getOptionText());
                    rb.setUserData(option.getId()); // Store option ID
                    rb.setToggleGroup(answerGroup);
                    // Restore previous selection if any
                    Object prevAnswer = studentAnswers.get(q.getId());
                    if (prevAnswer != null && prevAnswer.equals(option.getId())) {
                        rb.setSelected(true);
                    }
                    answerOptionsVBox.getChildren().add(rb);
                }
                break;
            case SHORT_ANSWER:
                TextField shortAnswerField = new TextField();
                shortAnswerField.setPromptText("Type your answer here");
                Object prevShortAnswer = studentAnswers.get(q.getId());
                if (prevShortAnswer instanceof String) {
                    shortAnswerField.setText((String) prevShortAnswer);
                }
                answerOptionsVBox.getChildren().add(shortAnswerField);
                break;
        }

        // Listener to save answer when an option changes
        answerGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                studentAnswers.put(q.getId(), newToggle.getUserData());
            } else {
                studentAnswers.put(q.getId(), null); // Cleared selection
            }
            updateQuestionNavButtonStatus(q.getId(), newToggle != null);
        });
        
        // For short answer, listener for text change
        if(q.getQuestionType() == QuestionType.SHORT_ANSWER && !answerOptionsVBox.getChildren().isEmpty()){
            TextField tf = (TextField) answerOptionsVBox.getChildren().get(0);
            tf.textProperty().addListener((obs, oldText, newText) -> {
                studentAnswers.put(q.getId(), newText.trim());
                 updateQuestionNavButtonStatus(q.getId(), !newText.trim().isEmpty());
            });
        }

        updateButtonStates();
        updateMarkForReviewButton();
        highlightCurrentQuestionNav();
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty()) return;
        Question q = questions.get(currentQuestionIndex);
        
        switch (q.getQuestionType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                ToggleGroup group = (ToggleGroup) ((RadioButton) answerOptionsVBox.getChildren().get(0)).getToggleGroup();
                Toggle selectedToggle = group.getSelectedToggle();
                if (selectedToggle != null) {
                    studentAnswers.put(q.getId(), selectedToggle.getUserData());
                } else {
                    studentAnswers.put(q.getId(), null); // No answer selected
                }
                break;
            case SHORT_ANSWER:
                if (!answerOptionsVBox.getChildren().isEmpty() && answerOptionsVBox.getChildren().get(0) instanceof TextField) {
                    TextField tf = (TextField) answerOptionsVBox.getChildren().get(0);
                    studentAnswers.put(q.getId(), tf.getText().trim());
                }
                break;
        }
         updateQuestionNavButtonStatus(q.getId(), studentAnswers.get(q.getId()) != null && 
            (!(studentAnswers.get(q.getId()) instanceof String) || !((String)studentAnswers.get(q.getId())).isEmpty()));
    }


    private void updateButtonStates() {
        prevQuestionButton.setDisable(currentQuestionIndex == 0);
        nextQuestionButton.setDisable(currentQuestionIndex == questions.size() - 1);
    }
    
    private void updateMarkForReviewButton() {
        if (questions.isEmpty()) return;
        Question currentQ = questions.get(currentQuestionIndex);
        boolean isMarked = markedForReview.getOrDefault(currentQ.getId(), false);
        markForReviewButton.setText(isMarked ? "Unmark Review" : "Mark for Review");
        markForReviewButton.setStyle(isMarked ? "-fx-background-color: #FF8C00; -fx-text-fill: white;" : "-fx-background-color: #FFA000; -fx-text-fill: white;");
    }

    private void setupQuestionNavigation() {
        questionNavFlowPane.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            Button qNavButton = new Button(String.valueOf(i + 1));
            qNavButton.setPrefSize(35, 35);
            qNavButton.getStyleClass().add("question-nav-button"); // For CSS styling
            final int questionIdx = i;
            qNavButton.setOnAction(e -> {
                saveCurrentAnswer(); 
                displayQuestion(questionIdx);
            });
            updateQuestionNavButtonStyle(qNavButton, q.getId());
            questionNavFlowPane.getChildren().add(qNavButton);
        }
         updateMarkedForReviewCount();
    }
    
    private void updateQuestionNavButtonStyle(Button btn, int questionId) {
        boolean isAnswered = studentAnswers.get(questionId) != null && 
                             (!(studentAnswers.get(questionId) instanceof String) || 
                              !((String)studentAnswers.get(questionId)).isEmpty());
        boolean isMarked = markedForReview.getOrDefault(questionId, false);

        String style = "-fx-font-weight: bold; -fx-background-radius: 3;";
        if (isMarked) {
            style += "-fx-background-color: #FFA000;"; // Orange for marked
        } else if (isAnswered) {
            style += "-fx-background-color: #4CAF50;"; // Green for answered
        } else {
            style += "-fx-background-color: #E0E0E0;"; // Grey for unanswered
        }
        btn.setStyle(style);
    }
    
    private void updateQuestionNavButtonStatus(int questionId, boolean answered) {
        // Find the button corresponding to questionId (or its index)
        int qIndex = -1;
        for(int i=0; i < questions.size(); i++){
            if(questions.get(i).getId() == questionId){
                qIndex = i;
                break;
            }
        }
        if(qIndex != -1 && qIndex < questionNavFlowPane.getChildren().size()){
            Node node = questionNavFlowPane.getChildren().get(qIndex);
            if(node instanceof Button){
                updateQuestionNavButtonStyle((Button)node, questionId);
            }
        }
    }

    private void highlightCurrentQuestionNav() {
        for (Node node : questionNavFlowPane.getChildren()) {
            Button btn = (Button) node;
            if (Integer.parseInt(btn.getText()) - 1 == currentQuestionIndex) {
                // You might add a border or a different style to highlight the current question nav button
                String currentStyle = btn.getStyle();
                if(!currentStyle.contains("-fx-border-color")){
                     btn.setStyle(currentStyle + " -fx-border-color: #0D47A1; -fx-border-width: 2px;");
                }
            } else {
                 btn.setStyle(btn.getStyle().replace(" -fx-border-color: #0D47A1; -fx-border-width: 2px;", ""));
            }
        }
    }
    
    private void updateMarkedForReviewCount(){
        long count = markedForReview.values().stream().filter(Boolean::booleanValue).count();
        markedForReviewCountLabel.setText("Marked: " + count);
    }

    @FXML protected void handleNextQuestionAction(ActionEvent event) {
        saveCurrentAnswer();
        if (currentQuestionIndex < questions.size() - 1) {
            displayQuestion(currentQuestionIndex + 1);
        } else { // On the last question, next could mean review or just stay
            quizStatusLabel.setText("You are on the last question.");
        }
    }

    @FXML protected void handlePreviousQuestionAction(ActionEvent event) {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            displayQuestion(currentQuestionIndex - 1);
        }
    }

    @FXML protected void handleMarkForReviewAction(ActionEvent event) {
        if (questions.isEmpty()) return;
        Question currentQ = questions.get(currentQuestionIndex);
        boolean currentMarkedStatus = markedForReview.getOrDefault(currentQ.getId(), false);
        markedForReview.put(currentQ.getId(), !currentMarkedStatus);
        updateMarkForReviewButton();
        updateQuestionNavButtonStatus(currentQ.getId(), studentAnswers.get(currentQ.getId()) != null);
        updateMarkedForReviewCount();
        // Highlight might also need to be updated if style changes
        highlightCurrentQuestionNav(); 
    }

    @FXML
    public void handleSubmitQuizAction(ActionEvent event) {
        boolean isAutoSubmit = (event != null && event.getSource() == submitQuizButton && timeRemainingSeconds <=0 );
        Node eventSourceNode = null; // To store the source of the event for getting the stage
        if (event != null) {
            eventSourceNode = (Node) event.getSource();
        }

        if (!isAutoSubmit && timeRemainingSeconds > 0) { 
            if (quizTimeline != null) quizTimeline.stop(); 
            saveCurrentAnswer(); 

            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Submit Quiz");
            confirmationDialog.setHeaderText("Are you sure you want to submit your answers?");
            long answeredCount = studentAnswers.values().stream().filter(ans -> ans != null && (!(ans instanceof String) || !((String)ans).isEmpty())).count();
            confirmationDialog.setContentText("You have answered " + answeredCount + " out of " + questions.size() + " questions.\nUnanswered questions will be marked as incorrect.");

            final Node finalEventSourceNodeForConfirmation = eventSourceNode; // Must be final or effectively final for lambda
            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Proceed to submit
            } else {
                if (quizTimeline != null && timeRemainingSeconds > 0) quizTimeline.play();
                return; 
            }
        } 
        
        if(timeRemainingSeconds <=0 && !isAutoSubmit) { // If timer up, but not auto-submit path (e.g. manual click after timer up but before auto-submit triggered)
             saveCurrentAnswer();
        }
        // If auto-submit, saveCurrentAnswer is called if timeRemainingSeconds <=0 already. If manual submit confirmed, it's also called.

        if (quizTimeline != null) quizTimeline.stop();

        quizStatusLabel.setText("Submitting quiz and calculating results...");
        quizStatusLabel.setTextFill(Color.BLUE);
        setInteractionDisabled(true);
        if(submitQuizButton != null) submitQuizButton.setDisable(true);

        final Node finalEventSourceNode = eventSourceNode; // Use the captured event source

        Platform.runLater(() -> {
            calculateAndSaveResults();
            System.out.println("Quiz submitted by " + currentUserObject.getUsername() + ". Results calculated.");
            // Pass the node that was the source of the original submit event
            // If auto-submit, eventSourceNode might be null if event was null. 
            // In that case, fall back to quizInterfaceRoot if possible, or handle error.
            Node nodeForStage = finalEventSourceNode != null ? finalEventSourceNode : quizInterfaceRoot;
            if (isAutoSubmit && finalEventSourceNode == null) {
                 // For auto-submit, event is simulated and source is submitQuizButton. So this path might not be common unless event is made null.
                 // If auto-submit event was new ActionEvent(submitQuizButton, submitQuizButton)
                 // then finalEventSourceNode should be submitQuizButton itself.
                 // If truly null, need a robust fallback. quizInterfaceRoot might still be an issue if quiz scene not active.
                 // The best for auto-submit is to use the quizInterfaceRoot as it's the Quiz UI itself.
                 nodeForStage = quizInterfaceRoot;
            }
            loadResultsScreen(nodeForStage);
        });
    }
    
    @FXML protected void handleReviewAllAction(ActionEvent event) {
        if (currentQuiz == null || questions.isEmpty()) {
            quizStatusLabel.setText("Cannot review: Quiz not loaded or no questions.");
            return;
        }
        saveCurrentAnswer(); // Save any pending answer
        if (quizTimeline != null) {
            quizTimeline.pause(); // Pause timer during review
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review_answers.fxml"));
            Parent reviewRoot = loader.load();
            ReviewAnswersController reviewController = loader.getController();
            
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            if (this.currentScene == null) this.currentScene = ((Node)event.getSource()).getScene(); // Fallback if not set in init

            reviewController.initializeData(currentQuiz, questions, studentAnswers, this, stage, this.currentScene, currentQuestionIndex);

            Scene reviewScene = new Scene(reviewRoot);
            stage.setScene(reviewScene);
            stage.setTitle("Review Answers - " + currentQuiz.getTitle());

        } catch (IOException e) {
            e.printStackTrace();
            quizStatusLabel.setText("Error loading review screen: " + e.getMessage());
        }
    }

    private void calculateAndSaveResults() {
        if (questions == null || currentUserObject == null) {
             System.err.println("Cannot calculate/save results: questions or user object is null");
            return;
        }
        double score = 0;
        int correctAnswers = 0;
        for(Question q : questions){
            Object studentAns = studentAnswers.get(q.getId());
            boolean answerIsCorrect = false; // for student_answers table
            if(studentAns != null){
                List<AnswerOption> options = q.getAnswerOptions() != null ? q.getAnswerOptions() : new ArrayList<>();
                if(q.getQuestionType() == QuestionType.MULTIPLE_CHOICE || q.getQuestionType() == QuestionType.TRUE_FALSE){
                    Integer selectedOptionId = (Integer) studentAns;
                    Optional<AnswerOption> correctOption = options.stream().filter(AnswerOption::isCorrect).findFirst();
                    if(correctOption.isPresent() && correctOption.get().getId() == selectedOptionId){
                        correctAnswers++;
                        answerIsCorrect = true;
                    }
                } else if (q.getQuestionType() == QuestionType.SHORT_ANSWER){
                    String textAnswer = ((String) studentAns).trim();
                    Optional<AnswerOption> correctOption = options.stream()
                                                            .filter(opt -> opt.isCorrect() && opt.getOptionText() != null)
                                                            .findFirst(); 
                    if(correctOption.isPresent() && correctOption.get().getOptionText().equalsIgnoreCase(textAnswer)){
                        correctAnswers++;
                        answerIsCorrect = true;
                    }
                }
            }
        }
        if(!questions.isEmpty()){
            score = ((double) correctAnswers / questions.size()) * 100;
        }
        this.finalScore = score; // Store score

        System.out.println(String.format("Quiz for %s (ID: %d) finished. Score: %.2f%% (%d/%d)", 
            currentUserObject.getUsername(), currentUserObject.getId(), score, correctAnswers, questions.size()));
        
        int totalTimeForQuizSeconds = currentQuiz.getTimeLimitMinutes() * 60;
        int timeTakenSeconds = totalTimeForQuizSeconds - timeRemainingSeconds;
        if (timeTakenSeconds < 0) timeTakenSeconds = totalTimeForQuizSeconds; // If timer ran out, timeRemainingSeconds might be negative or 0
        if (timeRemainingSeconds <= 0) timeTakenSeconds = totalTimeForQuizSeconds; // Ensure full time if timer expired
        this.finalTimeTakenSeconds = timeTakenSeconds; // Store time taken

        // Save QuizAttempt and StudentAnswer records to DB using QuizAttemptService.
        int attemptId = quizAttemptService.saveQuizAttempt(currentUserObject, currentQuiz, studentAnswers, questions, score, timeTakenSeconds);

        if (attemptId > 0) {
            System.out.println("Quiz attempt ID: " + attemptId + " saved successfully.");
        } else {
            System.err.println("Failed to save quiz attempt for user: " + currentUsername);
            // Optionally show an error to the user on the UI
            quizStatusLabel.setText("Error: Could not save your quiz attempt. Please contact support.");
            quizStatusLabel.setTextFill(Color.RED);
        }
    }
    
    private void loadResultsScreen(Node sourceNodeForStage) { // Accept a Node to get the Stage from
        try {
            if (quizTimeline != null) {
                quizTimeline.stop();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz_results.fxml"));
            Parent resultsRoot = loader.load();

            QuizResultsController resultsController = loader.getController();
            resultsController.initializeData(currentQuiz, studentAnswers, questions, currentUserObject.getUsername(), finalScore, finalTimeTakenSeconds);

            Stage stage = null;
            if (sourceNodeForStage != null && sourceNodeForStage.getScene() != null) {
                 stage = (Stage) sourceNodeForStage.getScene().getWindow();
            }
           
            if (stage == null) {
                // Fallback or error if stage is still null (e.g. sourceNodeForStage detached or not on a window)
                // This might happen if auto-submit uses quizInterfaceRoot and that scene is no longer active
                // A very robust way is to store the main stage reference early on.
                // For now, let's try to ensure sourceNodeForStage is always valid or handle this specific case.
                System.err.println("CRITICAL ERROR: Stage could not be determined for loading results screen. SourceNode: " + sourceNodeForStage);
                if (quizInterfaceRoot != null && quizInterfaceRoot.getScene() != null && quizInterfaceRoot.getScene().getWindow() != null) {
                    System.out.println("Attempting fallback to quizInterfaceRoot for stage.");
                    stage = (Stage) quizInterfaceRoot.getScene().getWindow(); 
                    // This fallback is only safe if quizInterfaceRoot is guaranteed to be on an active scene, which is NOT the case if submitted from review.
                    // The logic for nodeForStage in handleSubmitQuizAction needs to ensure it's a node *on the current, active scene*.
                    // If autosubmit uses submitQuizButton as source, that should be fine.
                } 
                if (stage == null) { // If fallback also failed
                     quizStatusLabel.setText("Error: Could not display results screen. Stage unresolved.");
                     quizStatusLabel.setTextFill(Color.RED);
                     return;
                }
            }

            Scene newScene = new Scene(resultsRoot);
            stage.setScene(newScene);
            stage.setTitle("Quiz Results - " + currentQuiz.getTitle());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            quizStatusLabel.setText("Error loading results screen: " + e.getMessage());
            quizStatusLabel.setTextFill(Color.RED);
        }  catch (NullPointerException e) {
             e.printStackTrace();
             quizStatusLabel.setText("Failed to load results screen. FXML missing or controller issue: " + e.getMessage());
             quizStatusLabel.setTextFill(Color.RED);
        }
    }

    public String getCurrentUsername() {
        return this.currentUsername;
    }

    public void resumeQuizFromReviewAndGoToQuestion(int questionIndex) {
        if (quizTimeline != null && timeRemainingSeconds > 0) {
            quizTimeline.play(); // Resume timer
        }
        if (questionIndex >= 0 && questionIndex < questions.size()) {
            displayQuestion(questionIndex);
        } else {
            displayQuestion(currentQuestionIndex); // fallback to current or last known valid
        }
        // The scene itself will be set by ReviewAnswersController using the stored previousScene
    }
} 