package com.example.triage.controllers;

import com.example.triage.database.FacilityDAO;
import com.example.triage.database.FloorDAO;
import com.example.triage.database.Patient;
import com.example.triage.database.PatientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class ReferralPopupController {

    @FXML private ComboBox<String> facilityBox;
    @FXML private ComboBox<String> floorBox;
    @FXML private Label errorLabel;

    private Patient patient;
    private Runnable onDone;

    private final FacilityDAO facilityDAO = new FacilityDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    public void init(Patient p, Runnable onDone) {
        this.patient = p;
        this.onDone = onDone;

        facilityBox.getItems().setAll(facilityDAO.getAllFacilities());

        facilityBox.setOnAction(e -> {
            // hide error once user interacts
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            int fid = facilityDAO.getFacilityIdByName(facilityBox.getValue());
            floorBox.getItems().setAll(floorDAO.getFloorsByFacility(fid));
        });

        floorBox.setOnAction(e -> {
            // hide error once user selects floor
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });
    }
    @FXML
    private void submit() {

        if (facilityBox.getValue() == null || floorBox.getValue() == null) {
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        patientDAO.requestReferral(
                patient.getId(),
                facilityBox.getValue(),
                floorBox.getValue()
        );

        if (onDone != null) {
            onDone.run();
        }
    }

    @FXML
    private void cancel() {
        if (onDone != null) {
            onDone.run();
        }
    }
}


