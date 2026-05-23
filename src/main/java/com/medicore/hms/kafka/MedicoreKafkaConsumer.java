package com.medicore.hms.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer stub for local dev without Kafka.
 */
@Component
@Slf4j
public class MedicoreKafkaConsumer {
    // No-op stub — real @KafkaListener annotations require Kafka broker
    // Restore full implementation from medicore-response4.zip when Kafka is available
}
