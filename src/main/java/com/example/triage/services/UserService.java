package com.example.triage.services;

import com.example.triage.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService {

    public static boolean isUserTableEmpty() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) == 0; // true if count is zero
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
