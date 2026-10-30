package com.example.BuildTwin._0.dto.wbs;

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
@Schema(description = "Detailed Progress Percentage Tracking metrics for a WBS Activity")
public class ActivityProgressPercentageResponse {

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "RCC Column Casting - Floor 1")
    private String activityName;

    @Schema(description = "Discipline / Trade", example = "CIVIL")
    private String discipline;

    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @Schema(description = "Planned quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Completed quantity", example = "250.0")
    private Double completedQuantity;

    @Schema(description = "Remaining quantity", example = "250.0")
    private Double remainingQuantity;

    @Schema(description = "Actual progress percentage achieved", example = "50.0")
    private Double actualProgressPercentage;

    @Schema(description = "Time-based planned progress percentage (linear / S-curve projection)", example = "45.0")
    private Double plannedProgressPercentage;

    @Schema(description = "Progress variance (Actual % - Planned %)", example = "5.0")
    private Double progressVariance;

    @Schema(description = "Performance status: AHEAD, ON_TRACK, BEHIND, NOT_STARTED, COMPLETED", example = "AHEAD")
    private String performanceStatus;

    @Schema(description = "Activity weightage factor in WBS hierarchy", example = "1.5")
    private Double weightage;

    @Schema(description = "Weighted progress contribution points", example = "75.0")
    private Double weightedProgressContribution;

    @Schema(description = "Total planned duration in days", example = "30")
    private Long plannedDurationDays;

    @Schema(description = "Elapsed days from planned start date to today", example = "14")
    private Long elapsedDays;

    @Schema(description = "Remaining days to planned finish date", example = "16")
    private Long remainingDays;

    @Schema(description = "Planned start date", example = "2026-09-15")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-15")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-15")
    private LocalDate actualStartDate;

    @Schema(description = "Actual completion date", example = "null")
    private LocalDate actualEndDate;

    @Schema(description = "Work package ID", example = "5")
    private Long workPackageId;

    @Schema(description = "Work package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Work package Name", example = "Substructure Works")
    private String workPackageName;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Towers")
    private String projectName;

    @Schema(description = "Location hierarchy breadcrumb", example = "Site Alpha > Building A > Floor 1 > Zone 2")
    private String locationHierarchy;

    @Schema(description = "Number of child sub-tasks", example = "3")
    private Integer subtaskCount;

    @Schema(description = "Aggregated weighted progress of children sub-tasks", example = "50.0")
    private Double subtasksWeightedProgress;

    @Builder.Default
    @Schema(description = "Child sub-tasks progress breakdown")
    private List<ActivityProgressPercentageResponse> subtasks = new ArrayList<>();
}
