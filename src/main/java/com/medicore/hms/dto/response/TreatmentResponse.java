package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreatmentResponse {
    private final UUID id;
    private final String drugName;
    private final String dosage;
    private final String frequency;
    private final String duration;
    private final String routeOfAdministration;
    private final String specialInstructions;
    private final String treatmentLine;
    private final Boolean isActive;
    private final Boolean isAiSuggested;
    private final String contraindicationWarning;
    private final String notes;
    private final String prescribedByName;
    private final LocalDateTime createdAt;
}
