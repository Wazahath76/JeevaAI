package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.BloodGroup;
import com.medicore.hms.enums.Gender;
import com.medicore.hms.enums.PatientStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientResponse {

    private final UUID id;
    private final String patientCode;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final Integer age;
    private final Gender gender;
    private final BloodGroup bloodGroup;
    private final String phone;
    private final String email;
    private final String address;

    // Emergency contact
    private final String emergencyContactName;
    private final String emergencyContactPhone;
    private final String emergencyContactRelation;

    // Medical background
    private final String knownAllergies;
    private final String chronicConditions;
    private final String familyHistory;
    private final String pastSurgeries;

    // Admission
    private final PatientStatus status;
    private final String ward;
    private final String bedNumber;
    private final LocalDateTime admissionDate;
    private final LocalDateTime dischargeDate;

    // Insurance
    private final String insuranceProvider;
    private final String insurancePolicyNo;

    // Doctors
    private final DoctorResponse primaryDoctor;
    private final DoctorResponse admittedBy;

    private final String notes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
