package com.asakaa.synthesis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "treatments")
public class Treatment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(length = 500)
    private String type;

    @Column(name = "drug_name")
    private String drugName;

    @Column(columnDefinition = "TEXT")
    private String dosage;

    @Column(length = 500)
    private String duration;

    @Column(columnDefinition = "TEXT")
    private String instructions;
}
