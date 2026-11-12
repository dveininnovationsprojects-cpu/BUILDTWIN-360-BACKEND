package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.integration.*;
import com.example.BuildTwin._0.service.ModuleIntegrationService;
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

@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
@Tag(
        name = "12. Module Integration & Collaboration",
        description = "Unified Cross-Module Integration APIs: Spatial Hierarchy, WBS & Activities, Schedule Baselines, Lookahead Planning, DPR Field Logging, and Cumulative Progress Engine Integration."
)
@SecurityRequirement(name = "BearerAuth")
public class ModuleIntegrationController {

    private final ModuleIntegrationService moduleIntegrationService;

    // =========================================================================
    // 1. PROJECT MODULE INTEGRATION SUMMARY
    // =========================================================================

    @GetMapping({"/projects/{projectId}/summary", "/project/{projectId}/summary"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Module Integration Summary",
            description = "Retrieves aggregated integration status, rollups, readiness score, and sync indicators across all core subsystems (Hierarchy, WBS, Baseline, Lookahead, DPR, and Progress Engine) for a specific project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project module integration summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectModuleIntegrationSummaryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectModuleIntegrationSummaryResponse>> getProjectModuleIntegrationSummary(
            @Parameter(description = "Project ID (e.g. 1)", example = "1")
            @PathVariable Long projectId) {

        ProjectModuleIntegrationSummaryResponse summary = moduleIntegrationService.getProjectModuleIntegrationSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Project module integration summary retrieved successfully"));
    }

    // =========================================================================
    // 2. REGISTERED MODULES LIST
    // =========================================================================

    @GetMapping("/projects/{projectId}/modules")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Registered Modules and Integration Readiness",
            description = "Returns a comprehensive list of all active platform modules, their endpoint base paths, semantic versions, and live entity counts for the specified project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Modules list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectModulesListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectModulesListResponse>> getProjectModulesList(
            @Parameter(description = "Project ID (e.g. 1)", example = "1")
            @PathVariable Long projectId) {

        ProjectModulesListResponse modulesList = moduleIntegrationService.getProjectModulesList(projectId);
        return ResponseEntity.ok(ApiResponse.success(modulesList, "Project modules list retrieved successfully"));
    }

    // =========================================================================
    // 3. PROJECT CROSS-REFERENCES MAPPING
    // =========================================================================

    @GetMapping("/projects/{projectId}/cross-references")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Cross-Module Entity Reference Mapping",
            description = "Provides full-table cross-reference mappings across WBS Activities, parent Work Packages, Sites, Buildings, Floors, Zones, and Assigned Contractors."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cross-references retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectCrossReferenceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectCrossReferenceResponse>> getProjectCrossReferences(
            @Parameter(description = "Project ID (e.g. 1)", example = "1")
            @PathVariable Long projectId) {

        ProjectCrossReferenceResponse crossReferences = moduleIntegrationService.getProjectCrossReferences(projectId);
        return ResponseEntity.ok(ApiResponse.success(crossReferences, "Cross-module references retrieved successfully"));
    }

    // =========================================================================
    // 4. ACTIVITY INTEGRATION DETAILS
    // =========================================================================

    @GetMapping("/activities/{activityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Deep Activity Integration Details",
            description = "Returns unified 360-degree integration snapshot for a single activity, including spatial lineage, WBS parentage, CPM predecessors/successors, active baseline snapshot, and latest DPR progress."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Activity integration details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ActivityIntegrationDetailsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<ApiResponse<ActivityIntegrationDetailsResponse>> getActivityIntegrationDetails(
            @Parameter(description = "Activity ID (e.g. 101)", example = "101")
            @PathVariable Long activityId) {

        ActivityIntegrationDetailsResponse details = moduleIntegrationService.getActivityIntegrationDetails(activityId);
        return ResponseEntity.ok(ApiResponse.success(details, "Activity integration details retrieved successfully"));
    }

    // =========================================================================
    // 5. SYSTEM INTEGRATION HEALTH
    // =========================================================================

    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Check System-Wide Module Integration Health",
            description = "Verifies operational readiness and operational state across all core subsystems (Hierarchy, WBS, Baseline, Lookahead, DPR, Progress Engine, and Executive Summary)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "System integration health retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ModuleHealthResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ModuleHealthResponse>> getSystemIntegrationHealth() {
        ModuleHealthResponse health = moduleIntegrationService.getSystemIntegrationHealth();
        return ResponseEntity.ok(ApiResponse.success(health, "Module integration health check completed"));
    }
}
