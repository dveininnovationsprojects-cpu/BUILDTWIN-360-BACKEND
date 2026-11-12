package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project-Wide Cross-Module Activity Reference Mapping Response")
public class ProjectCrossReferenceResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-001")
    private String projectCode;

    @Schema(description = "Total activities cross-referenced", example = "45")
    private Integer totalActivities;

    @Schema(description = "List of cross-referenced activities across hierarchy, WBS, and contractors")
    private List<ActivityCrossReferenceDto> activities;
}
