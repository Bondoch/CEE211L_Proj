package com.example.triage.services;

public class SessionManager {

    private static SessionManager instance;

    private String username;
    private String role;
    private int userId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void startSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public void endSession() {
        this.userId = 0;
        this.username = null;
        this.role = null;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isLoggedIn() {
        return username != null && role != null;
    }
}