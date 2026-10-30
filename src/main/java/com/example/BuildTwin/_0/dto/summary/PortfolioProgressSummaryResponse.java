package com.example.BuildTwin._0.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Executive Portfolio-wide Progress Summary Response")
public class PortfolioProgressSummaryResponse {

    @Schema(description = "Total projects registered in the portfolio", example = "5")
    private Long totalProjects;

    @Schema(description = "Active ongoing projects count", example = "3")
    private Long activeProjects;

    @Schema(description = "Completed projects count", example = "1")
    private Long completedProjects;

    @Schema(description = "Planned / On-hold projects count", example = "1")
    private Long plannedOrOnHoldProjects;

    @Schema(description = "Portfolio average actual progress percentage", example = "62.4")
    private Double averageProgressPercentage;

    @Schema(description = "Portfolio average planned progress percentage", example = "65.0")
    private Double averagePlannedProgressPercentage;

    @Schema(description = "Total estimated budget across all projects", example = "150000000.00")
    private BigDecimal totalPortfolioBudget;

    @Schema(description = "Total earned value across portfolio", example = "93600000.00")
    private BigDecimal totalPortfolioEarnedValue;

    @Schema(description = "Total activities across all projects", example = "180")
    private Long totalActivities;

    @Schema(description = "Total completed activities across all projects", example = "88")
    private Long totalCompletedActivities;

    @Schema(description = "Individual project progress summaries")
    private List<ProjectProgressSummaryResponse> projectSummaries;
}
