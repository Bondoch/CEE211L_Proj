package com.example.triage.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // ===== SEARCH + FILTER =====
    public List<Patient> getPatientsFiltered(
            String facility,
            Integer floor,
            String severity,
            String search,
            SortMode sort
    ) {

        List<Patient> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT p.*, u.id AS unit_id, u.label AS unit_label,
                   fac.name AS facility_name, f.floor_number
            FROM patients p
            JOIN units u ON p.unit_id=u.id
            JOIN floors f ON u.floor_id=f.id
            JOIN facilities fac ON f.facility_id=fac.id
            WHERE p.status='admitted'
        """);

        if (facility != null) sql.append(" AND fac.name=?");
        if (floor != null) sql.append(" AND f.floor_number=?");
        if (severity != null) sql.append(" AND p.severity=?");
        if (search != null)
            sql.append(" AND (LOWER(p.full_name) LIKE ? OR LOWER(p.patient_code) LIKE ?)");

        if (sort == SortMode.ADMISSION_DATE)
            sql.append(" ORDER BY p.admission_date DESC");
        else if (sort == SortMode.BED_ROOM)
            sql.append(" ORDER BY u.label");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int i = 1;
            if (facility != null) ps.setString(i++, facility);
            if (floor != null) ps.setInt(i++, floor);
            if (severity != null) ps.setString(i++, severity.toLowerCase());
            if (search != null) {
                String s = "%" + search.toLowerCase() + "%";
                ps.setString(i++, s);
                ps.setString(i++, s);
            }

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
                        rs.getString("unit_label"),
                        rs.getString("facility_name"),
                        rs.getInt("floor_number")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void dischargePatient(int patientId, int unitId) {

        String deletePatient = "DELETE FROM patients WHERE id = ?";
        String freeUnit = "UPDATE units SET status = 'AVAILABLE' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(deletePatient)) {
                ps.setInt(1, patientId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(freeUnit)) {
                ps.setInt(1, unitId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ===== EDIT =====
    public String getDiagnosisByPatientId(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT diagnosis FROM patients WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : "";
        } catch (SQLException e) {
            return "";
        }
    }

    public List<String> getAvailableUnits(String facility, int floor) {
        List<String> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                SELECT u.label FROM units u
                JOIN floors f ON u.floor_id=f.id
                JOIN facilities fac ON f.facility_id=fac.id
                WHERE u.status='AVAILABLE'
                  AND fac.name=?
                  AND f.floor_number=?
             """)) {
            ps.setString(1, facility);
            ps.setInt(2, floor);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException ignored) {}
        return list;
    }

    public int getUnitIdByLabel(String label) {
        String sql = "SELECT id FROM units WHERE label = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, label);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void updatePatientEdit(
            int pid, int oldUnit,
            String diagnosis, String severity,
            String newUnit
    ) {

        try (Connection c = DBConnection.getConnection()) {

            if (newUnit != null) {
                int newId = getUnitIdByLabel(newUnit);
                c.prepareStatement("UPDATE units SET status='AVAILABLE' WHERE id="+oldUnit).execute();
                c.prepareStatement("UPDATE units SET status='OCCUPIED' WHERE id="+newId).execute();
                c.prepareStatement("UPDATE patients SET unit_id="+newId+" WHERE id="+pid).execute();
            }

            PreparedStatement ps = c.prepareStatement(
                    "UPDATE patients SET diagnosis=?, severity=? WHERE id=?"
            );
            ps.setString(1, diagnosis);
            ps.setString(2, severity.toLowerCase());
            ps.setInt(3, pid);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addPatientAutoAssign(
            String fullName,
            int age,
            String gender,
            String diagnosis,
            String severity
    ) {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("""
            INSERT INTO patients (full_name, age, gender, diagnosis, severity, admission_date, status)
            VALUES (?, ?, ?, ?, ?, NOW(), 'admitted')
        """);
            ps.setString(1, fullName);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, diagnosis);
            ps.setString(5, severity.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
