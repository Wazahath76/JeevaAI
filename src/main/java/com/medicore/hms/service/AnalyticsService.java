package com.medicore.hms.service;

import com.medicore.hms.dto.response.AnalyticsDashboardResponse;
import com.medicore.hms.dto.response.DoctorAnalyticsResponse;

import java.util.UUID;

public interface AnalyticsService {
    AnalyticsDashboardResponse getHospitalDashboard();
    DoctorAnalyticsResponse    getDoctorAnalytics(UUID doctorId);
    DoctorAnalyticsResponse    getMyAnalytics();
}
