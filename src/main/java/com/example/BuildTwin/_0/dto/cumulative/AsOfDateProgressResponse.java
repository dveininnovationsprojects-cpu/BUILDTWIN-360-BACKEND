package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Historical Point-in-Time Cumulative Progress Snapshot as of a specific cutoff date")
public class AsOfDateProgressResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-BLR-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Cutoff / Snapshot Date", example = "2026-09-30")
    private LocalDate cutoffDate;

    @Schema(description = "Cumulative actual progress percentage achieved as of cutoff date", example = "38.5")
    private Double cumulativeActualProgressPercentage;

    @Schema(description = "Cumulative planned progress percentage scheduled as of cutoff date", example = "35.0")
    private Double cumulativePlannedProgressPercentage;

    @Schema(description = "Progress variance percentage as of cutoff date (Actual % - Planned %)", example = "3.5")
    private Double progressVariancePercentage;

    @Schema(description = "Historical performance status as of cutoff date: AHEAD, ON_TRACK, BEHIND", example = "AHEAD")
    private String performanceStatus;

    @Schema(description = "Total physical quantity completed as of cutoff date", example = "450.0")
    private Double totalCompletedQuantityAsOfDate;

    @Schema(description = "Cumulative Earned Value (EV) in project currency as of cutoff date", example = "385000.00")
    private BigDecimal earnedValueAsOfDate;

    @Schema(description = "Cumulative Planned Value (PV) in project currency as of cutoff date", example = "350000.00")
    private BigDecimal plannedValueAsOfDate;

    @Schema(description = "Schedule Performance Index (SPI) as of cutoff date", example = "1.10")
    private Double spiAsOfDate;

    @Schema(description = "Number of activities completed as of cutoff date", example = "5")
    private Integer completedActivitiesCount;

    @Schema(description = "Number of activities active/in-progress as of cutoff date", example = "12")
    private Integer inProgressActivitiesCount;

    @Schema(description = "Total number of daily progress log entries recorded up to cutoff date", example = "28")
    private Long totalLogsUpToCutoffDate;

    @Schema(description = "Activity-level cumulative progress snapshots as of cutoff date")
    @Builder.Default
    private List<ActivityAsOfDateSnapshot> activitySnapshots = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Activity Progress Snapshot as of Cutoff Date")
    public static class ActivityAsOfDateSnapshot {
        private Long activityId;
        private String activityCode;
        private String activityName;
        private String discipline;
        private String uom;
        private Double plannedQuantity;
        private Double cumulativeCompletedQuantity;
        private Double actualProgressPercentage;
        private Double plannedProgressPercentage;
        private Double variancePercentage;
        private String status;
    }
}
