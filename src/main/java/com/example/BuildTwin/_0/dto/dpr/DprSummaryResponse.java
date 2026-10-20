package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Executive summary metrics and KPIs for Daily Progress Reports on a project with Blocker insights")
public class DprSummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Heights Luxury Towers")
    private String projectName;

    @Schema(description = "Total DPR reports filed to date", example = "45")
    private Long totalReports;

    @Schema(description = "Count of DPRs currently in DRAFT status", example = "2")
    private Long draftReports;

    @Schema(description = "Count of DPRs awaiting management approval (SUBMITTED)", example = "3")
    private Long submittedReports;

    @Schema(description = "Count of DPRs approved and synced to WBS (APPROVED)", example = "38")
    private Long approvedReports;

    @Schema(description = "Count of DPRs rejected with feedback (REJECTED)", example = "2")
    private Long rejectedReports;

    @Schema(description = "Cumulative total manpower headcount deployed across all reports", example = "1920")
    private Long cumulativeManpowerLogged;

    @Schema(description = "Total safety incidents recorded to date", example = "1")
    private Long totalSafetyIncidents;

    @Schema(description = "Total activity blockers logged across all project DPRs", example = "12")
    private Long totalBlockersLogged;

    @Schema(description = "Active open activity blockers across project DPRs", example = "3")
    private Long openBlockersCount;

    @Schema(description = "Most recent report date filed", example = "2026-10-10")
    private LocalDate latestReportDate;

    @Schema(description = "Lifecycle status of the latest report", example = "APPROVED")
    private String latestReportStatus;
}
