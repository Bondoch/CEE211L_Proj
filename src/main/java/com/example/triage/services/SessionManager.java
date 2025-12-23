package com.example.triage.services;

public class SessionManager {

    private static SessionManager instance;

    private int userId;
    private int staffId;
    private String username;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void startSession(int userId, int staffId, String username, String role) {
        this.userId = userId;
        this.staffId = staffId;
        this.username = username;
        this.role = role;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getRole() {
        return role;
    }

    public void endSession() {
        userId = 0;
        staffId = 0;
        username = null;
        role = null;
    }
}



