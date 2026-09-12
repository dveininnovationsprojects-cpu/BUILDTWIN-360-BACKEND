package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "High-level summary of WBS work packages, activities, and cumulative project progress")
public class WbsSummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Total work packages", example = "4")
    private Long totalWorkPackages;

    @Schema(description = "Active work packages", example = "2")
    private Long activeWorkPackages;

    @Schema(description = "Completed work packages", example = "1")
    private Long completedWorkPackages;

    @Schema(description = "Total activities across all work packages", example = "18")
    private Long totalActivities;

    @Schema(description = "Planned activities", example = "6")
    private Long plannedActivities;

    @Schema(description = "In-progress activities", example = "8")
    private Long inProgressActivities;

    @Schema(description = "Completed activities", example = "4")
    private Long completedActivities;

    @Schema(description = "Delayed activities", example = "0")
    private Long delayedActivities;

    @Schema(description = "Overall WBS cumulative progress percentage", example = "42.5")
    private Double overallProgressPercentage;

    @Schema(description = "Total WBS allocated budget", example = "26500000.00")
    private BigDecimal totalAllocatedBudget;
}
