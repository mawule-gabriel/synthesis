package com.asakaa.synthesis.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResultResponse {
    private Long id;
    private Long consultationId;
    private String testName;
    private BigDecimal numericValue;
    private String unit;
    private Boolean isAbnormal;
    private String referenceRange;
    private LocalDateTime recordedAt;
}
