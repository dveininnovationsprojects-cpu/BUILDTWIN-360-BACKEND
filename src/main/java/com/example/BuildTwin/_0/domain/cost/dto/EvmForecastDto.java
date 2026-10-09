package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "EVM forecasting report focusing on Estimate At Completion (EAC), ETC, and projected completion variance")
public class EvmForecastDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Budget At Completion (BAC): Total sanctioned budget", example = "2000000.00")
    private BigDecimal budgetAtCompletion;

    @Schema(description = "Planned Value (PV)", example = "1000000.00")
    private BigDecimal plannedValue;

    @Schema(description = "Earned Value (EV)", example = "950000.00")
    private BigDecimal earnedValue;

    @Schema(description = "Actual Cost (AC)", example = "1100000.00")
    private BigDecimal actualCost;

    @Schema(description = "Cost Performance Index (CPI = EV / AC)", example = "0.8636")
    private BigDecimal costPerformanceIndex;

    @Schema(description = "Schedule Performance Index (SPI = EV / PV)", example = "0.9500")
    private BigDecimal schedulePerformanceIndex;

    @Schema(description = "Estimate At Completion (EAC = BAC / CPI)", example = "2315886.98")
    private BigDecimal estimateAtCompletion;

    @Schema(description = "Estimate To Complete (ETC = EAC - AC)", example = "1215886.98")
    private BigDecimal estimateToComplete;

    @Schema(description = "Variance At Completion (VAC = BAC - EAC)", example = "-315886.98")
    private BigDecimal varianceAtCompletion;

    @Schema(description = "Forecast interpretation and executive summary", example = "Project is trending over budget by INR 3,15,887 with CPI of 0.86. Corrective action recommended.")
    private String forecastSummary;
}
