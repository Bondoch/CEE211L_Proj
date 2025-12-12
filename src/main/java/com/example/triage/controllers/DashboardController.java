package com.example.triage.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.FadeTransition;


public class DashboardController {

    @FXML private VBox sidebar;
    @FXML private Button menuButton;
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnFacilities;
    @FXML private Button btnStaff;
    @FXML private Button btnSettings;
    @FXML private AnchorPane contentArea;
    @FXML private Pane overlay;

    private boolean menuOpen = false;

    @FXML
    public void initialize() {
        menuButton.setOnAction(e -> toggleSidebar());
        overlay.setOnMouseClicked(e -> toggleSidebar());
        setupMenuActions();
    }

    private void toggleSidebar() {

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), overlay);

        if (!menuOpen) {
            // OPEN SIDEBAR
            slide.setToX(200);
            overlay.setVisible(true);

            fade.setFromValue(0);
            fade.setToValue(1);

            menuOpen = true;

        } else {
            // CLOSE SIDEBAR
            slide.setToX(0);

            fade.setFromValue(1);
            fade.setToValue(0);

            // When fade-out finishes, hide the overlay
            fade.setOnFinished(e -> overlay.setVisible(false));

            menuOpen = false;
        }

        slide.play();
        fade.play();
    }

    private void setupMenuActions() {
        btnDashboard.setOnAction(e -> loadDashboardView());
        btnPatients.setOnAction(e -> loadPatientsView());
        btnFacilities.setOnAction(e -> loadFacilitiesView());
        btnStaff.setOnAction(e -> loadStaffView());
        btnSettings.setOnAction(e -> loadSettingsView());
    }

    private void loadDashboardView() {
        System.out.println("Dashboard clicked!");
    }

    private void loadPatientsView() {
        System.out.println("Patients clicked!");
    }

    private void loadFacilitiesView() {
        System.out.println("Facilities clicked!");
    }

    private void loadStaffView() {
        System.out.println("Staff Accounts clicked!");
    }

    private void loadSettingsView() {
        System.out.println("Settings clicked!");
    }
}
