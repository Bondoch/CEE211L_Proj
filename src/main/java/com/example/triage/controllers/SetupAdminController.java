package com.example.triage.controllers;

import com.example.triage.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SetupAdminController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleCreateAdmin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        errorLabel.setText("");

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'admin')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            // Redirect to log in screen
            Stage stage = (Stage) errorLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/triage/app/views/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to create admin account.");
        }
    }
}
