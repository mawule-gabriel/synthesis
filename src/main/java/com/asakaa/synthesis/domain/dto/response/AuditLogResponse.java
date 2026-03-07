package com.asakaa.synthesis.domain.dto.response;

import com.asakaa.synthesis.domain.entity.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long providerId;
    private String providerName;
    private String providerEmail;
    private String providerRole;
    private AuditAction action;
    private Long patientId;
    private String patientName;
    private String patientNationalId;
    private String resourceType;
    private Long resourceId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private LocalDateTime timestamp;
    private Boolean success;
    private String errorMessage;
}
