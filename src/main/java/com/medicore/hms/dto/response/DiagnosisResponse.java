package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.Severity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosisResponse {
    private final UUID id;
    private final String diagnosisName;
    private final String icd10Code;
    private final Boolean isPrimary;
    private final Severity severity;
    private final String description;
    private final String differentialDiagnosis;
    private final Boolean isActive;
    private final String diagnosedByName;
    private final LocalDateTime createdAt;
}
