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
@Schema(description = "Cost & Quality Control Unified Report")
public class CostQualityReportDto {

    private Long projectId;
    private String projectName;
    private LocalDateTime generatedAt;

    // Cost Breakdown Structure
    private BigDecimal baselineBudget;
    private BigDecimal revisedBudget;
    private BigDecimal actualCostIncurred;
    private BigDecimal committedCost;
    private BigDecimal projectedTotalCost;
    private BigDecimal netBudgetVariance;
    private String budgetStatus; // UNDER_BUDGET, ON_BUDGET, OVER_BUDGET, CRITICAL
    private Integer activeBudgetOverrunsCount;
    private BigDecimal totalOverrunAmount;

    // EVM Metrics
    private BigDecimal costPerformanceIndex;
    private BigDecimal schedulePerformanceIndex;
    private BigDecimal estimateAtCompletion;
    private BigDecimal varianceAtCompletion;

    // Quality & Blockers
    private Long totalIssuesReported;
    private Long openSnagsCount;
    private Long activeNcrsCount;
    private Long criticalBlockersCount;
    private Long overdueIssuesCount;
    private Double qualityResolutionPercentage;
    private Integer totalSiteDelayDays;
}
