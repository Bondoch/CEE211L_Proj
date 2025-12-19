package com.example.triage.controllers;

import com.example.triage.database.DBConnection;
import com.example.triage.services.SessionManager;
import com.example.triage.services.CapacityMonitor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.geometry.Insets;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardHomeController {

    @FXML private VBox capacityAlertBox;
    @FXML private Label welcomeLabel;

    // Admin Panel Labels
    @FXML private Label adminStaffCountLabel;
    @FXML private Label adminPatientsCountLabel;
    @FXML private Label adminCriticalCountLabel;
    @FXML private Label adminBedsCountLabel;

    // User Panel Labels
    @FXML private Label userPatientsCountLabel;
    @FXML private Label userCriticalCountLabel;
    @FXML private Label userBedsCountLabel;

    @FXML private VBox adminPanel;
    @FXML private VBox userPanel;

    // ✨ Dynamic Containers
    @FXML private VBox facilityCapacityContainer;
    @FXML private VBox recentActivityContainer;
    @FXML private VBox userRecentActivityContainer;
    @FXML private FlowPane criticalPatientsContainer;
    @FXML private Label criticalCountBadge;
    @FXML private VBox criticalPatientsSection;

    private String userRole;

    @FXML
    public void initialize() {
        loadUserRole();
        displayRolePanel();
        loadDashboardData();

        // ✅ START CAPACITY MONITORING
        CapacityMonitor monitor = CapacityMonitor.getInstance();
        if (capacityAlertBox != null) {
            monitor.setAlertContainer(capacityAlertBox);
        }
        monitor.startMonitoring();

        // ✨ Load new dashboard features
        if ("admin".equals(userRole)) {
            loadFacilityCapacity();
            loadRecentActivity();
            loadCriticalPatients();
        } else {
            loadRecentActivity(); // User panel activity
        }
    }

    private void loadUserRole() {
        userRole = SessionManager.getInstance().getRole();
        if (userRole == null) userRole = "user";
    }

    private void displayRolePanel() {
        if ("admin".equals(userRole)) {
            adminPanel.setVisible(true);
            adminPanel.setManaged(true);
            userPanel.setVisible(false);
            userPanel.setManaged(false);
        } else {
            userPanel.setVisible(true);
            userPanel.setManaged(true);
            adminPanel.setVisible(false);
            adminPanel.setManaged(false);
        }
    }

    private void loadDashboardData() {
        welcomeLabel.setText("Welcome, " + (userRole.equals("admin") ? "Administrator" : "User"));

        if ("admin".equals(userRole)) {
            loadStaffCount();
            loadPatientsCount(adminPatientsCountLabel);
            loadCriticalCount(adminCriticalCountLabel);
            loadBedsCount(adminBedsCountLabel);
        } else {
            loadPatientsCount(userPatientsCountLabel);
            loadCriticalCount(userCriticalCountLabel);
            loadBedsCount(userBedsCountLabel);
        }
    }

    private void loadStaffCount() {
        String sql = "SELECT COUNT(*) FROM staff";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                adminStaffCountLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) {
            adminStaffCountLabel.setText("0");
            e.printStackTrace();
        }
    }

    private void loadPatientsCount(Label label) {
        String sql = "SELECT COUNT(*) FROM patients WHERE status = 'admitted'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                label.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) {
            label.setText("0");
            e.printStackTrace();
        }
    }

    private void loadCriticalCount(Label label) {
        String sql = "SELECT COUNT(*) FROM patients WHERE severity = 'critical' AND status = 'admitted'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                label.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) {
            label.setText("0");
            e.printStackTrace();
        }
    }

    private void loadBedsCount(Label label) {
        String sql = "SELECT COUNT(*) FROM units WHERE status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                label.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) {
            label.setText("0");
            e.printStackTrace();
        }
    }

    // ================= ✨ NEW FEATURES =================

    private void loadFacilityCapacity() {
        if (facilityCapacityContainer == null) return;

        facilityCapacityContainer.getChildren().clear();

        String sql = """
            SELECT 
                f.name,
                COUNT(u.id) as total,
                SUM(CASE WHEN u.status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied
            FROM facilities f
            LEFT JOIN floors fl ON f.id = fl.facility_id
            LEFT JOIN units u ON fl.id = u.floor_id
            GROUP BY f.id, f.name
            ORDER BY f.name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String facilityName = rs.getString("name");
                int total = rs.getInt("total");
                int occupied = rs.getInt("occupied");

                double percentage = total > 0 ? (occupied * 100.0 / total) : 0.0;

                facilityCapacityContainer.getChildren().add(
                        createCapacityBar(facilityName, occupied, total, percentage)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCapacityBar(String name, int occupied, int total, double percentage) {
        VBox container = new VBox(6);

        // Header
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #034c81;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label countLabel = new Label(occupied + " / " + total);
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f858c;");

        header.getChildren().addAll(nameLabel, spacer, countLabel);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.setPrefHeight(8);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        String barColor = percentage >= 90 ? "#e74c3c" :
                percentage >= 75 ? "#ff9800" : "#5ba2f4";

        progressBar.setStyle(
                "-fx-accent: " + barColor + ";" +
                        "-fx-background-radius: 4;" +
                        "-fx-control-inner-background: #f0f0f0;"
        );

        container.getChildren().addAll(header, progressBar);
        return container;
    }

    private void loadRecentActivity() {
        VBox targetContainer = "admin".equals(userRole) ?
                recentActivityContainer : userRecentActivityContainer;

        if (targetContainer == null) return;

        targetContainer.getChildren().clear();

        String sql = """
            SELECT 
                p.patient_code,
                p.full_name,
                p.admission_date,
                p.severity,
                'admitted' as activity_type
            FROM patients p
            WHERE p.status = 'admitted'
            ORDER BY p.admission_date DESC
            LIMIT 8
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String code = rs.getString("patient_code");
                String name = rs.getString("full_name");
                Timestamp timestamp = rs.getTimestamp("admission_date");
                String severity = rs.getString("severity");

                targetContainer.getChildren().add(
                        createActivityItem(code, name, timestamp, severity)
                );
            }

            if (targetContainer.getChildren().isEmpty()) {
                Label empty = new Label("No recent activity");
                empty.setStyle("-fx-text-fill: #7f858c; -fx-font-size: 12px;");
                targetContainer.getChildren().add(empty);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createActivityItem(String code, String name, Timestamp timestamp, String severity) {
        HBox item = new HBox(12);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));
        item.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        // Icon
        String iconColor = getSeverityColor(severity);
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
                "-fx-background-color: " + iconColor + "20;" +
                        "-fx-background-radius: 6;" +
                        "-fx-min-width: 35;" +
                        "-fx-min-height: 35;" +
                        "-fx-max-width: 35;" +
                        "-fx-max-height: 35;"
        );
        FontIcon icon = new FontIcon("fas-user-injured");
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.web(iconColor));
        iconBox.getChildren().add(icon);

        // Info
        VBox info = new VBox(4);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #034c81;");

        Label codeLabel = new Label(code + " • " + capitalize(severity));
        codeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f858c;");

        info.getChildren().addAll(nameLabel, codeLabel);

        // Time
        Label timeLabel = new Label(formatTimestamp(timestamp));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9bbaa;");

        item.getChildren().addAll(iconBox, info, timeLabel);
        return item;
    }

    private void loadCriticalPatients() {
        if (criticalPatientsContainer == null || criticalCountBadge == null) return;

        criticalPatientsContainer.getChildren().clear();

        String sql = """
            SELECT 
                p.patient_code,
                p.full_name,
                p.age,
                p.severity,
                u.label as unit_label
            FROM patients p
            LEFT JOIN units u ON p.unit_id = u.id
            WHERE p.severity = 'critical' AND p.status = 'admitted'
            ORDER BY p.admission_date DESC
            LIMIT 6
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                count++;
                String code = rs.getString("patient_code");
                String name = rs.getString("full_name");
                int age = rs.getInt("age");
                String unit = rs.getString("unit_label");

                criticalPatientsContainer.getChildren().add(
                        createCriticalPatientCard(code, name, age, unit)
                );
            }

            criticalCountBadge.setText(String.valueOf(count));

            // Hide section if no critical patients
            if (count == 0) {
                criticalPatientsSection.setVisible(false);
                criticalPatientsSection.setManaged(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCriticalPatientCard(String code, String name, int age, String unit) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e74c3c;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 180;" +
                        "-fx-max-width: 180;"
        );

        Label codeLabel = new Label(code);
        codeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #034c81;");
        nameLabel.setWrapText(true);

        Label detailsLabel = new Label(age + " yrs • " + (unit != null ? unit : "N/A"));
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f858c;");

        card.getChildren().addAll(codeLabel, nameLabel, detailsLabel);
        return card;
    }

    // ================= HELPER METHODS =================

    private String getSeverityColor(String severity) {
        return switch (severity.toLowerCase()) {
            case "critical" -> "#e74c3c";
            case "high" -> "#ff9800";
            case "moderate" -> "#ffb300";
            default -> "#5ba2f4";
        };
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? "" :
                s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String formatTimestamp(Timestamp timestamp) {
        LocalDateTime ldt = timestamp.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        long minutesAgo = java.time.Duration.between(ldt, now).toMinutes();

        if (minutesAgo < 60) {
            return minutesAgo + "m ago";
        } else if (minutesAgo < 1440) {
            return (minutesAgo / 60) + "h ago";
        } else {
            return ldt.format(DateTimeFormatter.ofPattern("MMM dd"));
        }
    }
}