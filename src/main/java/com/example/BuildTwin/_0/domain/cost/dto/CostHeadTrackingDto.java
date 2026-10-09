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
@Schema(description = "Breakdown of cost performance for a specific Cost Head (Material, Labour, Equipment, Subcontractor, Overhead)")
public class CostHeadTrackingDto {

    @Schema(description = "Cost Head category", example = "LABOUR")
    private CostHead costHead;

    @Schema(description = "Original baseline budget for this head", example = "500000.00")
    private BigDecimal baselineBudget;

    @Schema(description = "Approved / revised budget for this head", example = "550000.00")
    private BigDecimal approvedBudget;

    @Schema(description = "Total actual expenses incurred to date", example = "320000.00")
    private BigDecimal actualIncurred;

    @Schema(description = "Total procurement / subcontract commitments committed", example = "80000.00")
    private BigDecimal committedAmount;

    @Schema(description = "Total forecasted expenditure (Actual + Committed)", example = "400000.00")
    private BigDecimal totalForecasted;

    @Schema(description = "Budget variance (Approved Budget - Total Forecasted)", example = "150000.00")
    private BigDecimal variance;

    @Schema(description = "Percentage of budget utilized", example = "72.73%")
    private String utilizationPercentage;

    @Schema(description = "Health status of this cost head (UNDER_BUDGET, ON_BUDGET, OVER_BUDGET)", example = "UNDER_BUDGET")
    private String status;
}
