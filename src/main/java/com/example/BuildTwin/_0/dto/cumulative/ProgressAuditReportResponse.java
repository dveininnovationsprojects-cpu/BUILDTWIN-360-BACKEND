package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comprehensive Cumulative Progress Audit and Data Integrity Report")
public class ProgressAuditReportResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-BLR-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Audit timestamp", example = "2026-10-27T18:00:00")
    private LocalDateTime auditTimestamp;

    @Schema(description = "Total activities audited across project", example = "45")
    private Integer totalActivitiesAudited;

    @Schema(description = "Number of activities with 100% verified and synchronized progress data", example = "42")
    private Integer cleanActivitiesCount;

    @Schema(description = "Number of activities with detected data discrepancies", example = "3")
    private Integer discrepantActivitiesCount;

    @Schema(description = "Count of activities with quantity mismatches", example = "2")
    private Integer quantityMismatchesCount;

    @Schema(description = "Count of activities with percentage formula mismatches", example = "1")
    private Integer percentageMismatchesCount;

    @Schema(description = "Count of activities with unapproved over-executions beyond 100%", example = "0")
    private Integer overExecutedActivitiesCount;

    @Schema(description = "Overall data integrity status: HEALTHY, MINOR_DISCREPANCIES, ACTION_REQUIRED", example = "MINOR_DISCREPANCIES")
    private String integrityStatus;

    @Schema(description = "Detailed list of flagged discrepancies")
    @Builder.Default
    private List<ProgressAuditItemDto> discrepancies = new ArrayList<>();
}
