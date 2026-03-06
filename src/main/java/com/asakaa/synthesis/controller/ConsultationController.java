package com.asakaa.synthesis.controller;

import com.asakaa.synthesis.domain.dto.request.ConsultationRequest;
import com.asakaa.synthesis.domain.dto.request.ConsultationUpdateRequest;
import com.asakaa.synthesis.domain.dto.response.ConsultationResponse;
import com.asakaa.synthesis.repository.ProviderRepository;
import com.asakaa.synthesis.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ProviderRepository providerRepository;

    @PostMapping
    public ResponseEntity<ConsultationResponse> openConsultation(
            @Valid @RequestBody ConsultationRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Long providerId = providerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Provider not found"))
                .getId();

        ConsultationResponse response = consultationService.openConsultation(request, providerId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultationResponse> updateConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationUpdateRequest request,
            Authentication authentication) {
        ConsultationResponse response = consultationService.updateConsultation(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ConsultationResponse> closeConsultation(
            @PathVariable Long id,
            Authentication authentication) {
        ConsultationResponse response = consultationService.closeConsultation(id, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getConsultationById(
            @PathVariable Long id,
            Authentication authentication) {
        ConsultationResponse response = consultationService.getConsultationById(id, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ConsultationResponse> getConsultationHistory(
            @PathVariable Long id,
            Authentication authentication) {
        ConsultationResponse response = consultationService.getConsultationById(id, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider/active")
    public ResponseEntity<List<ConsultationResponse>> getActiveConsultations(Authentication authentication) {
        String email = authentication.getName();
        Long providerId = providerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Provider not found"))
                .getId();

        List<ConsultationResponse> response = consultationService.getActiveConsultationsByProvider(providerId, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<ConsultationResponse>> getPatientConsultationHistory(
            @PathVariable Long patientId,
            Authentication authentication) {
        List<ConsultationResponse> response = consultationService.getConsultationsByPatient(patientId, authentication);
        return ResponseEntity.ok(response);
    }

}
