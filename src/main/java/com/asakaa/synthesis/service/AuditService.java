package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.response.AuditLogResponse;
import com.asakaa.synthesis.domain.entity.AuditAction;
import com.asakaa.synthesis.domain.entity.AuditLog;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.repository.AuditLogRepository;
import com.asakaa.synthesis.repository.PatientRepository;
import com.asakaa.synthesis.repository.ProviderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ProviderRepository providerRepository;
    private final PatientRepository patientRepository;

    /**
     * Log an audit event asynchronously
     * Uses REQUIRES_NEW to ensure audit logs are saved even if main transaction fails
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(AuditAction action, Long patientId, String details) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Attempted to log audit without authentication");
                return;
            }

            String email = authentication.getName();
            Provider provider = providerRepository.findByEmail(email).orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .providerId(provider != null ? provider.getId() : null)
                    .providerName(provider != null ? provider.getName() : "Unknown")
                    .providerEmail(email)
                    .providerRole(provider != null ? provider.getRole() : "Unknown")
                    .patientId(patientId)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();

            // Add patient information if available
            if (patientId != null) {
                patientRepository.findById(patientId).ifPresent(patient -> {
                    auditLog.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                    auditLog.setPatientNationalId(patient.getNationalId());
                });
            }

            // Add HTTP request information
            addRequestInfo(auditLog);

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} by {} for patient {}", action, email, patientId);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log audit with resource information
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(AuditAction action, Long patientId, String resourceType, Long resourceId, String details) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }

            String email = authentication.getName();
            Provider provider = providerRepository.findByEmail(email).orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .providerId(provider != null ? provider.getId() : null)
                    .providerName(provider != null ? provider.getName() : "Unknown")
                    .providerEmail(email)
                    .providerRole(provider != null ? provider.getRole() : "Unknown")
                    .patientId(patientId)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();

            if (patientId != null) {
                patientRepository.findById(patientId).ifPresent(patient -> {
                    auditLog.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                    auditLog.setPatientNationalId(patient.getNationalId());
                });
            }

            addRequestInfo(auditLog);
            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log failed action
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedAction(AuditAction action, Long patientId, String errorMessage) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication != null ? authentication.getName() : "Anonymous";
            Provider provider = providerRepository.findByEmail(email).orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .providerId(provider != null ? provider.getId() : null)
                    .providerName(provider != null ? provider.getName() : "Unknown")
                    .providerEmail(email)
                    .providerRole(provider != null ? provider.getRole() : "Unknown")
                    .patientId(patientId)
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();

            if (patientId != null) {
                patientRepository.findById(patientId).ifPresent(patient -> {
                    auditLog.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                    auditLog.setPatientNationalId(patient.getNationalId());
                });
            }

            addRequestInfo(auditLog);
            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to log failed action", e);
        }
    }

    /**
     * Get audit logs with filters (SUPER_ADMIN only)
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(
            Long providerId,
            Long patientId,
            AuditAction action,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean success,
            Pageable pageable) {

        Page<AuditLog> logs = auditLogRepository.findByFilters(
                providerId, patientId, action, startDate, endDate, success, pageable
        );

        return logs.map(this::toResponse);
    }

    /**
     * Get audit logs for specific patient
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getPatientAuditLogs(Long patientId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByPatientId(patientId, pageable);
        return logs.map(this::toResponse);
    }

    /**
     * Get audit logs for specific provider
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getProviderAuditLogs(Long providerId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByProviderId(providerId, pageable);
        return logs.map(this::toResponse);
    }

    /**
     * Get recent activity for patient (last 10 actions)
     */
    @Transactional(readOnly = true)
    public java.util.List<AuditLogResponse> getRecentPatientActivity(Long patientId) {
        return auditLogRepository.findTop10ByPatientIdOrderByTimestampDesc(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Add HTTP request information to audit log
     */
    private void addRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession(false) != null ? 
                    request.getSession(false).getId() : null);
            }
        } catch (Exception e) {
            log.debug("Could not extract request information", e);
        }
    }

    /**
     * Get client IP address (handles proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Convert entity to response DTO
     */
    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .providerId(log.getProviderId())
                .providerName(log.getProviderName())
                .providerEmail(log.getProviderEmail())
                .providerRole(log.getProviderRole())
                .action(log.getAction())
                .patientId(log.getPatientId())
                .patientName(log.getPatientName())
                .patientNationalId(log.getPatientNationalId())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .sessionId(log.getSessionId())
                .timestamp(log.getTimestamp())
                .success(log.getSuccess())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
