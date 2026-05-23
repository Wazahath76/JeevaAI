package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorAnalyticsResponse {
    private final UUID doctorId;
    private final String doctorName;
    private final String role;
    private final String department;
    private final long activePatientsCount;
    private final long totalPatientsEver;
    private final long totalRecommendationsRequested;
    private final long recommendationsApproved;
    private final long recommendationsRejected;
    private final long recommendationsModified;
    private final long recommendationsPending;
    private final double approvalRate;    // percentage
    private final long totalNotes;
    private final long totalTreatmentsPrescribed;
}
