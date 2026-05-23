package com.medicore.hms.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddVitalRequest {
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer pulseBpm;
    private BigDecimal temperatureCelsius;
    private Integer spo2Percent;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private Integer respiratoryRate;
    private Integer bloodGlucose;
    private String remarks;
}
