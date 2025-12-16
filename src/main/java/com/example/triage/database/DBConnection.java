package com.example.triage.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/triage_db";
    private static final String USER = "root";         // your MySQL username
    private static final String PASS = "root06214L@#";      // your MySQL password (update this!)

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Connected to MySQL successfully!");
            }
        } catch (Exception e) {
            System.out.println("Database connection failed:");
            e.printStackTrace();
        }
        return connection;
    }
}
