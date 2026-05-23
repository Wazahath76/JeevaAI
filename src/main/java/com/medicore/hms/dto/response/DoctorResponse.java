package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.DoctorRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponse {

    private final UUID id;
    private final String fullName;
    private final String email;
    private final DoctorRole role;
    private final String specialization;
    private final String department;
    private final String licenseNumber;
    private final String phone;
    private final String qualification;
    private final Integer experienceYears;
    private final Boolean isActive;
    private final Boolean isAvailable;
    private final String profileImageUrl;
    private final LocalDateTime createdAt;

    // Stats (populated selectively for analytics views)
    private final Long activePatientsCount;
    private final Long totalPatientsCount;
}
