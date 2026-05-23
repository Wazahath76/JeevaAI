package com.medicore.hms.service.impl;

import com.medicore.hms.dto.request.*;
import com.medicore.hms.dto.response.*;
import com.medicore.hms.entity.*;
import com.medicore.hms.enums.PatientStatus;
import com.medicore.hms.exception.BusinessException;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.exception.UnauthorizedActionException;
import com.medicore.hms.repository.*;
import com.medicore.hms.service.AuditService;
import com.medicore.hms.service.PatientService;
import com.medicore.hms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository            patientRepository;
    private final PatientVitalRepository       vitalRepository;
    private final DiagnosisRepository          diagnosisRepository;
    private final TreatmentRepository          treatmentRepository;
    private final ConsultationNoteRepository   noteRepository;
    private final LabResultRepository          labResultRepository;
    private final DischargeSummaryRepository   dischargeRepository;
    private final DoctorRepository             doctorRepository;
    private final DoctorPatientAssignmentRepository assignmentRepository;
    private final AuditService                 auditService;
    private final SecurityUtil                 securityUtil;

    // ── Patient CRUD ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PatientResponse createPatient(CreatePatientRequest req) {
        Doctor currentDoctor = securityUtil.getCurrentDoctor();

        Doctor primaryDoctor = (req.getPrimaryDoctorId() != null)
            ? doctorRepository.findById(req.getPrimaryDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", req.getPrimaryDoctorId()))
            : currentDoctor;

        String patientCode = generatePatientCode();

        Patient patient = Patient.builder()
            .patientCode(patientCode)
            .fullName(req.getFullName().trim())
            .dateOfBirth(req.getDateOfBirth())
            .gender(req.getGender())
            .bloodGroup(req.getBloodGroup() != null ? req.getBloodGroup() : com.medicore.hms.enums.BloodGroup.UNKNOWN)
            .phone(req.getPhone())
            .email(req.getEmail())
            .address(req.getAddress())
            .emergencyContactName(req.getEmergencyContactName())
            .emergencyContactPhone(req.getEmergencyContactPhone())
            .emergencyContactRelation(req.getEmergencyContactRelation())
            .knownAllergies(req.getKnownAllergies())
            .chronicConditions(req.getChronicConditions())
            .familyHistory(req.getFamilyHistory())
            .pastSurgeries(req.getPastSurgeries())
            .status(req.getStatus())
            .ward(req.getWard())
            .bedNumber(req.getBedNumber())
            .admissionDate(req.getStatus() == PatientStatus.IPD || req.getStatus() == PatientStatus.CRITICAL
                ? LocalDateTime.now() : null)
            .insuranceProvider(req.getInsuranceProvider())
            .insurancePolicyNo(req.getInsurancePolicyNo())
            .notes(req.getNotes())
            .admittedBy(currentDoctor)
            .primaryDoctor(primaryDoctor)
            .build();

        Patient saved = patientRepository.save(patient);

        auditService.log("Patient", saved.getId(), "CREATE", null,
            saved.getPatientCode(), "Patient admitted by Dr. " + currentDoctor.getFullName());

        log.info("Patient [{}] created by Dr. {}", saved.getPatientCode(), currentDoctor.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        Patient patient = patientRepository.findByIdWithDoctors(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        verifyPatientAccess(patient);
        return toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(UUID id, UpdatePatientRequest req) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        verifyPatientAccess(patient);

        if (req.getFullName()                  != null) patient.setFullName(req.getFullName());
        if (req.getPhone()                     != null) patient.setPhone(req.getPhone());
        if (req.getEmail()                     != null) patient.setEmail(req.getEmail());
        if (req.getAddress()                   != null) patient.setAddress(req.getAddress());
        if (req.getBloodGroup()                != null) patient.setBloodGroup(req.getBloodGroup());
        if (req.getEmergencyContactName()      != null) patient.setEmergencyContactName(req.getEmergencyContactName());
        if (req.getEmergencyContactPhone()     != null) patient.setEmergencyContactPhone(req.getEmergencyContactPhone());
        if (req.getEmergencyContactRelation()  != null) patient.setEmergencyContactRelation(req.getEmergencyContactRelation());
        if (req.getKnownAllergies()            != null) patient.setKnownAllergies(req.getKnownAllergies());
        if (req.getChronicConditions()         != null) patient.setChronicConditions(req.getChronicConditions());
        if (req.getFamilyHistory()             != null) patient.setFamilyHistory(req.getFamilyHistory());
        if (req.getPastSurgeries()             != null) patient.setPastSurgeries(req.getPastSurgeries());
        if (req.getWard()                      != null) patient.setWard(req.getWard());
        if (req.getBedNumber()                 != null) patient.setBedNumber(req.getBedNumber());
        if (req.getInsuranceProvider()         != null) patient.setInsuranceProvider(req.getInsuranceProvider());
        if (req.getInsurancePolicyNo()         != null) patient.setInsurancePolicyNo(req.getInsurancePolicyNo());
        if (req.getNotes()                     != null) patient.setNotes(req.getNotes());

        if (req.getStatus() != null) {
            PatientStatus old = patient.getStatus();
            patient.setStatus(req.getStatus());
            // Set discharge date when status moves to DISCHARGED
            if (req.getStatus() == PatientStatus.DISCHARGED && old != PatientStatus.DISCHARGED) {
                patient.setDischargeDate(LocalDateTime.now());
            }
            // Set admission date when admitted
            if ((req.getStatus() == PatientStatus.IPD || req.getStatus() == PatientStatus.CRITICAL)
                && patient.getAdmissionDate() == null) {
                patient.setAdmissionDate(LocalDateTime.now());
            }
        }

        Patient updated = patientRepository.save(patient);
        auditService.log("Patient", updated.getId(), "UPDATE", null, null,
            "Patient record updated by Dr. " + securityUtil.getCurrentDoctor().getFullName());

        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientSummaryResponse> getAllPatients(String search, PatientStatus status, Pageable pageable) {
        Page<Patient> page;
        if (search != null && !search.isBlank()) {
            page = patientRepository.searchPatients(search, pageable);
        } else if (status != null) {
            page = patientRepository.findByStatus(status, pageable);
        } else {
            page = patientRepository.findAll(pageable);
        }
        return PagedResponse.from(page.map(this::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientSummaryResponse> getMyPatients(Pageable pageable) {
        Doctor doctor = securityUtil.getCurrentDoctor();
        Page<Patient> page = patientRepository.findByPrimaryDoctorId(doctor.getId(), pageable);
        return PagedResponse.from(page.map(this::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> getAssignedPatients() {
        Doctor doctor = securityUtil.getCurrentDoctor();
        return patientRepository.findAssignedPatients(doctor.getId())
            .stream().map(this::toSummary).toList();
    }

    // ── Vitals ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VitalResponse addVital(UUID patientId, AddVitalRequest req) {
        Patient patient = findPatientOrThrow(patientId);
        verifyPatientAccess(patient);
        Doctor doctor = securityUtil.getCurrentDoctor();

        BigDecimal bmi = null;
        if (req.getWeightKg() != null && req.getHeightCm() != null
            && req.getHeightCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = req.getHeightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            bmi = req.getWeightKg().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
        }

        PatientVital vital = PatientVital.builder()
            .patient(patient)
            .recordedBy(doctor)
            .bloodPressureSystolic(req.getBloodPressureSystolic())
            .bloodPressureDiastolic(req.getBloodPressureDiastolic())
            .pulseBpm(req.getPulseBpm())
            .temperatureCelsius(req.getTemperatureCelsius())
            .spo2Percent(req.getSpo2Percent())
            .weightKg(req.getWeightKg())
            .heightCm(req.getHeightCm())
            .bmi(bmi)
            .respiratoryRate(req.getRespiratoryRate())
            .bloodGlucose(req.getBloodGlucose())
            .remarks(req.getRemarks())
            .build();

        PatientVital saved = vitalRepository.save(vital);
        return toVitalResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalResponse> getVitals(UUID patientId) {
        findPatientOrThrow(patientId);
        return vitalRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream().map(this::toVitalResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalResponse> getLatestVitals(UUID patientId, int limit) {
        findPatientOrThrow(patientId);
        return vitalRepository.findLatestVitals(patientId, limit)
            .stream().map(this::toVitalResponse).toList();
    }

    // ── Diagnosis ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DiagnosisResponse addDiagnosis(UUID patientId, AddDiagnosisRequest req) {
        Patient patient = findPatientOrThrow(patientId);
        verifyPatientAccess(patient);
        Doctor doctor = securityUtil.getCurrentDoctor();

        Diagnosis diagnosis = Diagnosis.builder()
            .patient(patient)
            .diagnosedBy(doctor)
            .diagnosisName(req.getDiagnosisName())
            .icd10Code(req.getIcd10Code())
            .isPrimary(req.getIsPrimary() != null && req.getIsPrimary())
            .severity(req.getSeverity())
            .description(req.getDescription())
            .differentialDiagnosis(req.getDifferentialDiagnosis())
            .isActive(true)
            .build();

        Diagnosis saved = diagnosisRepository.save(diagnosis);
        auditService.log("Diagnosis", saved.getId(), "CREATE", null,
            saved.getDiagnosisName(), "Diagnosis added for patient " + patient.getPatientCode());
        return toDiagnosisResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisResponse> getDiagnoses(UUID patientId) {
        findPatientOrThrow(patientId);
        return diagnosisRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream().map(this::toDiagnosisResponse).toList();
    }

    // ── Treatment ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TreatmentResponse addTreatment(UUID patientId, AddTreatmentRequest req) {
        Patient patient = findPatientOrThrow(patientId);
        verifyPatientAccess(patient);
        Doctor doctor = securityUtil.getCurrentDoctor();

        Treatment treatment = Treatment.builder()
            .patient(patient)
            .prescribedBy(doctor)
            .drugName(req.getDrugName())
            .dosage(req.getDosage())
            .frequency(req.getFrequency())
            .duration(req.getDuration())
            .routeOfAdministration(req.getRouteOfAdministration())
            .specialInstructions(req.getSpecialInstructions())
            .treatmentLine(req.getTreatmentLine())
            .notes(req.getNotes())
            .isActive(true)
            .isAiSuggested(false)
            .build();

        Treatment saved = treatmentRepository.save(treatment);
        auditService.log("Treatment", saved.getId(), "CREATE", null,
            saved.getDrugName(), "Treatment prescribed for patient " + patient.getPatientCode());
        return toTreatmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponse> getTreatments(UUID patientId) {
        findPatientOrThrow(patientId);
        return treatmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream().map(this::toTreatmentResponse).toList();
    }

    @Override
    @Transactional
    public TreatmentResponse stopTreatment(UUID patientId, UUID treatmentId) {
        Treatment treatment = treatmentRepository.findById(treatmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Treatment", "id", treatmentId));
        if (!treatment.getPatient().getId().equals(patientId)) {
            throw new BusinessException("Treatment does not belong to this patient");
        }
        treatment.setIsActive(false);
        Treatment saved = treatmentRepository.save(treatment);
        auditService.log("Treatment", saved.getId(), "STOP", "active", "stopped",
            "Treatment stopped by Dr. " + securityUtil.getCurrentDoctor().getFullName());
        return toTreatmentResponse(saved);
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ConsultationNoteResponse addNote(UUID patientId, AddNoteRequest req) {
        Patient patient = findPatientOrThrow(patientId);
        verifyPatientAccess(patient);
        Doctor doctor = securityUtil.getCurrentDoctor();

        ConsultationNote note = ConsultationNote.builder()
            .patient(patient)
            .author(doctor)
            .noteType(req.getNoteType())
            .content(req.getContent())
            .isVisibleToAll(req.getIsVisibleToAll() != null ? req.getIsVisibleToAll() : true)
            .sharedWithSuperSpecialist(req.getSharedWithSuperSpecialist() != null
                ? req.getSharedWithSuperSpecialist() : true)
            .isLocked(false)
            .build();

        ConsultationNote saved = noteRepository.save(note);
        // Lock immediately after save — notes are append-only
        saved.setIsLocked(true);
        noteRepository.save(saved);

        return toNoteResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationNoteResponse> getNotes(UUID patientId) {
        findPatientOrThrow(patientId);
        Doctor doctor = securityUtil.getCurrentDoctor();

        // Super specialists see all shared notes; others see all notes for patients they can access
        boolean isSuperSpecialist = doctor.getRole().name().equals("DOCTOR_SUPER_SPECIALIST");
        List<ConsultationNote> notes = isSuperSpecialist
            ? noteRepository.findSharedNotesForPatient(patientId)
            : noteRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return notes.stream().map(this::toNoteResponse).toList();
    }

    // ── Lab Results ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LabResultResponse addLabResult(UUID patientId, AddLabResultRequest req) {
        Patient patient = findPatientOrThrow(patientId);
        verifyPatientAccess(patient);
        Doctor doctor = securityUtil.getCurrentDoctor();

        LabResult result = LabResult.builder()
            .patient(patient)
            .orderedBy(doctor)
            .testName(req.getTestName())
            .testCategory(req.getTestCategory())
            .resultValue(req.getResultValue())
            .referenceRange(req.getReferenceRange())
            .unit(req.getUnit())
            .isAbnormal(req.getIsAbnormal() != null ? req.getIsAbnormal() : false)
            .reportUrl(req.getReportUrl())
            .remarks(req.getRemarks())
            .build();

        LabResult saved = labResultRepository.save(result);
        return toLabResultResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabResultResponse> getLabResults(UUID patientId) {
        findPatientOrThrow(patientId);
        return labResultRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream().map(this::toLabResultResponse).toList();
    }

    // ── Discharge ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DischargeSummaryResponse createDischargeSummary(UUID patientId, CreateDischargeSummaryRequest req) {
        Patient patient = findPatientOrThrow(patientId);
        verifyPatientAccess(patient);

        if (dischargeRepository.findByPatientId(patientId).isPresent()) {
            throw new BusinessException("Discharge summary already exists for this patient");
        }

        Doctor doctor = securityUtil.getCurrentDoctor();

        DischargeSummary summary = DischargeSummary.builder()
            .patient(patient)
            .dischargedBy(doctor)
            .dischargeDate(LocalDateTime.now())
            .finalDiagnosis(req.getFinalDiagnosis())
            .hospitalCourse(req.getHospitalCourse())
            .proceduresPerformed(req.getProceduresPerformed())
            .conditionAtDischarge(req.getConditionAtDischarge())
            .dischargeMedications(req.getDischargeMedications())
            .followUpInstructions(req.getFollowUpInstructions())
            .followUpDate(req.getFollowUpDate())
            .dietAdvice(req.getDietAdvice())
            .activityRestrictions(req.getActivityRestrictions())
            .build();

        DischargeSummary saved = dischargeRepository.save(summary);

        // Update patient status
        patient.setStatus(PatientStatus.DISCHARGED);
        patient.setDischargeDate(LocalDateTime.now());
        patientRepository.save(patient);

        auditService.log("DischargeSummary", saved.getId(), "CREATE", null,
            null, "Patient " + patient.getPatientCode() + " discharged by Dr. " + doctor.getFullName());

        return toDischargeResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DischargeSummaryResponse getDischargeSummary(UUID patientId) {
        DischargeSummary summary = dischargeRepository.findByPatientId(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Discharge summary not found for patient: " + patientId));
        return toDischargeResponse(summary);
    }

    // ── Access control ───────────────────────────────────────────────────────

    private void verifyPatientAccess(Patient patient) {
        Doctor doctor = securityUtil.getCurrentDoctor();
        String role   = doctor.getRole().name();

        // Admins and super specialists see everything
        if (role.equals("ADMIN") || role.equals("DOCTOR_SUPER_SPECIALIST")) return;

        // Primary doctor always has access
        if (patient.getPrimaryDoctor() != null
            && patient.getPrimaryDoctor().getId().equals(doctor.getId())) return;

        // Admitted-by doctor has access
        if (patient.getAdmittedBy() != null
            && patient.getAdmittedBy().getId().equals(doctor.getId())) return;

        // Check active assignment
        boolean isAssigned = assignmentRepository
            .existsByPatientIdAndAssignedDoctorIdAndStatus(
                patient.getId(), doctor.getId(),
                com.medicore.hms.enums.AssignmentStatus.ACTIVE);

        if (!isAssigned) {
            throw new UnauthorizedActionException(
                "You do not have access to patient: " + patient.getPatientCode());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Patient findPatientOrThrow(UUID id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    private String generatePatientCode() {
        int year  = LocalDate.now().getYear();
        long count = patientRepository.countByYear(year) + 1;
        return String.format("MED-%d-%04d", year, count);
    }

    private int calculateAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
            .id(p.getId())
            .patientCode(p.getPatientCode())
            .fullName(p.getFullName())
            .dateOfBirth(p.getDateOfBirth())
            .age(p.getDateOfBirth() != null ? calculateAge(p.getDateOfBirth()) : null)
            .gender(p.getGender())
            .bloodGroup(p.getBloodGroup())
            .phone(p.getPhone())
            .email(p.getEmail())
            .address(p.getAddress())
            .emergencyContactName(p.getEmergencyContactName())
            .emergencyContactPhone(p.getEmergencyContactPhone())
            .emergencyContactRelation(p.getEmergencyContactRelation())
            .knownAllergies(p.getKnownAllergies())
            .chronicConditions(p.getChronicConditions())
            .familyHistory(p.getFamilyHistory())
            .pastSurgeries(p.getPastSurgeries())
            .status(p.getStatus())
            .ward(p.getWard())
            .bedNumber(p.getBedNumber())
            .admissionDate(p.getAdmissionDate())
            .dischargeDate(p.getDischargeDate())
            .insuranceProvider(p.getInsuranceProvider())
            .insurancePolicyNo(p.getInsurancePolicyNo())
            .notes(p.getNotes())
            .primaryDoctor(p.getPrimaryDoctor() != null ? toDoctorRef(p.getPrimaryDoctor()) : null)
            .admittedBy(p.getAdmittedBy()     != null ? toDoctorRef(p.getAdmittedBy())     : null)
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    private PatientSummaryResponse toSummary(Patient p) {
        return PatientSummaryResponse.builder()
            .id(p.getId())
            .patientCode(p.getPatientCode())
            .fullName(p.getFullName())
            .dateOfBirth(p.getDateOfBirth())
            .age(p.getDateOfBirth() != null ? calculateAge(p.getDateOfBirth()) : null)
            .gender(p.getGender())
            .bloodGroup(p.getBloodGroup())
            .phone(p.getPhone())
            .status(p.getStatus())
            .ward(p.getWard())
            .bedNumber(p.getBedNumber())
            .primaryDoctorName(p.getPrimaryDoctor() != null ? p.getPrimaryDoctor().getFullName() : null)
            .admissionDate(p.getAdmissionDate())
            .createdAt(p.getCreatedAt())
            .build();
    }

    private DoctorResponse toDoctorRef(Doctor d) {
        return DoctorResponse.builder()
            .id(d.getId())
            .fullName(d.getFullName())
            .role(d.getRole())
            .specialization(d.getSpecialization())
            .department(d.getDepartment())
            .build();
    }

    private VitalResponse toVitalResponse(PatientVital v) {
        return VitalResponse.builder()
            .id(v.getId())
            .bloodPressureSystolic(v.getBloodPressureSystolic())
            .bloodPressureDiastolic(v.getBloodPressureDiastolic())
            .pulseBpm(v.getPulseBpm())
            .temperatureCelsius(v.getTemperatureCelsius())
            .spo2Percent(v.getSpo2Percent())
            .weightKg(v.getWeightKg())
            .heightCm(v.getHeightCm())
            .bmi(v.getBmi())
            .respiratoryRate(v.getRespiratoryRate())
            .bloodGlucose(v.getBloodGlucose())
            .remarks(v.getRemarks())
            .recordedByName(v.getRecordedBy() != null ? v.getRecordedBy().getFullName() : null)
            .recordedAt(v.getCreatedAt())
            .build();
    }

    private DiagnosisResponse toDiagnosisResponse(Diagnosis d) {
        return DiagnosisResponse.builder()
            .id(d.getId())
            .diagnosisName(d.getDiagnosisName())
            .icd10Code(d.getIcd10Code())
            .isPrimary(d.getIsPrimary())
            .severity(d.getSeverity())
            .description(d.getDescription())
            .differentialDiagnosis(d.getDifferentialDiagnosis())
            .isActive(d.getIsActive())
            .diagnosedByName(d.getDiagnosedBy() != null ? d.getDiagnosedBy().getFullName() : null)
            .createdAt(d.getCreatedAt())
            .build();
    }

    private TreatmentResponse toTreatmentResponse(Treatment t) {
        return TreatmentResponse.builder()
            .id(t.getId())
            .drugName(t.getDrugName())
            .dosage(t.getDosage())
            .frequency(t.getFrequency())
            .duration(t.getDuration())
            .routeOfAdministration(t.getRouteOfAdministration())
            .specialInstructions(t.getSpecialInstructions())
            .treatmentLine(t.getTreatmentLine())
            .isActive(t.getIsActive())
            .isAiSuggested(t.getIsAiSuggested())
            .contraindicationWarning(t.getContraindicationWarning())
            .notes(t.getNotes())
            .prescribedByName(t.getPrescribedBy() != null ? t.getPrescribedBy().getFullName() : null)
            .createdAt(t.getCreatedAt())
            .build();
    }

    private ConsultationNoteResponse toNoteResponse(ConsultationNote n) {
        return ConsultationNoteResponse.builder()
            .id(n.getId())
            .noteType(n.getNoteType())
            .content(n.getContent())
            .isVisibleToAll(n.getIsVisibleToAll())
            .sharedWithSuperSpecialist(n.getSharedWithSuperSpecialist())
            .isLocked(n.getIsLocked())
            .authorName(n.getAuthor() != null ? n.getAuthor().getFullName() : null)
            .authorRole(n.getAuthor() != null ? n.getAuthor().getRole().name() : null)
            .createdAt(n.getCreatedAt())
            .build();
    }

    private LabResultResponse toLabResultResponse(LabResult l) {
        return LabResultResponse.builder()
            .id(l.getId())
            .testName(l.getTestName())
            .testCategory(l.getTestCategory())
            .resultValue(l.getResultValue())
            .referenceRange(l.getReferenceRange())
            .unit(l.getUnit())
            .isAbnormal(l.getIsAbnormal())
            .reportUrl(l.getReportUrl())
            .remarks(l.getRemarks())
            .orderedByName(l.getOrderedBy() != null ? l.getOrderedBy().getFullName() : null)
            .createdAt(l.getCreatedAt())
            .build();
    }

    private DischargeSummaryResponse toDischargeResponse(DischargeSummary s) {
        return DischargeSummaryResponse.builder()
            .id(s.getId())
            .finalDiagnosis(s.getFinalDiagnosis())
            .hospitalCourse(s.getHospitalCourse())
            .proceduresPerformed(s.getProceduresPerformed())
            .conditionAtDischarge(s.getConditionAtDischarge())
            .dischargeMedications(s.getDischargeMedications())
            .followUpInstructions(s.getFollowUpInstructions())
            .followUpDate(s.getFollowUpDate())
            .dietAdvice(s.getDietAdvice())
            .activityRestrictions(s.getActivityRestrictions())
            .dischargedByName(s.getDischargedBy() != null ? s.getDischargedBy().getFullName() : null)
            .dischargeDate(s.getDischargeDate())
            .createdAt(s.getCreatedAt())
            .build();
    }
}
