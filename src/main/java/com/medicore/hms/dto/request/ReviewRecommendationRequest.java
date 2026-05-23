package com.medicore.hms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRecommendationRequest {
    // For APPROVE / REJECT / MODIFY
    private String doctorNotes;
    private String rejectionReason;      // required when REJECT
    private String modifiedTreatment;    // required when MODIFY
}
