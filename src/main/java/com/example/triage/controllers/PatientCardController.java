package com.example.triage.controllers;

import com.example.triage.database.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PatientCardController {

    @FXML private VBox root;
    @FXML private Label codeLabel;
    @FXML private Label nameLabel;
    @FXML private Label ageGenderLabel;
    @FXML private Label unitLabel;
    @FXML private Label severityLabel;
    @FXML private Label facilityFloorLabel;


    private Patient patient;

    @FXML
    public void initialize() {
        root.setOnMouseEntered(e -> {
            root.setStyle(root.getStyle() +
                    "-fx-border-color: #2ca3fa;" +
                    "-fx-effect: dropshadow(gaussian, rgba(44,163,250,0.35), 14, 0, 0, 4);"
            );
        });

        root.setOnMouseExited(e -> {
            root.setStyle(root.getStyle()
                    .replace("-fx-border-color: #2ca3fa;", "")
                    .replace(
                            "-fx-effect: dropshadow(gaussian, rgba(44,163,250,0.35), 14, 0, 0, 4);",
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
                    )
            );
        });
    }


    public void setPatient(Patient patient, Runnable onClick) {
        this.patient = patient;

        codeLabel.setText(patient.getPatientCode());
        nameLabel.setText(patient.getFullName());
        ageGenderLabel.setText(
                patient.getAge() + " yrs • " + patient.getGender()
        );
        unitLabel.setText(patient.getUnitLabel());
        facilityFloorLabel.setText(patient.getFacilityName() + " - F" + patient.getFloorNumber());
        severityLabel.setText(capitalize(patient.getSeverity()));

        applySeverityStyle(patient.getSeverity());

        root.setOnMouseClicked(e -> onClick.run());
    }

    private void applySeverityStyle(String severity) {
        switch (severity.toLowerCase()) {
            case "critical" -> severityLabel.setStyle(
                    "-fx-background-color: #ffe5e5; -fx-text-fill: #ff4d4d; -fx-font-weight: bold;"
            );
            case "high" -> severityLabel.setStyle(
                    "-fx-background-color: #fff1d6; -fx-text-fill: #ff9800; -fx-font-weight: bold;"
            );
            case "moderate" -> severityLabel.setStyle(
                    "-fx-background-color: #fff6cc; -fx-text-fill: #ffb300; -fx-font-weight: bold;"
            );
            default -> severityLabel.setStyle(
                    "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;"
            );
        }
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty()
                ? ""
                : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
