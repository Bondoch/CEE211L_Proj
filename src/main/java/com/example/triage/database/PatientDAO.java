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
        SELECT p.*,
               p.referral_status,
               p.referral_facility,
               p.referral_floor,
               u.id AS unit_id,
               u.label AS unit_label,
               fac.name AS facility_name,
               f.floor_number
        FROM patients p
        LEFT JOIN units u ON p.unit_id = u.id
        LEFT JOIN floors f ON u.floor_id = f.id
        LEFT JOIN facilities fac ON f.facility_id = fac.id
        WHERE p.status = 'admitted'
    """);
        if (facility != null) {
            sql.append(" AND (fac.name = ? OR p.referral_facility = ?)");
        }
        if (floor != null) {
            sql.append(" AND f.floor_number = ?");
        }
        if (severity != null) {
            sql.append(" AND p.severity = ?");
        }
        if (search != null) {
            sql.append(" AND (LOWER(p.full_name) LIKE ? OR LOWER(p.patient_code) LIKE ?)");
        }
        if (sort == SortMode.ADMISSION_DATE) {
            sql.append(" ORDER BY p.admission_date DESC");
        } else if (sort == SortMode.BED_ROOM) {
            sql.append(" ORDER BY u.label");
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (facility != null) {
                ps.setString(i++, facility); // fac.name
                ps.setString(i++, facility); // p.referral_facility
            }
            if (floor != null) {
                ps.setInt(i++, floor);
            }
            if (severity != null) {
                ps.setString(i++, severity.toLowerCase());
            }
            if (search != null) {
                String s = "%" + search.toLowerCase() + "%";
                ps.setString(i++, s);
                ps.setString(i++, s);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Patient p = new Patient(
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
                );
                p.setReferralStatus(rs.getString("referral_status"));
                p.setReferralFacility(rs.getString("referral_facility"));
                p.setReferralFloor(rs.getInt("referral_floor"));
                list.add(p);
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
    public void addPatientAutoAssign(
            String fullName,
            int age,
            String gender,
            String diagnosis,
            String severity
    ) {
        try (Connection c = DBConnection.getConnection()) {
            String unitSql = """
                   SELECT u.id
                   FROM units u
                   JOIN floors f ON u.floor_id = f.id
                   JOIN facilities fac ON f.facility_id = fac.id
                   WHERE u.status = 'AVAILABLE'
                   AND (
                   (? = 'critical' AND fac.type = 'ER')
                   OR (? IN ('high', 'moderate') AND fac.type = 'WARD'))
                   LIMIT 1""";
            PreparedStatement psUnit = c.prepareStatement(unitSql);
            psUnit.setString(1, severity.toLowerCase());
            psUnit.setString(2, severity.toLowerCase());
            ResultSet rs = psUnit.executeQuery();
            if (!rs.next()) {
                throw new SQLException("No available unit found for severity: " + severity);
            }
            int unitId = rs.getInt(1);
            String patientCode = generatePatientCode(c);
            PreparedStatement ps = c.prepareStatement("""
            INSERT INTO patients
            (patient_code, full_name, age, gender, diagnosis, severity, admission_date, status, unit_id)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), 'admitted', ?)
        """);
            ps.setString(1, patientCode);
            ps.setString(2, fullName);
            ps.setInt(3, age);
            ps.setString(4, gender);
            ps.setString(5, diagnosis);
            ps.setString(6, severity.toLowerCase());
            ps.setInt(7, unitId);
            ps.executeUpdate();
            c.prepareStatement(
                    "UPDATE units SET status='OCCUPIED' WHERE id=" + unitId
            ).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void requestReferral(int patientId, String facility, String floor) {
        String sql = """
        UPDATE patients
        SET referral_status = 'PENDING',
            referral_facility = ?,
            referral_floor = ?
        WHERE id = ?
    """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, facility);
            ps.setInt(2, Integer.parseInt(floor.replaceAll("\\D+", "")));
            ps.setInt(3, patientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void approveReferral(int patientId) {
        String getReferralSql = """
        SELECT referral_facility, referral_floor, unit_id
        FROM patients
        WHERE id = ?
    """;
        String findUnitSql = """
        SELECT u.id
        FROM units u
        JOIN floors f ON u.floor_id = f.id
        JOIN facilities fac ON f.facility_id = fac.id
        WHERE fac.name = ?
          AND f.floor_number = ?
          AND u.status = 'AVAILABLE'
        LIMIT 1
    """;

        try (Connection c = DBConnection.getConnection()) {

            PreparedStatement ps1 = c.prepareStatement(getReferralSql);
            ps1.setInt(1, patientId);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) return;
            String facility = rs.getString("referral_facility");

            int floor = rs.getInt("referral_floor");
            int oldUnit = rs.getInt("unit_id");

            PreparedStatement ps2 = c.prepareStatement(findUnitSql);
            ps2.setString(1, facility);
            ps2.setInt(2, floor);
            ResultSet rs2 = ps2.executeQuery();

            if (!rs2.next()) return;

            int newUnit = rs2.getInt("id");
            c.prepareStatement(
                    "UPDATE units SET status='AVAILABLE' WHERE id=" + oldUnit
            ).execute();
            c.prepareStatement(
                    "UPDATE units SET status='OCCUPIED' WHERE id=" + newUnit
            ).execute();
            c.prepareStatement(
                    "UPDATE patients SET " +
                            "unit_id=" + newUnit + ", " +
                            "referral_status='NONE', " +
                            "referral_facility=NULL, " +
                            "referral_floor=NULL " +
                            "WHERE id=" + patientId
            ).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void declineReferral(int patientId) {
        String sql = """
        UPDATE patients
        SET referral_status = 'DECLINED',
            referral_facility = NULL,
            referral_floor = NULL
        WHERE id = ?
    """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generatePatientCode(Connection c) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int count = rs.getInt(1) + 1;
            return "PT-" + (20000 + count);
        }
    }

    public Integer findAvailableUnit(String facility, int floor) {
        String sql = """
        SELECT u.id
        FROM units u
        JOIN floors f ON u.floor_id = f.id
        JOIN facilities fac ON f.facility_id = fac.id
        WHERE fac.name = ?
          AND f.floor_number = ?
          AND u.status = 'AVAILABLE'
        LIMIT 1
    """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, facility);
            ps.setInt(2, floor);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePatientEditAutoAssign(
            int patientId,
            int oldUnitId,
            Integer newUnitId,
            String diagnosis,
            String severity
    ) {
        try (Connection c = DBConnection.getConnection()) {
            if (newUnitId != null && newUnitId != oldUnitId) {
                c.prepareStatement(
                        "UPDATE units SET status='AVAILABLE' WHERE id=" + oldUnitId
                ).execute();
                c.prepareStatement(
                        "UPDATE units SET status='OCCUPIED' WHERE id=" + newUnitId
                ).execute();
                c.prepareStatement(
                        "UPDATE patients SET unit_id=" + newUnitId +
                                " WHERE id=" + patientId
                ).execute();
            }
            PreparedStatement ps = c.prepareStatement(
                    "UPDATE patients SET diagnosis=?, severity=? WHERE id=?"
            );
            ps.setString(1, diagnosis);
            ps.setString(2, severity.toLowerCase());
            ps.setInt(3, patientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





}
