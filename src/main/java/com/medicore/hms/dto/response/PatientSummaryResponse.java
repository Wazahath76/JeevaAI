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

// Lightweight card used in patient list
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientSummaryResponse {
    private final UUID id;
    private final String patientCode;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final Integer age;
    private final Gender gender;
    private final BloodGroup bloodGroup;
    private final String phone;
    private final PatientStatus status;
    private final String ward;
    private final String bedNumber;
    private final String primaryDoctorName;
    private final LocalDateTime admissionDate;
    private final LocalDateTime createdAt;
}
