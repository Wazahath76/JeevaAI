package com.medicore.hms.controller;

import com.medicore.hms.dto.request.CreateSurgicalPlanRequest;
import com.medicore.hms.dto.request.UpdateSurgicalPlanRequest;
import com.medicore.hms.dto.response.ApiResponse;
import com.medicore.hms.dto.response.SurgicalPlanResponse;
import com.medicore.hms.service.SurgicalPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/surgical-plans")
@RequiredArgsConstructor
public class SurgicalPlanController {

    private final SurgicalPlanService surgicalPlanService;

    /** POST /api/surgical-plans */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR_SURGEON','DOCTOR_SUPER_SPECIALIST','ADMIN','DOCTOR_GENERAL','DOCTOR_SPECIALIST')")
    public ResponseEntity<ApiResponse<SurgicalPlanResponse>> create(
        @Valid @RequestBody CreateSurgicalPlanRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Surgical plan created", surgicalPlanService.create(request)));
    }

    /** GET /api/surgical-plans/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SurgicalPlanResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(surgicalPlanService.getById(id)));
    }

    /** GET /api/surgical-plans/patient/{patientId} */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<SurgicalPlanResponse>>> getForPatient(
        @PathVariable UUID patientId
    ) {
        return ResponseEntity.ok(ApiResponse.success(surgicalPlanService.getForPatient(patientId)));
    }

    /** PUT /api/surgical-plans/{id} - update notes, status, assign surgeon/anaesthetist */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SurgicalPlanResponse>> update(
        @PathVariable UUID id,
        @RequestBody UpdateSurgicalPlanRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Surgical plan updated", surgicalPlanService.update(id, request))
        );
    }

    /** GET /api/surgical-plans/my/surgeries - surgeon sees their scheduled ops */
    @GetMapping("/my/surgeries")
    @PreAuthorize("hasAnyRole('DOCTOR_SURGEON','ADMIN')")
    public ResponseEntity<ApiResponse<List<SurgicalPlanResponse>>> getMySurgeries() {
        return ResponseEntity.ok(ApiResponse.success(surgicalPlanService.getMySurgeries()));
    }

    /** GET /api/surgical-plans/my/anaesthesia - anaesthetist sees their cases */
    @GetMapping("/my/anaesthesia")
    @PreAuthorize("hasAnyRole('DOCTOR_ANAESTHETIST','ADMIN')")
    public ResponseEntity<ApiResponse<List<SurgicalPlanResponse>>> getMyAnaesthesia() {
        return ResponseEntity.ok(ApiResponse.success(surgicalPlanService.getMyAnaesthesia()));
    }
}
