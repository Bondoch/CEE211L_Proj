package com.example.triage.controllers;

import com.example.triage.services.PermissionService;
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
import com.example.triage.database.SortMode;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientsController {
    @FXML private Pane dischargeBackdrop;
    @FXML private VBox dischargePopup;
    @FXML private Pane addPatientBackdrop;
    @FXML private VBox addPatientPopup;
    @FXML private TextField addPatientName;
    @FXML private TextField addPatientAge;
    @FXML private TextField addPatientDiagnosis;
    @FXML private ComboBox<String> addPatientGender;
    @FXML private ComboBox<String> addPatientSeverity;



    // ===== LEFT PANEL =====
    @FXML private ComboBox<String> facilityCombo;
    @FXML private ComboBox<String> floorCombo;
    @FXML private Label totalPatientsLabel;
    @FXML private Label criticalPatientsLabel;

    @FXML private CheckBox severityToggle;
    @FXML private ComboBox<String> severityBox;
    @FXML private ToggleButton sortByAdmissionBtn;
    @FXML private ToggleButton sortByBedBtn;
    @FXML private TextField searchField;

    // ===== CENTER =====
    @FXML private FlowPane patientGrid;
    @FXML private VBox emptyState;
    @FXML private Label areaLabel;

    // ===== DETAIL =====
    @FXML private Pane detailBackdrop;
    @FXML private VBox patientDetailCard;
    @FXML private Label detailName, detailAge, detailGender, detailSeverity, detailBed, detailAdmission;

    // ===== EDIT =====
    @FXML private Pane editBackdrop;
    @FXML private VBox editPatientPopup;
    @FXML private TextField editDiagnosis;
    @FXML private ComboBox<String> editSeverity;
    @FXML private ComboBox<String> editFloor;
    @FXML private ComboBox<String> editUnit;

    private final PatientDAO patientDAO = new PatientDAO();
    private final FacilityDAO facilityDAO = new FacilityDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    private int selectedPatientId = -1;
    private int selectedUnitId = -1;

    private SortMode currentSort = SortMode.NONE;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");


    @FXML
    public void initialize() {

        facilityCombo.getItems().setAll(facilityDAO.getAllFacilities());

        facilityCombo.setOnAction(e -> {
            int id = facilityDAO.getFacilityIdByName(facilityCombo.getValue());
            floorCombo.getItems().setAll(floorDAO.getFloorsByFacility(id));
            floorCombo.setDisable(false);
        });

        floorCombo.setOnAction(e -> loadPatientsUnified());

        severityBox.getItems().addAll("Moderate", "High", "Critical");
        severityToggle.selectedProperty().addListener((a,b,c)->loadPatientsUnified());
        severityBox.setOnAction(e -> loadPatientsUnified());

        searchField.textProperty().addListener((a,b,c)->loadPatientsUnified());

        sortByAdmissionBtn.setOnAction(e -> {
            sortByBedBtn.setSelected(false);
            currentSort = sortByAdmissionBtn.isSelected()
                    ? SortMode.ADMISSION_DATE
                    : SortMode.NONE;
            loadPatientsUnified();
        });

        sortByBedBtn.setOnAction(e -> {
            sortByAdmissionBtn.setSelected(false);
            currentSort = sortByBedBtn.isSelected()
                    ? SortMode.BED_ROOM
                    : SortMode.NONE;
            loadPatientsUnified();
        });

        // ===== ADD PATIENT POPUP COMBOS =====
        addPatientGender.getItems().setAll(
                "Male",
                "Female",
                "Other"
        );

        addPatientSeverity.getItems().setAll(
                "Moderate",
                "High",
                "Critical"
        );

    }

    private void loadPatientsUnified() {

        patientGrid.getChildren().clear();

        String facility = facilityCombo.getValue();
        Integer floor = floorCombo.getValue() == null
                ? null
                : Integer.parseInt(floorCombo.getValue().replaceAll("\\D+",""));

        String severity = severityToggle.isSelected() ? severityBox.getValue() : null;
        String search = searchField.getText().isBlank() ? null : searchField.getText();

        List<Patient> patients = patientDAO.getPatientsFiltered(
                facility, floor, severity, search, currentSort
        );

        int critical = 0;

        for (Patient p : patients) {
            if ("critical".equalsIgnoreCase(p.getSeverity())) critical++;
            patientGrid.getChildren().add(createPatientCard(p));
        }

        totalPatientsLabel.setText(String.valueOf(patients.size()));
        criticalPatientsLabel.setText(String.valueOf(critical));

        emptyState.setVisible(patients.isEmpty());
    }

    private VBox createPatientCard(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/triage/views/patient-card.fxml")
            );
            VBox card = loader.load();
            PatientCardController controller = loader.getController();

            controller.setPatient(patient, searchField.getText(), () ->
                    showPatientDetails(patient)
            );

            return card;

        } catch (IOException e) {
            return new VBox();
        }
    }

    private void showPatientDetails(Patient p) {

        selectedPatientId = p.getId();
        selectedUnitId = p.getUnitId();

        detailName.setText(p.getFullName());
        detailAge.setText(String.valueOf(p.getAge()));
        detailGender.setText(p.getGender());
        detailSeverity.setText(p.getSeverity());
        detailBed.setText(p.getUnitLabel());
        detailAdmission.setText(
                p.getAdmissionDate().toLocalDateTime().format(FORMATTER)
        );

        detailBackdrop.setVisible(true);
        detailBackdrop.setManaged(true);
        patientDetailCard.setVisible(true);
        patientDetailCard.setManaged(true);
    }

    // ===== EDIT =====

    @FXML
    public void handleEditPatient() {


        if (!PermissionService.canEditSeverity()) {
            showPermissionDenied("You are not allowed to edit patient details.");
            return;
        }


        editDiagnosis.setText(
                patientDAO.getDiagnosisByPatientId(selectedPatientId)
        );

        editSeverity.getItems().setAll("Moderate", "High", "Critical");
        editSeverity.setValue(detailSeverity.getText());

        boolean inpatient = detailBed.getText().toLowerCase().contains("room");
        editFloor.setDisable(!inpatient);
        editUnit.setDisable(!inpatient);

        if (inpatient) {
            int fid = facilityDAO.getFacilityIdByName(facilityCombo.getValue());
            editFloor.getItems().setAll(floorDAO.getFloorsByFacility(fid));

            editFloor.setOnAction(e -> {
                int f = Integer.parseInt(editFloor.getValue().replaceAll("\\D+",""));
                editUnit.getItems().setAll(
                        patientDAO.getAvailableUnits(facilityCombo.getValue(), f)
                );
            });
        }

        editBackdrop.setVisible(true);
        editBackdrop.setManaged(true);
        editPatientPopup.setVisible(true);
        editPatientPopup.setManaged(true);
    }

    @FXML
    public void confirmEdit() {

        String fromType =
                facilityDAO.getFacilityTypeByUnitId(selectedUnitId);

        String toType = fromType;

// Only check destination if a transfer is happening
        if (!editUnit.isDisabled() && editUnit.getValue() != null) {
            toType =
                    facilityDAO.getFacilityTypeByUnitId(
                            patientDAO.getUnitIdByLabel(editUnit.getValue())
                    );
        }

        if (!PermissionService.canTransfer(fromType, toType)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Transfer Restricted");
            alert.setHeaderText("Action Not Allowed");
            alert.setContentText(
                    "You are only allowed to transfer patients between inpatient wards."
            );
            alert.showAndWait();
            return;
        }


        patientDAO.updatePatientEdit(
                selectedPatientId,
                selectedUnitId,
                editDiagnosis.getText(),
                editSeverity.getValue(),
                editUnit.isDisabled() ? null : editUnit.getValue()
        );

        editBackdrop.setVisible(false);
        editPatientPopup.setVisible(false);
        detailBackdrop.setVisible(false);
        patientDetailCard.setVisible(false);

        loadPatientsUnified();
    }

    @FXML
    public void cancelEdit() {
        editPatientPopup.setVisible(false);
        editPatientPopup.setManaged(false);
        editBackdrop.setVisible(false);
        editBackdrop.setManaged(false);
    }

    public void handleCloseDetail() {
        patientDetailCard.setVisible(false);
        patientDetailCard.setManaged(false);
        detailBackdrop.setVisible(false);
        detailBackdrop.setManaged(false);
    }

    @FXML
    public void handleDischargePatient() {

        if (!PermissionService.canDischargePatient()) {
            showPermissionDenied("You are not allowed to discharge patients.");
            return;
        }


        dischargeBackdrop.setVisible(true);
        dischargeBackdrop.setManaged(true);
        dischargePopup.setVisible(true);
        dischargePopup.setManaged(true);
    }

    @FXML
    public void cancelDischarge() {
        dischargeBackdrop.setVisible(false);
        dischargeBackdrop.setManaged(false);
        dischargePopup.setVisible(false);
        dischargePopup.setManaged(false);
    }

    @FXML
    public void confirmDischarge() {
        patientDAO.dischargePatient(selectedPatientId, selectedUnitId);
        cancelDischarge();
        handleCloseDetail();
        loadPatientsUnified();
    }

    @FXML
    public void showAddPatientPopup() {

        if (!PermissionService.canAddPatient()) {
            showPermissionDenied("You are not allowed to add patients.");
            return;
        }

        addPatientBackdrop.setVisible(true);
        addPatientBackdrop.setManaged(true);
        addPatientPopup.setVisible(true);
        addPatientPopup.setManaged(true);
    }

    @FXML
    public void hideAddPatientPopup() {
        addPatientBackdrop.setVisible(false);
        addPatientBackdrop.setManaged(false);
        addPatientPopup.setVisible(false);
        addPatientPopup.setManaged(false);
    }

    @FXML
    public void confirmAddPatient() {

        if (addPatientName.getText().isBlank()
                || addPatientAge.getText().isBlank()
                || addPatientGender.getValue() == null
                || addPatientSeverity.getValue() == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Form");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please fill in all required fields.");
            alert.showAndWait();
            return;
        }

        patientDAO.addPatientAutoAssign(
                addPatientName.getText(),
                Integer.parseInt(addPatientAge.getText()),
                addPatientGender.getValue(),
                addPatientDiagnosis.getText(),
                addPatientSeverity.getValue()
        );

        hideAddPatientPopup();
        loadPatientsUnified();
    }


    private void showPermissionDenied(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Permission Denied");
        alert.setHeaderText("Action Not Allowed");
        alert.setContentText(message);
        alert.showAndWait();
    }


}
