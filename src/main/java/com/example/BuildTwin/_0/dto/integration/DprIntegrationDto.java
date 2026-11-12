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
@Schema(description = "Daily Progress Reporting (DPR) Field Module Integration Details")
public class DprIntegrationDto {

    @Schema(description = "Total Daily Progress Reports filed", example = "42")
    private Long totalDprs;

    @Schema(description = "Approved Daily Progress Reports count", example = "38")
    private Long approvedDprs;

    @Schema(description = "Pending / Draft DPRs count", example = "4")
    private Long pendingDprs;

    @Schema(description = "Date of the most recently submitted DPR", example = "2026-10-30")
    private LocalDate latestReportDate;

    @Schema(description = "Work shift of latest report", example = "GENERAL")
    private String latestWorkShift;

    @Schema(description = "Cumulative manpower headcount logged across DPRs", example = "1240")
    private Long totalManpowerHeadcount;

    @Schema(description = "Total recorded safety incidents across DPRs", example = "0")
    private Long totalSafetyIncidents;

    @Schema(description = "DPR Module Integration Status", example = "FIELD_ACTIVE")
    private String status;
}
