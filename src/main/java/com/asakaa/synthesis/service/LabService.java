package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.LabResultRequest;
import com.asakaa.synthesis.domain.dto.response.LabResultResponse;
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

        return toResponse(labResult);
    }

    public List<LabResultResponse> getLabResultsByConsultation(Long consultationId) {
        log.info("Fetching lab results for consultation ID: {}", consultationId);
        return labResultRepository.findByConsultationId(consultationId).stream()
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
