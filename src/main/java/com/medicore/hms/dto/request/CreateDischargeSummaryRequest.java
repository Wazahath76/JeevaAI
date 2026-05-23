package com.medicore.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateDischargeSummaryRequest {
    @NotBlank(message = "Final diagnosis is required")
    private String finalDiagnosis;
    private String hospitalCourse;
    private String proceduresPerformed;
    private String conditionAtDischarge;
    private String dischargeMedications;
    private String followUpInstructions;
    private LocalDateTime followUpDate;
    private String dietAdvice;
    private String activityRestrictions;
}
