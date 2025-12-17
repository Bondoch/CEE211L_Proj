package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.prefs.Preferences;
import com.example.triage.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SettingsController {

    // System Configuration
    @FXML private TextField systemNameField;
    @FXML private TextField facilityNameField;
    @FXML private ComboBox<String> timeZoneCombo;
    @FXML private ComboBox<String> languageCombo;

    // Database Configuration
    @FXML private TextField dbHostField;
    @FXML private TextField dbNameField;
    @FXML private CheckBox autoBackupCheck;
    @FXML private ComboBox<String> backupFrequencyCombo;
    @FXML private Label connectionStatusLabel;

    // Notification Settings
    @FXML private CheckBox criticalAlertsCheck;
    @FXML private CheckBox roomAvailabilityCheck;
    @FXML private CheckBox dischargeRemindersCheck;
    @FXML private CheckBox systemMaintenanceCheck;

    // Display Preferences
    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> fontSizeCombo;

    // Security Settings
    @FXML private ComboBox<String> sessionTimeoutCombo;
    @FXML private ComboBox<String> passwordLengthCombo;
    @FXML private CheckBox twoFactorCheck;

    // Capacity Warning Settings
    @FXML private Slider warningThresholdSlider;
    @FXML private Slider criticalThresholdSlider;
    @FXML private Label warningThresholdLabel;
    @FXML private Label criticalThresholdLabel;
    @FXML private CheckBox enableCapacityWarningsCheck;
    @FXML private CheckBox soundAlertCheck;

    // Preferences storage
    private Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);

    // Current logged-in user
    private String currentUsername = "admin"; // Default fallback

    // ✅ Method to receive username from DashboardController
    public void setCurrentUser(String username) {
        this.currentUsername = username;
        System.out.println("Settings: Current user set to " + username);
    }

    @FXML
    public void initialize() {
        // Populate combo boxes
        populateComboBoxes();

        // Load saved settings
        loadSettings();

        // Setup listeners
        setupListeners();
    }

    private void populateComboBoxes() {
        // Time Zones
        timeZoneCombo.getItems().addAll(
                "UTC+08:00 (Manila/Philippine Time)",
                "UTC+00:00 (GMT/UTC)",
                "UTC-05:00 (Eastern Time)",
                "UTC-08:00 (Pacific Time)",
                "UTC+01:00 (Central European Time)",
                "UTC+09:00 (Japan Standard Time)"
        );
        timeZoneCombo.setValue("UTC+08:00 (Manila/Philippine Time)");

        // Languages
        languageCombo.getItems().addAll(
                "English (US)",
                "English (UK)",
                "Filipino",
                "Spanish",
                "Mandarin Chinese"
        );
        languageCombo.setValue("English (US)");

        // Backup Frequency
        backupFrequencyCombo.getItems().addAll(
                "Every 6 hours",
                "Daily",
                "Every 3 days",
                "Weekly",
                "Monthly"
        );
        backupFrequencyCombo.setValue("Daily");

        // Themes
        themeCombo.getItems().addAll(
                "Light Mode",
                "Dark Mode",
                "High Contrast",
                "Blue Theme (Current)"
        );
        themeCombo.setValue("Blue Theme (Current)");

        // Font Sizes
        fontSizeCombo.getItems().addAll(
                "Small (12px)",
                "Medium (14px)",
                "Large (16px)",
                "Extra Large (18px)"
        );
        fontSizeCombo.setValue("Medium (14px)");

        // Session Timeout
        sessionTimeoutCombo.getItems().addAll(
                "15 minutes",
                "30 minutes",
                "1 hour",
                "2 hours",
                "4 hours",
                "Never"
        );
        sessionTimeoutCombo.setValue("30 minutes");

        // Password Length
        passwordLengthCombo.getItems().addAll(
                "6 characters",
                "8 characters",
                "10 characters",
                "12 characters"
        );
        passwordLengthCombo.setValue("8 characters");
    }

    private void setupListeners() {
        // Enable/disable backup frequency based on auto backup checkbox
        autoBackupCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            backupFrequencyCombo.setDisable(!newVal);
        });

        // Real-time validation for database host
        dbHostField.textProperty().addListener((obs, oldVal, newVal) -> {
            connectionStatusLabel.setText("Not tested");
            connectionStatusLabel.setTextFill(Color.web("#7f858c"));
        });

        dbNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            connectionStatusLabel.setText("Not tested");
            connectionStatusLabel.setTextFill(Color.web("#7f858c"));
        });

        // Capacity warning sliders
        if (warningThresholdSlider != null && warningThresholdLabel != null) {
            warningThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int value = newVal.intValue();
                warningThresholdLabel.setText(value + "%");

                // Ensure critical is always higher than warning
                if (criticalThresholdSlider.getValue() <= value) {
                    criticalThresholdSlider.setValue(value + 5);
                }
            });
        }

        if (criticalThresholdSlider != null && criticalThresholdLabel != null) {
            criticalThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int value = newVal.intValue();
                criticalThresholdLabel.setText(value + "%");

                // Ensure critical is always higher than warning
                if (value <= warningThresholdSlider.getValue()) {
                    criticalThresholdSlider.setValue(warningThresholdSlider.getValue() + 5);
                }
            });
        }
    }

    private void loadSettings() {
        // Load from preferences
        systemNameField.setText(prefs.get("systemName", "LifeLine Triage System"));
        facilityNameField.setText(prefs.get("facilityName", "Central Medical Center"));

        String savedTimeZone = prefs.get("timeZone", "UTC+08:00 (Manila/Philippine Time)");
        if (timeZoneCombo.getItems().contains(savedTimeZone)) {
            timeZoneCombo.setValue(savedTimeZone);
        }

        String savedLanguage = prefs.get("language", "English (US)");
        if (languageCombo.getItems().contains(savedLanguage)) {
            languageCombo.setValue(savedLanguage);
        }

        // Database settings
        dbHostField.setText(prefs.get("dbHost", "localhost:3306"));
        dbNameField.setText(prefs.get("dbName", "triage_db"));
        autoBackupCheck.setSelected(prefs.getBoolean("autoBackup", true));

        String savedBackupFreq = prefs.get("backupFrequency", "Daily");
        if (backupFrequencyCombo.getItems().contains(savedBackupFreq)) {
            backupFrequencyCombo.setValue(savedBackupFreq);
        }

        // Notifications
        criticalAlertsCheck.setSelected(prefs.getBoolean("criticalAlerts", true));
        roomAvailabilityCheck.setSelected(prefs.getBoolean("roomAvailability", true));
        dischargeRemindersCheck.setSelected(prefs.getBoolean("dischargeReminders", false));
        systemMaintenanceCheck.setSelected(prefs.getBoolean("systemMaintenance", true));

        // Display
        String savedTheme = prefs.get("theme", "Blue Theme (Current)");
        if (themeCombo.getItems().contains(savedTheme)) {
            themeCombo.setValue(savedTheme);
        }

        String savedFontSize = prefs.get("fontSize", "Medium (14px)");
        if (fontSizeCombo.getItems().contains(savedFontSize)) {
            fontSizeCombo.setValue(savedFontSize);
        }

        // Security
        String savedTimeout = prefs.get("sessionTimeout", "30 minutes");
        if (sessionTimeoutCombo.getItems().contains(savedTimeout)) {
            sessionTimeoutCombo.setValue(savedTimeout);
        }

        String savedPasswordLength = prefs.get("passwordLength", "8 characters");
        if (passwordLengthCombo.getItems().contains(savedPasswordLength)) {
            passwordLengthCombo.setValue(savedPasswordLength);
        }

        twoFactorCheck.setSelected(prefs.getBoolean("twoFactor", false));

        // Capacity Warning Settings
        if (warningThresholdSlider != null) {
            double warningValue = prefs.getDouble("warningThreshold", 80.0);
            warningThresholdSlider.setValue(warningValue);
            warningThresholdLabel.setText((int)warningValue + "%");
        }

        if (criticalThresholdSlider != null) {
            double criticalValue = prefs.getDouble("criticalThreshold", 95.0);
            criticalThresholdSlider.setValue(criticalValue);
            criticalThresholdLabel.setText((int)criticalValue + "%");
        }

        if (enableCapacityWarningsCheck != null) {
            enableCapacityWarningsCheck.setSelected(prefs.getBoolean("enableCapacityWarnings", true));
        }

        if (soundAlertCheck != null) {
            soundAlertCheck.setSelected(prefs.getBoolean("soundAlert", false));
        }
    }

    @FXML
    private void handleSaveChanges() {
        // Save all settings to preferences
        prefs.put("systemName", systemNameField.getText());
        prefs.put("facilityName", facilityNameField.getText());
        prefs.put("timeZone", timeZoneCombo.getValue());
        prefs.put("language", languageCombo.getValue());

        // Database
        prefs.put("dbHost", dbHostField.getText());
        prefs.put("dbName", dbNameField.getText());
        prefs.putBoolean("autoBackup", autoBackupCheck.isSelected());
        prefs.put("backupFrequency", backupFrequencyCombo.getValue());

        // Notifications
        prefs.putBoolean("criticalAlerts", criticalAlertsCheck.isSelected());
        prefs.putBoolean("roomAvailability", roomAvailabilityCheck.isSelected());
        prefs.putBoolean("dischargeReminders", dischargeRemindersCheck.isSelected());
        prefs.putBoolean("systemMaintenance", systemMaintenanceCheck.isSelected());

        // Display
        prefs.put("theme", themeCombo.getValue());
        prefs.put("fontSize", fontSizeCombo.getValue());

        // Security
        prefs.put("sessionTimeout", sessionTimeoutCombo.getValue());
        prefs.put("passwordLength", passwordLengthCombo.getValue());
        prefs.putBoolean("twoFactor", twoFactorCheck.isSelected());

        // Capacity Warning Settings
        if (warningThresholdSlider != null) {
            prefs.putDouble("warningThreshold", warningThresholdSlider.getValue());
        }
        if (criticalThresholdSlider != null) {
            prefs.putDouble("criticalThreshold", criticalThresholdSlider.getValue());
        }
        if (enableCapacityWarningsCheck != null) {
            prefs.putBoolean("enableCapacityWarnings", enableCapacityWarningsCheck.isSelected());
        }
        if (soundAlertCheck != null) {
            prefs.putBoolean("soundAlert", soundAlertCheck.isSelected());
        }

        // Show success message
        showSuccessAlert("Settings saved successfully!");
    }

    @FXML
    private void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Admin Password");
        dialog.setHeaderText("Enter your current password and new password");

        // Set the button types
        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Create the password fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        currentPassword.setStyle("-fx-font-size: 13px; -fx-pref-width: 250px;");

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        newPassword.setStyle("-fx-font-size: 13px; -fx-pref-width: 250px;");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");
        confirmPassword.setStyle("-fx-font-size: 13px; -fx-pref-width: 250px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(250);

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);
        grid.add(errorLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the current password field by default
        javafx.application.Platform.runLater(() -> currentPassword.requestFocus());

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == changeButtonType) {
                String current = currentPassword.getText();
                String newPass = newPassword.getText();
                String confirm = confirmPassword.getText();

                // Clear previous error
                errorLabel.setText("");

                // Validation
                if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                    errorLabel.setText("All fields are required!");
                    // Re-show dialog
                    handleChangePassword();
                    return;
                }

                if (!newPass.equals(confirm)) {
                    errorLabel.setText("New passwords do not match!");
                    handleChangePassword();
                    return;
                }

                // Check minimum length
                String passwordLengthStr = passwordLengthCombo.getValue();
                if (passwordLengthStr != null) {
                    int minLength = Integer.parseInt(passwordLengthStr.split(" ")[0]);
                    if (newPass.length() < minLength) {
                        showErrorAlert("Password must be at least " + minLength + " characters!");
                        handleChangePassword();
                        return;
                    }
                }

                // Verify current password and update in database
                if (verifyAndUpdatePassword(current, newPass)) {
                    showSuccessAlert("Password changed successfully!\nPlease remember your new password.");
                } else {
                    showErrorAlert("Failed to change password.\nPlease check your current password and try again.");
                    handleChangePassword();
                }
            }
        });
    }

    /**
     * Verifies the current password and updates to new password in database
     * @param currentPassword Current password entered by user
     * @param newPassword New password to set
     * @return true if successful, false otherwise
     */
    private boolean verifyAndUpdatePassword(String currentPassword, String newPassword) {
        // SQL to verify current password
        String verifySql = "SELECT id, password FROM users WHERE username = ? AND role = 'admin'";
        // SQL to update password
        String updateSql = "UPDATE users SET password = ? WHERE username = ? AND role = 'admin'";

        try (Connection conn = DBConnection.getConnection()) {

            // Step 1: Verify current password
            try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                verifyStmt.setString(1, currentUsername);
                ResultSet rs = verifyStmt.executeQuery();

                if (rs.next()) {
                    String dbPassword = rs.getString("password");

                    // TODO: Use password hashing (BCrypt, PBKDF2, etc.) for security
                    // For now, comparing plain text (NOT SECURE!)
                    if (!dbPassword.equals(currentPassword)) {
                        System.err.println("Current password incorrect");
                        return false;
                    }
                } else {
                    System.err.println("Admin user not found");
                    return false;
                }
            }

            // Step 2: Update to new password
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                // TODO: Hash the new password before storing
                // String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                // updateStmt.setString(1, hashedPassword);

                updateStmt.setString(1, newPassword); // Plain text (NOT SECURE!)
                updateStmt.setString(2, currentUsername);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Password updated successfully for user: " + currentUsername);
                    return true;
                } else {
                    System.err.println("No rows updated");
                    return false;
                }
            }

        } catch (Exception e) {
            System.err.println("Database error while changing password:");
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleTestConnection() {
        String host = dbHostField.getText();
        String dbName = dbNameField.getText();

        if (host.isEmpty() || dbName.isEmpty()) {
            connectionStatusLabel.setText("❌ Missing host or database name");
            connectionStatusLabel.setTextFill(Color.web("#ff6b6b"));
            return;
        }

        // Test actual database connection
        try {
            Connection conn = DBConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                connectionStatusLabel.setText("✅ Connection successful");
                connectionStatusLabel.setTextFill(Color.web("#4caf50"));

                // Show database info
                String dbVersion = conn.getMetaData().getDatabaseProductName() + " "
                        + conn.getMetaData().getDatabaseProductVersion();
                System.out.println("Connected to: " + dbVersion);

            } else {
                connectionStatusLabel.setText("❌ Connection failed");
                connectionStatusLabel.setTextFill(Color.web("#ff6b6b"));
            }

            // Fade effect
            FadeTransition fade = new FadeTransition(Duration.millis(200), connectionStatusLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

        } catch (Exception e) {
            connectionStatusLabel.setText("❌ Connection failed: " + e.getMessage());
            connectionStatusLabel.setTextFill(Color.web("#ff6b6b"));
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetToDefaults() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset to Defaults");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will reset all settings to their default values. This action cannot be undone.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Clear all preferences
            try {
                prefs.clear();

                // Reload defaults
                loadSettings();

                showSuccessAlert("Settings reset to defaults!");

            } catch (Exception e) {
                showErrorAlert("Failed to reset settings: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearAllData() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Clear All Data");
        confirm.setHeaderText("⚠️ DANGER: This will delete ALL data!");
        confirm.setContentText("This will permanently delete all patient records, facilities, and system data. This action CANNOT be undone!\n\nAre you absolutely sure?");

        ButtonType yesButton = new ButtonType("Yes, Delete Everything", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        if (confirm.showAndWait().get() == yesButton) {
            // Double confirmation
            Alert doubleConfirm = new Alert(Alert.AlertType.WARNING);
            doubleConfirm.setTitle("Final Confirmation");
            doubleConfirm.setHeaderText("Last chance to cancel!");
            doubleConfirm.setContentText("Type 'DELETE' to confirm:");

            TextField confirmField = new TextField();
            confirmField.setPromptText("Type DELETE here");
            doubleConfirm.getDialogPane().setContent(confirmField);

            if (doubleConfirm.showAndWait().get() == ButtonType.OK) {
                if ("DELETE".equals(confirmField.getText())) {
                    // TODO: Implement actual data deletion
                    showSuccessAlert("All data has been cleared.");
                } else {
                    showErrorAlert("Incorrect confirmation text. Data was NOT deleted.");
                }
            }
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}