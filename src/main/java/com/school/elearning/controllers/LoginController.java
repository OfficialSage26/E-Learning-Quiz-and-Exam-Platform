package com.school.elearning.controllers;

import java.io.IOException;
import java.net.URL;

import com.school.elearning.services.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label messageLabel;

    private AuthService authService; // Will be initialized

    public void initialize() {
        authService = new AuthService(); // Initialize AuthService
        messageLabel.setText(""); // Clear any default text
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean isAuthenticated = authService.login(username, password);

        if (isAuthenticated) {
            // Navigate to Dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent dashboardRoot = loader.load();

                // Get DashboardController and pass username
                DashboardController dashboardController = loader.getController();
                dashboardController.initializeData(username);

                Scene dashboardScene = new Scene(dashboardRoot);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(dashboardScene);
                stage.setTitle("Student Dashboard - " + username);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Error loading dashboard.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            passwordField.clear(); // Clear password field on failed login
        }
    }

    @FXML
    protected void handleRegisterButtonAction(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/registration.fxml");
            if (fxmlUrl == null) {
                messageLabel.setText("Error: Cannot find registration.fxml");
                System.err.println("Cannot find FXML file. Make sure /fxml/registration.fxml is in your resources folder.");
                return;
            }
            Parent registrationRoot = FXMLLoader.load(fxmlUrl);
            Scene registrationScene = new Scene(registrationRoot);

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(registrationScene);
            stage.setTitle("Create Student Account");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading registration screen.");
        }
    }
} 