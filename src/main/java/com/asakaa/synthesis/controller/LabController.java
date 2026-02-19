package com.asakaa.synthesis.controller;

import com.asakaa.synthesis.domain.dto.request.LabResultRequest;
import com.asakaa.synthesis.domain.dto.response.LabResultResponse;
import com.asakaa.synthesis.service.LabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/labs")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @PostMapping
    public ResponseEntity<LabResultResponse> addLabResult(@Valid @RequestBody LabResultRequest request) {
        LabResultResponse response = labService.addLabResult(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<LabResultResponse>> getByConsultation(@PathVariable Long consultationId) {
        List<LabResultResponse> response = labService.getLabResultsByConsultation(consultationId);
        return ResponseEntity.ok(response);
    }
}
