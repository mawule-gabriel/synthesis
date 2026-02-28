package com.asakaa.synthesis.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryTimelineEvent {

    private LocalDateTime eventDate;
    private String eventType;
    private String description;
    private String consultationId;
    private String details;
}
