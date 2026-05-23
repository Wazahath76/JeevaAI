package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DischargeSummaryResponse {
    private final UUID id;
    private final String finalDiagnosis;
    private final String hospitalCourse;
    private final String proceduresPerformed;
    private final String conditionAtDischarge;
    private final String dischargeMedications;
    private final String followUpInstructions;
    private final LocalDateTime followUpDate;
    private final String dietAdvice;
    private final String activityRestrictions;
    private final String dischargedByName;
    private final LocalDateTime dischargeDate;
    private final LocalDateTime createdAt;
}
