package com.medicore.hms.dto.request;

import com.medicore.hms.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddDiagnosisRequest {
    @NotBlank(message = "Diagnosis name is required")
    private String diagnosisName;
    private String icd10Code;
    private Boolean isPrimary = false;
    private Severity severity;
    private String description;
    private String differentialDiagnosis;
}
