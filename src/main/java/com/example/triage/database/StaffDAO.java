package com.example.triage.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    /* ================= GET ALL STAFF ================= */
    public List<Staff> getAllStaff() {

        List<Staff> list = new ArrayList<>();

        String sql = """
            SELECT s.id,
                   s.first_name,
                   s.last_name,
                   s.role,
                   f.name AS facility,
                   fl.floor_number
            FROM staff s
            JOIN facilities f ON s.facility_id = f.id
            JOIN floors fl ON s.floor_id = fl.id
            ORDER BY s.id
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                String fullName =
                        rs.getString("first_name") + " " +
                                rs.getString("last_name");

                String facilityDisplay =
                        rs.getString("facility") +
                                " • Floor " +
                                rs.getInt("floor_number");

                list.add(new Staff(
                        rs.getInt("id"),
                        fullName,
                        rs.getString("role"),
                        facilityDisplay,
                        false // onShift placeholder
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ================= ADD STAFF ================= */
    public void addStaff(String fullName, String role, int facilityId, int floorId) {

        String[] parts = fullName.trim().split("\\s+");
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[parts.length - 1] : "";

        String sql = """
            INSERT INTO staff (first_name, last_name, role, facility_id, floor_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, role.toLowerCase());
            ps.setInt(4, facilityId);
            ps.setInt(5, floorId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ================= UPDATE ================= */
    public void updateStaff(int id, String role, int facilityId, int floorId) {

        String sql = """
            UPDATE staff
            SET role = ?, facility_id = ?, floor_id = ?
            WHERE id = ?
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, role.toLowerCase());
            ps.setInt(2, facilityId);
            ps.setInt(3, floorId);
            ps.setInt(4, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ================= DELETE ================= */
    public void deleteStaff(int id) {

        String sql = "DELETE FROM staff WHERE id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
