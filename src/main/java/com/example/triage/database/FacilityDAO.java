package com.example.triage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FacilityDAO {
    public String getFacilityTypeByUnitId(int unitId) {
        String sql = """
        SELECT fac.type
        FROM units u
        JOIN floors f ON f.id = u.floor_id
        JOIN facilities fac ON fac.id = f.facility_id
        WHERE u.id = ?
    """;
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, unitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getAllFacilities() {
        List<String> facilities = new ArrayList<>();
        String sql = "SELECT name FROM facilities ORDER BY name";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                facilities.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return facilities;
    }

    public int getFacilityIdByName(String name) {
        String sql = "SELECT id FROM facilities WHERE name = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<String> getFacilitiesByType(String type) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM facilities WHERE type = ? ORDER BY name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public String getFacilityTypeByName(String facilityName) {
        String sql = "SELECT type FROM facilities WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, facilityName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
