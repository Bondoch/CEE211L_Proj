package com.example.triage.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private VBox sidebar;
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

    @FXML
    public void initialize() {

        sidebar.setTranslateX(-200);
        sidebar.setVisible(false);
        sidebar.setManaged(false);

        overlay.setVisible(false);
        overlay.setManaged(false);

        overlay.prefWidthProperty().bind(contentWrapper.widthProperty());
        overlay.prefHeightProperty().bind(contentWrapper.heightProperty());
        sidebar.prefHeightProperty().bind(contentWrapper.heightProperty());

        // Click outside sidebar closes it
        overlay.setOnMouseClicked(e -> {
            if (menuOpen) toggleSidebar();
        });

        itemDashboard.setOnMouseClicked(e -> selectMenu(itemDashboard, "Dashboard"));
        itemPatients.setOnMouseClicked(e -> selectMenu(itemPatients, "Patients"));
        itemFacilities.setOnMouseClicked(e -> selectMenu(itemFacilities, "Facilities"));
        itemStaff.setOnMouseClicked(e -> selectMenu(itemStaff, "Staff Accounts"));
        itemSettings.setOnMouseClicked(e -> selectMenu(itemSettings, "Settings"));

        selectMenu(itemDashboard, "Dashboard");
    }

    // -------------------------------------
    // SIDEBAR OPEN/CLOSE
    // -------------------------------------
    @FXML
    private void toggleSidebar() {

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), overlay);

        if (!menuOpen) {

            sidebar.setVisible(true);
            sidebar.setManaged(true);
            sidebar.toFront();   // ✅ sidebar on top

            overlay.setVisible(true);
            overlay.setManaged(true);
            // ❌ DO NOT call overlay.toFront()

            slide.setFromX(-200);
            slide.setToX(0);

            fade.setFromValue(0);
            fade.setToValue(0.5);

            menuOpen = true;

        } else {

            slide.setFromX(0);
            slide.setToX(-200);

            fade.setFromValue(0.5);
            fade.setToValue(0);

            fade.setOnFinished(e -> {
                overlay.setVisible(false);
                overlay.setManaged(false);

                sidebar.setVisible(false);
                sidebar.setManaged(false);
            });

            menuOpen = false;
        }

        slide.play();
        fade.play();
    }

    // -------------------------------------
    // MENU SELECTION HANDLING
    // -------------------------------------
    private void selectMenu(HBox selected, String name) {

        // Remove highlight from all items
        itemDashboard.getStyleClass().remove("active");
        itemStaff.getStyleClass().remove("active");
        itemPatients.getStyleClass().remove("active");
        itemFacilities.getStyleClass().remove("active");
        itemSettings.getStyleClass().remove("active");

        // Add highlight to clicked item
        selected.getStyleClass().add("active");

        // ✅ UPDATE TOP BAR TITLE
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