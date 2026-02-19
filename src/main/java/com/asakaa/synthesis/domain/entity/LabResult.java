package com.asakaa.synthesis.domain.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "lab_results")
public class LabResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "numeric_value", precision = 19, scale = 4)
    private BigDecimal numericValue;

    private String unit;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal;

    @Column(name = "reference_range")
    private String referenceRange;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
