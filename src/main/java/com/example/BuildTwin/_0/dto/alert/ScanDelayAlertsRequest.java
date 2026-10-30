package com.example.BuildTwin._0.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanDelayAlertsRequest {

    @Schema(description = "Evaluation reference date (defaults to current date if omitted)", example = "2026-10-15")
    private LocalDate asOfDate;

    @Schema(description = "Slippage percentage threshold to trigger PROGRESS_SLIPPAGE alert (default 20.0%)", example = "20.0")
    @Builder.Default
    private Double slippageThresholdPercentage = 20.0;

    @Schema(description = "Lookahead horizon in days to evaluate upcoming deadline risks (default 7 days)", example = "7")
    @Builder.Default
    private Integer upcomingRiskDaysHorizon = 7;

    @Schema(description = "Whether to evaluate and flag near-term upcoming tasks that have uncleared constraints or pending predecessors", example = "true")
    @Builder.Default
    private Boolean includeUpcomingRisks = true;
}
