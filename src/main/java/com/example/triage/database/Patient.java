package com.example.triage.database;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Patient {

    private int id;
    private String patientCode;
    private String fullName;
    private int age;
    private String gender;
    private String diagnosis;
    private String severity;
    private Timestamp admissionDate;
    private String unitLabel;
    private int unitId;
    private String facilityName;
    private int floorNumber;
    private String referralStatus; // NONE, PENDING, APPROVED, DECLINED
    private String referralFacility;
    private int referralFloor;




    // ================= CONSTRUCTOR =================
    public Patient(int id, String patientCode, String fullName, int age,
                   String gender, String severity, Timestamp admissionDate,
                   int unitId, String unitLabel,
                   String facilityName, int floorNumber) {


        this.facilityName = facilityName;
        this.floorNumber = floorNumber;
        this.id = id;
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.severity = severity;
        this.admissionDate = admissionDate;
        this.unitId = unitId;
        this.unitLabel = unitLabel;
    }

    // ================= ORIGINAL GETTERS =================
    public int getId() { return id; }
    public String getPatientCode() { return patientCode; }
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getDiagnosis() { return diagnosis; }
    public String getSeverity() { return severity; }
    public Timestamp getAdmissionDate() { return admissionDate; }
    public String getUnitLabel() { return unitLabel; }
    public int getUnitId() { return unitId; }
    public String getFacilityName() {return facilityName;}
    public int getFloorNumber() {return floorNumber;}


    // ================= COMPATIBILITY ALIASES =================
    // 🔹 Fixes controller expectations WITHOUT changing logic

    // Used by controller instead of getFullName()
    public String getName() {
        return fullName;
    }

    // Used when controller expects "Room"
    public String getRoom() {
        return unitLabel;
    }

    // Used when controller expects "Bed"
    public String getBed() {
        return unitLabel;
    }

    // Used when controller formats admission date
    public String getFormattedAdmissionDate() {
        if (admissionDate == null) return "";
        LocalDateTime ldt = admissionDate.toLocalDateTime();
        return ldt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"));
    }
    public String getReferralStatus() {
        return referralStatus == null ? "NONE" : referralStatus;
    }

    public void setReferralStatus(String referralStatus) {
        this.referralStatus = referralStatus;
    }

    public String getReferralFacility() {
        return referralFacility;
    }

    public void setReferralFacility(String referralFacility) {
        this.referralFacility = referralFacility;
    }



    public int getReferralFloor() {
        return referralFloor;
    }

    public void setReferralFloor(int referralFloor) {
        this.referralFloor = referralFloor;
    }


}
