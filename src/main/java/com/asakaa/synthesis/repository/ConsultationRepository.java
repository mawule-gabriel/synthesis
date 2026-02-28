package com.asakaa.synthesis.repository;

import com.asakaa.synthesis.domain.entity.Consultation;
import com.asakaa.synthesis.domain.entity.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByProviderIdAndStatus(Long providerId, ConsultationStatus status);

    List<Consultation> findByPatientId(Long patientId);

    List<Consultation> findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(Long patientId, LocalDateTime openedAfter);

    @Query("""
            SELECT d.conditionName as condition, pat.region as region, COUNT(c.id) as count
            FROM Consultation c
            JOIN c.patient pat
            JOIN c.diagnoses d
            WHERE c.openedAt BETWEEN :startDate AND :endDate
            GROUP BY d.conditionName, pat.region
            """)
    List<Map<String, Object>> countByConditionAndRegion(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
