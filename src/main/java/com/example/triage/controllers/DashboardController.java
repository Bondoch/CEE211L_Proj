package com.example.triage.controllers;

import com.example.triage.database.DBConnection;
import com.example.triage.services.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardController {

    @FXML private BorderPane sidebar;
    @FXML private Pane overlay;
    @FXML private StackPane contentWrapper;
    @FXML private BorderPane contentArea;

    @FXML private HBox itemDashboard;
    @FXML private HBox itemPatients;
    @FXML private HBox itemFacilities;
    @FXML private HBox itemStaff;
    @FXML private HBox itemSettings;

    @FXML private Button menuButton;
    @FXML private Label titleLabel;
    @FXML private VBox logoutPanel;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;


    private boolean menuOpen = false;
    private static final double SIDEBAR_WIDTH = 240;

    @FXML
    public void initialize() {
        sidebar.setTranslateX(-SIDEBAR_WIDTH);
        sidebar.setVisible(true);

        overlay.setVisible(false);
        overlay.setManaged(false);

        overlay.prefWidthProperty().bind(contentWrapper.widthProperty());
        overlay.prefHeightProperty().bind(contentWrapper.heightProperty());

        SessionManager session = SessionManager.getInstance();

        loadUserName(session.getStaffId());

        userRoleLabel.setText(
                switch (session.getRole()) {
                    case "ADMIN" -> "Administrator";
                    case "DOCTOR" -> "Doctor";
                    case "NURSE" -> "Nurse";
                    default -> session.getRole();
                }
        );


        overlay.setOnMouseClicked(e -> {
            if (menuOpen) toggleSidebar();
        });

        itemDashboard.setOnMouseClicked(e -> selectMenu(itemDashboard, "Dashboard"));
        itemStaff.setOnMouseClicked(e -> selectMenu(itemStaff, "Staff Accounts"));
        itemPatients.setOnMouseClicked(e -> selectMenu(itemPatients, "Patients"));
        itemFacilities.setOnMouseClicked(e -> selectMenu(itemFacilities, "Facilities"));
        itemSettings.setOnMouseClicked(e -> selectMenu(itemSettings, "Settings"));

        selectMenu(itemDashboard, "Dashboard");


    }

    private void loadUserName(int staffId) {

        if (staffId == 0) {
            userNameLabel.setText("Administrator");
            return;
        }

        String sql = "SELECT first_name, last_name FROM staff WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                userNameLabel.setText(
                        rs.getString("first_name") + " " + rs.getString("last_name")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            userNameLabel.setText("User");
        }
    }



    @FXML
    private void toggleSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(250), sidebar);
        FadeTransition fade = new FadeTransition(Duration.millis(250), overlay);

        if (!menuOpen) {
            overlay.setVisible(true);
            overlay.setManaged(true);

            overlay.toFront();
            sidebar.toFront();

            slide.setFromX(-SIDEBAR_WIDTH);
            slide.setToX(0);

            fade.setFromValue(0);
            fade.setToValue(0.5);

            menuOpen = true;

        } else {
            slide.setFromX(0);
            slide.setToX(-SIDEBAR_WIDTH);

            fade.setFromValue(0.5);
            fade.setToValue(0);

            fade.setOnFinished(e -> {
                overlay.setVisible(false);
                overlay.setManaged(false);
            });

            menuOpen = false;
        }

        slide.play();
        fade.play();
    }

    private void selectMenu(HBox selected, String name) {
        itemDashboard.getStyleClass().remove("active");
        itemStaff.getStyleClass().remove("active");
        itemPatients.getStyleClass().remove("active");
        itemFacilities.getStyleClass().remove("active");
        itemSettings.getStyleClass().remove("active");

        selected.getStyleClass().add("active");
        titleLabel.setText(name);

        switch (name) {
            case "Dashboard" -> loadContent("dashboard-home.fxml");
            case "Staff Accounts" -> loadContent("staff-accounts.fxml");
            case "Patients" -> loadContent("patients.fxml");
            case "Facilities" -> loadContent("facilities.fxml");
            case "Settings" -> loadContent("settings.fxml");
        }

        if (menuOpen) toggleSidebar();
    }

    private void loadContent(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/triage/views/" + fxml)
            );
            Parent view = loader.load();

            contentArea.setCenter(view);

            overlay.toFront();
            sidebar.toFront();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        overlay.setVisible(true);
        overlay.setManaged(true);
        overlay.toFront();

        logoutPanel.setVisible(true);
        logoutPanel.setManaged(true);
        logoutPanel.toFront();
    }

    @FXML
    private void cancelLogout() {
        logoutPanel.setVisible(false);
        logoutPanel.setManaged(false);
        overlay.setVisible(false);
        overlay.setManaged(false);
    }

    @FXML
    private void performLogout() {
        // Clear session
        SessionManager.getInstance().endSession();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/triage/views/login-view.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String formatRole(String role) {
        return role.charAt(0) + role.substring(1).toLowerCase();
    }

}