package com.medicore.hms.service.impl;

import com.medicore.hms.dto.request.AssignDoctorRequest;
import com.medicore.hms.dto.request.RevokeAssignmentRequest;
import com.medicore.hms.dto.response.AssignmentResponse;
import com.medicore.hms.entity.Doctor;
import com.medicore.hms.entity.DoctorPatientAssignment;
import com.medicore.hms.entity.Patient;
import com.medicore.hms.enums.AssignmentStatus;
import com.medicore.hms.exception.BusinessException;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.kafka.DoctorAssignedEvent;
import com.medicore.hms.kafka.MedicoreKafkaProducer;
import com.medicore.hms.repository.DoctorPatientAssignmentRepository;
import com.medicore.hms.repository.DoctorRepository;
import com.medicore.hms.repository.PatientRepository;
import com.medicore.hms.service.AssignmentService;
import com.medicore.hms.service.AuditService;
import com.medicore.hms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final DoctorPatientAssignmentRepository assignmentRepository;
    private final PatientRepository                 patientRepository;
    private final DoctorRepository                  doctorRepository;
    private final MedicoreKafkaProducer             kafkaProducer;
    private final AuditService                      auditService;
    private final SecurityUtil                      securityUtil;

    @Override
    @Transactional
    public AssignmentResponse assign(AssignDoctorRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", req.getPatientId()));

        Doctor targetDoctor = doctorRepository.findById(req.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", req.getDoctorId()));

        Doctor assigningDoctor = securityUtil.getCurrentDoctor();

        // Prevent duplicate active assignment for same doctor+patient
        if (assignmentRepository.existsByPatientIdAndAssignedDoctorIdAndStatus(
                req.getPatientId(), req.getDoctorId(), AssignmentStatus.ACTIVE)) {
            throw new BusinessException(
                "Dr. " + targetDoctor.getFullName() + " is already actively assigned to this patient");
        }

        // Validate doctor is active and available
        if (!targetDoctor.getIsActive()) {
            throw new BusinessException("Doctor account is deactivated: " + targetDoctor.getFullName());
        }

        DoctorPatientAssignment assignment = DoctorPatientAssignment.builder()
            .patient(patient)
            .assignedDoctor(targetDoctor)
            .assignedBy(assigningDoctor)
            .assignmentRole(req.getAssignmentRole())
            .status(AssignmentStatus.ACTIVE)
            .assignmentReason(req.getAssignmentReason())
            .build();

        DoctorPatientAssignment saved = assignmentRepository.save(assignment);

        // Publish Kafka event asynchronously
        kafkaProducer.publishDoctorAssigned(
            DoctorAssignedEvent.builder()
                .assignmentId(saved.getId())
                .patientId(patient.getId())
                .patientCode(patient.getPatientCode())
                .patientName(patient.getFullName())
                .assignedDoctorId(targetDoctor.getId())
                .assignedDoctorEmail(targetDoctor.getEmail())
                .assignedDoctorName(targetDoctor.getFullName())
                .assignmentRole(req.getAssignmentRole().name())
                .assignedByName(assigningDoctor.getFullName())
                .assignmentReason(req.getAssignmentReason())
                .occurredAt(LocalDateTime.now())
                .build()
        );

        auditService.log("DoctorPatientAssignment", saved.getId(), "ASSIGN",
            null, req.getAssignmentRole().name(),
            "Dr. " + targetDoctor.getFullName() + " assigned to patient " + patient.getPatientCode()
                + " by Dr. " + assigningDoctor.getFullName());

        log.info("Dr. [{}] assigned to patient [{}] as [{}]",
            targetDoctor.getEmail(), patient.getPatientCode(), req.getAssignmentRole());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AssignmentResponse revoke(RevokeAssignmentRequest req) {
        DoctorPatientAssignment assignment = assignmentRepository.findById(req.getAssignmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", req.getAssignmentId()));

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException("Assignment is not active. Current status: " + assignment.getStatus());
        }

        Doctor revokingDoctor = securityUtil.getCurrentDoctor();

        assignment.setStatus(AssignmentStatus.REVOKED);
        assignment.setRevocationReason(req.getRevocationReason());
        assignment.setCompletedAt(LocalDateTime.now());

        DoctorPatientAssignment saved = assignmentRepository.save(assignment);

        // Notify via Kafka
        kafkaProducer.publishDoctorRevoked(
            DoctorAssignedEvent.builder()
                .assignmentId(saved.getId())
                .patientId(saved.getPatient().getId())
                .patientCode(saved.getPatient().getPatientCode())
                .patientName(saved.getPatient().getFullName())
                .assignedDoctorId(saved.getAssignedDoctor().getId())
                .assignedDoctorEmail(saved.getAssignedDoctor().getEmail())
                .assignedDoctorName(saved.getAssignedDoctor().getFullName())
                .assignmentRole(saved.getAssignmentRole().name())
                .assignedByName(revokingDoctor.getFullName())
                .assignmentReason(req.getRevocationReason())
                .occurredAt(LocalDateTime.now())
                .build()
        );

        auditService.log("DoctorPatientAssignment", saved.getId(), "REVOKE",
            "ACTIVE", "REVOKED",
            "Assignment revoked by Dr. " + revokingDoctor.getFullName()
                + ". Reason: " + req.getRevocationReason());

        log.info("Assignment [{}] revoked by Dr. [{}]", saved.getId(), revokingDoctor.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForPatient(UUID patientId) {
        patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
        return assignmentRepository.findActiveAssignmentsForPatient(patientId)
            .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMyActiveAssignments() {
        Doctor doctor = securityUtil.getCurrentDoctor();
        return assignmentRepository
            .findByAssignedDoctorIdAndStatus(doctor.getId(), AssignmentStatus.ACTIVE)
            .stream().map(this::toResponse).toList();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private AssignmentResponse toResponse(DoctorPatientAssignment a) {
        return AssignmentResponse.builder()
            .id(a.getId())
            .patientId(a.getPatient().getId())
            .patientCode(a.getPatient().getPatientCode())
            .patientName(a.getPatient().getFullName())
            .assignedDoctorId(a.getAssignedDoctor().getId())
            .assignedDoctorName(a.getAssignedDoctor().getFullName())
            .assignedDoctorSpecialization(a.getAssignedDoctor().getSpecialization())
            .assignmentRole(a.getAssignmentRole())
            .status(a.getStatus())
            .assignedByName(a.getAssignedBy() != null ? a.getAssignedBy().getFullName() : null)
            .assignmentReason(a.getAssignmentReason())
            .revocationReason(a.getRevocationReason())
            .completedAt(a.getCompletedAt())
            .createdAt(a.getCreatedAt())
            .build();
    }
}
