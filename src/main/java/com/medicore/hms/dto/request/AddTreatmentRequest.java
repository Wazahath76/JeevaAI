package com.medicore.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddTreatmentRequest {
    @NotBlank(message = "Drug name is required")
    private String drugName;
    private String dosage;
    private String frequency;
    private String duration;
    private String routeOfAdministration;
    private String specialInstructions;
    private String treatmentLine;
    private String notes;
}
