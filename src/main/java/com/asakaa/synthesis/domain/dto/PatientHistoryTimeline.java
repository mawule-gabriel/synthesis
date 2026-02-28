package com.asakaa.synthesis.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryTimeline {

    private String patientId;
    private String timeframePeriod;
    private List<PatientHistoryTimelineEvent> events;
    private int totalConsultations;
    private int totalDiagnoses;
    private int totalLabResults;
    private int totalImagingFindings;
}
