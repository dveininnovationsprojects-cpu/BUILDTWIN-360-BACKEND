package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Activity-level Cumulative Progress calculation, weightage contribution, and sub-tree rollup")
public class ActivityCumulativeProgressResponse {

    @Schema(description = "Activity ID", example = "15")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Trade Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Work Package ID", example = "3")
    private Long workPackageId;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Work Package Name", example = "Civil Works Package")
    private String workPackageName;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Unit of Measure (UOM)", example = "CUM")
    private String uom;

    @Schema(description = "Planned quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Cumulative quantity completed to date", example = "220.0")
    private Double completedQuantity;

    @Schema(description = "Remaining balance quantity to complete", example = "280.0")
    private Double balanceQuantity;

    @Schema(description = "Cumulative actual progress percentage achieved", example = "44.0")
    private Double actualProgressPercentage;

    @Schema(description = "Time-based scheduled planned progress percentage", example = "40.0")
    private Double plannedProgressPercentage;

    @Schema(description = "Progress variance percentage (Actual % - Planned %)", example = "4.0")
    private Double progressVariancePercentage;

    @Schema(description = "Performance velocity status: AHEAD, ON_TRACK, BEHIND, COMPLETED, NOT_STARTED", example = "AHEAD")
    private String performanceStatus;

    @Schema(description = "Activity weightage factor in WBS hierarchy", example = "1.5")
    private Double weightage;

    @Schema(description = "Weighted progress contribution points", example = "66.0")
    private Double weightedContribution;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-31")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Actual completion date if completed", example = "null")
    private LocalDate actualEndDate;

    @Schema(description = "Activity status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Assigned trade contractor", example = "Apex Concrete Works")
    private String assignedContractor;

    @Schema(description = "Location reference / hierarchy", example = "Site Alpha > Building A > Floor 1")
    private String locationHierarchy;

    @Schema(description = "Number of child sub-tasks", example = "2")
    private Integer childCount;

    @Schema(description = "Recursive child sub-tasks progress breakdown")
    @Builder.Default
    private List<ActivityCumulativeProgressResponse> subtasks = new ArrayList<>();
}
