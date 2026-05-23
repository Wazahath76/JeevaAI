package com.medicore.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddLabResultRequest {
    @NotBlank(message = "Test name is required")
    private String testName;
    private String testCategory;
    private String resultValue;
    private String referenceRange;
    private String unit;
    private Boolean isAbnormal = false;
    private String reportUrl;
    private String remarks;
}
