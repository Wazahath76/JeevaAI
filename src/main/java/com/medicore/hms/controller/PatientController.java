package com.medicore.hms.controller;

import com.medicore.hms.dto.request.*;
import com.medicore.hms.dto.response.*;
import com.medicore.hms.enums.PatientStatus;
import com.medicore.hms.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ── Core CRUD ────────────────────────────────────────────────────────────

    /**
     * POST /api/patients
     * Admit a new patient. Any authenticated doctor can admit.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> create(
        @Valid @RequestBody CreatePatientRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Patient admitted successfully", patientService.createPatient(request)));
    }

    /**
     * GET /api/patients
     * Paginated list. Admins and super-specialists see all. Others see their own.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PatientSummaryResponse>>> getAll(
        @RequestParam(defaultValue = "0")   int page,
        @RequestParam(defaultValue = "20")  int size,
        @RequestParam(required = false)     String search,
        @RequestParam(required = false)     PatientStatus status
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            ApiResponse.success(patientService.getAllPatients(search, status, pageable))
        );
    }

    /**
     * GET /api/patients/my
     * Patients where logged-in doctor is primary doctor.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<PatientSummaryResponse>>> getMyPatients(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(patientService.getMyPatients(pageable)));
    }

    /**
     * GET /api/patients/assigned
     * Patients assigned to logged-in doctor as consultant/specialist.
     */
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<PatientSummaryResponse>>> getAssigned() {
        return ResponseEntity.ok(ApiResponse.success(patientService.getAssignedPatients()));
    }

    /**
     * GET /api/patients/{id}
     * Full EMR view of a patient.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id)));
    }

    /**
     * PUT /api/patients/{id}
     * Update patient demographics or admission info.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePatientRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Patient updated", patientService.updatePatient(id, request))
        );
    }

    // ── Vitals ───────────────────────────────────────────────────────────────

    /**
     * POST /api/patients/{id}/vitals
     */
    @PostMapping("/{id}/vitals")
    public ResponseEntity<ApiResponse<VitalResponse>> addVital(
        @PathVariable UUID id,
        @Valid @RequestBody AddVitalRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Vitals recorded", patientService.addVital(id, request)));
    }

    /**
     * GET /api/patients/{id}/vitals
     */
    @GetMapping("/{id}/vitals")
    public ResponseEntity<ApiResponse<List<VitalResponse>>> getVitals(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int limit   // 0 = all
    ) {
        List<VitalResponse> vitals = (limit > 0)
            ? patientService.getLatestVitals(id, limit)
            : patientService.getVitals(id);
        return ResponseEntity.ok(ApiResponse.success(vitals));
    }

    // ── Diagnosis ────────────────────────────────────────────────────────────

    /**
     * POST /api/patients/{id}/diagnoses
     */
    @PostMapping("/{id}/diagnoses")
    public ResponseEntity<ApiResponse<DiagnosisResponse>> addDiagnosis(
        @PathVariable UUID id,
        @Valid @RequestBody AddDiagnosisRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Diagnosis added", patientService.addDiagnosis(id, request)));
    }

    /**
     * GET /api/patients/{id}/diagnoses
     */
    @GetMapping("/{id}/diagnoses")
    public ResponseEntity<ApiResponse<List<DiagnosisResponse>>> getDiagnoses(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getDiagnoses(id)));
    }

    // ── Treatment ────────────────────────────────────────────────────────────

    /**
     * POST /api/patients/{id}/treatments
     */
    @PostMapping("/{id}/treatments")
    public ResponseEntity<ApiResponse<TreatmentResponse>> addTreatment(
        @PathVariable UUID id,
        @Valid @RequestBody AddTreatmentRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Treatment prescribed", patientService.addTreatment(id, request)));
    }

    /**
     * GET /api/patients/{id}/treatments
     */
    @GetMapping("/{id}/treatments")
    public ResponseEntity<ApiResponse<List<TreatmentResponse>>> getTreatments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getTreatments(id)));
    }

    /**
     * PATCH /api/patients/{patientId}/treatments/{treatmentId}/stop
     */
    @PatchMapping("/{patientId}/treatments/{treatmentId}/stop")
    public ResponseEntity<ApiResponse<TreatmentResponse>> stopTreatment(
        @PathVariable UUID patientId,
        @PathVariable UUID treatmentId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Treatment stopped", patientService.stopTreatment(patientId, treatmentId))
        );
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    /**
     * POST /api/patients/{id}/notes
     */
    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ConsultationNoteResponse>> addNote(
        @PathVariable UUID id,
        @Valid @RequestBody AddNoteRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Note added", patientService.addNote(id, request)));
    }

    /**
     * GET /api/patients/{id}/notes
     */
    @GetMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<List<ConsultationNoteResponse>>> getNotes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getNotes(id)));
    }

    // ── Lab Results ──────────────────────────────────────────────────────────

    /**
     * POST /api/patients/{id}/lab-results
     */
    @PostMapping("/{id}/lab-results")
    public ResponseEntity<ApiResponse<LabResultResponse>> addLabResult(
        @PathVariable UUID id,
        @Valid @RequestBody AddLabResultRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Lab result added", patientService.addLabResult(id, request)));
    }

    /**
     * GET /api/patients/{id}/lab-results
     */
    @GetMapping("/{id}/lab-results")
    public ResponseEntity<ApiResponse<List<LabResultResponse>>> getLabResults(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getLabResults(id)));
    }

    // ── Discharge ────────────────────────────────────────────────────────────

    /**
     * POST /api/patients/{id}/discharge
     */
    @PostMapping("/{id}/discharge")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> discharge(
        @PathVariable UUID id,
        @Valid @RequestBody CreateDischargeSummaryRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Patient discharged", patientService.createDischargeSummary(id, request)));
    }

    /**
     * GET /api/patients/{id}/discharge
     */
    @GetMapping("/{id}/discharge")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> getDischargeSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getDischargeSummary(id)));
    }
}
