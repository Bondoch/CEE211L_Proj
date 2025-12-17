package com.example.triage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FloorDAO {

    public List<String> getFloorsByFacility(int facilityId) {

        List<String> floors = new ArrayList<>();

        String sql = """
            SELECT floor_number
            FROM floors
            WHERE facility_id = ?
            ORDER BY floor_number
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, facilityId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                floors.add("Floor " + rs.getInt("floor_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return floors;
    }

    public int getFloorId(int facilityId, int floorNumber) {

        String sql = """
            SELECT id
            FROM floors
            WHERE facility_id = ? AND floor_number = ?
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, facilityId);
            ps.setInt(2, floorNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
