package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.SurgicalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SurgicalPlanResponse {
    private final UUID id;
    private final UUID patientId;
    private final String patientCode;
    private final String patientName;
    private final String procedureName;
    private final String procedureCode;
    private final SurgicalStatus status;
    private final String surgeonName;
    private final String anaesthetistName;
    private final String anaesthesiaType;
    private final String preOpNotes;
    private final String intraOpNotes;
    private final String postOpNotes;
    private final String complications;
    private final String theatreNumber;
    private final String createdByName;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;
}
