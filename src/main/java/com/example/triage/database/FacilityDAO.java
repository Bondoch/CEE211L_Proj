package com.example.triage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FacilityDAO {

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
}
