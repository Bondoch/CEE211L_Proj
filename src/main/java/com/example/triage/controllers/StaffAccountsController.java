package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import com.example.triage.database.Staff;

public class StaffAccountsController {

    @FXML private VBox staffRows;

    @FXML
    public void initialize() {

        staffRows.getChildren().add(
                createStaffRow(new Staff("John Doe", "Nurse", "ER", true))
        );

        staffRows.getChildren().add(
                createStaffRow(new Staff("Maria Reyes", "Doctor", "Ward A", false))
        );
    }

    private GridPane createStaffRow(Staff staff) {

        GridPane row = new GridPane();
        row.getStyleClass().add("table-row");
        row.setHgap(10);

        ColumnConstraints selectCol = new ColumnConstraints(40);
        ColumnConstraints nameCol = new ColumnConstraints();
        ColumnConstraints roleCol = new ColumnConstraints();
        ColumnConstraints facilityCol = new ColumnConstraints();
        ColumnConstraints shiftCol = new ColumnConstraints();

        nameCol.setPercentWidth(30);
        roleCol.setPercentWidth(20);
        facilityCol.setPercentWidth(30);
        shiftCol.setPercentWidth(20);

        row.getColumnConstraints().addAll(
                selectCol, nameCol, roleCol, facilityCol, shiftCol
        );

        CheckBox select = new CheckBox();
        Label name = new Label(staff.getName());
        Label role = new Label(staff.getRole());
        Label facility = new Label(staff.getFacility());
        Label shift = new Label(staff.isOnShift() ? "Yes" : "No");

        row.add(select, 0, 0);
        row.add(name, 1, 0);
        row.add(role, 2, 0);
        row.add(facility, 3, 0);
        row.add(shift, 4, 0);

        return row;
    }
}
