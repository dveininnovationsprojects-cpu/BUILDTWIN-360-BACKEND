package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Progress History & Cumulative Progress Engine Integration Details")
public class ProgressEngineIntegrationDto {

    @Schema(description = "Total historical progress journal logs", example = "142")
    private Long totalProgressLogs;

    @Schema(description = "Date of the most recent progress log update", example = "2026-10-30")
    private LocalDate lastProgressUpdateDate;

    @Schema(description = "Master cumulative actual progress percentage", example = "68.5")
    private Double cumulativeProgressPercentage;

    @Schema(description = "Master cumulative planned progress percentage", example = "72.0")
    private Double plannedProgressPercentage;

    @Schema(description = "Progress Variance % (Actual % - Planned %)", example = "-3.5")
    private Double progressVariance;

    @Schema(description = "Cumulative calculation weighting model", example = "WEIGHTAGE_FACTOR")
    private String calculationModel;

    @Schema(description = "Schedule health status (ON_TRACK, AT_RISK, DELAYED, AHEAD)", example = "ON_TRACK")
    private String scheduleHealth;

    @Schema(description = "Progress engine integration status", example = "SYNCHRONIZED")
    private String status;
}
