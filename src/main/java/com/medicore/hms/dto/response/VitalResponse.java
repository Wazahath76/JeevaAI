package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VitalResponse {
    private final UUID id;
    private final Integer bloodPressureSystolic;
    private final Integer bloodPressureDiastolic;
    private final Integer pulseBpm;
    private final BigDecimal temperatureCelsius;
    private final Integer spo2Percent;
    private final BigDecimal weightKg;
    private final BigDecimal heightCm;
    private final BigDecimal bmi;
    private final Integer respiratoryRate;
    private final Integer bloodGlucose;
    private final String remarks;
    private final String recordedByName;
    private final LocalDateTime recordedAt;
}
