package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.LabResultRequest;
import com.asakaa.synthesis.domain.dto.response.LabResultResponse;
import com.asakaa.synthesis.domain.entity.AuditAction;
import com.asakaa.synthesis.domain.entity.Consultation;
import com.asakaa.synthesis.domain.entity.LabResult;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.repository.ConsultationRepository;
import com.asakaa.synthesis.repository.LabResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabService {

    private final LabResultRepository labResultRepository;
    private final ConsultationRepository consultationRepository;
    private final AuditService auditService;

    @Transactional
    public LabResultResponse addLabResult(LabResultRequest request) {
        log.info("Adding lab result for consultation ID: {}", request.getConsultationId());

        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", request.getConsultationId()));

        LabResult labResult = LabResult.builder()
                .consultation(consultation)
                .testName(request.getTestName())
                .numericValue(request.getNumericValue())
                .unit(request.getUnit())
                .isAbnormal(request.getIsAbnormal())
                .referenceRange(request.getReferenceRange())
                .recordedAt(LocalDateTime.now())
                .build();

        labResult = labResultRepository.save(labResult);
        log.info("Lab result added with ID: {}", labResult.getId());

        // Audit log
        auditService.logAudit(AuditAction.ADD_LAB_RESULT, consultation.getPatient().getId(), "LabResult", labResult.getId(),
            String.format("Added lab result: %s for consultation ID: %d", request.getTestName(), consultation.getId()));

        return toResponse(labResult);
    }

    public List<LabResultResponse> getLabResultsByConsultation(Long consultationId) {
        log.info("Fetching lab results for consultation ID: {}", consultationId);
        
        List<LabResult> results = labResultRepository.findByConsultationId(consultationId);
        
        if (!results.isEmpty()) {
            // Audit log - get patient ID from first result
            Long patientId = results.get(0).getConsultation().getPatient().getId();
            auditService.logAudit(AuditAction.VIEW_LAB_RESULTS, patientId, "Consultation", consultationId,
                String.format("Viewed %d lab results for consultation ID: %d", results.size(), consultationId));
        }
        
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LabResultResponse toResponse(LabResult labResult) {
        return LabResultResponse.builder()
                .id(labResult.getId())
                .consultationId(labResult.getConsultation().getId())
                .testName(labResult.getTestName())
                .numericValue(labResult.getNumericValue())
                .unit(labResult.getUnit())
                .isAbnormal(labResult.getIsAbnormal())
                .referenceRange(labResult.getReferenceRange())
                .recordedAt(labResult.getRecordedAt())
                .build();
    }
}
