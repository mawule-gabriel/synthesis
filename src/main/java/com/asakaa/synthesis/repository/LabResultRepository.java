package com.asakaa.synthesis.repository;

import com.asakaa.synthesis.domain.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByConsultationId(Long consultationId);
}
