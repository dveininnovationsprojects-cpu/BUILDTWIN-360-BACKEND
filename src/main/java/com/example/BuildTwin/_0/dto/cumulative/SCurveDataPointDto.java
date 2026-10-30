package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Single periodic data point on the cumulative S-curve timeline")
public class SCurveDataPointDto {

    @Schema(description = "Milestone or bucket date", example = "2026-09-30")
    private LocalDate date;

    @Schema(description = "Planned cumulative progress percentage as of this date (BCWS S-Curve)", example = "42.5")
    private Double plannedProgressPercentage;

    @Schema(description = "Actual cumulative progress percentage achieved as of this date (BCWP S-Curve)", example = "45.0")
    private Double actualProgressPercentage;

    @Schema(description = "Progress variance % (Actual % - Planned %)", example = "2.5")
    private Double progressVariancePercentage;

    @Schema(description = "Planned Value (PV / BCWS) in project currency", example = "425000.00")
    private BigDecimal plannedValue;

    @Schema(description = "Earned Value (EV / BCWP) in project currency", example = "450000.00")
    private BigDecimal earnedValue;

    @Schema(description = "Schedule Performance Index (SPI = EV / PV)", example = "1.06")
    private Double schedulePerformanceIndex;

    @Schema(description = "Cumulative physical quantity completed as of date", example = "220.0")
    private Double cumulativeQuantityCompleted;

    @Schema(description = "Performance status: AHEAD, ON_TRACK, BEHIND", example = "AHEAD")
    private String status;
}
