package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.AssignmentStatus;
import com.medicore.hms.enums.DoctorRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignmentResponse {
    private final UUID id;
    private final UUID patientId;
    private final String patientCode;
    private final String patientName;
    private final UUID assignedDoctorId;
    private final String assignedDoctorName;
    private final String assignedDoctorSpecialization;
    private final DoctorRole assignmentRole;
    private final AssignmentStatus status;
    private final String assignedByName;
    private final String assignmentReason;
    private final String revocationReason;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;
}
