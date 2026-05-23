package com.medicore.hms.entity;

import com.medicore.hms.enums.BloodGroup;
import com.medicore.hms.enums.Gender;
import com.medicore.hms.enums.PatientStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "patients",
    indexes = {
        @Index(name = "idx_patient_code", columnList = "patient_code"),
        @Index(name = "idx_patient_status", columnList = "status"),
        @Index(name = "idx_patient_primary_doctor", columnList = "primary_doctor_id"),
        @Index(name = "idx_patient_admitted_by", columnList = "admitted_by")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    @NotBlank
    @Column(name = "patient_code", nullable = false, unique = true, length = 20)
    private String patientCode; // e.g. MED-2024-0001

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 10)
    @Builder.Default
    private BloodGroup bloodGroup = BloodGroup.UNKNOWN;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 255)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation", length = 100)
    private String emergencyContactRelation;

    // Medical Background - stored as comma-separated or JSON string for simplicity
    @Column(name = "known_allergies", columnDefinition = "TEXT")
    private String knownAllergies; // comma-separated

    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions; // comma-separated

    @Column(name = "family_history", columnDefinition = "TEXT")
    private String familyHistory;

    @Column(name = "past_surgeries", columnDefinition = "TEXT")
    private String pastSurgeries;

    // Admission
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PatientStatus status = PatientStatus.OPD;

    @Column(length = 100)
    private String ward;

    @Column(name = "bed_number", length = 20)
    private String bedNumber;

    @Column(name = "admission_date")
    private LocalDateTime admissionDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    // Insurance
    @Column(name = "insurance_provider", length = 255)
    private String insuranceProvider;

    @Column(name = "insurance_policy_no", length = 255)
    private String insurancePolicyNo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_by")
    private Doctor admittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id")
    private Doctor primaryDoctor;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PatientVital> vitals = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Treatment> treatments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConsultationNote> consultationNotes = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DoctorPatientAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AiRecommendation> aiRecommendations = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LabResult> labResults = new ArrayList<>();

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DischargeSummary dischargeSummary;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SurgicalPlan> surgicalPlans = new ArrayList<>();
}
