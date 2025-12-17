package com.example.triage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserDAO {

    public void createUser(String username, String password, String role) {

        String sql = """
            INSERT INTO users (username, password, role)
            VALUES (?, ?, ?)
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username.toLowerCase());
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePassword(int userId, String newPassword) {

        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
