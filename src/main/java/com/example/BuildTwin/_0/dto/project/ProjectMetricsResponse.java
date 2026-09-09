package com.example.BuildTwin._0.dto.project;

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
@Schema(description = "Executive metrics and status distribution for construction projects")
public class ProjectMetricsResponse {

    @Schema(description = "Total number of projects in portfolio", example = "5")
    private long totalProjects;

    @Schema(description = "Total projects in ACTIVE status", example = "3")
    private long activeProjects;

    @Schema(description = "Total projects in PLANNED status", example = "1")
    private long plannedProjects;

    @Schema(description = "Total projects on ON_HOLD", example = "0")
    private long onHoldProjects;

    @Schema(description = "Total completed projects", example = "1")
    private long completedProjects;

    @Schema(description = "Cumulative estimated budget across portfolio", example = "185000000.00")
    private BigDecimal totalEstimatedBudget;

    @Schema(description = "Total active sites across all projects", example = "8")
    private long totalSites;
}
