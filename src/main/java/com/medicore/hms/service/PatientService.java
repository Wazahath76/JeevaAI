package com.medicore.hms.service;

import com.medicore.hms.dto.request.*;
import com.medicore.hms.dto.response.*;
import com.medicore.hms.enums.PatientStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PatientService {

    // ── Core CRUD ────────────────────────────────────────────────────────────
    PatientResponse         createPatient(CreatePatientRequest request);
    PatientResponse         getPatientById(UUID id);
    PatientResponse         updatePatient(UUID id, UpdatePatientRequest request);
    PagedResponse<PatientSummaryResponse> getAllPatients(String search, PatientStatus status, Pageable pageable);
    PagedResponse<PatientSummaryResponse> getMyPatients(Pageable pageable);
    List<PatientSummaryResponse>          getAssignedPatients();

    // ── Vitals ───────────────────────────────────────────────────────────────
    VitalResponse           addVital(UUID patientId, AddVitalRequest request);
    List<VitalResponse>     getVitals(UUID patientId);
    List<VitalResponse>     getLatestVitals(UUID patientId, int limit);

    // ── Diagnosis ────────────────────────────────────────────────────────────
    DiagnosisResponse       addDiagnosis(UUID patientId, AddDiagnosisRequest request);
    List<DiagnosisResponse> getDiagnoses(UUID patientId);

    // ── Treatment ────────────────────────────────────────────────────────────
    TreatmentResponse       addTreatment(UUID patientId, AddTreatmentRequest request);
    List<TreatmentResponse> getTreatments(UUID patientId);
    TreatmentResponse       stopTreatment(UUID patientId, UUID treatmentId);

    // ── Notes ────────────────────────────────────────────────────────────────
    ConsultationNoteResponse       addNote(UUID patientId, AddNoteRequest request);
    List<ConsultationNoteResponse> getNotes(UUID patientId);

    // ── Lab Results ──────────────────────────────────────────────────────────
    LabResultResponse       addLabResult(UUID patientId, AddLabResultRequest request);
    List<LabResultResponse> getLabResults(UUID patientId);

    // ── Discharge ────────────────────────────────────────────────────────────
    DischargeSummaryResponse createDischargeSummary(UUID patientId, CreateDischargeSummaryRequest request);
    DischargeSummaryResponse getDischargeSummary(UUID patientId);
}
