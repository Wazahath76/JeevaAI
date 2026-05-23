package com.medicore.hms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
    name = "lab_results",
    indexes = {
        @Index(name = "idx_lab_patient", columnList = "patient_id"),
        @Index(name = "idx_lab_ordered_by", columnList = "ordered_by")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResult extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by")
    private Doctor orderedBy;

    @NotBlank
    @Column(name = "test_name", nullable = false, length = 255)
    private String testName;

    @Column(name = "test_category", length = 100) // e.g. Haematology, Biochemistry, Radiology
    private String testCategory;

    @Column(name = "result_value", columnDefinition = "TEXT")
    private String resultValue;

    @Column(name = "reference_range", length = 255)
    private String referenceRange;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "report_url", length = 1000)
    private String reportUrl; // S3/GCS file link

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
