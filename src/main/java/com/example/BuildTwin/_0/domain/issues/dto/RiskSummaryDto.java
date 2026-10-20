package com.example.BuildTwin._0.domain.issues.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project Risk Register KPI summary metrics")
public class RiskSummaryDto {

    @Schema(description = "Project ID", example = "100")
    private Long projectId;

    @Schema(description = "Total registered risks", example = "12")
    private long totalRisks;

    @Schema(description = "Identified risks count", example = "5")
    private long identifiedRisks;

    @Schema(description = "Active monitored risks count", example = "3")
    private long monitoredRisks;

    @Schema(description = "Mitigated risks count", example = "4")
    private long mitigatedRisks;

    @Schema(description = "Critical severity risks count", example = "2")
    private long criticalRisks;

    @Schema(description = "High severity risks count", example = "4")
    private long highRisks;

    @Schema(description = "Medium severity risks count", example = "5")
    private long mediumRisks;

    @Schema(description = "Low severity risks count", example = "1")
    private long lowRisks;
}
