package com.example.BuildTwin._0.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectHealthDto {

    private Long projectId;
    private BigDecimal healthScore; // 0.00 - 100.00
    private String status; // HEALTHY, AT_RISK, CRITICAL
    private BigDecimal scheduleVariance;
    private BigDecimal costVariance;
    private long openBlockersCount;
    private long lowStockAlertsCount;
    private long openQualityIssuesCount;
}
