package com.example.triage.controllers;

import com.example.triage.database.DBConnection;
import com.example.triage.services.CapacityMonitor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.geometry.Insets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardHomeController {

    @FXML private Label welcomeLabel;
    // Stat cards
    @FXML private Label adminStaffCountLabel;
    @FXML private Label adminPatientsCountLabel;
    @FXML private Label adminCriticalCountLabel;
    @FXML private Label adminBedsCountLabel;
    // Containers
    @FXML private VBox adminPanel;
    @FXML private VBox userPanel; // ignored on purpose
    @FXML private VBox facilityCapacityContainer;
    @FXML private VBox recentActivityContainer;
    @FXML private FlowPane criticalPatientsContainer;
    @FXML private Label criticalCountBadge;
    @FXML private VBox criticalPatientsSection;
    @FXML private VBox capacityAlertBox;

    @FXML
    public void initialize() {

        // 🔒 FORCE SINGLE DASHBOARD FOR ALL ROLES
        adminPanel.setVisible(true);
        adminPanel.setManaged(true);

        if (userPanel != null) {
            userPanel.setVisible(false);
            userPanel.setManaged(false);
        }

        welcomeLabel.setText("Welcome");

        loadStaffCount();
        loadPatientsCount();
        loadCriticalCount();
        loadBedsCount();

        loadFacilityCapacity();
        loadRecentActivity();
        loadCriticalPatients();

        // Capacity alerts
        CapacityMonitor monitor = CapacityMonitor.getInstance();
        monitor.setAlertContainer(capacityAlertBox);
        monitor.startMonitoring();
    }

    /* ================= COUNTS ================= */

    private void loadStaffCount() {
        setCount("SELECT COUNT(*) FROM staff WHERE on_shift = 1", adminStaffCountLabel);
    }

    private void loadPatientsCount() {
        setCount(
                "SELECT COUNT(*) FROM patients WHERE status = 'admitted'",
                adminPatientsCountLabel
        );
    }

    private void loadCriticalCount() {
        setCount(
                "SELECT COUNT(*) FROM patients WHERE severity = 'critical' AND status = 'admitted'",
                adminCriticalCountLabel
        );
    }

    private void loadBedsCount() {
        setCount(
                "SELECT COUNT(*) FROM units WHERE status = 'AVAILABLE'",
                adminBedsCountLabel
        );
    }

    private void setCount(String sql, Label label) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            label.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");

        } catch (Exception e) {
            label.setText("0");
            e.printStackTrace();
        }
    }

    /* ================= FACILITY CAPACITY ================= */

    private void loadFacilityCapacity() {
        facilityCapacityContainer.getChildren().clear();

        String sql = """
            SELECT f.name,
                   COUNT(u.id) total,
                   SUM(u.status='OCCUPIED') occupied
            FROM facilities f
            LEFT JOIN floors fl ON f.id = fl.facility_id
            LEFT JOIN units u ON fl.id = u.floor_id
            GROUP BY f.id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                facilityCapacityContainer.getChildren().add(
                        createCapacityBar(
                                rs.getString("name"),
                                rs.getInt("occupied"),
                                rs.getInt("total")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCapacityBar(String name, int occupied, int total) {
        VBox box = new VBox(6);

        HBox header = new HBox();
        Label title = new Label(name);
        Label count = new Label(occupied + " / " + total);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, count);

        ProgressBar bar = new ProgressBar(total == 0 ? 0 : (double) occupied / total);
        bar.setPrefHeight(8);

        box.getChildren().addAll(header, bar);
        return box;
    }

    /* ================= RECENT ACTIVITY ================= */

    private void loadRecentActivity() {
        recentActivityContainer.getChildren().clear();

        String sql = """
            SELECT patient_code, full_name, admission_date, severity
            FROM patients
            WHERE status='admitted'
            ORDER BY admission_date DESC
            LIMIT 8
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                recentActivityContainer.getChildren().add(
                        createActivityItem(
                                rs.getString("patient_code"),
                                rs.getString("full_name"),
                                rs.getTimestamp("admission_date"),
                                rs.getString("severity")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createActivityItem(
            String code, String name, Timestamp ts, String severity
    ) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(10));

        FontIcon icon = new FontIcon("fas-user-injured");
        Label info = new Label(name + " • " + code + " • " + severity);
        Label time = new Label(formatTime(ts));

        row.getChildren().addAll(icon, info, time);
        return row;
    }

    /* ================= CRITICAL PATIENTS ================= */

    private void loadCriticalPatients() {
        criticalPatientsContainer.getChildren().clear();

        String sql = """
            SELECT patient_code, full_name, age
            FROM patients
            WHERE severity='critical' AND status='admitted'
        """;

        int count = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                count++;
                criticalPatientsContainer.getChildren().add(
                        new Label(rs.getString("full_name"))
                );
            }

            criticalCountBadge.setText(String.valueOf(count));
            criticalPatientsSection.setVisible(count > 0);
            criticalPatientsSection.setManaged(count > 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= HELPERS ================= */

    private String formatTime(Timestamp ts) {
        LocalDateTime t = ts.toLocalDateTime();
        return t.format(DateTimeFormatter.ofPattern("MMM dd"));
    }
}
