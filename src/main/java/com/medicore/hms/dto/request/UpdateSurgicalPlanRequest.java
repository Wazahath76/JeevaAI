package com.medicore.hms.dto.request;

import com.medicore.hms.enums.SurgicalStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateSurgicalPlanRequest {
    private UUID surgeonId;
    private UUID anaesthetistId;
    private SurgicalStatus status;
    private LocalDateTime scheduledAt;
    private String anaesthesiaType;
    private String preOpNotes;
    private String intraOpNotes;
    private String postOpNotes;
    private String complications;
    private String theatreNumber;
}
