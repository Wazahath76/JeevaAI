package com.medicore.hms.kafka;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String DOCTOR_ASSIGNED      = "medicore.doctor.assigned";
    public static final String DOCTOR_REVOKED       = "medicore.doctor.revoked";
    public static final String AI_RECOMMENDATION    = "medicore.ai.recommendation";
    public static final String PATIENT_ADMITTED     = "medicore.patient.admitted";
    public static final String PATIENT_DISCHARGED   = "medicore.patient.discharged";
    public static final String SURGERY_SCHEDULED    = "medicore.surgery.scheduled";
    public static final String AUDIT_EVENT          = "medicore.audit.event";
}
