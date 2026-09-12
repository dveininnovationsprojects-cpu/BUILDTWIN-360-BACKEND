package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.*;
import com.example.BuildTwin._0.service.WbsActivityService;
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
@Tag(name = "3. Work Package & WBS Module", description = "Work Breakdown Structure (WBS), Activity Sequencing, Quantity Tracking & Progress Milestones")
@SecurityRequirement(name = "BearerAuth")
public class
WbsActivityController {

    private final WbsActivityService wbsActivityService;

    @PostMapping("/api/v1/work-packages/{workPackageId}/activities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Create WBS Activity under Work Package",
            description = "Creates an actionable construction task/activity (e.g., Column Starter, Raft Pouring, Conduiting) with planned quantity, UOM, and optional location hierarchy."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "WBS Activity created successfully",
                    content = @Content(schema = @Schema(implementation = WbsActivityResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid hierarchy linkage"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Work Package or Location entity not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Activity code already exists in this work package")
    })
    public ResponseEntity<ApiResponse<WbsActivityResponse>> createActivity(
            @PathVariable Long workPackageId,
            @Valid @RequestBody CreateWbsActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse created = wbsActivityService.createActivity(workPackageId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "WBS Activity created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/work-packages/{workPackageId}/activities")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all activities of a Work Package",
            description = "Retrieves all WBS activities configured under a specific work package, ordered by sequence."
    )
    public ResponseEntity<ApiResponse<List<WbsActivityResponse>>> getActivitiesByWorkPackage(
            @PathVariable Long workPackageId) {
        List<WbsActivityResponse> activities = wbsActivityService.getActivitiesByWorkPackageId(workPackageId);
        return ResponseEntity.ok(ApiResponse.success(activities, "WBS activities retrieved successfully"));
    }

    @GetMapping("/api/v1/work-packages/{workPackageId}/activities/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List activities of a Work Package with pagination",
            description = "Paginated list of WBS activities under a work package with optional status filter."
    )
    public ResponseEntity<ApiResponse<PageResponse<WbsActivityResponse>>> getActivitiesByWorkPackagePaginated(
            @PathVariable Long workPackageId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<WbsActivityResponse> activities = wbsActivityService.getActivitiesByWorkPackageIdPaginated(
                workPackageId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(activities, "Paginated WBS activities retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/wbs-activities")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all activities of a Project",
            description = "Retrieves all WBS activities across all work packages in a project, with filtering by status or discipline and pagination."
    )
    public ResponseEntity<ApiResponse<PageResponse<WbsActivityResponse>>> getActivitiesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String discipline,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<WbsActivityResponse> activities = wbsActivityService.getActivitiesByProjectId(
                projectId, status, discipline, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(activities, "Project activities retrieved successfully"));
    }

    @GetMapping("/api/v1/wbs-activities/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get WBS Activity details by ID",
            description = "Retrieves comprehensive information of a specific WBS activity."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> getActivityById(@PathVariable Long id) {
        WbsActivityResponse activity = wbsActivityService.getActivityById(id);
        return ResponseEntity.ok(ApiResponse.success(activity, "WBS activity retrieved successfully"));
    }

    @PutMapping("/api/v1/wbs-activities/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update WBS Activity details",
            description = "Updates activity scope, quantities, planned dates, trade contractor, or sequence order."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWbsActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.updateActivity(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "WBS activity updated successfully"));
    }

    @PatchMapping("/api/v1/wbs-activities/{id}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Update WBS Activity progress and logged quantity",
            description = "Updates completed quantity and automatically recalculates progress percentage. Auto-transitions status to IN_PROGRESS or COMPLETED."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> updateActivityProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWbsActivityProgressRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.updateActivityProgress(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity progress updated successfully"));
    }

    @PatchMapping("/api/v1/wbs-activities/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Update WBS Activity status",
            description = "Updates activity status (PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD)."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> updateActivityStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWbsActivityStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.updateActivityStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity status updated successfully"));
    }

    @DeleteMapping("/api/v1/wbs-activities/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete WBS Activity",
            description = "Permanently deletes a WBS Activity from the work package."
    )
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        wbsActivityService.deleteActivity(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "WBS activity deleted successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/wbs-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project WBS Summary & Overall Progress",
            description = "Executive rollup showing total work packages, activity counts by status, total allocated budget, and overall weighted progress %."
    )
    public ResponseEntity<ApiResponse<WbsSummaryResponse>> getWbsSummary(@PathVariable Long projectId) {
        WbsSummaryResponse summary = wbsActivityService.getWbsSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Project WBS summary retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/wbs-tree")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Full Project WBS Breakdown Tree",
            description = "Returns complete hierarchical breakdown: Project -> Work Packages -> Activities with location strings, quantities, and progress percentages."
    )
    public ResponseEntity<ApiResponse<WbsTreeResponse>> getWbsTree(@PathVariable Long projectId) {
        WbsTreeResponse tree = wbsActivityService.getWbsTree(projectId);
        return ResponseEntity.ok(ApiResponse.success(tree, "Project WBS tree breakdown retrieved successfully"));
    }
}
