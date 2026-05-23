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
public class PatientAdmittedEvent {
    private UUID   patientId;
    private String patientCode;
    private String patientName;
    private String status;
    private String ward;
    private String admittedByName;
    private UUID   primaryDoctorId;
    private String primaryDoctorName;
    private LocalDateTime admittedAt;
}
