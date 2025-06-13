package com.school.elearning;

import java.io.IOException;
import java.net.URL;

import com.school.elearning.database.DatabaseManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database first
        DatabaseManager.initializeDatabase();

        try {
            // Load the FXML file for the login screen
            // We'll create login.fxml in the resources/fxml directory later
            URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file. Make sure /fxml/login.fxml is in your resources folder.");
                // Fallback to a simple scene if FXML is not found for now
                primaryStage.setScene(new Scene(new javafx.scene.layout.StackPane(new javafx.scene.control.Label("Error: login.fxml not found")), 600, 400));
            } else {
                Parent root = FXMLLoader.load(fxmlUrl);
                Scene scene = new Scene(root, 800, 600); // Initial size, can be adjusted

                // Apply a stylesheet later if needed
                // scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                primaryStage.setTitle("E-Learning Quiz Platform");
                primaryStage.setScene(scene);
                primaryStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception, maybe show an error dialog
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 