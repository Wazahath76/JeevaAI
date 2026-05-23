package com.medicore.hms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "patient_vitals",
    indexes = {
        @Index(name = "idx_vitals_patient", columnList = "patient_id"),
        @Index(name = "idx_vitals_recorded_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientVital extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private Doctor recordedBy;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "pulse_bpm")
    private Integer pulseBpm;

    @Column(name = "temperature_c", precision = 4, scale = 1)
    private BigDecimal temperatureCelsius;

    @Column(name = "spo2_percent")
    private Integer spo2Percent;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "blood_glucose_mg_dl")
    private Integer bloodGlucose;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
