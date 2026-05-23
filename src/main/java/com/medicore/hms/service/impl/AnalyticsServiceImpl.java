package com.medicore.hms.service.impl;

import com.medicore.hms.dto.response.AnalyticsDashboardResponse;
import com.medicore.hms.dto.response.DoctorAnalyticsResponse;
import com.medicore.hms.entity.Doctor;
import com.medicore.hms.enums.PatientStatus;
import com.medicore.hms.enums.RecommendationStatus;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.repository.*;
import com.medicore.hms.service.AnalyticsService;
import com.medicore.hms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PatientRepository            patientRepository;
    private final DoctorRepository             doctorRepository;
    private final AiRecommendationRepository   aiRecRepository;
    private final DiagnosisRepository          diagnosisRepository;
    private final TreatmentRepository          treatmentRepository;
    private final ConsultationNoteRepository   noteRepository;
    private final DoctorPatientAssignmentRepository assignmentRepository;
    private final SecurityUtil                 securityUtil;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse getHospitalDashboard() {

        // ── Patient counts ────────────────────────────────────────────────────
        long totalPatients    = patientRepository.count();
        long opdPatients      = patientRepository.findByStatus(PatientStatus.OPD,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long ipdPatients      = patientRepository.findByStatus(PatientStatus.IPD,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long criticalPatients = patientRepository.findByStatus(PatientStatus.CRITICAL,
            org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long activePatients   = opdPatients + ipdPatients + criticalPatients;

        // Discharged today
        long dischargedToday = patientRepository
            .countAdmissionsByDate(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                LocalDateTime.now()
            ).size(); // proxy using discharge events today

        // ── Doctor counts ─────────────────────────────────────────────────────
        long totalDoctors     = doctorRepository.count();
        long availableDoctors = doctorRepository.findAvailableDoctors(null, null).size();

        // ── AI stats ──────────────────────────────────────────────────────────
        List<Object[]> aiStats = aiRecRepository.countByStatus();
        long totalRecs = 0, approvedRecs = 0, rejectedRecs = 0, modifiedRecs = 0, pendingRecs = 0;
        for (Object[] row : aiStats) {
            RecommendationStatus status = (RecommendationStatus) row[0];
            long count = ((Number) row[1]).longValue();
            totalRecs += count;
            switch (status) {
                case APPROVED        -> approvedRecs = count;
                case REJECTED        -> rejectedRecs = count;
                case MODIFIED        -> modifiedRecs = count;
                case PENDING_REVIEW  -> pendingRecs  = count;
            }
        }

        // ── Charts: patients by status ────────────────────────────────────────
        Map<String, Long> patientsByStatus = new LinkedHashMap<>();
        patientRepository.countByStatus().forEach(row ->
            patientsByStatus.put(row[0].toString(), ((Number) row[1]).longValue()));

        // ── Charts: doctors by department ─────────────────────────────────────
        Map<String, Long> doctorsByDept = new LinkedHashMap<>();
        doctorRepository.countByDepartment().forEach(row -> {
            String dept = row[0] != null ? row[0].toString() : "Unassigned";
            doctorsByDept.put(dept, ((Number) row[1]).longValue());
        });

        // ── Charts: doctors by role ───────────────────────────────────────────
        Map<String, Long> doctorsByRole = new LinkedHashMap<>();
        doctorRepository.countByRole().forEach(row ->
            doctorsByRole.put(row[0].toString(), ((Number) row[1]).longValue()));

        // ── Charts: admission trend (last 30 days) ────────────────────────────
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Map<String, Object>> admissionTrend = new ArrayList<>();
        patientRepository.countAdmissionsByDate(thirtyDaysAgo, LocalDateTime.now())
            .forEach(row -> admissionTrend.add(Map.of(
                "date",  row[0].toString(),
                "count", ((Number) row[1]).longValue()
            )));

        // ── Charts: top diagnoses ─────────────────────────────────────────────
        List<Map<String, Object>> topDiagnoses = new ArrayList<>();
        diagnosisRepository.findTopDiagnoses().forEach(row ->
            topDiagnoses.add(Map.of(
                "name",  row[0].toString(),
                "count", ((Number) row[1]).longValue()
            )));

        // ── Charts: AI by status ──────────────────────────────────────────────
        Map<String, Long> aiByStatus = new LinkedHashMap<>();
        aiStats.forEach(row -> aiByStatus.put(row[0].toString(), ((Number) row[1]).longValue()));

        return AnalyticsDashboardResponse.builder()
            .totalPatients(totalPatients)
            .activePatients(activePatients)
            .opdPatients(opdPatients)
            .ipdPatients(ipdPatients)
            .criticalPatients(criticalPatients)
            .dischargedToday(dischargedToday)
            .totalDoctors(totalDoctors)
            .availableDoctors(availableDoctors)
            .totalRecommendations(totalRecs)
            .approvedRecommendations(approvedRecs)
            .rejectedRecommendations(rejectedRecs)
            .modifiedRecommendations(modifiedRecs)
            .pendingRecommendations(pendingRecs)
            .patientsByStatus(patientsByStatus)
            .doctorsByDepartment(doctorsByDept)
            .doctorsByRole(doctorsByRole)
            .admissionTrend(admissionTrend)
            .topDiagnoses(topDiagnoses)
            .aiRecommendationsByStatus(aiByStatus)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorAnalyticsResponse getDoctorAnalytics(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        return buildDoctorStats(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorAnalyticsResponse getMyAnalytics() {
        return buildDoctorStats(securityUtil.getCurrentDoctor());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private DoctorAnalyticsResponse buildDoctorStats(Doctor doctor) {
        UUID id = doctor.getId();

        long activePatients = assignmentRepository.countActivePatients(id);
        long totalPrimary   = patientRepository.findByPrimaryDoctorId(
            id, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();

        long totalRecs    = aiRecRepository.findByRequestedByIdOrderByCreatedAtDesc(id).size();
        long approved     = aiRecRepository.countByDoctorAndStatus(id, RecommendationStatus.APPROVED);
        long rejected     = aiRecRepository.countByDoctorAndStatus(id, RecommendationStatus.REJECTED);
        long modified     = aiRecRepository.countByDoctorAndStatus(id, RecommendationStatus.MODIFIED);
        long pending      = aiRecRepository.countByDoctorAndStatus(id, RecommendationStatus.PENDING_REVIEW);

        long reviewed     = approved + rejected + modified;
        double approvalRate = reviewed > 0
            ? Math.round(((double) approved / reviewed) * 1000.0) / 10.0
            : 0.0;

        long totalNotes       = noteRepository.findByAuthorIdOrderByCreatedAtDesc(id).size();
        long totalTreatments  = treatmentRepository.findByPrescribedByIdOrderByCreatedAtDesc(id).size();

        return DoctorAnalyticsResponse.builder()
            .doctorId(doctor.getId())
            .doctorName(doctor.getFullName())
            .role(doctor.getRole().name())
            .department(doctor.getDepartment())
            .activePatientsCount(activePatients)
            .totalPatientsEver(totalPrimary + activePatients)
            .totalRecommendationsRequested(totalRecs)
            .recommendationsApproved(approved)
            .recommendationsRejected(rejected)
            .recommendationsModified(modified)
            .recommendationsPending(pending)
            .approvalRate(approvalRate)
            .totalNotes(totalNotes)
            .totalTreatmentsPrescribed(totalTreatments)
            .build();
    }
}
