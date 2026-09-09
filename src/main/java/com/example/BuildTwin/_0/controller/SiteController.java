package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.site.CreateSiteRequest;
import com.example.BuildTwin._0.dto.site.SiteResponse;
import com.example.BuildTwin._0.dto.site.UpdateSiteRequest;
import com.example.BuildTwin._0.dto.site.UpdateSiteStatusRequest;
import com.example.BuildTwin._0.service.SiteService;
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
@Tag(name = "2. Project & Site Master Setup", description = "Physical Construction Sites, Zones, Towers, Lat/Long Coordinates & Site Engineering Allocations")
@SecurityRequirement(name = "BearerAuth")
public class SiteController {

    private final SiteService siteService;

    @PostMapping("/api/v1/projects/{projectId}/sites")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create site / tower under project",
            description = "Registers a new physical construction site, building tower, or work zone under an existing project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Site created successfully",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Site code already exists under this project")
    })
    public ResponseEntity<ApiResponse<SiteResponse>> createSite(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSiteRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        SiteResponse created = siteService.createSite(projectId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Site created successfully under project"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/projects/{projectId}/sites")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all sites of a project",
            description = "Retrieves all physical sites, towers, and zones associated with a given construction project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sites retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSitesByProject(@PathVariable Long projectId) {
        List<SiteResponse> sites = siteService.getSitesByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(sites, "Sites retrieved successfully for project"));
    }

    @GetMapping("/api/v1/projects/{projectId}/sites/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List sites of a project with pagination",
            description = "Paginated list of construction sites under a project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated sites retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<SiteResponse>>> getSitesByProjectPaginated(
            @PathVariable Long projectId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        PageResponse<SiteResponse> sites = siteService.getSitesByProjectIdPaginated(projectId, page, size);
        return ResponseEntity.ok(ApiResponse.success(sites, "Paginated sites retrieved successfully for project"));
    }

    @GetMapping("/api/v1/sites/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get site details by ID",
            description = "Fetches complete information of a specific construction site or tower by its ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Site details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteById(@PathVariable Long id) {
        SiteResponse site = siteService.getSiteById(id);
        return ResponseEntity.ok(ApiResponse.success(site, "Site details retrieved successfully"));
    }

    @PutMapping("/api/v1/sites/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Update site details",
            description = "Updates site code, name, category, GPS coordinates, built-up area, and incharge."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Site updated successfully",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Site code duplicate in this project")
    })
    public ResponseEntity<ApiResponse<SiteResponse>> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSiteRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        SiteResponse updated = siteService.updateSite(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Site updated successfully"));
    }

    @PatchMapping("/api/v1/sites/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update site status",
            description = "Changes operational status of the site (ACTIVE, INACTIVE, COMPLETED)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Site status updated successfully",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<ApiResponse<SiteResponse>> updateSiteStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSiteStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        SiteResponse updated = siteService.updateSiteStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Site status updated successfully"));
    }

    @DeleteMapping("/api/v1/sites/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete site",
            description = "Removes a construction site record from the system."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        siteService.deleteSite(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Site deleted successfully"));
    }
}
