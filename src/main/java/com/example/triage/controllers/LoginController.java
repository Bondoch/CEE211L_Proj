package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import com.example.triage.database.DBConnection;
import com.example.triage.services.SessionManager;
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
import javafx.util.Duration;
import java.util.prefs.Preferences;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private HBox errorContainer;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private javafx.scene.layout.Pane adminSetupBackdrop;
    @FXML private javafx.scene.layout.VBox adminSetupCard;
    @FXML private TextField adminUsernameField;
    @FXML private PasswordField adminPasswordField;
    @FXML private PasswordField adminConfirmField;
    @FXML private Label adminSetupErrorLabel;

    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim().toLowerCase();
        String password = passwordField.getText().trim();

        hideError();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (authenticateUser(username, password)) {
            System.out.println("Login successful for user: " + username);
            loadDashboard();
        } else {
            showError("Invalid username or password. Please try again.");
            shakeFields();
        }
    }

    @FXML
    private void onCreateAccountClick() {
        showAdminSetupOverlay();
    }

    private void showAdminSetupOverlay() {
        adminSetupBackdrop.setVisible(true);
        adminSetupBackdrop.setManaged(true);

        adminSetupCard.setVisible(true);
        adminSetupCard.setManaged(true);

        clearAdminSetupForm();
    }

    @FXML
    private void hideAdminSetup() {
        adminSetupBackdrop.setVisible(false);
        adminSetupBackdrop.setManaged(false);

        adminSetupCard.setVisible(false);
        adminSetupCard.setManaged(false);

        clearAdminSetupForm();
    }


    private void clearAdminSetupForm() {
        if (adminUsernameField != null) adminUsernameField.clear();
        if (adminPasswordField != null) adminPasswordField.clear();
        if (adminConfirmField != null) adminConfirmField.clear();

        if (adminSetupErrorLabel != null) {
            adminSetupErrorLabel.setText("");
            adminSetupErrorLabel.setVisible(false);
            adminSetupErrorLabel.setManaged(false);
        }
    }




    private boolean authenticateUser(String username, String password) {

        String sql = """
        SELECT u.id AS user_id,
               u.staff_id,
               u.role
        FROM users u
        WHERE u.username = ? AND u.password = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId  = rs.getInt("user_id");
                int staffId = rs.getInt("staff_id");
                String role = rs.getString("role").toUpperCase();

                SessionManager.getInstance()
                        .startSession(userId, staffId, username, role);

                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database connection error.");
            return false;
        }
    }

    private void loadDashboard() {
        FadeTransition fade = new FadeTransition(Duration.millis(300), usernameField.getScene().getRoot());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        fade.setOnFinished(event -> showDashboard());
        fade.play();
    }

    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/triage/views/dashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load(), 900, 600);

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

    private void showError(String message) {
        if (errorContainer != null) {
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
            errorLabel.setText(message);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), errorContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
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

    private void shakeFields() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), usernameField.getParent());
        shake.setFromX(0);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setByX(10);
        shake.play();
    }

    private void loadSavedCredentials() {
        String savedUsername = prefs.get("savedUsername", null);
        boolean rememberMe = prefs.getBoolean("rememberMe", false);

        if (rememberMe && savedUsername != null) {
            usernameField.setText(savedUsername);
            rememberMeCheckBox.setSelected(true);
            passwordField.requestFocus();
        }
    }

    @FXML
    public void initialize() {
        if (passwordField != null) {
            passwordField.setOnAction(event -> onLoginButtonClick());
        }
        hideError();
        loadSavedCredentials();
    }

    @FXML
    private void handleCreateAdminOverlay() {

        String username = adminUsernameField.getText().trim().toLowerCase();
        String password = adminPasswordField.getText();
        String confirm  = adminConfirmField.getText();

        adminSetupErrorLabel.setVisible(false);
        adminSetupErrorLabel.setManaged(false);
        adminSetupErrorLabel.setText("");

        // Validation
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showAdminError("All fields are required.");
            return;
        }

        if (!password.equals(confirm)) {
            showAdminError("Passwords do not match.");
            return;
        }

        // Admin already exists
        if (adminExists()) {
            showAdminError("Administrator setup has already been completed.");
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'admin')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // (hashing later if required)
            stmt.executeUpdate();

            hideAdminSetup();
            showError("Admin account created. You may now log in.");

        } catch (Exception e) {
            e.printStackTrace();
            showAdminError("Failed to create admin account.");
        }
    }


    private boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'admin'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true; // fail-safe: block creation
    }


    private void showAdminError(String message) {
        adminSetupErrorLabel.setText(message);
        adminSetupErrorLabel.setVisible(true);
        adminSetupErrorLabel.setManaged(true);
    }


}