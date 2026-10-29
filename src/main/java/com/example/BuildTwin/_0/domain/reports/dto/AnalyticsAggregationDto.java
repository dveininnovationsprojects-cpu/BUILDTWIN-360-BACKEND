package com.example.BuildTwin._0.domain.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Executive Analytics Aggregation and Overall Health KPI")
public class AnalyticsAggregationDto {

    private Long projectId;
    private String projectName;
    private String projectCode;
    private LocalDateTime generatedAt;

    @Schema(description = "Overall Executive Health Status", example = "OPTIMAL")
    private String overallHealthStatus; // OPTIMAL, GOOD, ATTENTION_REQUIRED, CRITICAL

    @Schema(description = "Composite Health Score out of 100", example = "92.5")
    private Double compositeHealthScore;

    // Component KPI Scores
    private String budgetHealth;   // ON_BUDGET, WARNING, CRITICAL
    private String scheduleHealth; // ON_SCHEDULE, DELAYED
    private String inventoryHealth;// SUFFICIENT, LOW_STOCK
    private String qualityHealth;  // PASSING, AUDIT_FAILURES

    private BigDecimal cpi;
    private BigDecimal spi;
    private Double qualityResolutionRate;
    private Integer activeBlockersCount;
    private Integer unreadCriticalAlertsCount;
}
