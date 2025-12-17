package com.example.triage.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.util.*;
import com.example.triage.database.DBConnection;

public class FacilitiesController {

    /* ================================
       FXML REFERENCES
       ================================ */
    @FXML private ComboBox<String> facilitySelector;
    @FXML private ComboBox<Integer> floorSelector;

    @FXML private FlowPane unitGrid;
    @FXML private VBox emptyState;

    @FXML private Label facilityTitle;
    @FXML private Label availableCount;
    @FXML private Label occupiedCount;

    @FXML private Button editFacilityBtn;
    @FXML private VBox editFacilityPanel;
    @FXML private ComboBox<String> facilityStatusBox;

    /* ADD FACILITY */
    @FXML private VBox addFacilityPanel;
    @FXML private TextField addFacilityName;
    @FXML private ComboBox<String> addFacilityType;
    @FXML private Spinner<Integer> addFacilityFloors;

    /* REMOVE FACILITY */
    @FXML private VBox removeFacilityPanel;
    @FXML private ComboBox<String> removeFacilitySelector;
    @FXML private ComboBox<Integer> removeFloorSelector;

    /* ================================
       STATE
       ================================ */
    private boolean editMode = false;

    /* ================================
       INITIALIZATION
       ================================ */
    @FXML
    public void initialize() {
        setupEditMode();
        setupAddRemovePanels();
        loadFacilities();
        setupSelectors();
        clearView();
    }

    /* ================================
       FACILITY / FLOOR LOADING
       ================================ */
    private void loadFacilities() {
        facilitySelector.getItems().clear();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("SELECT name FROM facilities ORDER BY name");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                facilitySelector.getItems().add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFloors(String facilityName) {
        floorSelector.getItems().clear();

        String sql = """
                SELECT f.floor_number
                FROM floors f
                JOIN facilities fac ON fac.id = f.facility_id
                WHERE fac.name = ?
                ORDER BY f.floor_number
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, facilityName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                floorSelector.getItems().add(rs.getInt("floor_number"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ================================
       DROPDOWN LOGIC
       ================================ */
    private void setupSelectors() {
        facilitySelector.setOnAction(e -> {
            String facility = facilitySelector.getValue();
            floorSelector.getItems().clear();
            if (facility != null) loadFloors(facility);
            clearView();
        });

        floorSelector.setOnAction(e -> renderUnits());
    }

    /* ================================
       UNIT RENDERING
       ================================ */
    private void renderUnits() {
        unitGrid.getChildren().clear();

        String facility = facilitySelector.getValue();
        Integer floor = floorSelector.getValue();

        if (facility == null || floor == null) {
            clearView();
            return;
        }

        emptyState.setVisible(false);
        facilityTitle.setText(facility + " - Floor " + floor);

        int available = 0;
        int occupied = 0;

        String sql = """
                SELECT u.id, u.label, u.status
                FROM units u
                JOIN floors f ON f.id = u.floor_id
                JOIN facilities fac ON fac.id = f.facility_id
                WHERE fac.name = ? AND f.floor_number = ?
                ORDER BY
                    CASE
                        WHEN u.label LIKE 'Bed %' THEN 1
                        WHEN u.label LIKE 'Room %' THEN 2
                        ELSE 3
                    END,
                    CAST(SUBSTRING(u.label, LOCATE(' ', u.label) + 1) AS UNSIGNED)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, facility);
            ps.setInt(2, floor);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String label = rs.getString("label");
                String status = rs.getString("status");

                unitGrid.getChildren().add(createUnitBox(id, label, status));

                if ("AVAILABLE".equals(status)) available++;
                if ("OCCUPIED".equals(status)) occupied++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        availableCount.setText(String.valueOf(available));
        occupiedCount.setText(String.valueOf(occupied));
    }

    private StackPane createUnitBox(int id, String labelText, String status) {
        Label label = new Label(labelText);
        label.setStyle("""
            -fx-font-weight: bold;
            -fx-text-fill: #034c81;
        """);

        StackPane box = new StackPane(label);
        box.setPrefSize(120, 80);
        box.setStyle(getUnitStyle(status));

        box.setOnMouseClicked(e -> {
            if (editMode && facilityStatusBox.getValue() != null) {
                updateUnitStatus(id, facilityStatusBox.getValue().toUpperCase());
                renderUnits();
            }
        });

        return box;
    }

    private String getUnitStyle(String status) {
        return switch (status) {
            case "AVAILABLE" ->
                    "-fx-background-color:#e8f5e9;-fx-border-color:#4caf50;-fx-border-radius:8;";
            case "OCCUPIED" ->
                    "-fx-background-color:#ffebee;-fx-border-color:#f44336;-fx-border-radius:8;";
            default ->
                    "-fx-background-color:#eeeeee;-fx-border-color:#9e9e9e;-fx-border-radius:8;";
        };
    }

    private void updateUnitStatus(int id, String status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("UPDATE units SET status=? WHERE id=?")) {

            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ================================
       EDIT MODE
       ================================ */
    private void setupEditMode() {
        facilityStatusBox.getItems().addAll("Available", "Occupied", "Unavailable");
        editFacilityPanel.setVisible(false);
        editFacilityPanel.setManaged(false);
    }

    @FXML private void handleEditFacility() {
        editMode = true;
        editFacilityPanel.setVisible(true);
        editFacilityPanel.setManaged(true);
    }

    @FXML private void handleDoneEdit() {
        editMode = false;
        editFacilityPanel.setVisible(false);
        editFacilityPanel.setManaged(false);
    }

    /* ================================
       ADD / REMOVE FACILITY
       ================================ */
    private void setupAddRemovePanels() {
        addFacilityPanel.setVisible(false);
        addFacilityPanel.setManaged(false);

        removeFacilityPanel.setVisible(false);
        removeFacilityPanel.setManaged(false);

        // UPDATED TYPES
        addFacilityType.getItems().addAll("ER", "WARD", "ICU", "OR");

        addFacilityFloors.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1)
        );
    }

    @FXML
    private void handleAddFacility() {
        addFacilityPanel.setVisible(true);
        addFacilityPanel.setManaged(true);
        removeFacilityPanel.setVisible(false);
        removeFacilityPanel.setManaged(false);
    }

    @FXML
    private void confirmAddFacility() {

        String name = addFacilityName.getText();
        String type = addFacilityType.getValue();
        int floors = addFacilityFloors.getValue();

        if (name == null || name.isBlank() || type == null) return;

        int bedCount = 0;
        int roomCount = 0;

        System.out.println("Creating facility type: " + type);

        switch (type) {
            case "ER" -> bedCount = 10;
            case "WARD", "ICU" -> {
                bedCount = 10;
                roomCount = 4;
            }
            case "OR" -> roomCount = 4;
        }

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement facPS =
                    conn.prepareStatement(
                            "INSERT INTO facilities (name, type) VALUES (?, ?)",
                            Statement.RETURN_GENERATED_KEYS);

            facPS.setString(1, name);
            facPS.setString(2, type);
            facPS.executeUpdate();

            ResultSet facKeys = facPS.getGeneratedKeys();
            if (!facKeys.next()) return;

            int facilityId = facKeys.getInt(1);

            for (int floor = 1; floor <= floors; floor++) {

                PreparedStatement floorPS =
                        conn.prepareStatement(
                                "INSERT INTO floors (facility_id, floor_number) VALUES (?, ?)",
                                Statement.RETURN_GENERATED_KEYS);

                floorPS.setInt(1, facilityId);
                floorPS.setInt(2, floor);
                floorPS.executeUpdate();

                ResultSet floorKeys = floorPS.getGeneratedKeys();
                if (!floorKeys.next()) continue;

                int floorId = floorKeys.getInt(1);

                for (int b = 1; b <= bedCount; b++) {
                    conn.prepareStatement(
                                    "INSERT INTO units (floor_id, label, status) VALUES (" +
                                            floorId + ", 'Bed " + b + "', 'AVAILABLE')")
                            .executeUpdate();
                }

                for (int r = 1; r <= roomCount; r++) {
                    conn.prepareStatement(
                                    "INSERT INTO units (floor_id, label, status) VALUES (" +
                                            floorId + ", 'Room " + r + "', 'AVAILABLE')")
                            .executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadFacilities();
        addFacilityPanel.setVisible(false);
        addFacilityPanel.setManaged(false);
    }

    @FXML
    private void handleRemoveFacility() {
        removeFacilityPanel.setVisible(true);
        removeFacilityPanel.setManaged(true);
        addFacilityPanel.setVisible(false);
        addFacilityPanel.setManaged(false);

        removeFacilitySelector.getItems().setAll(facilitySelector.getItems());
    }

    @FXML
    private void confirmRemoveFacility() {
        String facility = removeFacilitySelector.getValue();
        Integer floor = removeFloorSelector.getValue();
        if (facility == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            if (floor == null) {
                conn.prepareStatement(
                        "DELETE FROM facilities WHERE name='" + facility + "'").executeUpdate();
            } else {
                conn.prepareStatement("""
                        DELETE f FROM floors f
                        JOIN facilities fac ON fac.id=f.facility_id
                        WHERE fac.name='""" + facility + "' AND f.floor_number=" + floor)
                        .executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadFacilities();
        clearView();
        removeFacilityPanel.setVisible(false);
        removeFacilityPanel.setManaged(false);
    }

    /* ================================
       HELPERS
       ================================ */
    private void clearView() {
        unitGrid.getChildren().clear();
        emptyState.setVisible(true);
        facilityTitle.setText("Select a facility");
        availableCount.setText("0");
        occupiedCount.setText("0");
    }
}
