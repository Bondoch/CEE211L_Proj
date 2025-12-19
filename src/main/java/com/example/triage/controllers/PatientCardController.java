package com.example.triage.controllers;

import com.example.triage.database.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class PatientCardController {


    /* ===== ROOT ===== */
    @FXML private VBox root;

    /* ===== TEXT FLOWS (FOR HIGHLIGHTING) ===== */
    @FXML private TextFlow nameFlow;
    @FXML private TextFlow codeFlow;

    /* ===== OTHER LABELS ===== */
    @FXML private Label ageGenderLabel;
    @FXML private Label unitLabel;
    @FXML private Label severityLabel;
    @FXML private Label facilityFloorLabel;

    private Patient patient;

    /* ================= INITIALIZE ================= */

    @FXML
    public void initialize() {
        root.setOnMouseEntered(e ->
                root.setStyle(root.getStyle()
                        + "-fx-border-color: #2ca3fa;"
                        + "-fx-effect: dropshadow(gaussian, rgba(44,163,250,0.35), 14, 0, 0, 4);")
        );

        root.setOnMouseExited(e ->
                root.setStyle(root.getStyle()
                        .replace("-fx-border-color: #2ca3fa;", "")
                        .replace(
                                "-fx-effect: dropshadow(gaussian, rgba(44,163,250,0.35), 14, 0, 0, 4);",
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
                        ))
        );
    }

    /* ================= PUBLIC SETTER ================= */

    public void setPatient(
            Patient patient,
            String searchText,
            Runnable onClick
    ) {
        this.patient = patient;

        // 🔍 Highlight name & patient code
        applyHighlight(nameFlow, patient.getFullName(), searchText);
        applyHighlight(codeFlow, patient.getPatientCode(), searchText);

        // Other info
        ageGenderLabel.setText(patient.getAge() + " • " + patient.getGender());
        unitLabel.setText(patient.getUnitLabel());
        facilityFloorLabel.setText(
                patient.getFacilityName() + " • Floor " + patient.getFloorNumber()
        );

        severityLabel.setText(capitalize(patient.getSeverity()));
        applySeverityStyle(patient.getSeverity());

        root.setOnMouseClicked(e -> onClick.run());
    }

    /* ================= HIGHLIGHT LOGIC ================= */

    private void applyHighlight(
            TextFlow flow,
            String text,
            String query
    ) {
        flow.getChildren().clear();

        if (query == null || query.isBlank()) {
            flow.getChildren().add(new Text(text));
            return;
        }

        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();

        int start = 0;
        int index;

        while ((index = lowerText.indexOf(lowerQuery, start)) >= 0) {

            if (index > start) {
                flow.getChildren().add(
                        new Text(text.substring(start, index))
                );
            }

            Text highlight = new Text(
                    text.substring(index, index + query.length())
            );
            highlight.setStyle(
                    "-fx-fill: #034c81;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-color: #e6f4ff;"
            );

            flow.getChildren().add(highlight);
            start = index + query.length();
        }

        if (start < text.length()) {
            flow.getChildren().add(
                    new Text(text.substring(start))
            );
        }
    }

    /* ================= SEVERITY STYLE ================= */

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
        return (s == null || s.isEmpty())
                ? ""
                : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}
