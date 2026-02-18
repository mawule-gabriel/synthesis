package com.asakaa.synthesis.controller;

import com.asakaa.synthesis.domain.dto.response.TranscriptionResponse;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/transcribe")
@RequiredArgsConstructor
public class TranscribeController {

    private final TranscriptionService transcriptionService;

    private static final Set<String> SUPPORTED_AUDIO_TYPES = Set.of(
            "audio/wav",
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/x-m4a",
            "audio/m4a"
    );

    @PostMapping
    public ResponseEntity<TranscriptionResponse> transcribe(
            @RequestParam("audio") MultipartFile audio) {

        if (audio.isEmpty()) {
            throw new ValidationException("Audio file is required.");
        }

        String contentType = audio.getContentType();
        if (contentType == null || !SUPPORTED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException(
                    "Unsupported audio format: " + contentType +
                    ". Supported formats: WAV, MP3, MP4/M4A.");
        }

        TranscriptionResponse response = transcriptionService.transcribe(audio);
        return ResponseEntity.ok(response);
    }
}
