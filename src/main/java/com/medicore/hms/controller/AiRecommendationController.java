package com.medicore.hms.controller;

import com.medicore.hms.dto.request.ReviewRecommendationRequest;
import com.medicore.hms.dto.response.AiRecommendationResponse;
import com.medicore.hms.dto.response.ApiResponse;
import com.medicore.hms.service.AiRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiService;

    /**
     * POST /api/ai/recommend/{patientId}
     * Trigger an AI recommendation for a patient.
     * Builds clinical context from all patient data and calls Claude.
     */
    @PostMapping("/recommend/{patientId}")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> requestRecommendation(
        @PathVariable UUID patientId
    ) {
        AiRecommendationResponse response = aiService.requestRecommendation(patientId);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("AI recommendation generated. Awaiting doctor review.", response));
    }

    /**
     * GET /api/ai/recommendations/patient/{patientId}
     * All AI recommendations for a patient.
     */
    @GetMapping("/recommendations/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AiRecommendationResponse>>> getForPatient(
        @PathVariable UUID patientId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(aiService.getRecommendationsForPatient(patientId))
        );
    }

    /**
     * PUT /api/ai/recommendations/{id}/approve
     * Doctor approves the AI recommendation with optional notes.
     */
    @PutMapping("/recommendations/{id}/approve")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> approve(
        @PathVariable UUID id,
        @RequestBody(required = false) ReviewRecommendationRequest request
    ) {
        ReviewRecommendationRequest req = request != null ? request : new ReviewRecommendationRequest();
        return ResponseEntity.ok(
            ApiResponse.success("Recommendation approved", aiService.approveRecommendation(id, req))
        );
    }

    /**
     * PUT /api/ai/recommendations/{id}/reject
     * Doctor rejects with mandatory reason.
     */
    @PutMapping("/recommendations/{id}/reject")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> reject(
        @PathVariable UUID id,
        @Valid @RequestBody ReviewRecommendationRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Recommendation rejected", aiService.rejectRecommendation(id, request))
        );
    }

    /**
     * PUT /api/ai/recommendations/{id}/modify
     * Doctor accepts the recommendation with their own modified treatment plan.
     */
    @PutMapping("/recommendations/{id}/modify")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> modify(
        @PathVariable UUID id,
        @Valid @RequestBody ReviewRecommendationRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Recommendation modified and saved", aiService.modifyRecommendation(id, request))
        );
    }
}
