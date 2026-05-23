package com.medicore.hms.entity;

import com.medicore.hms.enums.Severity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
    name = "diagnoses",
    indexes = {
        @Index(name = "idx_diagnosis_patient", columnList = "patient_id"),
        @Index(name = "idx_diagnosis_doctor", columnList = "diagnosed_by")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosed_by", nullable = false)
    private Doctor diagnosedBy;

    @NotBlank
    @Column(name = "diagnosis_name", nullable = false, length = 500)
    private String diagnosisName;

    @Column(name = "icd10_code", length = 20)
    private String icd10Code;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "differential_diagnosis", columnDefinition = "TEXT")
    private String differentialDiagnosis;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
