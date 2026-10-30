package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Cumulative S-Curve Time-Series Progression and Earned Value Projection")
public class CumulativeSCurveResponse {

    @Schema(description = "Scope level: PROJECT, WORK_PACKAGE, or ACTIVITY", example = "PROJECT")
    private String scope;

    @Schema(description = "Entity ID", example = "1")
    private Long entityId;

    @Schema(description = "Entity Name", example = "Skyline Residency Phase 1")
    private String entityName;

    @Schema(description = "Entity Code", example = "PRJ-BLR-001")
    private String entityCode;

    @Schema(description = "Time-series interval: DAILY, WEEKLY, or MONTHLY", example = "WEEKLY")
    private String interval;

    @Schema(description = "Start date of S-Curve timeline", example = "2026-09-01")
    private LocalDate timelineStartDate;

    @Schema(description = "End date of S-Curve timeline", example = "2026-12-31")
    private LocalDate timelineEndDate;

    @Schema(description = "Current actual cumulative progress percentage achieved to date", example = "44.0")
    private Double currentActualPercentage;

    @Schema(description = "Current scheduled planned progress percentage", example = "40.0")
    private Double currentPlannedPercentage;

    @Schema(description = "Total baseline budget amount", example = "1000000.00")
    private BigDecimal totalBudget;

    @Schema(description = "Total Earned Value achieved (EV)", example = "440000.00")
    private BigDecimal totalEarnedValue;

    @Schema(description = "Total Planned Value scheduled to date (PV)", example = "400000.00")
    private BigDecimal totalPlannedValue;

    @Schema(description = "Schedule Performance Index (SPI)", example = "1.10")
    private Double currentSpi;

    @Schema(description = "Overall status: AHEAD, ON_TRACK, BEHIND", example = "AHEAD")
    private String overallStatus;

    @Schema(description = "Chronological list of S-curve data points")
    @Builder.Default
    private List<SCurveDataPointDto> curveData = new ArrayList<>();
}
