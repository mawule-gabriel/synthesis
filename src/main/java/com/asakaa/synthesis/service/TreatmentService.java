package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.TreatmentRequest;
import com.asakaa.synthesis.domain.dto.response.TreatmentPlanResponse;
import com.asakaa.synthesis.domain.dto.response.TreatmentResponse;
import com.asakaa.synthesis.domain.entity.AuditAction;
import com.asakaa.synthesis.domain.entity.Diagnosis;
import com.asakaa.synthesis.domain.entity.Treatment;
import com.asakaa.synthesis.exception.DiagnosticException;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.integration.bedrock.BedrockClient;
import com.asakaa.synthesis.integration.bedrock.BedrockPromptBuilder;
import com.asakaa.synthesis.repository.DiagnosisRepository;
import com.asakaa.synthesis.repository.TreatmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final DiagnosisRepository diagnosisRepository;
    private final TreatmentRepository treatmentRepository;
    private final BedrockPromptBuilder bedrockPromptBuilder;
    private final BedrockClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditService auditService;

    @Transactional
    public TreatmentPlanResponse generateTreatmentPlan(TreatmentRequest request) {
        log.info("Generating treatment plan for diagnosis ID: {}", request.getDiagnosisId());

        // Fetch diagnosis
        Diagnosis diagnosis = diagnosisRepository.findById(request.getDiagnosisId())
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis", request.getDiagnosisId()));

        // Build treatment prompt
        String prompt = bedrockPromptBuilder.buildTreatmentPrompt(diagnosis.getConditionName(), request);
        log.debug("Generated treatment prompt for diagnosis ID: {}", request.getDiagnosisId());

        // Invoke Bedrock
        String rawResponse;
        try {
            rawResponse = bedrockClient.invoke(prompt);
        } catch (Exception e) {
            log.error("Failed to invoke Bedrock for treatment plan, diagnosis ID: {}", request.getDiagnosisId(), e);
            throw new DiagnosticException(
                    "Failed to generate treatment plan for diagnosis " + request.getDiagnosisId(), e);
        }

        // Parse and save treatments
        List<Treatment> treatments = parseTreatmentResponse(rawResponse, diagnosis);
        treatments = treatmentRepository.saveAll(treatments);

        log.info("Generated and saved {} treatments for diagnosis ID: {}", treatments.size(), request.getDiagnosisId());

        // Audit log
        auditService.logAudit(AuditAction.GENERATE_TREATMENT_PLAN, diagnosis.getConsultation().getPatient().getId(), "Diagnosis", diagnosis.getId(),
            String.format("Generated treatment plan for diagnosis ID: %d (%s), %d treatments", diagnosis.getId(), diagnosis.getConditionName(), treatments.size()));

        // Build response
        return TreatmentPlanResponse.builder()
                .diagnosisId(diagnosis.getId())
                .conditionName(diagnosis.getConditionName())
                .treatments(treatments.stream()
                        .map(this::toTreatmentResponse)
                        .collect(Collectors.toList()))
                .followUpInstructions(extractFollowUpInstructions(rawResponse))
                .patientEducation(extractPatientEducation(rawResponse))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public List<TreatmentResponse> getTreatmentsByDiagnosis(Long diagnosisId) {
        log.info("Fetching treatments for diagnosis ID: {}", diagnosisId);

        List<Treatment> treatments = treatmentRepository.findByDiagnosisId(diagnosisId);

        return treatments.stream()
                .map(this::toTreatmentResponse)
                .collect(Collectors.toList());
    }

    private List<Treatment> parseTreatmentResponse(String rawResponse, Diagnosis diagnosis) {
        List<Treatment> treatments = new ArrayList<>();

        try {
            // Extract JSON from response if it contains markdown code blocks
            String jsonContent = extractJson(rawResponse);
            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode treatmentsNode = root.get("treatments");

            if (treatmentsNode != null && treatmentsNode.isArray()) {
                for (JsonNode treatmentNode : treatmentsNode) {
                    Treatment treatment = Treatment.builder()
                            .diagnosis(diagnosis)
                            .type(treatmentNode.has("type") ? treatmentNode.get("type").asText() : "Medication")
                            .drugName(treatmentNode.has("drugName") ? treatmentNode.get("drugName").asText() : null)
                            .dosage(treatmentNode.has("dosage") ? treatmentNode.get("dosage").asText() : null)
                            .duration(treatmentNode.has("duration") ? treatmentNode.get("duration").asText() : null)
                            .instructions(treatmentNode.has("instructions") ? treatmentNode.get("instructions").asText() : null)
                            .build();
                    treatments.add(treatment);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse treatment response", e);
            throw new DiagnosticException("Failed to parse treatment plan response", e);
        }

        return treatments;
    }

    private String extractJson(String rawResponse) {
        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String extractFollowUpInstructions(String rawResponse) {
        try {
            String jsonContent = extractJson(rawResponse);
            JsonNode root = objectMapper.readTree(jsonContent);
            if (root.has("followUpInstructions")) {
                return root.get("followUpInstructions").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to extract follow-up instructions", e);
        }
        return "Follow up in 7-14 days or sooner if symptoms worsen";
    }

    private String extractPatientEducation(String rawResponse) {
        try {
            String jsonContent = extractJson(rawResponse);
            JsonNode root = objectMapper.readTree(jsonContent);
            if (root.has("patientEducation")) {
                return root.get("patientEducation").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to extract patient education", e);
        }
        return "Take medications as prescribed and monitor for side effects";
    }

    private TreatmentResponse toTreatmentResponse(Treatment treatment) {
        return TreatmentResponse.builder()
                .id(treatment.getId())
                .type(treatment.getType())
                .drugName(treatment.getDrugName())
                .dosage(treatment.getDosage())
                .duration(treatment.getDuration())
                .instructions(treatment.getInstructions())
                .build();
    }
}
