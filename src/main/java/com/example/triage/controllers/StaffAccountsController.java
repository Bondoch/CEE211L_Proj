package com.example.triage.controllers;

import com.example.triage.database.*;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class StaffAccountsController {

    /* ================= TABLE ================= */
    @FXML private VBox staffRows;
    @FXML private Button editStaffBtn;

    /* ================= ON SHIFT ================= */
    @FXML private FlowPane onShiftContainer;

    /* ================= POPUP LAYER ================= */
    @FXML private StackPane staffStack;
    @FXML private Pane staffBackdrop;

    @FXML private VBox addStaffPopup;
    @FXML private VBox editStaffPopup;
    @FXML private VBox deleteConfirmPopup;

    /* ================= ADD FIELDS ================= */
    @FXML private TextField addNameField;
    @FXML private ComboBox<String> addRoleBox;
    @FXML private ComboBox<String> addFacilityBox;
    @FXML private ComboBox<String> addFloorBox;

    /* ================= EDIT FIELDS ================= */
    @FXML private ComboBox<String> editRoleBox;
    @FXML private ComboBox<String> editFacilityBox;
    @FXML private ComboBox<String> editFloorBox;

    /* ================= STATE ================= */
    private final List<Staff> staffList = new ArrayList<>();
    private final List<CheckBox> rowSelectors = new ArrayList<>();

    private final StaffDAO staffDAO = new StaffDAO();
    private final FacilityDAO facilityDAO = new FacilityDAO();
    private final FloorDAO floorDAO = new FloorDAO();
    private final UserDAO userDAO = new UserDAO();

    /* ================= INIT ================= */
    public void initialize() {
        System.out.println("📋 StaffAccountsController initialized");

        editStaffBtn.setDisable(true);

        addRoleBox.getItems().addAll("Doctor", "Nurse", "Technician", "Staff");
        editRoleBox.getItems().addAll(addRoleBox.getItems());

        // Load facilities
        List<String> facilities = facilityDAO.getAllFacilities();
        System.out.println("🏥 Loaded " + facilities.size() + " facilities: " + facilities);

        addFacilityBox.getItems().setAll(facilities);
        editFacilityBox.getItems().setAll(facilities);

        addFacilityBox.setOnAction(e -> {
            String facility = addFacilityBox.getValue();
            if (facility == null) return;

            int facilityId = facilityDAO.getFacilityIdByName(facility);
            List<String> floors = floorDAO.getFloorsByFacility(facilityId);
            System.out.println("🏢 Loaded " + floors.size() + " floors for " + facility);

            addFloorBox.getItems().setAll(floors);
        });

        editFacilityBox.setOnAction(e -> {
            String facility = editFacilityBox.getValue();
            if (facility == null) return;

            int facilityId = facilityDAO.getFacilityIdByName(facility);
            editFloorBox.getItems().setAll(
                    floorDAO.getFloorsByFacility(facilityId)
            );
        });

        hideAllPopups();
        loadStaffFromDatabase();
    }

    /* ================= DB LOAD ================= */
    private void loadStaffFromDatabase() {
        System.out.println("🔄 Loading staff from database...");
        staffList.clear();
        staffList.addAll(staffDAO.getAllStaff());
        System.out.println("✅ Loaded " + staffList.size() + " staff members");
        refreshTable();
    }

    /* ================= TABLE ================= */
    private void refreshTable() {
        staffRows.getChildren().clear();
        rowSelectors.clear();
        onShiftContainer.getChildren().clear();

        for (Staff staff : staffList) {
            CheckBox cb = new CheckBox();
            rowSelectors.add(cb);
            cb.selectedProperty().addListener((o, a, b) -> updateEditButton());

            staffRows.getChildren().add(createStaffRow(staff, cb));

            if (staff.isOnShift()) {
                onShiftContainer.getChildren().add(createOnShiftCard(staff));
            }
        }

        editStaffBtn.setDisable(true);
    }

    private GridPane createStaffRow(Staff staff, CheckBox cb) {
        GridPane row = new GridPane();
        row.setPrefWidth(Double.MAX_VALUE);

        ColumnConstraints c0 = new ColumnConstraints(50);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        ColumnConstraints c3 = new ColumnConstraints();
        ColumnConstraints c4 = new ColumnConstraints();

        c1.setPercentWidth(42);
        c2.setPercentWidth(18);
        c3.setPercentWidth(22);
        c4.setPercentWidth(10);

        row.getColumnConstraints().addAll(c0, c1, c2, c3, c4);

        row.add(cell(cb, true), 0, 0);
        row.add(cell(new Label(staff.getName()), false), 1, 0);
        row.add(cell(new Label(staff.getRole()), false), 2, 0);
        row.add(cell(new Label(staff.getFacility()), false), 3, 0);
        row.add(cell(new Label(staff.isOnShift() ? "Yes" : "No"), false), 4, 0);

        return row;
    }

    private StackPane cell(Node n, boolean center) {
        StackPane p = new StackPane(n);
        p.setMinHeight(44);
        p.setStyle("-fx-padding:10;-fx-border-color:#e0e0e0;-fx-border-width:0 1 0 0;");
        if (!center) StackPane.setAlignment(n, javafx.geometry.Pos.CENTER_LEFT);
        return p;
    }

    private VBox createOnShiftCard(Staff staff) {
        VBox v = new VBox(
                new Label(staff.getName()),
                new Label(staff.getRole() + " • " + staff.getFacility())
        );
        v.setSpacing(4);
        v.setStyle("""
            -fx-background-color:white;
            -fx-padding:12;
            -fx-background-radius:10;
            -fx-border-color:#2ca3fa;
            -fx-border-radius:10;
        """);
        return v;
    }

    /* ================= SELECTION ================= */
    private void updateEditButton() {
        editStaffBtn.setDisable(
                rowSelectors.stream().filter(CheckBox::isSelected).count() != 1
        );
    }

    private int selectedIndex() {
        for (int i = 0; i < rowSelectors.size(); i++)
            if (rowSelectors.get(i).isSelected()) return i;
        return -1;
    }

    /* ================= POPUPS ================= */
    private void showPopup(VBox popup) {
        System.out.println("📤 Showing popup: " + popup.getId());

        staffBackdrop.setVisible(true);
        staffBackdrop.setManaged(true);
        staffBackdrop.toFront();

        popup.setVisible(true);
        popup.setManaged(true);
        popup.toFront();

        staffStack.setOnMouseClicked(e -> hideAllPopups());
        popup.setOnMouseClicked(e -> e.consume());

        FadeTransition fade = new FadeTransition(Duration.millis(200), popup);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    private void hideAllPopups() {
        System.out.println("📥 Hiding all popups");

        staffBackdrop.setVisible(false);
        staffBackdrop.setManaged(false);

        addStaffPopup.setVisible(false);
        addStaffPopup.setManaged(false);

        editStaffPopup.setVisible(false);
        editStaffPopup.setManaged(false);

        deleteConfirmPopup.setVisible(false);
        deleteConfirmPopup.setManaged(false);

        staffStack.setOnMouseClicked(null);
    }

    /* ================= ADD ================= */
    @FXML
    private void showAddStaffPopup() {
        System.out.println("➕ Add Staff button clicked");
        showPopup(addStaffPopup);
    }

    @FXML
    private void confirmAddStaff() {
        System.out.println("💾 Confirm Add Staff clicked");

        String name = addNameField.getText();
        String role = addRoleBox.getValue();
        String facility = addFacilityBox.getValue();
        String floor = addFloorBox.getValue();

        System.out.println("📝 Input Values:");
        System.out.println("  Name: " + name);
        System.out.println("  Role: " + role);
        System.out.println("  Facility: " + facility);
        System.out.println("  Floor: " + floor);

        // ✅ Guard clause FIRST
        if (name == null || name.isBlank()
                || role == null
                || facility == null
                || floor == null) {
            System.err.println("❌ Validation failed - missing fields!");

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText("Cannot Add Staff");
            alert.setContentText("Please fill in all fields:\n" +
                    "- Name: " + (name == null || name.isBlank() ? "❌ Missing" : "✅") + "\n" +
                    "- Role: " + (role == null ? "❌ Missing" : "✅") + "\n" +
                    "- Facility: " + (facility == null ? "❌ Missing" : "✅") + "\n" +
                    "- Floor: " + (floor == null ? "❌ Missing" : "✅"));
            alert.showAndWait();
            return;
        }

        try {
            int facilityId = facilityDAO.getFacilityIdByName(facility);
            int floorNumber = Integer.parseInt(floor.replace("Floor ", ""));
            int floorId = floorDAO.getFloorId(facilityId, floorNumber);

            System.out.println("🔍 Database IDs:");
            System.out.println("  Facility ID: " + facilityId);
            System.out.println("  Floor Number: " + floorNumber);
            System.out.println("  Floor ID: " + floorId);

            // === INSERT STAFF ===
            System.out.println("📥 Inserting staff into database...");
            staffDAO.addStaff(name, role, facilityId, floorId);
            System.out.println("✅ Staff inserted successfully");

            // === CREATE USER ACCOUNT ===
            String[] parts = name.trim().split("\\s+");
            String baseUsername = parts[parts.length - 1].toLowerCase();
            String username = baseUsername + (System.currentTimeMillis() % 10000);
            String password = role.toLowerCase() + "123";
            String userRole = role.equalsIgnoreCase("Doctor") ? "admin" : "user";

            System.out.println("👤 Creating user account:");
            System.out.println("  Username: " + username);
            System.out.println("  Password: " + password);
            System.out.println("  Role: " + userRole);

            userDAO.createUser(username, password, userRole);
            System.out.println("✅ User account created successfully");

            // === SUCCESS ALERT ===
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Staff Added Successfully");
            success.setContentText("Staff member: " + name + "\n" +
                    "Username: " + username + "\n" +
                    "Password: " + password + "\n" +
                    "Role: " + userRole);
            success.showAndWait();

            // === RELOAD FROM DATABASE ===
            loadStaffFromDatabase();
            hideAllPopups();

            // Clear form
            addNameField.clear();
            addRoleBox.setValue(null);
            addFacilityBox.setValue(null);
            addFloorBox.setValue(null);

        } catch (Exception e) {
            System.err.println("❌ Error adding staff:");
            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Failed to Add Staff");
            error.setContentText("Error: " + e.getMessage());
            error.showAndWait();
        }
    }

    /* ================= EDIT ================= */
    @FXML
    private void handleEditStaff() {
        int i = selectedIndex();
        if (i == -1) return;

        Staff staff = staffList.get(i);
        editRoleBox.setValue(staff.getRole());

        if (staff.getFacility().contains(" • ")) {
            String[] parts = staff.getFacility().split(" • ");
            editFacilityBox.setValue(parts[0]);
            editFloorBox.setValue(parts[1]);
        }

        showPopup(editStaffPopup);
    }

    @FXML
    private void confirmEditStaff() {
        int index = selectedIndex();
        if (index == -1) return;

        Staff staff = staffList.get(index);

        int facilityId = facilityDAO.getFacilityIdByName(editFacilityBox.getValue());
        int floorNumber = Integer.parseInt(editFloorBox.getValue().replace("Floor ", ""));
        int floorId = floorDAO.getFloorId(facilityId, floorNumber);

        staffDAO.updateStaff(staff.getId(), editRoleBox.getValue(), facilityId, floorId);

        loadStaffFromDatabase();
        hideAllPopups();
    }

    /* ================= DELETE ================= */
    @FXML
    private void showDeleteConfirmPopup() {
        if (rowSelectors.stream().noneMatch(CheckBox::isSelected)) return;
        showPopup(deleteConfirmPopup);
    }

    @FXML
    private void confirmDeleteStaff() {
        for (int i = rowSelectors.size() - 1; i >= 0; i--) {
            if (rowSelectors.get(i).isSelected()) {
                staffDAO.deleteStaff(staffList.get(i).getId());
            }
        }

        loadStaffFromDatabase();
        hideAllPopups();
    }
}