package com.medicore.hms.controller;

import com.medicore.hms.dto.request.AssignDoctorRequest;
import com.medicore.hms.dto.request.RevokeAssignmentRequest;
import com.medicore.hms.dto.response.ApiResponse;
import com.medicore.hms.dto.response.AssignmentResponse;
import com.medicore.hms.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * POST /api/assignments
     * Assign a doctor to a patient at runtime with a specific role.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> assign(
        @Valid @RequestBody AssignDoctorRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Doctor assigned successfully", assignmentService.assign(request)));
    }

    /**
     * POST /api/assignments/revoke
     * Revoke an active doctor assignment.
     */
    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<AssignmentResponse>> revoke(
        @Valid @RequestBody RevokeAssignmentRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Assignment revoked", assignmentService.revoke(request))
        );
    }

    /**
     * GET /api/assignments/patient/{patientId}
     * All active assignments for a patient (used in EMR assignment panel).
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getForPatient(
        @PathVariable UUID patientId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(assignmentService.getAssignmentsForPatient(patientId))
        );
    }

    /**
     * GET /api/assignments/my
     * All active assignments for the currently logged-in doctor.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignments() {
        return ResponseEntity.ok(
            ApiResponse.success(assignmentService.getMyActiveAssignments())
        );
    }
}
