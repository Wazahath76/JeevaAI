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
public class DoctorAssignedEvent {
    private UUID   assignmentId;
    private UUID   patientId;
    private String patientCode;
    private String patientName;
    private UUID   assignedDoctorId;
    private String assignedDoctorEmail;
    private String assignedDoctorName;
    private String assignmentRole;
    private String assignedByName;
    private String assignmentReason;
    private LocalDateTime occurredAt;
}
