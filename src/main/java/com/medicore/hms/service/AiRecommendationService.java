package com.medicore.hms.service;

import com.medicore.hms.dto.request.ReviewRecommendationRequest;
import com.medicore.hms.dto.response.AiRecommendationResponse;

import java.util.List;
import java.util.UUID;

public interface AiRecommendationService {
    AiRecommendationResponse requestRecommendation(UUID patientId);
    AiRecommendationResponse approveRecommendation(UUID recommendationId, ReviewRecommendationRequest request);
    AiRecommendationResponse rejectRecommendation(UUID recommendationId, ReviewRecommendationRequest request);
    AiRecommendationResponse modifyRecommendation(UUID recommendationId, ReviewRecommendationRequest request);
    List<AiRecommendationResponse> getRecommendationsForPatient(UUID patientId);
}
