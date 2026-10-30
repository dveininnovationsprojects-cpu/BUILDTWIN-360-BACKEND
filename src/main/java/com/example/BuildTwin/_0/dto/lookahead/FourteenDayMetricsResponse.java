package com.example.BuildTwin._0.dto.lookahead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "14-Day Look-Ahead Metrics comparing Week 1 (Immediate Commitment) vs Week 2 (Lookahead Make-Ready)")
public class FourteenDayMetricsResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "14-Day Window Start Date", example = "2026-10-01")
    private LocalDate windowStartDate;

    @Schema(description = "14-Day Window End Date", example = "2026-10-14")
    private LocalDate windowEndDate;

    @Schema(description = "Consolidated 14-Day Schedule KPIs and Readiness Score")
    private LookaheadMetricsResponse overallMetrics;

    @Schema(description = "Week 1 (Days 1 to 7) Execution KPIs")
    private LookaheadMetricsResponse week1Metrics;

    @Schema(description = "Week 2 (Days 8 to 14) Lookahead KPIs")
    private LookaheadMetricsResponse week2Metrics;

    @Schema(description = "Readiness Trend Comparison (HEALTHY, MODERATE_RISK, CRITICAL_ATTENTION_REQUIRED)", example = "HEALTHY")
    private String lookaheadHealthStatus;

    @Schema(description = "Actionable summary note for project planner and site engineers", example = "Week 1 has 90% readiness; Week 2 has 2 pending constraints requiring material staging.")
    private String plannerSummary;
}
