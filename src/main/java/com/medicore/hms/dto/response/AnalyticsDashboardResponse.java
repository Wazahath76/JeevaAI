package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsDashboardResponse {
    // Patient stats
    private final long totalPatients;
    private final long activePatients;
    private final long opdPatients;
    private final long ipdPatients;
    private final long criticalPatients;
    private final long dischargedToday;

    // Doctor stats
    private final long totalDoctors;
    private final long availableDoctors;

    // AI stats
    private final long totalRecommendations;
    private final long approvedRecommendations;
    private final long rejectedRecommendations;
    private final long modifiedRecommendations;
    private final long pendingRecommendations;

    // Charts data
    private final Map<String, Long> patientsByStatus;
    private final Map<String, Long> doctorsByDepartment;
    private final Map<String, Long> doctorsByRole;
    private final List<Map<String, Object>> admissionTrend;     // last 30 days
    private final List<Map<String, Object>> topDiagnoses;       // top 10
    private final Map<String, Long> aiRecommendationsByStatus;
}
