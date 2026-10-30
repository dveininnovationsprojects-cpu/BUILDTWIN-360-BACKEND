package com.example.BuildTwin._0.dto.lookahead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comprehensive 14-Day (Two-Week / Sprint) Look-Ahead Schedule Package with Week 1 (Immediate Commitment) and Week 2 (Make-Ready Lookahead) breakdown")
public class FourteenDayLookaheadResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "14-Day Window Start Date", example = "2026-10-01")
    private LocalDate windowStartDate;

    @Schema(description = "14-Day Window End Date", example = "2026-10-14")
    private LocalDate windowEndDate;

    @Schema(description = "Lookahead Window Duration in Calendar Days", example = "14")
    private Integer lookaheadDays;

    @Schema(description = "Week 1 Start Date (Days 1 to 7 - Immediate Commitment Window)", example = "2026-10-01")
    private LocalDate week1StartDate;

    @Schema(description = "Week 1 End Date (Days 1 to 7)", example = "2026-10-07")
    private LocalDate week1EndDate;

    @Schema(description = "Week 1 Execution KPIs and Metrics")
    private LookaheadMetricsResponse week1Metrics;

    @Schema(description = "Week 2 Start Date (Days 8 to 14 - Make-Ready & Constraint Removal Window)", example = "2026-10-08")
    private LocalDate week2StartDate;

    @Schema(description = "Week 2 End Date (Days 8 to 14)", example = "2026-10-14")
    private LocalDate week2EndDate;

    @Schema(description = "Week 2 Lookahead KPIs and Readiness Metrics")
    private LookaheadMetricsResponse week2Metrics;

    @Schema(description = "Consolidated 14-Day Schedule KPIs and Overall Readiness")
    private LookaheadMetricsResponse overallMetrics;

    @Schema(description = "14-Day Day-by-Day (Day 1 to Day 14) workload execution distribution matrix")
    @Builder.Default
    private List<LookaheadDailySummary> dailyBreakdown = new ArrayList<>();

    @Schema(description = "Activities active or starting during Week 1 (Days 1 to 7)")
    @Builder.Default
    private List<LookaheadActivityItemResponse> week1Activities = new ArrayList<>();

    @Schema(description = "Activities scheduled or starting during Week 2 (Days 8 to 14)")
    @Builder.Default
    private List<LookaheadActivityItemResponse> week2Activities = new ArrayList<>();

    @Schema(description = "All distinct activities active in the complete 14-day lookahead window")
    @Builder.Default
    private List<LookaheadActivityItemResponse> allActivities = new ArrayList<>();

    @Schema(description = "Identified constraints and hindrances blocking upcoming Week 2 activities requiring make-ready resolution")
    private LookaheadConstraintSummaryResponse week2Constraints;

    @Schema(description = "Overall Lookahead Health Status (HEALTHY, MODERATE_RISK, CRITICAL_ATTENTION_REQUIRED)", example = "HEALTHY")
    private String lookaheadHealthStatus;

    @Schema(description = "Actionable summary note for project planner and site engineers", example = "14-Day Look-Ahead active with 12 activities across CIVIL and MEP trades. Week 1 is 90% ready. Week 2 has 2 pending predecessor constraints.")
    private String plannerSummary;
}
