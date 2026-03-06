package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.AuthRequest;
import com.asakaa.synthesis.domain.dto.request.RegisterRequest;
import com.asakaa.synthesis.domain.dto.response.AuthResponse;
import com.asakaa.synthesis.domain.entity.Clinic;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.repository.ClinicRepository;
import com.asakaa.synthesis.repository.ProviderRepository;
import com.asakaa.synthesis.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ProviderRepository providerRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (providerRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        if ("SUPER_ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new ValidationException("Cannot register as SUPER_ADMIN via public endpoint");
        }

        // Look up clinic by registration code
        Clinic clinic = null;
        if (request.getClinicRegistrationCode() != null && !request.getClinicRegistrationCode().isBlank()) {
            clinic = clinicRepository.findByRegistrationCode(request.getClinicRegistrationCode())
                    .orElseThrow(() -> new ValidationException("Invalid clinic registration code"));
        }

        Provider provider = Provider.builder()
                .name(request.getName())
                .role(request.getRole())
                .clinic(clinic)
                .region(request.getRegion())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        provider = providerRepository.save(provider);

        String token = jwtUtil.generateToken(provider.getEmail(), provider.getRole());

        AuthResponse.AuthResponseBuilder responseBuilder = AuthResponse.builder()
                .token(token)
                .email(provider.getEmail())
                .name(provider.getName())
                .role(provider.getRole());

        if (clinic != null) {
            responseBuilder
                    .clinicId(clinic.getId())
                    .clinicName(clinic.getName());
        }

        return responseBuilder.build();
    }

    public AuthResponse login(AuthRequest request) {
        Provider provider = providerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), provider.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(provider.getEmail(), provider.getRole());

        AuthResponse.AuthResponseBuilder responseBuilder = AuthResponse.builder()
                .token(token)
                .email(provider.getEmail())
                .name(provider.getName())
                .role(provider.getRole());

        if (provider.getClinic() != null) {
            responseBuilder
                    .clinicId(provider.getClinic().getId())
                    .clinicName(provider.getClinic().getName());
        }

        return responseBuilder.build();
    }
}

