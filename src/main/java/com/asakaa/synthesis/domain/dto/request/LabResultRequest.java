package com.asakaa.synthesis.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResultRequest {

    @NotNull(message = "Consultation ID is required")
    private Long consultationId;

    @NotBlank(message = "Test name is required")
    private String testName;

    private BigDecimal numericValue;
    private String unit;
    private Boolean isAbnormal;
    private String referenceRange;
}
