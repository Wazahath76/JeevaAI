package com.medicore.hms.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicore.hms.dto.request.ReviewRecommendationRequest;
import com.medicore.hms.dto.response.AiRecommendationResponse;
import com.medicore.hms.entity.*;
import com.medicore.hms.enums.RecommendationStatus;
import com.medicore.hms.exception.AiServiceException;
import com.medicore.hms.exception.BusinessException;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.repository.*;
import com.medicore.hms.service.AiRecommendationService;
import com.medicore.hms.service.AuditService;
import com.medicore.hms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final AiRecommendationRepository recommendationRepository;
    private final PatientRepository          patientRepository;
    private final PatientVitalRepository     vitalRepository;
    private final DiagnosisRepository        diagnosisRepository;
    private final TreatmentRepository        treatmentRepository;
    private final LabResultRepository        labResultRepository;
    private final AuditService               auditService;
    private final SecurityUtil               securityUtil;
    private final WebClient                  anthropicWebClient;
    private final ObjectMapper               objectMapper;

    @Value("${anthropic.model}")
    private String aiModel;

    @Value("${anthropic.max-tokens}")
    private int maxTokens;

    @Value("${anthropic.timeout-seconds}")
    private int timeoutSeconds;

    @Value("${anthropic.api-key}")
    private String anthropicApiKey;



    // ── Request recommendation ────────────────────────────────────────────────

    @Override
    @Transactional
    public AiRecommendationResponse requestRecommendation(UUID patientId) {

        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            throw new AiServiceException("AI service not configured. Contact administrator.");
        }
        Patient patient = patientRepository.findByIdWithDoctors(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        Doctor requestingDoctor = securityUtil.getCurrentDoctor();

        // Build clinical context
        String clinicalContext = buildClinicalPrompt(patient, patientId);

        // Call Claude API
        String aiResponse = callClaudeApi(clinicalContext);

        // Parse structured fields from response
        Map<String, String> parsed = parseAiResponse(aiResponse);

        AiRecommendation recommendation = AiRecommendation.builder()
            .patient(patient)
            .requestedBy(requestingDoctor)
            .promptContext(clinicalContext)
            .aiResponse(aiResponse)
            .suggestedTreatment(parsed.get("suggested_treatment"))
            .suggestedDrugs(parsed.get("suggested_drugs"))
            .contraindicationWarnings(parsed.get("contraindication_warnings"))
            .referralSuggestion(parsed.get("referral_suggestion"))
            .urgencyLevel(parsed.get("urgency_level"))
            .status(RecommendationStatus.PENDING_REVIEW)
            .aiModel(aiModel)
            .build();

        AiRecommendation saved = recommendationRepository.save(recommendation);

        auditService.log("AiRecommendation", saved.getId(), "CREATE", null, null,
            "AI recommendation requested for patient " + patient.getPatientCode()
                + " by Dr. " + requestingDoctor.getFullName());

        log.info("AI recommendation generated for patient [{}]", patient.getPatientCode());
        return toResponse(saved);
    }

    // ── Review actions ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AiRecommendationResponse approveRecommendation(UUID recId, ReviewRecommendationRequest req) {
        AiRecommendation rec = findAndValidateForReview(recId);
        Doctor reviewer      = securityUtil.getCurrentDoctor();

        rec.setStatus(RecommendationStatus.APPROVED);
        rec.setReviewedBy(reviewer);
        rec.setDoctorNotes(req.getDoctorNotes());
        rec.setReviewedAt(LocalDateTime.now());

        AiRecommendation saved = recommendationRepository.save(rec);
        auditService.log("AiRecommendation", saved.getId(), "APPROVE", "PENDING_REVIEW", "APPROVED",
            "Recommendation approved by Dr. " + reviewer.getFullName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AiRecommendationResponse rejectRecommendation(UUID recId, ReviewRecommendationRequest req) {
        if (req.getRejectionReason() == null || req.getRejectionReason().isBlank()) {
            throw new BusinessException("Rejection reason is mandatory when rejecting a recommendation");
        }

        AiRecommendation rec = findAndValidateForReview(recId);
        Doctor reviewer      = securityUtil.getCurrentDoctor();

        rec.setStatus(RecommendationStatus.REJECTED);
        rec.setReviewedBy(reviewer);
        rec.setDoctorNotes(req.getDoctorNotes());
        rec.setRejectionReason(req.getRejectionReason());
        rec.setReviewedAt(LocalDateTime.now());

        AiRecommendation saved = recommendationRepository.save(rec);
        auditService.log("AiRecommendation", saved.getId(), "REJECT", "PENDING_REVIEW", "REJECTED",
            "Recommendation rejected by Dr. " + reviewer.getFullName()
                + ". Reason: " + req.getRejectionReason());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AiRecommendationResponse modifyRecommendation(UUID recId, ReviewRecommendationRequest req) {
        if (req.getModifiedTreatment() == null || req.getModifiedTreatment().isBlank()) {
            throw new BusinessException("Modified treatment plan is required when modifying a recommendation");
        }

        AiRecommendation rec = findAndValidateForReview(recId);
        Doctor reviewer      = securityUtil.getCurrentDoctor();

        rec.setStatus(RecommendationStatus.MODIFIED);
        rec.setReviewedBy(reviewer);
        rec.setDoctorNotes(req.getDoctorNotes());
        rec.setModifiedTreatment(req.getModifiedTreatment());
        rec.setReviewedAt(LocalDateTime.now());

        AiRecommendation saved = recommendationRepository.save(rec);
        auditService.log("AiRecommendation", saved.getId(), "MODIFY", "PENDING_REVIEW", "MODIFIED",
            "Recommendation modified by Dr. " + reviewer.getFullName());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiRecommendationResponse> getRecommendationsForPatient(UUID patientId) {
        return recommendationRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream().map(this::toResponse).toList();
    }

    // ── Prompt building ───────────────────────────────────────────────────────

    private String buildClinicalPrompt(Patient patient, UUID patientId) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are an AI clinical decision support assistant helping medical doctors.\n");
        sb.append("Analyze the following patient data and provide structured treatment recommendations.\n\n");

        // Patient demographics
        sb.append("=== PATIENT PROFILE ===\n");
        sb.append("Name: ").append(patient.getFullName()).append("\n");
        sb.append("Age: ").append(calculateAge(patient.getDateOfBirth())).append(" years\n");
        sb.append("Gender: ").append(patient.getGender()).append("\n");
        sb.append("Blood Group: ").append(patient.getBloodGroup()).append("\n");
        sb.append("Status: ").append(patient.getStatus()).append("\n");

        if (patient.getKnownAllergies() != null) {
            sb.append("Known Allergies: ").append(patient.getKnownAllergies()).append("\n");
        }
        if (patient.getChronicConditions() != null) {
            sb.append("Chronic Conditions: ").append(patient.getChronicConditions()).append("\n");
        }
        if (patient.getFamilyHistory() != null) {
            sb.append("Family History: ").append(patient.getFamilyHistory()).append("\n");
        }
        if (patient.getPastSurgeries() != null) {
            sb.append("Past Surgeries: ").append(patient.getPastSurgeries()).append("\n");
        }

        // Latest vitals
        List<PatientVital> vitals = vitalRepository.findLatestVitals(patientId, 3);
        if (!vitals.isEmpty()) {
            sb.append("\n=== RECENT VITALS ===\n");
            vitals.forEach(v -> {
                sb.append("[").append(v.getCreatedAt()).append("] ");
                if (v.getBloodPressureSystolic()  != null) sb.append("BP: ").append(v.getBloodPressureSystolic()).append("/").append(v.getBloodPressureDiastolic()).append(" mmHg  ");
                if (v.getPulseBpm()                != null) sb.append("Pulse: ").append(v.getPulseBpm()).append(" bpm  ");
                if (v.getTemperatureCelsius()      != null) sb.append("Temp: ").append(v.getTemperatureCelsius()).append("°C  ");
                if (v.getSpo2Percent()             != null) sb.append("SpO2: ").append(v.getSpo2Percent()).append("%  ");
                if (v.getBloodGlucose()            != null) sb.append("Glucose: ").append(v.getBloodGlucose()).append(" mg/dL");
                if (v.getBmi()                     != null) sb.append("  BMI: ").append(v.getBmi());
                sb.append("\n");
            });
        }

        // Active diagnoses
        List<Diagnosis> diagnoses = diagnosisRepository.findByPatientIdAndIsActiveTrue(patientId);
        if (!diagnoses.isEmpty()) {
            sb.append("\n=== CURRENT DIAGNOSES ===\n");
            diagnoses.forEach(d -> {
                sb.append("- ").append(d.getDiagnosisName());
                if (d.getIcd10Code()   != null) sb.append(" [").append(d.getIcd10Code()).append("]");
                if (d.getSeverity()    != null) sb.append(" | Severity: ").append(d.getSeverity());
                if (d.getIsPrimary())           sb.append(" [PRIMARY]");
                if (d.getDescription() != null) sb.append("\n  Details: ").append(d.getDescription());
                sb.append("\n");
            });
        }

        // Active treatments
        List<Treatment> treatments = treatmentRepository.findByPatientIdAndIsActiveTrue(patientId);
        if (!treatments.isEmpty()) {
            sb.append("\n=== CURRENT MEDICATIONS ===\n");
            treatments.forEach(t -> {
                sb.append("- ").append(t.getDrugName());
                if (t.getDosage()    != null) sb.append(" ").append(t.getDosage());
                if (t.getFrequency() != null) sb.append(" | ").append(t.getFrequency());
                if (t.getDuration()  != null) sb.append(" for ").append(t.getDuration());
                if (t.getRouteOfAdministration() != null) sb.append(" [").append(t.getRouteOfAdministration()).append("]");
                sb.append("\n");
            });
        }

        // Recent abnormal lab results
        List<LabResult> abnormalLabs = labResultRepository.findByPatientIdAndIsAbnormalTrue(patientId);
        if (!abnormalLabs.isEmpty()) {
            sb.append("\n=== ABNORMAL LAB RESULTS ===\n");
            abnormalLabs.stream().limit(10).forEach(l -> {
                sb.append("- ").append(l.getTestName()).append(": ").append(l.getResultValue());
                if (l.getUnit()           != null) sb.append(" ").append(l.getUnit());
                if (l.getReferenceRange() != null) sb.append(" (ref: ").append(l.getReferenceRange()).append(")");
                sb.append("\n");
            });
        }

        sb.append("\n=== YOUR TASK ===\n");
        sb.append("Based on this clinical data, provide a structured recommendation in the following EXACT format:\n\n");
        sb.append("URGENCY_LEVEL: [ROUTINE/URGENT/EMERGENCY]\n\n");
        sb.append("SUGGESTED_TREATMENT:\n[Describe the recommended line of treatment in detail]\n\n");
        sb.append("SUGGESTED_DRUGS:\n[List each drug with dose, frequency, duration, and rationale]\n\n");
        sb.append("CONTRAINDICATION_WARNINGS:\n[List any drug interactions or contraindications given the patient's allergies and current medications]\n\n");
        sb.append("REFERRAL_SUGGESTION:\n[Suggest any specialist referral if needed, or write NONE]\n\n");
        sb.append("CLINICAL_REASONING:\n[Brief explanation of your recommendation]\n\n");
        sb.append("IMPORTANT: Always recommend the doctor to validate and apply clinical judgment. This is decision support, not a prescription.");

        return sb.toString();
    }

    // ── Claude API call ───────────────────────────────────────────────────────

    private String callClaudeApi(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));

            String responseBody = anthropicWebClient
                .post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

            JsonNode root    = objectMapper.readTree(responseBody);
            JsonNode content = root.path("content");

            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText();
            }

            // Check for API error response
            if (root.has("error")) {
                String errorMsg = root.path("error").path("message").asText("Unknown AI error");
                throw new AiServiceException("Anthropic API error: " + errorMsg);
            }

            throw new AiServiceException("Empty or unexpected response from AI service");

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call Anthropic API: {}", e.getMessage(), e);
            throw new AiServiceException("AI recommendation service unavailable: " + e.getMessage(), e);
        }
    }

    // ── Response parsing ──────────────────────────────────────────────────────

    private Map<String, String> parseAiResponse(String response) {
        Map<String, String> result = new HashMap<>();
        if (response == null || response.isBlank()) return result;

        result.put("suggested_treatment",      extractSection(response, "SUGGESTED_TREATMENT"));
        result.put("suggested_drugs",          extractSection(response, "SUGGESTED_DRUGS"));
        result.put("contraindication_warnings",extractSection(response, "CONTRAINDICATION_WARNINGS"));
        result.put("referral_suggestion",      extractSection(response, "REFERRAL_SUGGESTION"));

        // Extract urgency level from first line
        String urgency = "ROUTINE";
        if (response.contains("URGENCY_LEVEL:")) {
            String line = response.substring(response.indexOf("URGENCY_LEVEL:") + 14).split("\n")[0].trim();
            if (line.contains("EMERGENCY")) urgency = "EMERGENCY";
            else if (line.contains("URGENT")) urgency = "URGENT";
        }
        result.put("urgency_level", urgency);

        return result;
    }

    private String extractSection(String text, String sectionName) {
        try {
            String marker = sectionName + ":";
            int start = text.indexOf(marker);
            if (start == -1) return null;

            start += marker.length();

            // Find next section header (ALL_CAPS_WORD:)
            int end = text.length();
            String[] sections = {
                "URGENCY_LEVEL:", "SUGGESTED_TREATMENT:", "SUGGESTED_DRUGS:",
                "CONTRAINDICATION_WARNINGS:", "REFERRAL_SUGGESTION:", "CLINICAL_REASONING:", "IMPORTANT:"
            };
            for (String s : sections) {
                int idx = text.indexOf(s, start);
                if (idx != -1 && idx < end) end = idx;
            }

            return text.substring(start, end).trim();
        } catch (Exception e) {
            return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AiRecommendation findAndValidateForReview(UUID recId) {
        AiRecommendation rec = recommendationRepository.findById(recId)
            .orElseThrow(() -> new ResourceNotFoundException("AI Recommendation", "id", recId));

        if (rec.getStatus() != RecommendationStatus.PENDING_REVIEW) {
            throw new BusinessException(
                "Recommendation has already been reviewed. Status: " + rec.getStatus());
        }
        return rec;
    }

    private int calculateAge(java.time.LocalDate dob) {
        if (dob == null) return 0;
        return java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private AiRecommendationResponse toResponse(AiRecommendation r) {
        return AiRecommendationResponse.builder()
            .id(r.getId())
            .suggestedTreatment(r.getSuggestedTreatment())
            .suggestedDrugs(r.getSuggestedDrugs())
            .contraindicationWarnings(r.getContraindicationWarnings())
            .referralSuggestion(r.getReferralSuggestion())
            .urgencyLevel(r.getUrgencyLevel())
            .status(r.getStatus())
            .doctorNotes(r.getDoctorNotes())
            .rejectionReason(r.getRejectionReason())
            .modifiedTreatment(r.getModifiedTreatment())
            .requestedByName(r.getRequestedBy() != null ? r.getRequestedBy().getFullName() : null)
            .reviewedByName(r.getReviewedBy()   != null ? r.getReviewedBy().getFullName()   : null)
            .aiModel(r.getAiModel())
            .reviewedAt(r.getReviewedAt())
            .createdAt(r.getCreatedAt())
            .build();
    }
}
