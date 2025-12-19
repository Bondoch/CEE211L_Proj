package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import com.example.triage.database.Patient;
import com.example.triage.database.PatientDAO;
import com.example.triage.database.FacilityDAO;
import com.example.triage.database.FloorDAO;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    // ✅ REMOVED: @FXML private TextField searchField; (not in FXML)

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

    // ===== ADD POPUP =====
    @FXML private Pane addPatientBackdrop;
    @FXML private VBox addPatientPopup;
    @FXML private TextField addPatientName;
    @FXML private TextField addPatientAge;
    @FXML private TextField addPatientDiagnosis;
    @FXML private ComboBox<String> addPatientGender;
    @FXML private ComboBox<String> addPatientSeverity;

    private final PatientDAO patientDAO = new PatientDAO();
    private final FacilityDAO facilityDAO = new FacilityDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    private int selectedPatientId = -1;
    private int selectedUnitId = -1;

    private List<Patient> currentPatientList;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");

    // ================= INITIALIZE =================

    @FXML
    public void initialize() {
        System.out.println("✅ PatientsController initialized");

        // Clear grids & hide states
        patientGrid.getChildren().clear();
        emptyState.setVisible(true);
        emptyState.setManaged(true);
        areaLabel.setText("Select Facility and Floor");

        // Load facilities
        facilityCombo.getItems().setAll(facilityDAO.getAllFacilities());
        facilityCombo.setValue(null);

        // Floor disabled until facility selected
        floorCombo.getItems().clear();
        floorCombo.setDisable(true);
        floorCombo.setValue(null);

        // Facility selected → load floors
        facilityCombo.setOnAction(e -> handleFacilitySelection());

        // Floor selected → load patients
        floorCombo.setOnAction(e -> handleFloorSelection());

        // Populate dropdowns
        if (severityBox != null)
            severityBox.getItems().addAll("Low", "Moderate", "High", "Critical");

        if (addPatientGender != null)
            addPatientGender.getItems().addAll("Male", "Female");

        if (addPatientSeverity != null)
            addPatientSeverity.getItems().addAll("Low", "Moderate", "High", "Critical");

        // ✨ Severity toggle listener
        severityToggle.selectedProperty().addListener((obs, wasOn, isOn) -> {
            severityBox.setDisable(!isOn);
            if (!isOn) {
                severityBox.setValue(null);
                applyFiltersAndSort();
            }
        });

        // ✨ Severity box listener
        severityBox.setOnAction(e -> applyFiltersAndSort());

        // ✨ Sort button listeners
        sortByAdmissionBtn.setOnAction(e -> {
            if (sortByAdmissionBtn.isSelected()) {
                sortByBedBtn.setSelected(false);
            }
            applyFiltersAndSort();
        });

        sortByBedBtn.setOnAction(e -> {
            if (sortByBedBtn.isSelected()) {
                sortByAdmissionBtn.setSelected(false);
            }
            applyFiltersAndSort();
        });

        // ✅ REMOVED: Search field listener (not in FXML)

        // ✨ Backdrop click to close detail
        if (detailBackdrop != null) {
            detailBackdrop.setOnMouseClicked(e -> handleCloseDetail());
        }
    }

    private void handleFacilitySelection() {
        String facility = facilityCombo.getValue();
        if (facility == null) return;

        int facilityId = facilityDAO.getFacilityIdByName(facility);
        floorCombo.getItems().setAll(floorDAO.getFloorsByFacility(facilityId));

        floorCombo.setDisable(false);
        floorCombo.setValue(null);

        // Reset UI
        patientGrid.getChildren().clear();
        emptyState.setVisible(true);
        emptyState.setManaged(true);
        areaLabel.setText(facility);
    }

    private void handleFloorSelection() {
        if (facilityCombo.getValue() == null || floorCombo.getValue() == null)
            return;

        loadPatients();
    }

    // ================= LOAD PATIENTS =================

    @FXML
    public void loadPatients() {
        if (facilityCombo.getValue() == null || floorCombo.getValue() == null) {
            patientGrid.getChildren().clear();
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }

        String facility = facilityCombo.getValue();
        int floor = parseFloor(floorCombo.getValue());

        areaLabel.setText(facility + " — Floor " + floor);

        currentPatientList = patientDAO.getPatientsByFacilityAndFloor(facility, floor);

        applyFiltersAndSort();
    }

    // ✨ Apply all filters and sorting
    private void applyFiltersAndSort() {
        if (currentPatientList == null) return;

        List<Patient> filtered = currentPatientList.stream()
                .filter(this::matchesFilters)
                .collect(Collectors.toList());

        // Apply sorting
        if (sortByAdmissionBtn.isSelected()) {
            filtered.sort(Comparator.comparing(Patient::getAdmissionDate).reversed());
        } else if (sortByBedBtn.isSelected()) {
            filtered.sort(Comparator.comparing(Patient::getUnitLabel));
        }

        // Update UI
        displayPatients(filtered);
    }

    // ✨ Check if patient matches filters
    private boolean matchesFilters(Patient patient) {
        // Severity filter
        if (severityToggle.isSelected() && severityBox.getValue() != null) {
            String selectedSeverity = severityBox.getValue().toLowerCase();
            if (!patient.getSeverity().equalsIgnoreCase(selectedSeverity)) {
                return false;
            }
        }

        // ✅ REMOVED: Search filter (not in FXML)

        return true;
    }

    // ✨ Display filtered patients
    private void displayPatients(List<Patient> patients) {
        patientGrid.getChildren().clear();

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

    // ================= CREATE CARD =================

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

    // ================= PATIENT DETAILS =================

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

        // ✨ Show backdrop
        if (detailBackdrop != null) {
            detailBackdrop.setVisible(true);
            detailBackdrop.setManaged(true);
            detailBackdrop.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        }

        patientDetailCard.setVisible(true);
        patientDetailCard.setManaged(true);
        patientDetailCard.toFront();

        FadeTransition ft = new FadeTransition(Duration.millis(180), patientDetailCard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    private void handleCloseDetail() {
        patientDetailCard.setVisible(false);
        patientDetailCard.setManaged(false);

        if (detailBackdrop != null) {
            detailBackdrop.setVisible(false);
            detailBackdrop.setManaged(false);
        }
    }

    // ✨ Edit Patient (placeholder)
    @FXML
    public void handleEditPatient() {
        System.out.println("✏️ Edit Patient ID: " + selectedPatientId);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Patient");
        alert.setHeaderText("Edit Patient Feature");
        alert.setContentText("Edit patient functionality will be implemented here.\nPatient ID: " + selectedPatientId);
        alert.showAndWait();

        // TODO: Implement edit patient popup or navigation
    }

    @FXML
    public void handleDischargePatient() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Discharge Patient");
        confirm.setHeaderText("Confirm Patient Discharge");
        confirm.setContentText("Are you sure you want to discharge this patient?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            patientDAO.dischargePatient(selectedPatientId, selectedUnitId);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Patient Discharged");
            success.setContentText("Patient has been successfully discharged.");
            success.showAndWait();

            handleCloseDetail();
            loadPatients();
        }
    }

    // ================= ADD PATIENT POPUP =================

    @FXML
    public void showAddPatientPopup() {
        if (addPatientBackdrop != null) {
            addPatientBackdrop.setVisible(true);
            addPatientBackdrop.setManaged(true);
        }

        if (addPatientPopup != null) {
            addPatientPopup.setVisible(true);
            addPatientPopup.setManaged(true);
            addPatientPopup.toFront();

            FadeTransition ft = new FadeTransition(Duration.millis(180), addPatientPopup);
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
        // Validation
        if (addPatientName.getText().isEmpty() ||
                addPatientAge.getText().isEmpty() ||
                addPatientGender.getValue() == null ||
                addPatientSeverity.getValue() == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText("Cannot Add Patient");
            alert.setContentText("Please fill in all required fields.");
            alert.showAndWait();
            return;
        }

        try {
            String name = addPatientName.getText();
            int age = Integer.parseInt(addPatientAge.getText());
            String gender = addPatientGender.getValue();
            String diagnosis = addPatientDiagnosis.getText();
            String severity = addPatientSeverity.getValue();

            patientDAO.addPatientAutoAssign(name, age, gender, diagnosis, severity);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Patient Added");
            success.setContentText("Patient has been successfully admitted.");
            success.showAndWait();

            hideAddPatientPopup();
            loadPatients();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Invalid Age");
            alert.setContentText("Please enter a valid age number.");
            alert.showAndWait();
        }
    }

    private void clearAddPatientForm() {
        if (addPatientName != null) addPatientName.clear();
        if (addPatientAge != null) addPatientAge.clear();
        if (addPatientDiagnosis != null) addPatientDiagnosis.clear();
        if (addPatientGender != null) addPatientGender.setValue(null);
        if (addPatientSeverity != null) addPatientSeverity.setValue(null);
    }

    // ================= HELPER =================

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? "" :
                s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}