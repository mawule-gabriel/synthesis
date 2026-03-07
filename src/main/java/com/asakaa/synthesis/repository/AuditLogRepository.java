package com.asakaa.synthesis.repository;

import com.asakaa.synthesis.domain.entity.AuditAction;
import com.asakaa.synthesis.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find by provider
    Page<AuditLog> findByProviderId(Long providerId, Pageable pageable);

    // Find by patient
    Page<AuditLog> findByPatientId(Long patientId, Pageable pageable);

    // Find by action
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    // Find by provider and patient
    Page<AuditLog> findByProviderIdAndPatientId(Long providerId, Long patientId, Pageable pageable);

    // Find by date range
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find failed actions
    Page<AuditLog> findBySuccessFalse(Pageable pageable);

    // Complex query: Filter by multiple criteria
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:providerId IS NULL OR a.providerId = :providerId)
        AND (:patientId IS NULL OR a.patientId = :patientId)
        AND (:action IS NULL OR a.action = :action)
        AND (CAST(:startDate AS timestamp) IS NULL OR a.timestamp >= :startDate)
        AND (CAST(:endDate AS timestamp) IS NULL OR a.timestamp <= :endDate)
        AND (:success IS NULL OR a.success = :success)
        ORDER BY a.timestamp DESC
    """)
    Page<AuditLog> findByFilters(
            @Param("providerId") Long providerId,
            @Param("patientId") Long patientId,
            @Param("action") AuditAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("success") Boolean success,
            Pageable pageable
    );

    // Suspicious activity detection: Provider accessing many different patients
    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.providerId = :providerId
        AND a.timestamp >= :since
        AND a.action IN ('VIEW_PATIENT_PROFILE', 'VIEW_PATIENT_HISTORY')
        GROUP BY a.patientId
        HAVING COUNT(DISTINCT a.patientId) > :threshold
    """)
    List<AuditLog> findSuspiciousAccessPatterns(
            @Param("providerId") Long providerId,
            @Param("since") LocalDateTime since,
            @Param("threshold") int threshold
    );

    // Count actions by provider in time range
    @Query("""
        SELECT COUNT(a) FROM AuditLog a
        WHERE a.providerId = :providerId
        AND a.timestamp BETWEEN :start AND :end
    """)
    Long countByProviderAndTimeRange(
            @Param("providerId") Long providerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Get recent activity for patient
    List<AuditLog> findTop10ByPatientIdOrderByTimestampDesc(Long patientId);

    // Get recent activity for provider
    List<AuditLog> findTop20ByProviderIdOrderByTimestampDesc(Long providerId);
}
