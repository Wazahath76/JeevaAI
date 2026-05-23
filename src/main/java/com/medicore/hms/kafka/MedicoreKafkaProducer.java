package com.medicore.hms.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kafka producer stub for local dev without Kafka.
 * Replace with real KafkaTemplate implementation when Kafka is available.
 */
@Component
@Slf4j
public class MedicoreKafkaProducer {

    public void publishDoctorAssigned(DoctorAssignedEvent event) {
        log.info("[KAFKA-STUB] Doctor assigned: patient={} doctor={} role={}",
            event.getPatientCode(), event.getAssignedDoctorName(), event.getAssignmentRole());
    }

    public void publishDoctorRevoked(DoctorAssignedEvent event) {
        log.info("[KAFKA-STUB] Doctor revoked: patient={} doctor={}",
            event.getPatientCode(), event.getAssignedDoctorName());
    }

    public void publishPatientAdmitted(PatientAdmittedEvent event) {
        log.info("[KAFKA-STUB] Patient admitted: code={} ward={}",
            event.getPatientCode(), event.getWard());
    }

    public void publishSurgeryScheduled(SurgeryScheduledEvent event) {
        log.info("[KAFKA-STUB] Surgery scheduled: patient={} procedure={} surgeon={}",
            event.getPatientCode(), event.getProcedureName(), event.getSurgeonName());
    }
}
