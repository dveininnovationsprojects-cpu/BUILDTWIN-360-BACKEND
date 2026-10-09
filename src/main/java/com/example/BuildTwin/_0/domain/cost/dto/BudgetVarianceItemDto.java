package com.example.BuildTwin._0.domain.cost.dto;

import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Line item comparison between Baseline Budget, Revised Budget, and Actual Incurred Cost")
public class BudgetVarianceItemDto {

    @Schema(description = "Budget record ID", example = "1")
    private Long budgetId;

    @Schema(description = "Standard Cost Breakdown Structure (CBS) code", example = "RCC_CIVIL")
    private String costCode;

    @Schema(description = "Cost Head classification", example = "MATERIAL")
    private CostHead costHead;

    @Schema(description = "Description of work / cost head", example = "Cement & Aggregates for Substructure")
    private String description;

    @Schema(description = "Original baseline budget allocated", example = "500000.00")
    private BigDecimal baselineAmount;

    @Schema(description = "Authorized revised budget", example = "550000.00")
    private BigDecimal revisedAmount;

    @Schema(description = "Actual expenses incurred to date", example = "380000.00")
    private BigDecimal actualIncurred;

    @Schema(description = "Procurement commitments committed", example = "70000.00")
    private BigDecimal committedAmount;

    @Schema(description = "Total projected cost (Actual + Committed)", example = "450000.00")
    private BigDecimal totalProjected;

    @Schema(description = "Variance (Revised Budget - Total Projected)", example = "100000.00")
    private BigDecimal variance;

    @Schema(description = "Percentage of budget utilized", example = "81.82%")
    private String utilizationPercentage;

    @Schema(description = "Variance status (UNDER_BUDGET, ON_BUDGET, OVER_BUDGET)", example = "UNDER_BUDGET")
    private String status;
}
