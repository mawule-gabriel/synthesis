package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.EscalationRequest;
import com.asakaa.synthesis.domain.dto.response.EscalationResponse;
import com.asakaa.synthesis.domain.entity.*;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.integration.telemedicine.TelemedicineAdapter;
import com.asakaa.synthesis.repository.ConsultationRepository;
import com.asakaa.synthesis.repository.EscalationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final ConsultationRepository consultationRepository;
    private final EscalationRepository escalationRepository;
    private final TelemedicineAdapter telemedicineAdapter;
    private final NotificationService notificationService;

    @Transactional
    public EscalationResponse escalate(EscalationRequest request) {
        log.info("Processing escalation for consultation ID: {}", request.getConsultationId());

        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", request.getConsultationId()));

        String caseSummary = buildCaseSummary(consultation, request);
        String urgency = request.getUrgencyLevel() != null ? request.getUrgencyLevel() : "ROUTINE";

        String referralId = telemedicineAdapter.sendEscalation(
                caseSummary, urgency, request.getSpecialistType());

        // Persist the escalation
        Escalation escalation = Escalation.builder()
                .consultation(consultation)
                .referralId(referralId)
                .specialistType(request.getSpecialistType())
                .urgencyLevel(urgency)
                .status("SUBMITTED")
                .caseSummary(caseSummary)
                .referralNotes(request.getReferralNotes())
                .submittedAt(LocalDateTime.now())
                .build();
        escalation = escalationRepository.save(escalation);

        // Update consultation status
        consultation.setStatus(ConsultationStatus.ESCALATED);
        consultationRepository.save(consultation);

        notificationService.notifyEscalation(consultation.getId(), referralId);

        log.info("Escalation completed. Referral ID: {}, Escalation ID: {}", referralId, escalation.getId());

        return toResponse(escalation);
    }

    public EscalationResponse getEscalationById(Long id) {
        log.info("Fetching escalation ID: {}", id);
        Escalation escalation = escalationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation", id));
        return toResponse(escalation);
    }

    public List<EscalationResponse> getEscalationsByConsultation(Long consultationId) {
        log.info("Fetching escalations for consultation ID: {}", consultationId);
        return escalationRepository.findByConsultationId(consultationId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EscalationResponse toResponse(Escalation escalation) {
        return EscalationResponse.builder()
                .id(escalation.getId())
                .escalationId(escalation.getReferralId())
                .consultationId(escalation.getConsultation().getId())
                .specialistType(escalation.getSpecialistType())
                .urgencyLevel(escalation.getUrgencyLevel())
                .status(escalation.getStatus())
                .caseSummary(escalation.getCaseSummary())
                .referralNotes(escalation.getReferralNotes())
                .submittedAt(escalation.getSubmittedAt())
                .build();
    }

    private String buildCaseSummary(Consultation consultation, EscalationRequest request) {
        Patient patient = consultation.getPatient();
        int age = Period.between(patient.getDateOfBirth(), LocalDateTime.now().toLocalDate()).getYears();

        StringBuilder summary = new StringBuilder();

        summary.append("=== SPECIALIST REFERRAL CASE SUMMARY ===\n\n");

        summary.append("PATIENT INFORMATION:\n");
        summary.append(String.format("- Name: %s %s\n", patient.getFirstName(), patient.getLastName()));
        summary.append(String.format("- Age: %d years\n", age));
        summary.append(String.format("- Gender: %s\n", patient.getGender() != null ? patient.getGender() : "Not specified"));
        summary.append(String.format("- Blood Group: %s\n", patient.getBloodGroup() != null ? patient.getBloodGroup() : "Not specified"));
        summary.append(String.format("- Allergies: %s\n", patient.getAllergies() != null ? patient.getAllergies() : "None reported"));
        summary.append(String.format("- Clinic: %s\n\n", patient.getClinicName() != null ? patient.getClinicName() : "Not specified"));

        summary.append("CONSULTATION DETAILS:\n");
        summary.append(String.format("- Chief Complaint: %s\n", consultation.getChiefComplaint()));
        summary.append(String.format("- Vital Signs: %s\n", consultation.getVitals() != null ? consultation.getVitals() : "Not recorded"));
        summary.append(String.format("- Consultation Date: %s\n", consultation.getOpenedAt()));
        summary.append(String.format("- Provider: %s\n\n", consultation.getProvider().getName()));

        if (consultation.getNotes() != null && !consultation.getNotes().isEmpty()) {
            summary.append("CLINICAL NOTES:\n");
            summary.append(consultation.getNotes()).append("\n\n");
        }

        if (!consultation.getDiagnoses().isEmpty()) {
            summary.append("DIFFERENTIAL DIAGNOSES:\n");
            consultation.getDiagnoses().stream()
                    .sorted(Comparator.comparing(Diagnosis::getConfidenceScore).reversed())
                    .forEach(diagnosis -> {
                        summary.append(String.format("- %s (Confidence: %.0f%%)\n",
                                diagnosis.getConditionName(),
                                diagnosis.getConfidenceScore().doubleValue() * 100));
                        summary.append(String.format("  Reasoning: %s\n", diagnosis.getReasoning()));
                    });
            summary.append("\n");
        }

        if (!consultation.getDiagnoses().isEmpty()) {
            boolean hasTreatments = consultation.getDiagnoses().stream()
                    .anyMatch(d -> !d.getTreatments().isEmpty());

            if (hasTreatments) {
                summary.append("CURRENT TREATMENT PLAN:\n");
                consultation.getDiagnoses().forEach(diagnosis -> {
                    if (!diagnosis.getTreatments().isEmpty()) {
                        summary.append(String.format("For %s:\n", diagnosis.getConditionName()));
                        diagnosis.getTreatments().forEach(treatment -> {
                            summary.append(String.format("  - %s: %s, %s for %s\n",
                                    treatment.getType() != null ? treatment.getType() : "Treatment",
                                    treatment.getDrugName() != null ? treatment.getDrugName() : "Not specified",
                                    treatment.getDosage() != null ? treatment.getDosage() : "Dosage not specified",
                                    treatment.getDuration() != null ? treatment.getDuration() : "Duration not specified"));
                        });
                    }
                });
                summary.append("\n");
            }
        }


        if (!consultation.getImageAnalyses().isEmpty()) {
            summary.append("IMAGING FINDINGS (AI-INTERPRETED):\n");
            consultation.getImageAnalyses().forEach(analysis -> {
                summary.append(String.format("- Analyzed at: %s\n", analysis.getAnalyzedAt()));
                summary.append(String.format("  Assessment: %s\n", analysis.getDescription()));
                if (!analysis.getFindings().isEmpty()) {
                    summary.append("  Key Findings: ").append(String.join(", ", analysis.getFindings())).append("\n");
                }
            });
            summary.append("\n");
        }


        summary.append("REFERRAL INFORMATION:\n");
        summary.append(String.format("- Specialist Type: %s\n", request.getSpecialistType()));
        summary.append(String.format("- Urgency Level: %s\n", request.getUrgencyLevel() != null ? request.getUrgencyLevel() : "ROUTINE"));
        if (request.getReferralNotes() != null && !request.getReferralNotes().isEmpty()) {
            summary.append(String.format("- Referral Notes: %s\n", request.getReferralNotes()));
        }

        return summary.toString();
    }
}
