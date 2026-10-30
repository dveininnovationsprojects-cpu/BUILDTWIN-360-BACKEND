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
@Schema(description = "Complete 7-Day Look-Ahead Schedule Package with KPI metrics, daily workload matrix, and activity cards")
public class LookaheadScheduleResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Lookahead Window Start Date", example = "2026-10-01")
    private LocalDate windowStartDate;

    @Schema(description = "Lookahead Window End Date", example = "2026-10-07")
    private LocalDate windowEndDate;

    @Schema(description = "Lookahead Window Duration in Calendar Days", example = "7")
    private Integer lookaheadDays;

    @Schema(description = "High-level Lookahead KPIs and Readiness Summary")
    private LookaheadMetricsResponse metrics;

    @Schema(description = "Day-by-Day (Day 1 to Day 7) activity execution matrix")
    @Builder.Default
    private List<LookaheadDailySummary> dailyBreakdown = new ArrayList<>();

    @Schema(description = "Detailed list of activities scheduled or active in this 7-day lookahead window")
    @Builder.Default
    private List<LookaheadActivityItemResponse> activities = new ArrayList<>();
}
