package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for Earned Value Management (EVM) Analytics and Performance KPIs")
public class EvmMetricsDto {

    @Schema(description = "Target project ID", example = "101")
    private Long projectId;

    /** Budget At Completion (BAC): Total baseline budget */
    @Schema(description = "Budget At Completion (BAC): Total baseline budget approved", example = "1000000.00")
    private BigDecimal budgetAtCompletion;

    /** Planned Value (PV): Approved baseline budget allocated for work scheduled */
    @Schema(description = "Planned Value (PV): Approved baseline budget allocated for work scheduled", example = "400000.00")
    private BigDecimal plannedValue;

    /** Earned Value (EV): Value of actual work completed */
    @Schema(description = "Earned Value (EV): Value of actual work completed", example = "450000.00")
    private BigDecimal earnedValue;

    /** Actual Cost (AC): Total actual cost incurred for work completed */
    @Schema(description = "Actual Cost (AC): Total actual cost incurred for work completed", example = "420000.00")
    private BigDecimal actualCost;

    /** Schedule Variance (SV = EV - PV) */
    @Schema(description = "Schedule Variance (SV = EV - PV)", example = "50000.00")
    private BigDecimal scheduleVariance;

    /** Cost Variance (CV = EV - AC) */
    @Schema(description = "Cost Variance (CV = EV - AC)", example = "30000.00")
    private BigDecimal costVariance;

    /** Schedule Performance Index (SPI = EV / PV) */
    @Schema(description = "Schedule Performance Index (SPI = EV / PV)", example = "1.1250")
    private BigDecimal schedulePerformanceIndex;

    /** Cost Performance Index (CPI = EV / AC) */
    @Schema(description = "Cost Performance Index (CPI = EV / AC)", example = "1.0714")
    private BigDecimal costPerformanceIndex;

    /** Estimate At Completion (EAC = BAC / CPI) */
    @Schema(description = "Estimate At Completion (EAC = BAC / CPI)", example = "933333.33")
    private BigDecimal estimateAtCompletion;

    /** Estimate To Complete (ETC = EAC - AC) */
    @Schema(description = "Estimate To Complete (ETC = EAC - AC)", example = "513333.33")
    private BigDecimal estimateToComplete;

    /** Variance At Completion (VAC = BAC - EAC) */
    @Schema(description = "Variance At Completion (VAC = BAC - EAC)", example = "66666.67")
    private BigDecimal varianceAtCompletion;

    @Schema(description = "Status of Schedule Performance (ON_SCHEDULE, BEHIND_SCHEDULE)", example = "ON_SCHEDULE")
    private String spiStatus;

    @Schema(description = "Status of Cost Performance (UNDER_BUDGET, OVER_BUDGET)", example = "UNDER_BUDGET")
    private String cpiStatus;
}
