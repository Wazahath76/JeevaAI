package com.medicore.hms.controller;

import com.medicore.hms.dto.response.AnalyticsDashboardResponse;
import com.medicore.hms.dto.response.ApiResponse;
import com.medicore.hms.dto.response.DoctorAnalyticsResponse;
import com.medicore.hms.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/hospital
     * Hospital-wide dashboard: patient counts, AI stats, admission trends, top diagnoses.
     * Accessible to Super Specialists and Admins.
     */
    @GetMapping("/hospital")
    @PreAuthorize("hasAnyRole('DOCTOR_SUPER_SPECIALIST','ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getHospitalDashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getHospitalDashboard()));
    }

    /**
     * GET /api/analytics/doctor/{id}
     * Per-doctor analytics: patient load, AI approval rate, prescriptions, notes.
     * Super Specialists can view any doctor's stats.
     */
    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR_SUPER_SPECIALIST','ADMIN')")
    public ResponseEntity<ApiResponse<DoctorAnalyticsResponse>> getDoctorAnalytics(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDoctorAnalytics(id)));
    }

    /**
     * GET /api/analytics/me
     * Any doctor can see their own analytics.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DoctorAnalyticsResponse>> getMyAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getMyAnalytics()));
    }
}
