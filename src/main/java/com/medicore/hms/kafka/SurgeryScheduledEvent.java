package com.medicore.hms.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurgeryScheduledEvent {
    private UUID   surgicalPlanId;
    private UUID   patientId;
    private String patientCode;
    private String patientName;
    private String procedureName;
    private UUID   surgeonId;
    private String surgeonName;
    private UUID   anaesthetistId;
    private String anaesthetistName;
    private String theatreNumber;
    private LocalDateTime scheduledAt;
    private LocalDateTime occurredAt;
}
