package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabResultResponse {
    private final UUID id;
    private final String testName;
    private final String testCategory;
    private final String resultValue;
    private final String referenceRange;
    private final String unit;
    private final Boolean isAbnormal;
    private final String reportUrl;
    private final String remarks;
    private final String orderedByName;
    private final LocalDateTime createdAt;
}
