package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.building.BuildingResponse;
import com.example.BuildTwin._0.dto.building.CreateBuildingRequest;
import com.example.BuildTwin._0.dto.building.UpdateBuildingRequest;
import com.example.BuildTwin._0.dto.building.UpdateBuildingStatusRequest;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "2. Project & Site Master Setup", description = "Building / Tower Master, Levels & High-Rise Physical Blocks")
@SecurityRequirement(name = "BearerAuth")
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping("/api/v1/sites/{siteId}/buildings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create building / tower under site",
            description = "Registers a new tower, block, or building facility under a physical construction site."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Building created successfully",
                    content = @Content(schema = @Schema(implementation = BuildingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Building code already exists under this site")
    })
    public ResponseEntity<ApiResponse<BuildingResponse>> createBuilding(
            @PathVariable Long siteId,
            @Valid @RequestBody CreateBuildingRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        BuildingResponse created = buildingService.createBuilding(siteId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Building created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/sites/{siteId}/buildings")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all buildings of a site",
            description = "Retrieves all towers/blocks registered under a site."
    )
    public ResponseEntity<ApiResponse<List<BuildingResponse>>> getBuildingsBySite(@PathVariable Long siteId) {
        List<BuildingResponse> buildings = buildingService.getBuildingsBySiteId(siteId);
        return ResponseEntity.ok(ApiResponse.success(buildings, "Buildings retrieved successfully"));
    }

    @GetMapping("/api/v1/sites/{siteId}/buildings/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List buildings of a site with pagination",
            description = "Paginated list of buildings/towers under a site."
    )
    public ResponseEntity<ApiResponse<PageResponse<BuildingResponse>>> getBuildingsBySitePaginated(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BuildingResponse> buildings = buildingService.getBuildingsBySiteIdPaginated(siteId, page, size);
        return ResponseEntity.ok(ApiResponse.success(buildings, "Paginated buildings retrieved successfully"));
    }

    @GetMapping("/api/v1/buildings/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get building by ID",
            description = "Retrieves details of a specific building / tower by its ID."
    )
    public ResponseEntity<ApiResponse<BuildingResponse>> getBuildingById(@PathVariable Long id) {
        BuildingResponse building = buildingService.getBuildingById(id);
        return ResponseEntity.ok(ApiResponse.success(building, "Building retrieved successfully"));
    }

    @PutMapping("/api/v1/buildings/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Update building",
            description = "Updates building details, total floors, built up area, or type."
    )
    public ResponseEntity<ApiResponse<BuildingResponse>> updateBuilding(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBuildingRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        BuildingResponse updated = buildingService.updateBuilding(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Building updated successfully"));
    }

    @PatchMapping("/api/v1/buildings/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update building status",
            description = "Changes construction progress status (PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD)."
    )
    public ResponseEntity<ApiResponse<BuildingResponse>> updateBuildingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBuildingStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        BuildingResponse updated = buildingService.updateBuildingStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Building status updated successfully"));
    }

    @DeleteMapping("/api/v1/buildings/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete building",
            description = "Permanently removes a building and cascades deletion to child floors and zones."
    )
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        buildingService.deleteBuilding(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Building deleted successfully"));
    }
}
