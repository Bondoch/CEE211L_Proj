package com.example.triage.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {

    @FXML private VBox sidebar;
    @FXML private Pane overlay;
    @FXML private HBox itemDashboard;
    @FXML private HBox itemPatients;
    @FXML private HBox itemFacilities;
    @FXML private HBox itemStaff;
    @FXML private HBox itemSettings;
    @FXML private AnchorPane contentArea;
    @FXML private Button menuButton;

    private boolean menuOpen = false;

    @FXML
    public void initialize() {

        // Sidebar starts hidden
        sidebar.setTranslateX(-200);
        overlay.setVisible(false);

        // Clicking dim background closes the sidebar
        overlay.setOnMouseClicked(e -> {
            if (menuOpen) toggleSidebar();
        });

        // Menu item clicks
        itemDashboard.setOnMouseClicked(e -> selectMenu(itemDashboard, "Dashboard"));
        itemPatients.setOnMouseClicked(e -> selectMenu(itemPatients, "Patients"));
        itemFacilities.setOnMouseClicked(e -> selectMenu(itemFacilities, "Facilities"));
        itemStaff.setOnMouseClicked(e -> selectMenu(itemStaff, "Staff Accounts"));
        itemSettings.setOnMouseClicked(e -> selectMenu(itemSettings, "Settings"));

        // Default active tab
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
            slide.setToX(200);
            menuOpen = true;

            overlay.setVisible(true);
            fade.setFromValue(0);
            fade.setToValue(0.5);

        } else {
            slide.setToX(0);
            menuOpen = false;

            fade.setFromValue(0.5);
            fade.setToValue(0);

            fade.setOnFinished(e -> overlay.setVisible(false));
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
        itemPatients.getStyleClass().remove("active");
        itemFacilities.getStyleClass().remove("active");
        itemStaff.getStyleClass().remove("active");
        itemSettings.getStyleClass().remove("active");

        // Add highlight to clicked item
        selected.getStyleClass().add("active");

        System.out.println(name + " selected!");
    }
}