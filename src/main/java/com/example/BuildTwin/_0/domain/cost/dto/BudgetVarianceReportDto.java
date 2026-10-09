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
@Schema(description = "Executive Baseline Budget vs Actual Expense variance analysis report")
public class BudgetVarianceReportDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Towers Phase 1")
    private String projectName;

    @Schema(description = "Original Baseline Budget (BAC) approved at project sanction", example = "5000000.00")
    private BigDecimal totalBaselineBudget;

    @Schema(description = "Total Revised / Authorized Budget with approved change orders", example = "5200000.00")
    private BigDecimal totalRevisedBudget;

    @Schema(description = "Total actual expenses incurred to date", example = "3100000.00")
    private BigDecimal totalActualExpense;

    @Schema(description = "Total committed open PO expenditure", example = "900000.00")
    private BigDecimal totalCommittedExpense;

    @Schema(description = "Total forecasted expenditure (Actual + Committed)", example = "4000000.00")
    private BigDecimal totalProjectedCost;

    @Schema(description = "Net budget variance (Revised Budget - Total Projected)", example = "1200000.00")
    private BigDecimal netVariance;

    @Schema(description = "Percentage of authorized budget utilized", example = "76.92%")
    private String variancePercentage;

    @Schema(description = "Project cost health status (UNDER_BUDGET, ON_BUDGET, OVER_BUDGET, CRITICAL)", example = "UNDER_BUDGET")
    private String overallStatus;

    @Schema(description = "Detailed line-item comparison per CBS cost code")
    private List<BudgetVarianceItemDto> lineItems;
}
