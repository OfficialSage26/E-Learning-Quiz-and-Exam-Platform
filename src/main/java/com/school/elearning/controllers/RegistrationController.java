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

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Label messageLabel;

    private AuthService authService; // To be initialized

    public void initialize() {
        authService = new AuthService(); // Initialize AuthService
        messageLabel.setText("");
    }

    @FXML
    protected void handleRegisterButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("All fields are required.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Basic password policy (e.g., minimum length)
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = authService.register(username, password);
        if (success) {
            messageLabel.setText("Registration successful! You can now log in.");
            messageLabel.setStyle("-fx-text-fill: green;");
            // Optionally, disable fields or navigate back to login after a delay
            // For now, clear fields
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            // AuthService prints specific error to console, show a generic one on UI or a specific one if detectable
            messageLabel.setText("Registration failed. Username might be taken.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void handleBackToLoginButtonAction(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
            if (fxmlUrl == null) {
                messageLabel.setText("Error: Cannot find login.fxml");
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
            messageLabel.setText("Error loading login screen.");
        }
    }
} 