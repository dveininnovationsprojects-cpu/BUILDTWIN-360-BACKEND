package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Work Package Cumulative Progress calculation, weightage rollups, and activity breakdowns")
public class WorkPackageCumulativeProgressResponse {

    @Schema(description = "Work Package ID", example = "3")
    private Long workPackageId;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Work Package Name", example = "Civil Works Package")
    private String workPackageName;

    @Schema(description = "Discipline / Trade", example = "CIVIL")
    private String discipline;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Weighting Model used for rollup", example = "WEIGHTAGE_FACTOR")
    private WeightingModel weightingModel;

    @Schema(description = "Cumulative actual progress percentage achieved", example = "46.5")
    private Double cumulativeActualProgressPercentage;

    @Schema(description = "Cumulative scheduled planned progress percentage", example = "42.0")
    private Double cumulativePlannedProgressPercentage;

    @Schema(description = "Progress variance percentage (Actual % - Planned %)", example = "4.5")
    private Double progressVariancePercentage;

    @Schema(description = "Performance status: AHEAD, ON_TRACK, BEHIND, COMPLETED, NOT_STARTED", example = "AHEAD")
    private String performanceStatus;

    @Schema(description = "Allocated budget for work package", example = "500000.00")
    private BigDecimal budgetAmount;

    @Schema(description = "Earned Value (EV = Budget * Actual %)", example = "232500.00")
    private BigDecimal earnedValue;

    @Schema(description = "Planned Value (PV = Budget * Planned %)", example = "210000.00")
    private BigDecimal plannedValue;

    @Schema(description = "Schedule Performance Index (SPI)", example = "1.11")
    private Double spi;

    @Schema(description = "Total activities configured under package", example = "10")
    private Integer totalActivities;

    @Schema(description = "Completed activities count", example = "3")
    private Integer completedActivities;

    @Schema(description = "In-progress activities count", example = "5")
    private Integer inProgressActivities;

    @Schema(description = "Planned / unstarted activities count", example = "2")
    private Integer plannedActivities;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-11-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date", example = "null")
    private LocalDate actualEndDate;

    @Schema(description = "Individual activity cumulative progress contributions")
    @Builder.Default
    private List<ActivityCumulativeProgressResponse> activities = new ArrayList<>();
}
