package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.ConsultationRequest;
import com.asakaa.synthesis.domain.dto.request.ConsultationUpdateRequest;
import com.asakaa.synthesis.domain.dto.response.ConsultationResponse;
import com.asakaa.synthesis.domain.dto.response.DiagnosisResponse;
import com.asakaa.synthesis.domain.dto.response.TreatmentResponse;
import com.asakaa.synthesis.domain.entity.AuditAction;
import com.asakaa.synthesis.domain.entity.Consultation;
import com.asakaa.synthesis.domain.entity.ConsultationStatus;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.ClinicAccessDeniedException;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.repository.ConsultationRepository;
import com.asakaa.synthesis.repository.PatientRepository;
import com.asakaa.synthesis.repository.ProviderRepository;
import com.asakaa.synthesis.security.ClinicAccessGuard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final ObjectMapper objectMapper;
    private final ClinicAccessGuard clinicAccessGuard;
    private final AuditService auditService;

    @Transactional
    public ConsultationResponse openConsultation(ConsultationRequest request, Long providerId, Authentication authentication) {
        log.info("Opening consultation for patient ID: {} by provider ID: {}", request.getPatientId(), providerId);

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        clinicAccessGuard.verifyPatientAccess(authentication, patient);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", providerId));

        Consultation consultation = Consultation.builder()
                .patient(patient)
                .provider(provider)
                .status(ConsultationStatus.OPEN)
                .chiefComplaint(request.getChiefComplaint())
                .vitals(normalizeVitalsJson(request.getVitals()))
                .notes(request.getNotes())
                .openedAt(LocalDateTime.now())
                .build();

        consultation = consultationRepository.save(consultation);

        // Audit log
        auditService.logAudit(AuditAction.OPEN_CONSULTATION, patient.getId(), "Consultation", consultation.getId(),
            String.format("Opened consultation ID: %d, Chief complaint: %s", consultation.getId(), request.getChiefComplaint()));

        log.info("Consultation opened successfully with ID: {}", consultation.getId());
        return toResponse(consultation);
    }

    @Transactional
    public ConsultationResponse updateConsultation(Long id, ConsultationUpdateRequest request, Authentication authentication) {
        log.info("Updating consultation with ID: {}", id);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", id));

        clinicAccessGuard.verifyConsultationAccess(authentication, consultation);

        if (request.getVitals() != null) {
            consultation.setVitals(normalizeVitalsJson(request.getVitals()));
        }
        if (request.getNotes() != null) {
            consultation.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            consultation.setStatus(request.getStatus());
        }

        consultation = consultationRepository.save(consultation);

        // Audit log
        auditService.logAudit(AuditAction.UPDATE_CONSULTATION, consultation.getPatient().getId(), "Consultation", consultation.getId(),
            String.format("Updated consultation ID: %d", consultation.getId()));

        log.info("Consultation updated successfully with ID: {}", consultation.getId());
        return toResponse(consultation);
    }

    @Transactional
    public ConsultationResponse closeConsultation(Long id, Authentication authentication) {
        log.info("Closing consultation with ID: {}", id);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", id));

        clinicAccessGuard.verifyConsultationAccess(authentication, consultation);

        consultation.setStatus(ConsultationStatus.CLOSED);
        consultation.setClosedAt(LocalDateTime.now());

        consultation = consultationRepository.save(consultation);

        // Audit log
        auditService.logAudit(AuditAction.CLOSE_CONSULTATION, consultation.getPatient().getId(), "Consultation", consultation.getId(),
            String.format("Closed consultation ID: %d", consultation.getId()));

        log.info("Consultation closed successfully with ID: {}", consultation.getId());
        return toResponse(consultation);
    }

    public ConsultationResponse getConsultationById(Long id, Authentication authentication) {
        log.info("Fetching consultation with ID: {}", id);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", id));

        clinicAccessGuard.verifyConsultationAccess(authentication, consultation);

        // Audit log
        auditService.logAudit(AuditAction.VIEW_CONSULTATION, consultation.getPatient().getId(), "Consultation", consultation.getId(),
            String.format("Viewed consultation ID: %d", consultation.getId()));

        return toResponse(consultation);
    }

    public List<ConsultationResponse> getActiveConsultationsByProvider(Long providerId, Authentication authentication) {
        log.info("Fetching active consultations for provider ID: {}", providerId);

        Provider authProvider = clinicAccessGuard.getCurrentProvider(authentication);
        if (!clinicAccessGuard.isSuperAdmin(authentication) && !authProvider.getId().equals(providerId)) {
            throw new ClinicAccessDeniedException("Cannot fetch other provider's active consultations");
        }

        List<Consultation> consultations = consultationRepository.findByProviderIdAndStatus(
                providerId, ConsultationStatus.OPEN);

        return consultations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ConsultationResponse> getConsultationsByPatient(Long patientId, Authentication authentication) {
        log.info("Fetching consultations for patient ID: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
                
        clinicAccessGuard.verifyPatientAccess(authentication, patient);

        List<Consultation> consultations = consultationRepository.findByPatientId(patientId);

        // Audit log
        auditService.logAudit(AuditAction.VIEW_PATIENT_HISTORY, patientId, "Patient", patientId,
            String.format("Viewed consultation history for patient ID: %d (%d consultations)", patientId, consultations.size()));

        return consultations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ConsultationResponse toResponse(Consultation consultation) {
        return ConsultationResponse.builder()
                .id(consultation.getId())
                .patientId(consultation.getPatient().getId())
                .patientName(consultation.getPatient().getFirstName() + " " + consultation.getPatient().getLastName())
                .providerId(consultation.getProvider().getId())
                .providerName(consultation.getProvider().getName())
                .status(consultation.getStatus())
                .chiefComplaint(consultation.getChiefComplaint())
                .vitals(consultation.getVitals())
                .notes(consultation.getNotes())
                .openedAt(consultation.getOpenedAt())
                .closedAt(consultation.getClosedAt())
                .diagnoses(consultation.getDiagnoses().stream()
                        .map(diagnosis -> DiagnosisResponse.builder()
                                .id(diagnosis.getId())
                                .conditionName(diagnosis.getConditionName())
                                .confidenceScore(diagnosis.getConfidenceScore())
                                .reasoning(diagnosis.getReasoning())
                                .source(diagnosis.getSource())
                                .treatments(diagnosis.getTreatments().stream()
                                        .map(treatment -> TreatmentResponse.builder()
                                                .id(treatment.getId())
                                                .type(treatment.getType())
                                                .drugName(treatment.getDrugName())
                                                .dosage(treatment.getDosage())
                                                .duration(treatment.getDuration())
                                                .instructions(treatment.getInstructions())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private String normalizeVitalsJson(String vitals) {
        if (vitals == null || vitals.isBlank()) {
            return null;
        }

        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature());
            JsonNode jsonNode = mapper.readTree(vitals);
            if (jsonNode.isTextual()) {
                String nestedJson = jsonNode.asText();
                jsonNode = mapper.readTree(nestedJson);
            }
            return mapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("Vitals must be valid JSON.");
        }
    }
}
