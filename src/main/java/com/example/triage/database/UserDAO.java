package com.example.triage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserDAO {

    public void createUser(int staffId, String username, String password, String role) {

        String sql = """
        INSERT INTO users (staff_id, username, password, role)
        VALUES (?, ?, ?, ?)
    """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, staffId);
            ps.setString(2, username.toLowerCase());
            ps.setString(3, password);
            ps.setString(4, role.toUpperCase());

            int rows = ps.executeUpdate();
            System.out.println("👤 User rows inserted = " + rows);

        } catch (Exception e) {
            System.err.println("❌ User insert failed");
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
