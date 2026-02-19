package com.asakaa.synthesis.controller;

import com.asakaa.synthesis.domain.dto.request.DiagnosticRequest;
import com.asakaa.synthesis.domain.dto.response.DiagnosticResponse;
import com.asakaa.synthesis.service.DiagnosticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/diagnostic")
@RequiredArgsConstructor
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    @PostMapping("/analyze")
    public ResponseEntity<DiagnosticResponse> analyze(@Valid @RequestBody DiagnosticRequest request) {
        DiagnosticResponse response = diagnosticService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<com.asakaa.synthesis.domain.dto.response.ImageAnalysisResponse> analyzeImage(
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            @RequestParam(value = "consultationId", required = false) Long consultationId,
            @RequestParam(value = "clinicalContext", required = false) String clinicalContext) {
        
        try {
            // Validate file is not empty
            if (image.isEmpty()) {
                throw new com.asakaa.synthesis.exception.ValidationException("Image file is required");
            }

            // Get file bytes and media type
            byte[] imageBytes = image.getBytes();
            String mediaType = image.getContentType();

            // Validate media type
            if (mediaType == null || (!mediaType.equals("image/jpeg") && !mediaType.equals("image/png"))) {
                throw new com.asakaa.synthesis.exception.ValidationException(
                        "Invalid file type. Only JPEG and PNG images are supported.");
            }

            // Call service
            com.asakaa.synthesis.domain.dto.response.ImageAnalysisResponse response = 
                    diagnosticService.analyzeImage(imageBytes, mediaType, clinicalContext, consultationId);

            return ResponseEntity.ok(response);

        } catch (java.io.IOException e) {
            throw new com.asakaa.synthesis.exception.DiagnosticException(
                    "Failed to read image file. The file may be corrupted.", e);
        }
    }
}
