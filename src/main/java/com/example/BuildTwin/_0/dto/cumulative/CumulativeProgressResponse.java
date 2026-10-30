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
@Schema(description = "Executive Master Cumulative Progress Dashboard with Multi-Model Weighting, EVM, and Multi-Dimensional Rollups")
public class CumulativeProgressResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-BLR-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Project Status", example = "ACTIVE")
    private String projectStatus;

    @Schema(description = "Project Planned Start Date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Project Planned End Date", example = "2026-12-31")
    private LocalDate plannedEndDate;

    @Schema(description = "Project Actual Start Date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Selected Primary Weighting Model", example = "WEIGHTAGE_FACTOR")
    private WeightingModel weightingModel;

    // Core Cumulative Percentages
    @Schema(description = "Overall Cumulative Actual Progress Percentage achieved across project", example = "48.5")
    private Double overallActualProgressPercentage;

    @Schema(description = "Overall Cumulative Scheduled Planned Progress Percentage to date", example = "45.0")
    private Double overallPlannedProgressPercentage;

    @Schema(description = "Overall Progress Variance Percentage (Actual % - Planned %)", example = "3.5")
    private Double overallProgressVariancePercentage;

    @Schema(description = "Overall Performance Status: AHEAD, ON_TRACK, BEHIND, COMPLETED, NOT_STARTED", example = "AHEAD")
    private String overallPerformanceStatus;

    // Multi-Weighting Models Comparison
    @Schema(description = "Cumulative Progress % using WBS Activity Weightage Factor model", example = "48.5")
    private Double weightageFactorProgressPercentage;

    @Schema(description = "Cumulative Progress % using Cost / Budget Weighted model (Earned Value)", example = "47.2")
    private Double budgetWeightedProgressPercentage;

    @Schema(description = "Cumulative Progress % using Duration Weighted model", example = "49.0")
    private Double durationWeightedProgressPercentage;

    @Schema(description = "Cumulative Progress % using Equal Weighted model (simple task average)", example = "46.8")
    private Double equalWeightedProgressPercentage;

    // Financial & Earned Value (EVM) Metrics
    @Schema(description = "Total allocated project budget", example = "2500000.00")
    private BigDecimal totalProjectBudget;

    @Schema(description = "Earned Value (EV / BCWP) achieved", example = "1180000.00")
    private BigDecimal earnedValue;

    @Schema(description = "Planned Value (PV / BCWS) scheduled to date", example = "1125000.00")
    private BigDecimal plannedValue;

    @Schema(description = "Schedule Variance in currency (SV = EV - PV)", example = "55000.00")
    private BigDecimal scheduleVarianceAmount;

    @Schema(description = "Schedule Performance Index (SPI = EV / PV)", example = "1.05")
    private Double schedulePerformanceIndex;

    // Physical Quantities Rollup
    @Schema(description = "Cumulative physical output completed across all activities", example = "1425.5")
    private Double totalCompletedQuantity;

    @Schema(description = "Total planned baseline quantity sum across all activities", example = "2940.0")
    private Double totalPlannedQuantity;

    @Schema(description = "Total balance physical quantity remaining", example = "1514.5")
    private Double totalBalanceQuantity;

    // Activity Status Distribution
    @Schema(description = "Total activities configured in project", example = "45")
    private Integer totalActivities;

    @Schema(description = "Completed activities count", example = "12")
    private Integer completedActivities;

    @Schema(description = "In-progress activities count", example = "20")
    private Integer inProgressActivities;

    @Schema(description = "Planned activities count", example = "10")
    private Integer plannedActivities;

    @Schema(description = "Delayed activities count", example = "3")
    private Integer delayedActivities;

    @Schema(description = "Activities running ahead of schedule", example = "8")
    private Integer aheadActivities;

    @Schema(description = "Activities running on track with schedule", example = "24")
    private Integer onTrackActivities;

    @Schema(description = "Activities running behind schedule", example = "3")
    private Integer behindActivities;

    // Multi-Dimensional Rollups
    @Schema(description = "Cumulative progress rollup breakdown by Trade Discipline")
    @Builder.Default
    private List<DisciplineCumulativeRollupDto> disciplineRollup = new ArrayList<>();

    @Schema(description = "Cumulative progress rollup breakdown by Work Package")
    @Builder.Default
    private List<WorkPackageCumulativeRollupDto> workPackageRollup = new ArrayList<>();

    @Schema(description = "Cumulative progress rollup breakdown by Building / Spatial Location")
    @Builder.Default
    private List<LocationCumulativeRollupDto> locationRollup = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Trade Discipline Cumulative Rollup")
    public static class DisciplineCumulativeRollupDto {
        private String discipline;
        private Integer totalActivities;
        private Integer completedActivities;
        private Integer inProgressActivities;
        private Double plannedPercentage;
        private Double actualPercentage;
        private Double variancePercentage;
        private String performanceStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Work Package Cumulative Rollup")
    public static class WorkPackageCumulativeRollupDto {
        private Long workPackageId;
        private String workPackageCode;
        private String workPackageName;
        private String discipline;
        private String status;
        private BigDecimal budgetAmount;
        private Integer totalActivities;
        private Double plannedPercentage;
        private Double actualPercentage;
        private Double variancePercentage;
        private String performanceStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Location / Building Cumulative Rollup")
    public static class LocationCumulativeRollupDto {
        private Long buildingId;
        private String buildingName;
        private Integer totalActivities;
        private Double plannedPercentage;
        private Double actualPercentage;
        private Double variancePercentage;
        private String performanceStatus;
    }
}
