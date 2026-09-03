package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.hierarchy.HierarchyTreeResponse;
import com.example.BuildTwin._0.dto.hierarchy.HierarchyValidationResponse;
import com.example.BuildTwin._0.service.HierarchyService;
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
@RequestMapping("/api/v1/hierarchy")
@RequiredArgsConstructor
@Tag(name = "2. Project & Site Master Setup", description = "Digital Twin Physical Hierarchy (Project -> Site -> Building -> Floor -> Zone) & Relationship Lineage Validation")
@SecurityRequirement(name = "BearerAuth")
public class HierarchyController {

    private final HierarchyService hierarchyService;

    @GetMapping("/tree/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get full physical hierarchy tree",
            description = "Returns complete hierarchical tree structure of the construction project: Project -> Sites -> Buildings -> Floors -> Zones for 3D/Digital Twin navigation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hierarchy tree retrieved successfully",
                    content = @Content(schema = @Schema(implementation = HierarchyTreeResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<HierarchyTreeResponse>> getProjectHierarchyTree(@PathVariable Long projectId) {
        HierarchyTreeResponse tree = hierarchyService.getProjectHierarchyTree(projectId);
        return ResponseEntity.ok(ApiResponse.success(tree, "Physical hierarchy tree retrieved successfully"));
    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Validate Project-Site-Building-Floor-Zone relationships",
            description = "Verifies whether specified Site belongs to Project, Building belongs to Site, Floor belongs to Building, and Zone belongs to Floor. Used before logging Daily Progress (DPR) or assigning inspections."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hierarchy validation result",
                    content = @Content(schema = @Schema(implementation = HierarchyValidationResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<HierarchyValidationResponse>> validateHierarchy(
            @Parameter(description = "Project ID", required = false) @RequestParam(required = false) Long projectId,
            @Parameter(description = "Site ID", required = false) @RequestParam(required = false) Long siteId,
            @Parameter(description = "Building ID", required = false) @RequestParam(required = false) Long buildingId,
            @Parameter(description = "Floor ID", required = false) @RequestParam(required = false) Long floorId,
            @Parameter(description = "Zone ID", required = false) @RequestParam(required = false) Long zoneId) {

        HierarchyValidationResponse result = hierarchyService.validateHierarchy(projectId, siteId, buildingId, floorId, zoneId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }
}
