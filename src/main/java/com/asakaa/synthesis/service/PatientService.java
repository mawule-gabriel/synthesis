package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.PatientRequest;
import com.asakaa.synthesis.domain.dto.response.PatientResponse;
import com.asakaa.synthesis.domain.entity.Clinic;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.repository.ClinicRepository;
import com.asakaa.synthesis.repository.PatientRepository;
import com.asakaa.synthesis.security.ClinicAccessGuard;
import com.asakaa.synthesis.util.PatientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final PatientMapper patientMapper;
    private final ClinicAccessGuard clinicAccessGuard;

    @Transactional
    public PatientResponse createPatient(PatientRequest request, Authentication authentication) {
        log.info("Creating patient with national ID: {}", request.getNationalId());

        if (patientRepository.existsByNationalId(request.getNationalId())) {
            throw new ValidationException("Patient with national ID " + request.getNationalId() + " already exists");
        }

        Patient patient = patientMapper.toEntity(request);

        // Auto-assign to provider's clinic unless super-admin with explicit clinicId
        if (!clinicAccessGuard.isSuperAdmin(authentication)) {
            Provider provider = clinicAccessGuard.getCurrentProvider(authentication);
            if (provider.getClinic() != null) {
                patient.setClinic(provider.getClinic());
            }
        }

        patient = patientRepository.save(patient);

        log.info("Patient created successfully with ID: {}", patient.getId());
        return patientMapper.toResponse(patient);
    }

    public PatientResponse getPatientById(Long id, Authentication authentication) {
        log.info("Fetching patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        clinicAccessGuard.verifyPatientAccess(authentication, patient);

        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request, Authentication authentication) {
        log.info("Updating patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        clinicAccessGuard.verifyPatientAccess(authentication, patient);

        // Check if nationalId is being changed to an existing one
        if (!patient.getNationalId().equals(request.getNationalId()) &&
                patientRepository.existsByNationalId(request.getNationalId())) {
            throw new ValidationException("Patient with national ID " + request.getNationalId() + " already exists");
        }

        patient.setNationalId(request.getNationalId());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId()).orElse(null);
            patient.setClinic(clinic);
        }

        patient.setRegion(request.getRegion());

        patient = patientRepository.save(patient);

        log.info("Patient updated successfully with ID: {}", patient.getId());
        return patientMapper.toResponse(patient);
    }

    public Page<PatientResponse> getAllPatients(Pageable pageable, Authentication authentication) {
        log.info("Fetching all patients, page: {}", pageable.getPageNumber());

        if (clinicAccessGuard.isSuperAdmin(authentication)) {
            return patientRepository.findAll(pageable)
                    .map(patientMapper::toResponse);
        }

        Provider provider = clinicAccessGuard.getCurrentProvider(authentication);
        Long clinicId = provider.getClinic() != null ? provider.getClinic().getId() : null;

        if (clinicId == null) {
            return Page.empty(pageable);
        }

        return patientRepository.findByClinicId(clinicId, pageable)
                .map(patientMapper::toResponse);
    }

    public Page<PatientResponse> searchPatients(String query, Pageable pageable, Authentication authentication) {
        log.info("Searching patients with query: {}", query);

        if (clinicAccessGuard.isSuperAdmin(authentication)) {
            return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            query, query, pageable)
                    .map(patientMapper::toResponse);
        }

        Provider provider = clinicAccessGuard.getCurrentProvider(authentication);
        Long clinicId = provider.getClinic() != null ? provider.getClinic().getId() : null;

        if (clinicId == null) {
            return Page.empty(pageable);
        }

        return patientRepository.searchByClinicId(clinicId, query, pageable)
                .map(patientMapper::toResponse);
    }
}
