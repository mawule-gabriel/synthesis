package com.asakaa.synthesis.controller;

import com.asakaa.synthesis.domain.dto.response.AuditLogResponse;
import com.asakaa.synthesis.domain.entity.AuditAction;
import com.asakaa.synthesis.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * Get all audit logs with filters (SUPER_ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Boolean success,
            Pageable pageable) {

        // Log this audit access
        auditService.logAudit(AuditAction.VIEW_AUDIT_LOGS, null, 
            String.format("Filters: providerId=%s, patientId=%s, action=%s", providerId, patientId, action));

        Page<AuditLogResponse> logs = auditService.getAuditLogs(
            providerId, patientId, action, startDate, endDate, success, pageable
        );

        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs for specific patient (SUPER_ADMIN only)
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getPatientAuditLogs(
            @PathVariable Long patientId,
            Pageable pageable) {

        auditService.logAudit(AuditAction.VIEW_AUDIT_LOGS, patientId, 
            "Viewing audit logs for patient: " + patientId);

        Page<AuditLogResponse> logs = auditService.getPatientAuditLogs(patientId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs for specific provider (SUPER_ADMIN only)
     */
    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getProviderAuditLogs(
            @PathVariable Long providerId,
            Pageable pageable) {

        auditService.logAudit(AuditAction.VIEW_AUDIT_LOGS, null, 
            "Viewing audit logs for provider: " + providerId);

        Page<AuditLogResponse> logs = auditService.getProviderAuditLogs(providerId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get recent activity for patient (SUPER_ADMIN only)
     */
    @GetMapping("/patient/{patientId}/recent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getRecentPatientActivity(
            @PathVariable Long patientId) {

        auditService.logAudit(AuditAction.VIEW_AUDIT_LOGS, patientId, 
            "Viewing recent activity for patient: " + patientId);

        List<AuditLogResponse> logs = auditService.getRecentPatientActivity(patientId);
        return ResponseEntity.ok(logs);
    }
}
