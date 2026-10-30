package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Work Package-level Progress Percentage Tracking Breakdown")
public class WorkPackageProgressPercentageResponse {

    @Schema(description = "Work Package ID", example = "5")
    private Long workPackageId;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Work Package Name", example = "Substructure Works")
    private String workPackageName;

    @Schema(description = "Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Work Package Status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Towers")
    private String projectName;

    @Schema(description = "Overall actual progress percentage across activities", example = "62.5")
    private Double overallActualProgressPercentage;

    @Schema(description = "Overall planned progress percentage according to schedule", example = "58.0")
    private Double overallPlannedProgressPercentage;

    @Schema(description = "Progress variance (Actual % - Planned %)", example = "4.5")
    private Double progressVariance;

    @Schema(description = "Overall performance status: AHEAD, ON_TRACK, BEHIND, NOT_STARTED, COMPLETED", example = "ON_TRACK")
    private String performanceStatus;

    @Schema(description = "Total activities configured under work package", example = "12")
    private Integer totalActivities;

    @Schema(description = "Completed activities count", example = "5")
    private Integer completedActivities;

    @Schema(description = "In-progress activities count", example = "4")
    private Integer inProgressActivities;

    @Schema(description = "Planned / unstarted activities count", example = "3")
    private Integer plannedActivities;

    @Schema(description = "Delayed activities count", example = "0")
    private Integer delayedActivities;

    @Builder.Default
    @Schema(description = "List of activities with individual progress tracking metrics")
    private List<ActivityProgressPercentageResponse> activities = new ArrayList<>();
}
