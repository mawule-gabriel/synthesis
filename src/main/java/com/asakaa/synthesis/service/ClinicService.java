package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.ClinicRegistrationRequest;
import com.asakaa.synthesis.domain.dto.response.AuthResponse;
import com.asakaa.synthesis.domain.dto.response.ClinicResponse;
import com.asakaa.synthesis.domain.entity.Clinic;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.repository.ClinicRepository;
import com.asakaa.synthesis.repository.ProviderRepository;
import com.asakaa.synthesis.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerClinic(ClinicRegistrationRequest request) {
        if (clinicRepository.existsByName(request.getClinicName())) {
            throw new ValidationException("A clinic with this name already exists");
        }

        if (providerRepository.existsByEmail(request.getAdminEmail())) {
            throw new ValidationException("Email already registered");
        }

        // Generate a unique registration code for staff onboarding
        String registrationCode = generateRegistrationCode();

        Clinic clinic = Clinic.builder()
                .name(request.getClinicName())
                .address(request.getClinicAddress())
                .region(request.getClinicRegion())
                .registrationCode(registrationCode)
                .build();

        clinic = clinicRepository.save(clinic);

        // Create the first provider as CLINIC_ADMIN
        Provider admin = Provider.builder()
                .name(request.getAdminName())
                .role("CLINIC_ADMIN")
                .email(request.getAdminEmail())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .clinic(clinic)
                .build();

        providerRepository.save(admin);

        log.info("Clinic '{}' registered with code '{}' and admin '{}'",
                clinic.getName(), registrationCode, admin.getEmail());

        String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole());

        return AuthResponse.builder()
                .token(token)
                .email(admin.getEmail())
                .name(admin.getName())
                .role(admin.getRole())
                .clinicId(clinic.getId())
                .clinicName(clinic.getName())
                .registrationCode(registrationCode)
                .build();
    }

    public ClinicResponse getClinicById(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + clinicId));

        return toClinicResponse(clinic);
    }

    public ClinicResponse getClinicStaff(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + clinicId));

        return toClinicResponse(clinic);
    }

    public List<ClinicResponse> getAllClinics() {
        return clinicRepository.findAll().stream()
                .map(this::toClinicResponse)
                .collect(Collectors.toList());
    }

    private ClinicResponse toClinicResponse(Clinic clinic) {
        List<ClinicResponse.ProviderSummary> staff = clinic.getProviders().stream()
                .map(p -> ClinicResponse.ProviderSummary.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .role(p.getRole())
                        .email(p.getEmail())
                        .build())
                .collect(Collectors.toList());

        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .region(clinic.getRegion())
                .registrationCode(clinic.getRegistrationCode())
                .createdAt(clinic.getCreatedAt())
                .staff(staff)
                .build();
    }

    private String generateRegistrationCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (clinicRepository.existsByRegistrationCode(code));
        return code;
    }
}
