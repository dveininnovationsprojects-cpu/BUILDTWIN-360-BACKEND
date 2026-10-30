package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project-level Progress Percentage Tracking Dashboard with Discipline, Work Package, and Location Breakdown")
public class ProjectProgressPercentageResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-SKY-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Skyline Towers")
    private String projectName;

    @Schema(description = "Overall cumulative actual progress percentage", example = "54.2")
    private Double overallActualProgressPercentage;

    @Schema(description = "Overall planned progress percentage according to schedule timeline", example = "50.0")
    private Double overallPlannedProgressPercentage;

    @Schema(description = "Overall progress variance (Actual % - Planned %)", example = "4.2")
    private Double overallProgressVariance;

    @Schema(description = "Overall project performance status: AHEAD, ON_TRACK, BEHIND", example = "ON_TRACK")
    private String overallPerformanceStatus;

    @Schema(description = "Total activities across project", example = "45")
    private Integer totalActivities;

    @Schema(description = "Planned activities count", example = "15")
    private Integer plannedActivities;

    @Schema(description = "In-progress activities count", example = "18")
    private Integer inProgressActivities;

    @Schema(description = "Completed activities count", example = "10")
    private Integer completedActivities;

    @Schema(description = "Delayed activities count", example = "2")
    private Integer delayedActivities;

    @Schema(description = "Count of activities performing ahead of schedule", example = "8")
    private Integer aheadActivities;

    @Schema(description = "Count of activities on track with schedule", example = "25")
    private Integer onTrackActivities;

    @Schema(description = "Count of activities lagging behind schedule", example = "2")
    private Integer behindActivities;

    @Builder.Default
    @Schema(description = "Progress breakdown by trade discipline")
    private List<DisciplineProgressSummary> disciplineBreakdown = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Progress breakdown by work package")
    private List<WorkPackageProgressSummary> workPackageBreakdown = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Progress breakdown by physical building / location")
    private List<LocationProgressSummary> locationBreakdown = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Discipline Progress Summary")
    public static class DisciplineProgressSummary {
        private String discipline;
        private Integer totalActivities;
        private Integer completedActivities;
        private Integer inProgressActivities;
        private Double plannedProgressPercentage;
        private Double actualProgressPercentage;
        private Double progressVariance;
        private String performanceStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Work Package Progress Summary")
    public static class WorkPackageProgressSummary {
        private Long workPackageId;
        private String workPackageCode;
        private String workPackageName;
        private String discipline;
        private String status;
        private Double weightage;
        private Integer totalActivities;
        private Double plannedProgressPercentage;
        private Double actualProgressPercentage;
        private Double progressVariance;
        private String performanceStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Location Progress Summary")
    public static class LocationProgressSummary {
        private Long buildingId;
        private String buildingName;
        private Integer totalActivities;
        private Double plannedProgressPercentage;
        private Double actualProgressPercentage;
        private Double progressVariance;
        private String performanceStatus;
    }
}
