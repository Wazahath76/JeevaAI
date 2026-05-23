package com.medicore.hms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discharge_summaries",
    indexes = @Index(name = "idx_discharge_patient", columnList = "patient_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeSummary extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discharged_by")
    private Doctor dischargedBy;

    @Column(name = "discharge_date", nullable = false)
    private LocalDateTime dischargeDate;

    @Column(name = "final_diagnosis", columnDefinition = "TEXT")
    private String finalDiagnosis;

    @Column(name = "hospital_course", columnDefinition = "TEXT")
    private String hospitalCourse;

    @Column(name = "procedures_performed", columnDefinition = "TEXT")
    private String proceduresPerformed;

    @Column(name = "condition_at_discharge", length = 100)
    private String conditionAtDischarge;

    @Column(name = "discharge_medications", columnDefinition = "TEXT")
    private String dischargeMedications;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "diet_advice", columnDefinition = "TEXT")
    private String dietAdvice;

    @Column(name = "activity_restrictions", columnDefinition = "TEXT")
    private String activityRestrictions;

    @Column(name = "summary_pdf_url", length = 1000)
    private String summaryPdfUrl;
}
