package com.example.triage.controllers;

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
}