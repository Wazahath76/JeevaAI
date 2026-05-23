package com.medicore.hms.dto.request;

import com.medicore.hms.enums.DoctorRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignDoctorRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Assignment role is required")
    private DoctorRole assignmentRole;

    private String assignmentReason;
}
