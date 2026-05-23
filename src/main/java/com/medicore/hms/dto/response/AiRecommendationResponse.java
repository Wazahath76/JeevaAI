package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.RecommendationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiRecommendationResponse {
    private final UUID id;
    private final String suggestedTreatment;
    private final String suggestedDrugs;
    private final String contraindicationWarnings;
    private final String referralSuggestion;
    private final String urgencyLevel;
    private final RecommendationStatus status;
    private final String doctorNotes;
    private final String rejectionReason;
    private final String modifiedTreatment;
    private final String requestedByName;
    private final String reviewedByName;
    private final String aiModel;
    private final LocalDateTime reviewedAt;
    private final LocalDateTime createdAt;
}
