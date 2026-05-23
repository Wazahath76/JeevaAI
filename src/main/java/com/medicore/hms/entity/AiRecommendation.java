package com.medicore.hms.entity;

import com.medicore.hms.enums.RecommendationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ai_recommendations",
    indexes = {
        @Index(name = "idx_ai_rec_patient", columnList = "patient_id"),
        @Index(name = "idx_ai_rec_status", columnList = "status"),
        @Index(name = "idx_ai_rec_reviewed_by", columnList = "reviewed_by")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private Doctor requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Doctor reviewedBy;

    // Raw prompt sent to Claude
    @Column(name = "prompt_context", columnDefinition = "TEXT")
    private String promptContext;

    // Full Claude response
    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;

    // Structured extracted fields
    @Column(name = "suggested_treatment", columnDefinition = "TEXT")
    private String suggestedTreatment;

    @Column(name = "suggested_drugs", columnDefinition = "TEXT")
    private String suggestedDrugs;

    @Column(name = "contraindication_warnings", columnDefinition = "TEXT")
    private String contraindicationWarnings;

    @Column(name = "referral_suggestion", columnDefinition = "TEXT")
    private String referralSuggestion;

    @Column(name = "urgency_level", length = 20)
    private String urgencyLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING_REVIEW;

    // Doctor's decision
    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Doctor's modified version (if MODIFIED)
    @Column(name = "modified_treatment", columnDefinition = "TEXT")
    private String modifiedTreatment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Model used for traceability
    @Column(name = "ai_model", length = 100)
    @Builder.Default
    private String aiModel = "claude-sonnet-4-20250514";
}
