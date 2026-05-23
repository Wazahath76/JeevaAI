package com.medicore.hms.service;

import com.medicore.hms.dto.request.AssignDoctorRequest;
import com.medicore.hms.dto.request.RevokeAssignmentRequest;
import com.medicore.hms.dto.response.AssignmentResponse;

import java.util.List;
import java.util.UUID;

public interface AssignmentService {
    AssignmentResponse     assign(AssignDoctorRequest request);
    AssignmentResponse     revoke(RevokeAssignmentRequest request);
    List<AssignmentResponse> getAssignmentsForPatient(UUID patientId);
    List<AssignmentResponse> getMyActiveAssignments();
}
