package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Project-wide Activity Quantity Tracking Dashboard with trade discipline rollups")
public class ProjectQuantityTrackingResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-SKY-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Skyline Heights Luxury Towers")
    private String projectName;

    @Schema(description = "Total number of tracked WBS activities", example = "45")
    private Integer totalActivities;

    @Schema(description = "Count of fully completed activities", example = "15")
    private Integer completedActivities;

    @Schema(description = "Count of activities currently in progress", example = "20")
    private Integer inProgressActivities;

    @Schema(description = "Count of planned activities not yet started", example = "10")
    private Integer plannedActivities;

    @Schema(description = "Count of activities that have exceeded planned baseline quantity", example = "1")
    private Integer overExecutedActivities;

    @Schema(description = "Total planned baseline quantity across project activities", example = "45000.0")
    private Double totalPlannedQuantity;

    @Schema(description = "Total completed quantity across project activities", example = "27500.0")
    private Double totalCompletedQuantity;

    @Schema(description = "Total remaining balance quantity across project activities", example = "17500.0")
    private Double totalBalanceQuantity;

    @Schema(description = "Overall physical quantity completion percentage", example = "61.1")
    private Double overallQuantityProgressPercentage;

    @Schema(description = "Rollup metrics by engineering discipline (CIVIL, STRUCTURAL, MEP, FINISHING)")
    @Builder.Default
    private List<QuantityDisciplineRollup> disciplineRollups = new ArrayList<>();

    @Schema(description = "Individual activity quantity tracking records")
    @Builder.Default
    private List<ActivityQuantityTrackingResponse> activities = new ArrayList<>();
}
