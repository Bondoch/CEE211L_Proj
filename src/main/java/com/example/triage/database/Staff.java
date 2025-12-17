package com.example.triage.database;

public class Staff {

    private int id;
    private String name;
    private String role;
    private String facility;
    private boolean onShift;

    public Staff(int id, String name, String role, String facility, boolean onShift) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.facility = facility;
        this.onShift = onShift;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getFacility() { return facility; }
    public boolean isOnShift() { return onShift; }
}
