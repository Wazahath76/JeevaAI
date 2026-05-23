package com.medicore.hms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
    name = "treatments",
    indexes = {
        @Index(name = "idx_treatment_patient", columnList = "patient_id"),
        @Index(name = "idx_treatment_doctor", columnList = "prescribed_by")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treatment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescribed_by", nullable = false)
    private Doctor prescribedBy;

    @NotBlank
    @Column(name = "drug_name", nullable = false, length = 255)
    private String drugName;

    @Column(length = 100)
    private String dosage; // e.g., "500mg"

    @Column(length = 100)
    private String frequency; // e.g., "Twice daily"

    @Column(length = 100)
    private String duration; // e.g., "7 days"

    @Column(name = "route_of_administration", length = 100)
    private String routeOfAdministration; // Oral, IV, IM, etc.

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_ai_suggested", nullable = false)
    @Builder.Default
    private Boolean isAiSuggested = false;

    @Column(name = "contraindication_warning", columnDefinition = "TEXT")
    private String contraindicationWarning;

    // Line of treatment context
    @Column(name = "treatment_line", length = 500)
    private String treatmentLine; // e.g., "First-line", "Adjuvant therapy"

    @Column(columnDefinition = "TEXT")
    private String notes;
}
