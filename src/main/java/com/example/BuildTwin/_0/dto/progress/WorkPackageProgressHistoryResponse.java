package com.example.BuildTwin._0.dto.progress;

import com.example.BuildTwin._0.dto.common.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Work Package level Progress History breakdown and timeline")
public class WorkPackageProgressHistoryResponse {

    @Schema(description = "Work Package ID", example = "3")
    private Long workPackageId;

    @Schema(description = "Work Package Name", example = "Civil Works Package")
    private String workPackageName;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Total progress history entries recorded in this work package", example = "24")
    private Long totalEntries;

    @Schema(description = "Total physical quantity logged across all package activities", example = "850.0")
    private Double totalQuantityLogged;

    @Schema(description = "Count of unique activities logged", example = "6")
    private Integer uniqueActivitiesCount;

    @Schema(description = "Total blocker days recorded in package", example = "2")
    private Long totalBlockersEncountered;

    @Schema(description = "Chronological list of progress history entries")
    @Builder.Default
    private List<ProgressHistoryItemResponse> history = new ArrayList<>();

    @Schema(description = "Optional paginated view of history items")
    private PageResponse<ProgressHistoryItemResponse> paginatedHistory;
}
