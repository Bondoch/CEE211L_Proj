package com.example.triage.services;


public class PermissionService {


    private PermissionService() {System.out.println("🔍 Current role: " + role());
    }
    private static String role() {
        return SessionManager.getInstance().getRole();
    }

    /* ===== ROLE CHECKS ===== */
    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(role());
    }
    public static boolean isDoctor() {
        return "doctor".equalsIgnoreCase(role());
    }
    public static boolean isNurseOrTechnician() {
        return "nurse".equalsIgnoreCase(role())
                || "technician".equalsIgnoreCase(role());
    }
    public static boolean isAdminOrDoctor() {
        return isAdmin() || isDoctor();
    }

    /* ===== MANAGEMENT ===== */
    public static boolean canManageStaff() {
        return isAdmin();
    }
    public static boolean canManageFacilities() {
        return isAdmin();
    }
    /* ===== PATIENT ACTIONS ===== */
    public static boolean canAddPatient() {
        return isAdmin() || isDoctor() || isNurseOrTechnician();
    }
    public static boolean canEditSeverity() {
        return canAddPatient();
    }
    public static boolean canDischargePatient() {
        return canAddPatient();
    }

    /* ===== TRANSFER LOGIC ===== */
    public static boolean canTransfer(
            String fromFacilityType,
            String toFacilityType
    ) {
        // Admin & Doctor: unrestricted
        if (isAdmin() || isDoctor()) {
            return true;
        }
        return isNurseOrTechnician()
                && "WARD".equalsIgnoreCase(fromFacilityType)
                && "WARD".equalsIgnoreCase(toFacilityType);
    }
}
