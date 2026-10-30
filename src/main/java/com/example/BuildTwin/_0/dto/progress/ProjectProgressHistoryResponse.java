package com.example.BuildTwin._0.dto.progress;

import com.example.BuildTwin._0.dto.common.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Executive Project-Wide Progress History Register & Journal Summary")
public class ProjectProgressHistoryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Project Code", example = "PRJ-BLR-001")
    private String projectCode;

    // Project-Wide Summary Metrics
    @Schema(description = "Total progress history entries recorded across project", example = "45")
    private Long totalEntries;

    @Schema(description = "Cumulative physical output logged across all activities", example = "1425.5")
    private Double totalQuantityLogged;

    @Schema(description = "Count of unique activities that have recorded progress", example = "18")
    private Integer uniqueActivitiesCount;

    @Schema(description = "Cumulative manpower headcount logged across all shifts", example = "350")
    private Long totalManpowerLogged;

    @Schema(description = "Total blockers or site hindrances logged", example = "4")
    private Long totalBlockersEncountered;

    @Schema(description = "Earliest progress date on record", example = "2026-09-01")
    private LocalDate earliestLogDate;

    @Schema(description = "Most recent progress date on record", example = "2026-09-28")
    private LocalDate latestLogDate;

    @Schema(description = "Paginated list of chronological progress history items")
    private PageResponse<ProgressHistoryItemResponse> items;
}
