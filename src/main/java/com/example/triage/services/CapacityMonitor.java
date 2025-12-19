package com.example.triage.services;

import com.example.triage.database.DBConnection;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class CapacityMonitor {

    private static CapacityMonitor instance;
    private Preferences prefs = Preferences.userNodeForPackage(CapacityMonitor.class);

    private Timer monitorTimer;
    private boolean isMonitoring = false;

    private Stage primaryStage;
    private VBox alertContainer; // For dashboard alerts

    private boolean warningAlertShown = false;
    private boolean criticalAlertShown = false;

    private CapacityMonitor() {}

    public static CapacityMonitor getInstance() {
        if (instance == null) {
            instance = new CapacityMonitor();
        }
        return instance;
    }

    // ================= START/STOP MONITORING =================

    public void startMonitoring() {
        if (isMonitoring) return;

        boolean enabled = prefs.getBoolean("enableCapacityWarnings", true);
        if (!enabled) return;

        isMonitoring = true;
        monitorTimer = new Timer(true);

        // Check every 30 seconds
        monitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkCapacity();
            }
        }, 0, 30000); // 30 seconds

        System.out.println("✅ Capacity monitoring started");
    }

    public void stopMonitoring() {
        if (monitorTimer != null) {
            monitorTimer.cancel();
            monitorTimer = null;
        }
        isMonitoring = false;
        System.out.println("⏹️ Capacity monitoring stopped");
    }

    // ================= CAPACITY CHECK =================

    private void checkCapacity() {
        try {
            double currentCapacity = getCurrentCapacity();
            double warningThreshold = prefs.getDouble("warningThreshold", 80.0);
            double criticalThreshold = prefs.getDouble("criticalThreshold", 95.0);

            System.out.println("📊 Current Capacity: " + String.format("%.1f%%", currentCapacity));

            if (currentCapacity >= criticalThreshold) {
                handleCriticalCapacity(currentCapacity);
            } else if (currentCapacity >= warningThreshold) {
                handleWarningCapacity(currentCapacity);
            } else {
                // Reset alerts when capacity drops
                resetAlerts();
            }

            // Update dashboard UI
            updateDashboardAlert(currentCapacity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= GET CAPACITY FROM DATABASE =================

    public double getCurrentCapacity() {
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied
            FROM units
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total");
                int occupied = rs.getInt("occupied");

                if (total == 0) return 0.0;

                return (occupied * 100.0) / total;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // ================= ALERT HANDLERS =================

    private void handleWarningCapacity(double capacity) {
        if (warningAlertShown) return; // Don't spam alerts

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Capacity Warning");
            alert.setHeaderText("Facility Capacity High");
            alert.setContentText(
                    String.format("Current capacity: %.1f%%\n\n" +
                            "Please prepare for potential overflow.", capacity)
            );

            alert.show();
            warningAlertShown = true;
            criticalAlertShown = false;
        });
    }

    private void handleCriticalCapacity(double capacity) {
        if (criticalAlertShown) return;

        boolean soundEnabled = prefs.getBoolean("soundAlert", false);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("🚨 CRITICAL CAPACITY");
            alert.setHeaderText("FACILITY AT CRITICAL CAPACITY");
            alert.setContentText(
                    String.format("Current capacity: %.1f%%\n\n" +
                            "IMMEDIATE ACTION REQUIRED!\n" +
                            "Consider patient transfers or discharge.", capacity)
            );

            // Play sound if enabled
            if (soundEnabled) {
                playAlertSound();
            }

            alert.show();
            criticalAlertShown = true;
        });
    }

    private void resetAlerts() {
        warningAlertShown = false;
        criticalAlertShown = false;
    }

    // ================= SOUND ALERT =================

    private void playAlertSound() {
        // Simple beep (cross-platform)
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    // ================= DASHBOARD INTEGRATION =================

    public void setAlertContainer(VBox container) {
        this.alertContainer = container;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void updateDashboardAlert(double capacity) {
        if (alertContainer == null) return;

        double warningThreshold = prefs.getDouble("warningThreshold", 80.0);
        double criticalThreshold = prefs.getDouble("criticalThreshold", 95.0);

        Platform.runLater(() -> {
            alertContainer.getChildren().clear();

            if (capacity >= criticalThreshold) {
                alertContainer.getChildren().add(
                        createAlertBox("CRITICAL", capacity, "#ffebee", "#c62828")
                );
            } else if (capacity >= warningThreshold) {
                alertContainer.getChildren().add(
                        createAlertBox("WARNING", capacity, "#fff3e0", "#e65100")
                );
            }
        });
    }

    private VBox createAlertBox(String level, double capacity, String bgColor, String textColor) {
        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: " + textColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;"
        );

        Label title = new Label(level + " CAPACITY");
        title.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + textColor + ";"
        );

        Label detail = new Label(String.format("%.1f%% occupied", capacity));
        detail.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: " + textColor + ";"
        );

        box.getChildren().addAll(title, detail);
        return box;
    }

    // ================= PUBLIC GETTERS =================

    public String getCapacityStatus() {
        double capacity = getCurrentCapacity();
        double warning = prefs.getDouble("warningThreshold", 80.0);
        double critical = prefs.getDouble("criticalThreshold", 95.0);

        if (capacity >= critical) return "CRITICAL";
        if (capacity >= warning) return "WARNING";
        return "NORMAL";
    }

    public String getCapacityPercentage() {
        return String.format("%.1f%%", getCurrentCapacity());
    }
}