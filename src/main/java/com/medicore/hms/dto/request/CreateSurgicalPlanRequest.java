package com.medicore.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateSurgicalPlanRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotBlank(message = "Procedure name is required")
    private String procedureName;

    private String procedureCode;
    private UUID   surgeonId;
    private UUID   anaesthetistId;
    private LocalDateTime scheduledAt;
    private String anaesthesiaType;
    private String preOpNotes;
    private String theatreNumber;
}
