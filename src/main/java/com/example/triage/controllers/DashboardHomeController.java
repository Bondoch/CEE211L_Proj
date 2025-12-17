package com.example.triage.controllers;

import com.example.triage.database.DBConnection;
import com.example.triage.services.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardHomeController {

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

    private String userRole;

    @FXML
    public void initialize() {
        loadUserRole();
        displayRolePanel();
        loadDashboardData();
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
        String sql = "SELECT COUNT(*) FROM patients WHERE severity = 'violet' OR severity = 'red'";
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
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = 'available'";
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
}