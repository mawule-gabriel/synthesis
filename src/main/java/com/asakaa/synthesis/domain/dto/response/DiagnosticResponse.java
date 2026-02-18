package com.asakaa.synthesis.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticResponse {

    private Long consultationId;
    private List<DifferentialDto> differentials;
    private List<String> immediateActions;
    private String safetyNotes;
    private List<String> nextQuestions;
    private List<String> physicalExams;
    private List<String> citations;
    private LocalDateTime generatedAt;
}
