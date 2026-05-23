package com.medicore.hms.entity;

import com.medicore.hms.enums.DoctorRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "doctors",
    indexes = {
        @Index(name = "idx_doctor_email", columnList = "email"),
        @Index(name = "idx_doctor_role", columnList = "role"),
        @Index(name = "idx_doctor_department", columnList = "department"),
        @Index(name = "idx_doctor_available", columnList = "is_available, is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DoctorRole role;

    @Column(length = 255)
    private String specialization;

    @Column(length = 255)
    private String department;

    @NotBlank
    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    // Patients this doctor is primarily responsible for
    @OneToMany(mappedBy = "primaryDoctor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Patient> primaryPatients = new ArrayList<>();

    // Assignments as consulting/specialist doctor
    @OneToMany(mappedBy = "assignedDoctor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DoctorPatientAssignment> assignments = new ArrayList<>();

    // Notes written by this doctor
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConsultationNote> notes = new ArrayList<>();
}
