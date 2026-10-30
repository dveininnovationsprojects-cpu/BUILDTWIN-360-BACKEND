package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.summary.PortfolioProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.WorkPackageProgressSummaryDto;
import com.example.BuildTwin._0.service.ProjectProgressSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.BuildTwin._0.domain.projects.model.Project;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "11. Project Progress Summary",
        description = "Executive Project Progress Summary Dashboard: Overall progress percentages, schedule health & EVM metrics, activity status distribution, physical quantity tracking, work package rollups, and discipline breakdown."
)
@SecurityRequirement(name = "BearerAuth")
public class ProjectProgressSummaryController {

    private final ProjectProgressSummaryService projectProgressSummaryService;

    // =========================================================================
    // 1. PROJECT PROGRESS SUMMARY DASHBOARD
    // =========================================================================

    @GetMapping({"/api/v1/projects/{projectId}/progress-summary", "/api/v1/projects/{projectId}/progress/summary"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Progress Summary",
            description = "Retrieves executive progress summary for a project, including overall progress percentage, planned progress %, progress variance, schedule health (ON_TRACK/DELAYED/etc.), EVM figures, activity status counts, physical quantity tracking, and work package / discipline rollups."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project progress summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectProgressSummaryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectProgressSummaryResponse>> getProjectProgressSummary(
            @Parameter(description = "Project ID (e.g. 1)", example = "1")
            @PathVariable Long projectId) {

        ProjectProgressSummaryResponse summary = projectProgressSummaryService.getProjectProgressSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Project progress summary retrieved successfully"));
    }

    // =========================================================================
    // 2. PORTFOLIO-WIDE PROGRESS SUMMARY
    // =========================================================================

    @GetMapping("/api/v1/projects/progress-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Portfolio Progress Summary",
            description = "Retrieves high-level executive portfolio progress summary across all registered construction projects."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Portfolio progress summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PortfolioProgressSummaryResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PortfolioProgressSummaryResponse>> getPortfolioProgressSummary() {
        PortfolioProgressSummaryResponse summary = projectProgressSummaryService.getPortfolioProgressSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Portfolio progress summary retrieved successfully"));
    }

    // =========================================================================
    // 3. WORK PACKAGE PROGRESS SUMMARY
    // =========================================================================

    @GetMapping("/api/v1/work-packages/{workPackageId}/progress-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Work Package Progress Summary",
            description = "Retrieves progress summary metrics for an individual work package and its activities."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Work package progress summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkPackageProgressSummaryDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Work package not found")
    })
    public ResponseEntity<ApiResponse<WorkPackageProgressSummaryDto>> getWorkPackageProgressSummary(
            @Parameter(description = "Work Package ID (e.g. 1)", example = "1")
            @PathVariable Long workPackageId) {

        WorkPackageProgressSummaryDto summary = projectProgressSummaryService.getWorkPackageProgressSummary(workPackageId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Work package progress summary retrieved successfully"));
    }
}
