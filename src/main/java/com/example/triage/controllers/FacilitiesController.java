package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class FacilitiesController {

    @FXML private ComboBox<String> facilitySelector;
    @FXML private ComboBox<String> floorSelector;

    @FXML private Button editFacilityBtn;
    @FXML private VBox editFacilityPanel;
    @FXML private ComboBox<String> facilityStatusBox;
    @FXML private Button doneEditBtn;

    @FXML
    public void initialize() {

        // Populate status dropdown
        facilityStatusBox.getItems().addAll(
                "Available",
                "Occupied",
                "Unavailable"
        );

        // Toggle edit panel
        editFacilityBtn.setOnAction(e -> toggleEditPanel());

        doneEditBtn.setOnAction(e -> {
            editFacilityPanel.setVisible(false);
            editFacilityPanel.setManaged(false);
        });
    }

    private void toggleEditPanel() {
        boolean show = !editFacilityPanel.isVisible();
        editFacilityPanel.setVisible(show);
        editFacilityPanel.setManaged(show);
    }
}