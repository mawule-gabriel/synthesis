package com.asakaa.synthesis.domain.entity;

public enum AuditAction {
    // Patient Actions
    VIEW_PATIENT_PROFILE,
    CREATE_PATIENT,
    UPDATE_PATIENT,
    DELETE_PATIENT,
    SEARCH_PATIENT,
    
    // Consultation Actions
    OPEN_CONSULTATION,
    VIEW_CONSULTATION,
    UPDATE_CONSULTATION,
    CLOSE_CONSULTATION,
    VIEW_CONSULTATION_HISTORY,
    VIEW_PATIENT_HISTORY,
    
    // Diagnostic Actions
    RUN_DIAGNOSTIC_ANALYSIS,
    VIEW_DIAGNOSTIC_RESULTS,
    ANALYZE_MEDICAL_IMAGE,
    
    // Treatment Actions
    GENERATE_TREATMENT_PLAN,
    VIEW_TREATMENT_PLAN,
    
    // Lab Actions
    ADD_LAB_RESULT,
    VIEW_LAB_RESULTS,
    
    // Escalation Actions
    ESCALATE_TO_SPECIALIST,
    VIEW_ESCALATION,
    
    // File Actions
    DOWNLOAD_MEDICAL_FILE,
    UPLOAD_MEDICAL_FILE,
    
    // Authentication Actions
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    REGISTER_PROVIDER,
    
    // Admin Actions
    VIEW_AUDIT_LOGS,
    EXPORT_AUDIT_LOGS
}
