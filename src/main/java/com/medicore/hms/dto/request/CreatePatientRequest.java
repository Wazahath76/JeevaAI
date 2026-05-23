package com.medicore.hms.dto.request;

import com.medicore.hms.enums.BloodGroup;
import com.medicore.hms.enums.Gender;
import com.medicore.hms.enums.PatientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreatePatientRequest {

    @NotBlank(message = "Patient full name is required")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private BloodGroup bloodGroup;

    private String phone;
    private String email;
    private String address;

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;

    // Medical background
    private String knownAllergies;       // comma-separated
    private String chronicConditions;    // comma-separated
    private String familyHistory;
    private String pastSurgeries;

    // Admission details
    @NotNull(message = "Patient status is required")
    private PatientStatus status;

    private String ward;
    private String bedNumber;

    // Insurance
    private String insuranceProvider;
    private String insurancePolicyNo;

    // Primary doctor (defaults to current logged-in doctor if not given)
    private UUID primaryDoctorId;

    private String notes;
}
