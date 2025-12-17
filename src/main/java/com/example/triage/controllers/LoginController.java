package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import com.example.triage.database.DBConnection;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import java.util.prefs.Preferences;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private HBox errorContainer;

    @FXML
    private CheckBox rememberMeCheckBox;

    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    // Store logged-in username to pass to dashboard
    private String loggedInUsername = "";

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Clear previous errors
        hideError();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Authenticate
        if (authenticateUser(username, password)) {
            System.out.println("Login successful for user: " + username);
            loggedInUsername = username; // Store username
            loadDashboard();
        } else {
            showError("Invalid username or password. Please try again.");

            // Shake animation for wrong credentials
            shakeFields();
        }
    }

    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Save username if Remember Me is checked
                if (rememberMeCheckBox.isSelected()) {
                    prefs.put("savedUsername", username);
                    prefs.putBoolean("rememberMe", true);
                } else {
                    prefs.remove("savedUsername");
                    prefs.putBoolean("rememberMe", false);
                }
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database connection error. Please contact IT support.");
            return false;
        }
    }

    private void loadDashboard() {
        FadeTransition fade = new FadeTransition(Duration.millis(300), usernameField.getScene().getRoot());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        fade.setOnFinished(event -> {
            showDashboard();
        });

        fade.play();
    }

    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/triage/views/dashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load(), 900, 600);

            // ✅ PASS USERNAME TO DASHBOARD CONTROLLER
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(loggedInUsername);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Dashboard - LifeLine Triage System");
            stage.setScene(dashboardScene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dashboardScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard. Please restart the application.");
        }
    }

    // ========================================
    // ERROR HANDLING METHODS
    // ========================================

    private void showError(String message) {
        if (errorContainer != null) {
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
            errorLabel.setText(message);

            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), errorContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            // Fallback if errorContainer is not defined
            errorLabel.setText(message);
        }
    }

    private void hideError() {
        if (errorContainer != null) {
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
        }
        errorLabel.setText("");
    }

    // ========================================
    // UI FEEDBACK ANIMATIONS
    // ========================================

    private void shakeFields() {
        // Shake animation for visual feedback on wrong credentials
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), usernameField.getParent());
        shake.setFromX(0);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setByX(10);
        shake.play();
    }

    // ========================================
    // REMEMBER ME FUNCTIONALITY
    // ========================================

    private void loadSavedCredentials() {
        String savedUsername = prefs.get("savedUsername", null);
        boolean rememberMe = prefs.getBoolean("rememberMe", false);

        if (rememberMe && savedUsername != null) {
            usernameField.setText(savedUsername);
            rememberMeCheckBox.setSelected(true);
            passwordField.requestFocus(); // Focus on password field
        }
    }

    // ========================================
    // INITIALIZATION
    // ========================================

    @FXML
    public void initialize() {
        // Allow Enter key to submit from password field
        if (passwordField != null) {
            passwordField.setOnAction(event -> onLoginButtonClick());
        }

        // Hide error container initially
        hideError();

        // Load saved username if "Remember Me" was checked
        loadSavedCredentials();
    }
}