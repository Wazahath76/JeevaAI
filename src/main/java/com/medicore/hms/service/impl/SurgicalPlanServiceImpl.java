package com.medicore.hms.service.impl;

import com.medicore.hms.dto.request.CreateSurgicalPlanRequest;
import com.medicore.hms.dto.request.UpdateSurgicalPlanRequest;
import com.medicore.hms.dto.response.SurgicalPlanResponse;
import com.medicore.hms.entity.Doctor;
import com.medicore.hms.entity.Patient;
import com.medicore.hms.entity.SurgicalPlan;
import com.medicore.hms.enums.SurgicalStatus;
import com.medicore.hms.exception.BusinessException;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.kafka.MedicoreKafkaProducer;
import com.medicore.hms.kafka.SurgeryScheduledEvent;
import com.medicore.hms.repository.DoctorRepository;
import com.medicore.hms.repository.PatientRepository;
import com.medicore.hms.repository.SurgicalPlanRepository;
import com.medicore.hms.service.AuditService;
import com.medicore.hms.service.SurgicalPlanService;
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
public class SurgicalPlanServiceImpl implements SurgicalPlanService {

    private final SurgicalPlanRepository surgicalPlanRepository;
    private final PatientRepository      patientRepository;
    private final DoctorRepository       doctorRepository;
    private final MedicoreKafkaProducer  kafkaProducer;
    private final AuditService           auditService;
    private final SecurityUtil           securityUtil;

    @Override
    @Transactional
    public SurgicalPlanResponse create(CreateSurgicalPlanRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", req.getPatientId()));

        Doctor creator      = securityUtil.getCurrentDoctor();
        Doctor surgeon      = null;
        Doctor anaesthetist = null;

        if (req.getSurgeonId() != null) {
            surgeon = doctorRepository.findById(req.getSurgeonId())
                .orElseThrow(() -> new ResourceNotFoundException("Surgeon", "id", req.getSurgeonId()));
            validateDoctorRole(surgeon, "DOCTOR_SURGEON", "surgeon");
        }

        if (req.getAnaesthetistId() != null) {
            anaesthetist = doctorRepository.findById(req.getAnaesthetistId())
                .orElseThrow(() -> new ResourceNotFoundException("Anaesthetist", "id", req.getAnaesthetistId()));
            validateDoctorRole(anaesthetist, "DOCTOR_ANAESTHETIST", "anaesthetist");
        }

        SurgicalPlan plan = SurgicalPlan.builder()
            .patient(patient)
            .createdBy(creator)
            .surgeon(surgeon)
            .anaesthetist(anaesthetist)
            .procedureName(req.getProcedureName())
            .procedureCode(req.getProcedureCode())
            .status(SurgicalStatus.PLANNED)
            .scheduledAt(req.getScheduledAt())
            .anaesthesiaType(req.getAnaesthesiaType())
            .preOpNotes(req.getPreOpNotes())
            .theatreNumber(req.getTheatreNumber())
            .build();

        SurgicalPlan saved = surgicalPlanRepository.save(plan);

        // Publish Kafka event if surgeon or anaesthetist assigned
        if (surgeon != null || anaesthetist != null) {
            kafkaProducer.publishSurgeryScheduled(buildSurgeryEvent(saved));
        }

        auditService.log("SurgicalPlan", saved.getId(), "CREATE", null,
            req.getProcedureName(), "Surgical plan created for patient " + patient.getPatientCode());

        log.info("Surgical plan [{}] created for patient [{}]", saved.getId(), patient.getPatientCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SurgicalPlanResponse update(UUID planId, UpdateSurgicalPlanRequest req) {
        SurgicalPlan plan = surgicalPlanRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("SurgicalPlan", "id", planId));

        if (plan.getStatus() == SurgicalStatus.COMPLETED || plan.getStatus() == SurgicalStatus.CANCELLED) {
            throw new BusinessException("Cannot update a " + plan.getStatus().name().toLowerCase() + " surgical plan");
        }

        // Assign surgeon
        if (req.getSurgeonId() != null) {
            Doctor surgeon = doctorRepository.findById(req.getSurgeonId())
                .orElseThrow(() -> new ResourceNotFoundException("Surgeon", "id", req.getSurgeonId()));
            validateDoctorRole(surgeon, "DOCTOR_SURGEON", "surgeon");
            plan.setSurgeon(surgeon);
        }

        // Assign anaesthetist
        if (req.getAnaesthetistId() != null) {
            Doctor anaesthetist = doctorRepository.findById(req.getAnaesthetistId())
                .orElseThrow(() -> new ResourceNotFoundException("Anaesthetist", "id", req.getAnaesthetistId()));
            validateDoctorRole(anaesthetist, "DOCTOR_ANAESTHETIST", "anaesthetist");
            plan.setAnaesthetist(anaesthetist);
        }

        // Update status with lifecycle timestamps
        if (req.getStatus() != null) {
            SurgicalStatus prev = plan.getStatus();
            plan.setStatus(req.getStatus());

            if (req.getStatus() == SurgicalStatus.IN_PROGRESS && plan.getStartedAt() == null) {
                plan.setStartedAt(LocalDateTime.now());
            }
            if (req.getStatus() == SurgicalStatus.COMPLETED && plan.getCompletedAt() == null) {
                plan.setCompletedAt(LocalDateTime.now());
            }

            auditService.log("SurgicalPlan", plan.getId(), "STATUS_CHANGE",
                prev.name(), req.getStatus().name(), "Surgery status updated");
        }

        if (req.getScheduledAt()    != null) plan.setScheduledAt(req.getScheduledAt());
        if (req.getAnaesthesiaType()!= null) plan.setAnaesthesiaType(req.getAnaesthesiaType());
        if (req.getPreOpNotes()     != null) plan.setPreOpNotes(req.getPreOpNotes());
        if (req.getIntraOpNotes()   != null) plan.setIntraOpNotes(req.getIntraOpNotes());
        if (req.getPostOpNotes()    != null) plan.setPostOpNotes(req.getPostOpNotes());
        if (req.getComplications()  != null) plan.setComplications(req.getComplications());
        if (req.getTheatreNumber()  != null) plan.setTheatreNumber(req.getTheatreNumber());

        SurgicalPlan updated = surgicalPlanRepository.save(plan);

        // Re-publish if newly scheduled with a full team
        if (req.getStatus() == SurgicalStatus.SCHEDULED) {
            kafkaProducer.publishSurgeryScheduled(buildSurgeryEvent(updated));
        }

        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SurgicalPlanResponse getById(UUID planId) {
        return toResponse(surgicalPlanRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("SurgicalPlan", "id", planId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurgicalPlanResponse> getForPatient(UUID patientId) {
        return surgicalPlanRepository.findByPatientWithDoctors(patientId)
            .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurgicalPlanResponse> getMySurgeries() {
        Doctor doctor = securityUtil.getCurrentDoctor();
        return surgicalPlanRepository.findBySurgeonIdAndStatus(doctor.getId(), SurgicalStatus.SCHEDULED)
            .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurgicalPlanResponse> getMyAnaesthesia() {
        Doctor doctor = securityUtil.getCurrentDoctor();
        return surgicalPlanRepository.findByAnaesthetistIdAndStatus(doctor.getId(), SurgicalStatus.SCHEDULED)
            .stream().map(this::toResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateDoctorRole(Doctor doctor, String expectedRole, String roleLabel) {
        if (!doctor.getRole().name().equals(expectedRole)) {
            throw new BusinessException(
                "Doctor " + doctor.getFullName() + " is not a " + roleLabel
                    + " (role: " + doctor.getRole() + ")");
        }
    }

    private SurgeryScheduledEvent buildSurgeryEvent(SurgicalPlan plan) {
        return SurgeryScheduledEvent.builder()
            .surgicalPlanId(plan.getId())
            .patientId(plan.getPatient().getId())
            .patientCode(plan.getPatient().getPatientCode())
            .patientName(plan.getPatient().getFullName())
            .procedureName(plan.getProcedureName())
            .surgeonId(plan.getSurgeon() != null ? plan.getSurgeon().getId() : null)
            .surgeonName(plan.getSurgeon() != null ? plan.getSurgeon().getFullName() : null)
            .anaesthetistId(plan.getAnaesthetist() != null ? plan.getAnaesthetist().getId() : null)
            .anaesthetistName(plan.getAnaesthetist() != null ? plan.getAnaesthetist().getFullName() : null)
            .theatreNumber(plan.getTheatreNumber())
            .scheduledAt(plan.getScheduledAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private SurgicalPlanResponse toResponse(SurgicalPlan p) {
        return SurgicalPlanResponse.builder()
            .id(p.getId())
            .patientId(p.getPatient().getId())
            .patientCode(p.getPatient().getPatientCode())
            .patientName(p.getPatient().getFullName())
            .procedureName(p.getProcedureName())
            .procedureCode(p.getProcedureCode())
            .status(p.getStatus())
            .surgeonName(p.getSurgeon()       != null ? p.getSurgeon().getFullName()       : null)
            .anaesthetistName(p.getAnaesthetist() != null ? p.getAnaesthetist().getFullName() : null)
            .anaesthesiaType(p.getAnaesthesiaType())
            .preOpNotes(p.getPreOpNotes())
            .intraOpNotes(p.getIntraOpNotes())
            .postOpNotes(p.getPostOpNotes())
            .complications(p.getComplications())
            .theatreNumber(p.getTheatreNumber())
            .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null)
            .scheduledAt(p.getScheduledAt())
            .startedAt(p.getStartedAt())
            .completedAt(p.getCompletedAt())
            .createdAt(p.getCreatedAt())
            .build();
    }
}
