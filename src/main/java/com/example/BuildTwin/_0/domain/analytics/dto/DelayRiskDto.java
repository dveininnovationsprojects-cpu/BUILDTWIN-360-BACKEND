package com.example.BuildTwin._0.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelayRiskDto {

    private Long projectId;
    private BigDecimal delayRiskScore; // 0.00 - 100.00
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private long criticalBlockersCount;
    private long affectedActivitiesCount;
}
