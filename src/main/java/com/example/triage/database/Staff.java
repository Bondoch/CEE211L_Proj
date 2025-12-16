package com.example.triage.database;

public class Staff {

    private final String name;
    private final String role;
    private final String facility;
    private final boolean onShift;

    public Staff(String name, String role, String facility, boolean onShift) {
        this.name = name;
        this.role = role;
        this.facility = facility;
        this.onShift = onShift;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getFacility() {
        return facility;
    }

    public boolean isOnShift() {
        return onShift;
    }
}
