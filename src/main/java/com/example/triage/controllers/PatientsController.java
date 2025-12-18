package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import com.example.triage.database.Patient;
import com.example.triage.database.PatientDAO;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.example.triage.controllers.PatientCardController;


public class PatientsController {

    // ===== LEFT PANEL =====
    @FXML private ComboBox<String> facilityCombo;
    @FXML private ComboBox<String> floorCombo;
    @FXML private Label totalPatientsLabel;
    @FXML private Label criticalPatientsLabel;

    @FXML private CheckBox severityToggle;
    @FXML private ComboBox<String> severityBox;
    @FXML private ToggleButton sortByAdmissionBtn;
    @FXML private ToggleButton sortByBedBtn;

    // ===== CENTER AREA =====
    @FXML private StackPane patientStack;
    @FXML private Label areaLabel;
    @FXML private FlowPane patientGrid;
    @FXML private VBox emptyState;

    // ===== DETAIL CARD =====
    @FXML private Pane detailBackdrop;
    @FXML private VBox patientDetailCard;
    @FXML private Button closeDetailBtn;

    @FXML private Label detailName;
    @FXML private Label detailAge;
    @FXML private Label detailGender;
    @FXML private Label detailSeverity;
    @FXML private Label detailBed;
    @FXML private Label detailAdmission;

    // ===== ADD / EDIT POPUP =====
    @FXML private Pane addPatientBackdrop;
    @FXML private VBox addPatientPopup;
    @FXML private TextField addPatientName;
    @FXML private TextField addPatientAge;
    @FXML private TextField addPatientDiagnosis;
    @FXML private ComboBox<String> addPatientGender;
    @FXML private ComboBox<String> addPatientSeverity;

    private final PatientDAO patientDAO = new PatientDAO();

    private int selectedPatientId = -1;
    private int selectedUnitId = -1;
    private boolean isEditMode = false;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");

    // ================= INITIALIZE =================

    @FXML
    public void initialize() {
        System.out.println("PatientsController loaded ✅");

        facilityCombo.getItems().addAll(
                "Emergency Room",
                "Inpatient Ward",
                "ICU",
                "Pediatric Ward",
                "Surgery Ward"
        );
        facilityCombo.setValue("Inpatient Ward");

        floorCombo.getItems().addAll(
                "Ground Floor",
                "Floor 1",
                "Floor 2",
                "Floor 3"
        );
        floorCombo.setValue("Floor 2");

        if (severityBox != null)
            severityBox.getItems().addAll("Low", "Moderate", "High", "Critical");

        if (addPatientGender != null)
            addPatientGender.getItems().addAll("Male", "Female");

        if (addPatientSeverity != null)
            addPatientSeverity.getItems().addAll("Low", "Moderate", "High", "Critical");

        patientDetailCard.setVisible(false);
        patientDetailCard.setManaged(false);
        detailBackdrop.setVisible(false);
        detailBackdrop.setManaged(false);

        emptyState.setVisible(false);
        emptyState.setManaged(false);

        facilityCombo.setOnAction(e -> loadPatients());
        floorCombo.setOnAction(e -> loadPatients());

        loadPatients();
        severityToggle.selectedProperty().addListener((obs, wasOn, isOn) -> {
            severityBox.setDisable(!isOn);
            if (!isOn) severityBox.setValue(null);
        });

    }

    // ================= LOAD =================

    private void loadPatients() {
        patientGrid.getChildren().clear();

        String facility = facilityCombo.getValue();
        int floor = parseFloor(floorCombo.getValue());

        areaLabel.setText(facility + " — Floor " + floor);

        List<Patient> patients =
                patientDAO.getPatientsByFacilityAndFloor(facility, floor);

        int critical = 0;

        for (Patient p : patients) {
            if ("critical".equalsIgnoreCase(p.getSeverity())) critical++;
            patientGrid.getChildren().add(createPatientCard(p));
        }

        totalPatientsLabel.setText(String.valueOf(patients.size()));
        criticalPatientsLabel.setText(String.valueOf(critical));

        emptyState.setVisible(patients.isEmpty());
        emptyState.setManaged(patients.isEmpty());
    }

    private int parseFloor(String value) {
        if (value.equalsIgnoreCase("Ground Floor")) return 0;
        return Integer.parseInt(value.replaceAll("\\D+", ""));
    }

    // ================= CARD =================

    private VBox createPatientCard(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/triage/views/patient-card.fxml")
            );

            VBox card = loader.load();
            PatientCardController controller = loader.getController();

            controller.setPatient(patient, () ->
                    showPatientDetails(
                            patient.getId(),
                            patient.getUnitId(),
                            patient.getFullName(),
                            patient.getAge(),
                            patient.getGender(),
                            capitalize(patient.getSeverity()),
                            patient.getUnitLabel(),
                            patient.getAdmissionDate()
                                    .toLocalDateTime()
                                    .format(FORMATTER)
                    )
            );

            return card;

        } catch (IOException e) {
            e.printStackTrace();
            return new VBox();
        }
    }


    // ================= DETAILS =================

    private void showPatientDetails(
            int patientId,
            int unitId,
            String name,
            int age,
            String gender,
            String severity,
            String bed,
            String admission
    ) {
        selectedPatientId = patientId;
        selectedUnitId = unitId;

        detailName.setText(name);
        detailAge.setText(String.valueOf(age));
        detailGender.setText(gender);
        detailSeverity.setText(severity);
        detailBed.setText(bed);
        detailAdmission.setText(admission);

        detailBackdrop.setVisible(true);
        detailBackdrop.setManaged(true);

        patientDetailCard.setVisible(true);
        patientDetailCard.setManaged(true);
        patientDetailCard.toFront();

        FadeTransition ft =
                new FadeTransition(Duration.millis(180), patientDetailCard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    private void handleCloseDetail() {
        patientDetailCard.setVisible(false);
        patientDetailCard.setManaged(false);
        detailBackdrop.setVisible(false);
        detailBackdrop.setManaged(false);
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? "" :
                s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ================= ADD PATIENT (FXML FIX) =================


    // ================= ACTIONS =================

    @FXML
    public void handleEditPatient() {
        isEditMode = true;
    }

    @FXML
    public void handleDischargePatient() {
        patientDAO.dischargePatient(selectedPatientId, selectedUnitId);
        handleCloseDetail();
        loadPatients();
    }
    // ================= ADD PATIENT POPUP =================

    @FXML
    public void showAddPatientPopup() {
        isEditMode = false;

        if (addPatientBackdrop != null) {
            addPatientBackdrop.setVisible(true);
            addPatientBackdrop.setManaged(true);
        }

        if (addPatientPopup != null) {
            addPatientPopup.setVisible(true);
            addPatientPopup.setManaged(true);
            addPatientPopup.toFront();

            FadeTransition ft =
                    new FadeTransition(Duration.millis(180), addPatientPopup);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    @FXML
    public void hideAddPatientPopup() {
        if (addPatientBackdrop != null) {
            addPatientBackdrop.setVisible(false);
            addPatientBackdrop.setManaged(false);
        }

        if (addPatientPopup != null) {
            addPatientPopup.setVisible(false);
            addPatientPopup.setManaged(false);
        }

        clearAddPatientForm();
    }

    @FXML
    public void confirmAddPatient() {

        String name = addPatientName.getText();
        int age = Integer.parseInt(addPatientAge.getText());
        String gender = addPatientGender.getValue();
        String diagnosis = addPatientDiagnosis.getText();
        String severity = addPatientSeverity.getValue();

        String facility = facilityCombo.getValue();
        int floor = parseFloor(floorCombo.getValue());

        if (isEditMode) {
            patientDAO.updatePatient(
                    selectedPatientId, name, age, gender, diagnosis, severity
            );
        } else {
            patientDAO.addPatient(
                    name, age, gender, diagnosis, severity, facility, floor
            );
        }

        hideAddPatientPopup();
        loadPatients();
    }

    private void clearAddPatientForm() {
        if (addPatientName != null) addPatientName.clear();
        if (addPatientAge != null) addPatientAge.clear();
        if (addPatientDiagnosis != null) addPatientDiagnosis.clear();
        if (addPatientGender != null) addPatientGender.setValue(null);
        if (addPatientSeverity != null) addPatientSeverity.setValue(null);
    }

}
