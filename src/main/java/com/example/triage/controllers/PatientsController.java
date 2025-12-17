package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class PatientsController {

    @FXML private ComboBox<String> facilityCombo;
    @FXML private ComboBox<String> floorCombo;
    @FXML private Label areaLabel;
    @FXML private FlowPane patientGrid;
    @FXML private StackPane patientStack;
    @FXML private VBox patientDetailCard;
    @FXML private Button closeDetailBtn;
    @FXML private VBox emptyState;

    // Detail card labels
    @FXML private Label detailName;
    @FXML private Label detailAge;
    @FXML private Label detailGender;
    @FXML private Label detailSeverity;
    @FXML private Label detailBed;
    @FXML private Label detailAdmission;

    // Statistics labels
    @FXML private Label totalPatientsLabel;
    @FXML private Label criticalPatientsLabel;
    @FXML private ComboBox<String> severityFilter;
    @FXML private ToggleButton sortByAdmissionBtn;
    @FXML private ToggleButton sortByBedBtn;
    @FXML private CheckBox severityToggle;
    @FXML private ComboBox<String> severityBox;
    @FXML private Pane detailBackdrop;

    @FXML
    public void initialize() {

        // Populate facility dropdown
        facilityCombo.getItems().addAll(
                "Emergency Room",
                "Inpatient Ward",
                "ICU",
                "Pediatric Ward",
                "Surgery Ward"
        );

        // Populate floor dropdown
        floorCombo.getItems().addAll(
                "Ground Floor",
                "Floor 1",
                "Floor 2",
                "Floor 3"
        );

        // Set default selection
        facilityCombo.setValue("Inpatient Ward");
        floorCombo.setValue("Floor 2");

        // ================================
        // SEVERITY FILTER (STRUCTURE ONLY)
        // ================================

        severityBox.getItems().addAll(
                "Moderate",
                "Under Observation",
                "Critical"
        );

        ToggleGroup sortGroup = new ToggleGroup();
        sortByAdmissionBtn.setToggleGroup(sortGroup);
        sortByBedBtn.setToggleGroup(sortGroup);

        // Dropdown only enabled when toggle is active
        severityToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            severityBox.setDisable(!newVal);
        });

        // Ensure disabled by default
        severityBox.setDisable(true);

        // ================================

        // Load initial data
        loadPatients();

        // Listeners for combo boxes
        facilityCombo.setOnAction(e -> loadPatients());
        floorCombo.setOnAction(e -> loadPatients());

        // Close detail card
        if (closeDetailBtn != null) {
            closeDetailBtn.setOnAction(e -> hideDetailCard());
        }

        // Hide detail card initially
        if (patientDetailCard != null) {
            patientDetailCard.setVisible(false);
            patientDetailCard.setManaged(false);
        }
    }

    private void loadPatients() {
        String facility = facilityCombo.getValue();
        String floor = floorCombo.getValue();

        if (facility == null || floor == null) {
            return;
        }

        // Update area label
        areaLabel.setText(facility + " — " + floor);

        // Clear existing cards
        patientGrid.getChildren().clear();

        // Generate sample patient cards (replace with actual database query)
        int patientCount = 8;
        int criticalCount = 2;

        for (int i = 1; i <= patientCount; i++) {
            VBox card = createPatientCard(
                    "PT-" + (1000 + i),
                    "Patient " + i,
                    45 + i,
                    i % 2 == 0 ? "Male" : "Female",
                    i <= criticalCount ? "Critical" : (i <= 4 ? "High" : "Moderate"),
                    "Room " + (i % 5 + 1) + "A - Bed " + (i % 3 + 1),
                    "Dec 15, 2024 - 08:30 AM"
            );
            patientGrid.getChildren().add(card);
        }

        // Update statistics
        totalPatientsLabel.setText(String.valueOf(patientCount));
        criticalPatientsLabel.setText(String.valueOf(criticalCount));

        // Hide empty state
        if (emptyState != null) {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
        }
    }

    private VBox createPatientCard(String id, String name, int age, String gender,
                                   String severity, String bed, String admission) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setMinHeight(180);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 18;" +
                        "-fx-border-color: #e8e8e8;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 18;" +
                            "-fx-border-color: #2ca3fa;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(44,163,250,0.2), 12, 0, 0, 3);"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 18;" +
                            "-fx-border-color: #e8e8e8;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
            );
        });

        // Click to show details
        card.setOnMouseClicked(e -> {
            e.consume();
            showPatientDetails(id, name, age, gender, severity, bed, admission);
        });

        // Header with ID and severity badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label(id);
        idLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #7f858c;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label severityBadge = new Label(severity);
        severityBadge.setPadding(new Insets(4, 10, 4, 10));

        String badgeColor = switch(severity) {
            case "Critical" -> "-fx-background-color: #ffebee; -fx-text-fill: #c62828;";
            case "High" -> "-fx-background-color: #fff3e0; -fx-text-fill: #e65100;";
            case "Moderate" -> "-fx-background-color: #fff9c4; -fx-text-fill: #f57f17;";
            default -> "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;";
        };

        severityBadge.setStyle(
                badgeColor +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(idLabel, spacer, severityBadge);

        // Patient name with icon
        HBox nameBox = new HBox(8);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon userIcon = new FontIcon("fas-user");
        userIcon.setIconSize(14);
        userIcon.setIconColor(javafx.scene.paint.Color.web("#2ca3fa"));

        Label nameLabel = new Label(name);
        nameLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #034c81;"
        );

        nameBox.getChildren().addAll(userIcon, nameLabel);

        // Age and Gender
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label ageLabel = new Label(age + " yrs");
        ageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f858c;");

        Label genderLabel = new Label(gender);
        genderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f858c;");

        infoBox.getChildren().addAll(ageLabel, createDot(), genderLabel);

        // Bed assignment
        HBox bedBox = new HBox(6);
        bedBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon bedIcon = new FontIcon("fas-bed");
        bedIcon.setIconSize(12);
        bedIcon.setIconColor(javafx.scene.paint.Color.web("#7f858c"));

        Label bedLabel = new Label(bed);
        bedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f858c;");

        bedBox.getChildren().addAll(bedIcon, bedLabel);


        card.getChildren().addAll(header, nameBox, infoBox, bedBox);

        return card;
    }

    private Label createDot() {
        Label dot = new Label("•");
        dot.setStyle("-fx-font-size: 12px; -fx-text-fill: #c9bbaa;");
        return dot;
    }

    private void showPatientDetails(String id, String name, int age, String gender,
                                    String severity, String bed, String admission) {

        // Populate detail card
        detailName.setText(name);
        detailAge.setText(String.valueOf(age));
        detailGender.setText(gender);
        detailSeverity.setText(severity);
        detailBed.setText(bed);
        detailAdmission.setText(admission);

        // Update severity color
        String severityColor = switch (severity) {
            case "Critical" -> "#c62828";
            case "High" -> "#e65100";
            case "Moderate" -> "#f57f17";
            default -> "#2e7d32";
        };
        detailSeverity.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + severityColor + ";"
        );

        // === SHOW DETAIL CARD ===
        patientDetailCard.setVisible(true);
        patientDetailCard.setManaged(true);
        patientDetailCard.toFront();

        // Click outside to close
        patientStack.setOnMouseClicked(e -> hideDetailCard());
        patientDetailCard.setOnMouseClicked(e -> e.consume());

        FadeTransition fade = new FadeTransition(Duration.millis(200), patientDetailCard);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void hideDetailCard() {
        FadeTransition fade = new FadeTransition(Duration.millis(200), patientDetailCard);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            patientDetailCard.setVisible(false);
            patientDetailCard.setManaged(false);
            patientStack.setOnMouseClicked(null);
        });
        fade.play();
    }
}

// here