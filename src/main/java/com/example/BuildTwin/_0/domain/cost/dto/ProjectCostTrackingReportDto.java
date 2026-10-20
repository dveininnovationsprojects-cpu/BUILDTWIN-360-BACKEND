package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Overall project cost tracking report categorized across Material, Labour, and other heads")
public class ProjectCostTrackingReportDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Total original baseline budget across all heads", example = "2000000.00")
    private BigDecimal totalBaselineBudget;

    @Schema(description = "Total authorized / revised budget across all heads", example = "2200000.00")
    private BigDecimal totalApprovedBudget;

    @Schema(description = "Total actual expenses incurred to date", example = "1250000.00")
    private BigDecimal totalActualExpense;

    @Schema(description = "Total procurement and subcontract commitments open", example = "450000.00")
    private BigDecimal totalCommittedCost;

    @Schema(description = "Total forecasted expenditure (Actual + Committed)", example = "1700000.00")
    private BigDecimal totalProjectedCost;

    @Schema(description = "Net budget variance (Approved - Projected)", example = "500000.00")
    private BigDecimal netVariance;

    @Schema(description = "Overall cost health (UNDER_BUDGET, ON_BUDGET, OVER_BUDGET)", example = "UNDER_BUDGET")
    private String overallStatus;

    @Schema(description = "Breakdown per Cost Head (MATERIAL, LABOUR, EQUIPMENT, SUBCONTRACTOR, OVERHEAD)")
    private List<CostHeadTrackingDto> costHeadBreakdown;
}
