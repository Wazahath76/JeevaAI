package com.medicore.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RevokeAssignmentRequest {

    @NotNull(message = "Assignment ID is required")
    private UUID assignmentId;

    @NotBlank(message = "Revocation reason is required")
    private String revocationReason;
}
