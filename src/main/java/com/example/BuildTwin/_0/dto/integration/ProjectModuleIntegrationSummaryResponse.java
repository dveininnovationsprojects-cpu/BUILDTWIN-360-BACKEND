package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Master Project Module Integration Summary Response")
public class ProjectModuleIntegrationSummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Commercial Tower 1")
    private String projectName;

    @Schema(description = "Project Status", example = "ACTIVE")
    private String projectStatus;

    @Schema(description = "Estimated Budget", example = "10000000.00")
    private BigDecimal estimatedBudget;

    @Schema(description = "Overall system integration status (FULLY_INTEGRATED, PARTIALLY_INTEGRATED, INITIALIZED)", example = "FULLY_INTEGRATED")
    private String integrationStatus;

    @Schema(description = "Module readiness & integration health score (0 - 100)", example = "92")
    private Integer readinessScore;

    @Schema(description = "Spatial hierarchy module integration details")
    private HierarchyIntegrationDto hierarchy;

    @Schema(description = "WBS & Activity module integration details")
    private WbsIntegrationDto wbs;

    @Schema(description = "Schedule baseline & revisions module integration details")
    private ScheduleBaselineIntegrationDto scheduleBaseline;

    @Schema(description = "Lookahead planning module integration details")
    private LookaheadIntegrationDto lookahead;

    @Schema(description = "Daily Progress Reporting (DPR) module integration details")
    private DprIntegrationDto dpr;

    @Schema(description = "Progress History & Cumulative Engine integration details")
    private ProgressEngineIntegrationDto progressEngine;

    @Schema(description = "List of active integrated module keys")
    private List<String> integratedModules;

    @Schema(description = "Integration synchronization timestamp")
    private LocalDateTime syncedAt;
}
