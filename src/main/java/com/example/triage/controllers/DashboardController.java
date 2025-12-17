package com.example.triage.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;

public class DashboardController {

    @FXML private BorderPane sidebar;
    @FXML private Pane overlay;
    @FXML private HBox itemDashboard;
    @FXML private HBox itemPatients;
    @FXML private HBox itemFacilities;
    @FXML private HBox itemStaff;
    @FXML private HBox itemSettings;
    @FXML private HBox itemLogout;
    @FXML private Button menuButton;
    @FXML private StackPane contentWrapper;
    @FXML private Label titleLabel;
    @FXML private BorderPane contentArea;
    @FXML private VBox logoutPanel;

    private boolean menuOpen = false;
    private static final double SIDEBAR_WIDTH = 240;
    private static final double TOP_BAR_HEIGHT = 65;

    @FXML
    public void initialize() {

        /* ================================
           SIDEBAR — CRITICAL FIX
           ================================ */

        sidebar.setTranslateX(-SIDEBAR_WIDTH);   // hidden by default
        sidebar.setVisible(true);                // always visible logically

        /* ================================
           OVERLAY
           ================================ */

        overlay.setVisible(false);
        overlay.setManaged(false);

        overlay.prefWidthProperty().bind(contentWrapper.widthProperty());
        overlay.prefHeightProperty().bind(contentWrapper.heightProperty());

        overlay.setOnMouseClicked(e -> {
            if (menuOpen) toggleSidebar();
        });

        /* ================================
           MENU HANDLERS
           ================================ */

        itemDashboard.setOnMouseClicked(e -> selectMenu(itemDashboard, "Dashboard"));
        itemPatients.setOnMouseClicked(e -> selectMenu(itemPatients, "Patients"));
        itemFacilities.setOnMouseClicked(e -> selectMenu(itemFacilities, "Facilities"));
        itemStaff.setOnMouseClicked(e -> selectMenu(itemStaff, "Staff Accounts"));
        itemSettings.setOnMouseClicked(e -> selectMenu(itemSettings, "Settings"));

        selectMenu(itemDashboard, "Dashboard");
    }

    /* ================================
       SIDEBAR ANIMATION (UNCHANGED)
       ================================ */
    @FXML
    private void toggleSidebar() {

        TranslateTransition slide = new TranslateTransition(Duration.millis(250), sidebar);
        FadeTransition fade = new FadeTransition(Duration.millis(250), overlay);

        if (!menuOpen) {

            overlay.setVisible(true);
            overlay.setManaged(true);

            // 🔑 CRITICAL FIX — Z-ORDER
            overlay.toBack();      // overlay BEHIND sidebar
            sidebar.toFront();     // sidebar ALWAYS on top

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

    /* ================================
       MENU SELECTION
       ================================ */
    private void selectMenu(HBox selected, String name) {

        itemDashboard.getStyleClass().remove("active");
        itemStaff.getStyleClass().remove("active");
        itemPatients.getStyleClass().remove("active");
        itemFacilities.getStyleClass().remove("active");
        itemSettings.getStyleClass().remove("active");

        selected.getStyleClass().add("active");
        titleLabel.setText(name);

        switch (name) {
            case "Dashboard":
                loadContent("dashboard-home.fxml");
                break;
            case "Staff Accounts":
                loadContent("staff-accounts.fxml");
                break;
            case "Patients":
                loadContent("patients.fxml");
                break;
            case "Facilities":
                loadContent("facilities.fxml");
                break;
            case "Settings":
                loadSettingsWithStage();
                break;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================================
       LOAD SETTINGS WITH STAGE REFERENCE
       ================================ */
    private void loadSettingsWithStage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/triage/views/settings.fxml")
            );
            Parent view = loader.load();

            // Get controller and pass Stage reference
            SettingsController settingsController = loader.getController();
            if (settingsController != null) {
                Stage stage = (Stage) sidebar.getScene().getWindow();
                settingsController.setPrimaryStage(stage);

                // Also pass current username if available
                // settingsController.setCurrentUser(currentUsername);
            }

            contentArea.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================================
       LOGOUT
       ================================ */
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
    public void performLogout() {
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