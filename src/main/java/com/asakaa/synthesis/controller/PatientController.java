package com.asakaa.synthesis.controller;

import com.asakaa.synthesis.domain.dto.request.PatientRequest;
import com.asakaa.synthesis.domain.dto.response.PatientResponse;
import com.asakaa.synthesis.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request,
            Authentication authentication) {
        PatientResponse response = patientService.createPatient(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id,
            Authentication authentication) {
        PatientResponse response = patientService.getPatientById(id, authentication);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request,
            Authentication authentication) {
        PatientResponse response = patientService.updatePatient(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            Pageable pageable,
            Authentication authentication) {
        Page<PatientResponse> response = patientService.getAllPatients(pageable, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PatientResponse>> searchPatients(
            @RequestParam("q") String query,
            Pageable pageable,
            Authentication authentication) {
        Page<PatientResponse> response = patientService.searchPatients(query, pageable, authentication);
        return ResponseEntity.ok(response);
    }
}
