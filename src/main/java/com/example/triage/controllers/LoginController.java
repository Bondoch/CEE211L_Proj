package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import com.example.triage.database.DBConnection;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        errorLabel.setText(""); // Clear older error messages

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        if (authenticateUser(username, password)) {
            System.out.println("Login successful!");

            loadDashboard();
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            return rs.next(); // True = login successful

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/triage/views/dashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load(), 900, 600);

            // Get current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Dashboard");
            stage.setScene(dashboardScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
