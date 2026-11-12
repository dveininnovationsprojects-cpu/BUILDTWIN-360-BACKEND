package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Work Breakdown Structure (WBS) & Activity Module Integration Details")
public class WbsIntegrationDto {

    @Schema(description = "Total work packages configured", example = "8")
    private Long workPackageCount;

    @Schema(description = "Total WBS activities defined", example = "45")
    private Long activityCount;

    @Schema(description = "Completed activities count", example = "22")
    private Long completedActivitiesCount;

    @Schema(description = "In-progress activities count", example = "16")
    private Long inProgressActivitiesCount;

    @Schema(description = "Planned / unstarted activities count", example = "7")
    private Long plannedActivitiesCount;

    @Schema(description = "Total milestone count", example = "5")
    private Long milestoneCount;

    @Schema(description = "WBS Module Status", example = "ACTIVE")
    private String status;
}
