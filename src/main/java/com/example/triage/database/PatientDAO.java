package com.example.triage.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // ================= ADD =================
    public void addPatient(
            String fullName,
            int age,
            String gender,
            String diagnosis,
            String severity,
            String facility,
            int floor
    ) {

        try {
            int unitId = findAvailableUnit(facility, floor);

            String sql = """
            INSERT INTO patients
            (patient_code, full_name, age, gender, diagnosis, severity, unit_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'admitted')
        """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, "PT-" + System.currentTimeMillis() % 100000);
                ps.setString(2, fullName);
                ps.setInt(3, age);
                ps.setString(4, gender);
                ps.setString(5, diagnosis);
                ps.setString(6, severity.toLowerCase());
                ps.setInt(7, unitId);

                ps.executeUpdate();
                markUnitOccupied(unitId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // ================= EDIT =================
    public void updatePatient(int id, String name, int age,
                              String gender, String diagnosis, String severity) {

        String sql = """
            UPDATE patients
            SET full_name=?, age=?, gender=?, diagnosis=?, severity=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, diagnosis);
            ps.setString(5, severity.toLowerCase());
            ps.setInt(6, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= FETCH =================
    public List<Patient> getPatients(String facility, int floor) {

        List<Patient> list = new ArrayList<>();

        String sql = """
            SELECT p.*, u.id AS unit_id, u.label AS unit_label
            FROM patients p
            JOIN units u ON p.unit_id = u.id
            JOIN floors f ON u.floor_id = f.id
            JOIN facilities fac ON f.facility_id = fac.id
            WHERE fac.name = ?
              AND f.floor_number = ?
              AND p.status = 'admitted'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, facility);
            ps.setInt(2, floor);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("patient_code"),
                        rs.getString("full_name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("severity"),
                        rs.getTimestamp("admission_date"),
                        rs.getInt("unit_id"),
                        rs.getString("unit_label")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= COMPAT =================
    public List<Patient> getPatientsByFacilityAndFloor(String facility, int floor) {
        return getPatients(facility, floor);
    }

    // ================= DISCHARGE =================
    public void dischargePatient(int patientId, int unitId) {
        try (Connection c = DBConnection.getConnection()) {
            c.prepareStatement(
                    "UPDATE patients SET status='discharged' WHERE id=" + patientId
            ).execute();

            c.prepareStatement(
                    "UPDATE units SET status='AVAILABLE' WHERE id=" + unitId
            ).execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= INTERNAL =================
    private int findAvailableUnit(String facility, int floor) throws SQLException {

        String sql = """
        SELECT u.id
        FROM units u
        JOIN floors f ON u.floor_id = f.id
        JOIN facilities fac ON f.facility_id = fac.id
        WHERE u.status = 'AVAILABLE'
          AND fac.name = ?
          AND f.floor_number = ?
        ORDER BY u.id ASC
        LIMIT 1
    """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, facility);
            ps.setInt(2, floor);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }

        throw new SQLException("No available units for selected location");
    }




    private void markUnitOccupied(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.prepareStatement(
                    "UPDATE units SET status='OCCUPIED' WHERE id=" + id
            ).execute();
        }
    }
}
