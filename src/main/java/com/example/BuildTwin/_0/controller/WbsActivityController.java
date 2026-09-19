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
@Tag(name = "3. Work Package & WBS Module", description = "Work Breakdown Structure (WBS), Activity Management, Quantity Tracking, Assignments & Milestones")
@SecurityRequirement(name = "BearerAuth")
public class WbsActivityController {

    private final WbsActivityService wbsActivityService;

    @PostMapping({"/api/v1/work-packages/{workPackageId}/activities", "/api/v1/work-packages/{workPackageId}/wbs-activities"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Create WBS Activity under Work Package",
            description = "Creates an actionable construction task/activity. Supports optional parentId to create a child sub-task under an existing activity."
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

    @PostMapping({"/api/v1/wbs-activities/{parentId}/children", "/api/v1/activities/{parentId}/children"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Create Child WBS Activity under Parent Task",
            description = "Directly adds a sub-task / child activity under a parent WBS task. Inherits trade discipline and location if unspecified."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> createChildActivity(
            @PathVariable Long parentId,
            @Valid @RequestBody CreateWbsActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse created = wbsActivityService.createChildActivity(parentId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Child WBS Activity created successfully under parent"), HttpStatus.CREATED);
    }

    @PatchMapping({"/api/v1/wbs-activities/{id}/reparent", "/api/v1/activities/{id}/reparent"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Reparent WBS Activity",
            description = "Moves an activity under a new parent or to the root level. Performs cycle prevention checks and recalculates WBS hierarchy paths & levels."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> reparentActivity(
            @PathVariable Long id,
            @Valid @RequestBody ReparentWbsActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse reparented = wbsActivityService.reparentActivity(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(reparented, "WBS Activity reparented successfully"));
    }

    @GetMapping({"/api/v1/wbs-activities/{parentId}/children", "/api/v1/activities/{parentId}/children"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Direct Children of WBS Activity",
            description = "Retrieves all immediate subordinate activities under a specific parent WBS activity, ordered by sequence."
    )
    public ResponseEntity<ApiResponse<List<WbsActivityResponse>>> getChildActivities(
            @PathVariable Long parentId) {
        List<WbsActivityResponse> children = wbsActivityService.getChildActivities(parentId);
        return ResponseEntity.ok(ApiResponse.success(children, "Child WBS activities retrieved successfully"));
    }

    @GetMapping({"/api/v1/wbs-activities/{id}/tree", "/api/v1/activities/{id}/tree"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Sub-tree of a WBS Activity",
            description = "Retrieves an activity and its complete recursive child tree down to leaf activities."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> getActivityTree(@PathVariable Long id) {
        WbsActivityResponse tree = wbsActivityService.getActivityTree(id);
        return ResponseEntity.ok(ApiResponse.success(tree, "WBS Activity sub-tree retrieved successfully"));
    }

    @GetMapping({"/api/v1/activities", "/api/v1/wbs-activities"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Search and filter Activities across project, trade & locations",
            description = "Multi-criteria activity search filtering by project, work package, location (site, building, floor, zone), trade, status, contractor, or incharge with pagination."
    )
    public ResponseEntity<ApiResponse<PageResponse<WbsActivityResponse>>> searchActivities(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long workPackageId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contractor,
            @RequestParam(required = false) Long inchargeUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<WbsActivityResponse> activities = wbsActivityService.searchActivities(
                projectId, workPackageId, siteId, buildingId, floorId, zoneId,
                discipline, status, contractor, inchargeUserId,
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(activities, "Activities retrieved successfully"));
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

    @GetMapping({"/api/v1/wbs-activities/{id}", "/api/v1/activities/{id}"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get WBS Activity details by ID",
            description = "Retrieves comprehensive information of a specific WBS activity."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> getActivityById(@PathVariable Long id) {
        WbsActivityResponse activity = wbsActivityService.getActivityById(id);
        return ResponseEntity.ok(ApiResponse.success(activity, "WBS activity retrieved successfully"));
    }

    @PutMapping({"/api/v1/wbs-activities/{id}", "/api/v1/activities/{id}"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update WBS Activity details (Full Update)",
            description = "Updates activity scope, quantities, planned dates, trade contractor, or sequence order."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWbsActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.updateActivity(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "WBS activity updated successfully"));
    }

    @PatchMapping({"/api/v1/wbs-activities/{id}/progress", "/api/v1/activities/{id}/progress"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Update Activity progress and logged quantity (with auto rollup)",
            description = "Updates completed quantity and automatically recalculates progress percentage. Auto-rolls up progress to parent activities."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> updateActivityProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWbsActivityProgressRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.updateActivityProgress(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity progress updated successfully"));
    }

    @PatchMapping({"/api/v1/wbs-activities/{id}/status", "/api/v1/activities/{id}/status"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Update Activity execution status",
            description = "Updates activity status (PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD) and rolls up to parent."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> updateActivityStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWbsActivityStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.updateActivityStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity status updated successfully"));
    }

    @PatchMapping({"/api/v1/wbs-activities/{id}/assign", "/api/v1/activities/{id}/assign"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Assign Contractor and Incharge Engineer to Activity",
            description = "Quickly assigns or changes the trade contractor and site incharge engineer for this activity."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> assignActivity(
            @PathVariable Long id,
            @Valid @RequestBody AssignActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.assignActivity(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity assigned successfully"));
    }

    @PatchMapping({"/api/v1/wbs-activities/{id}/schedule", "/api/v1/activities/{id}/schedule"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update Activity Schedule & Milestone Dates",
            description = "Reschedules or logs planned and actual start/completion dates for this activity."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> scheduleActivity(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.scheduleActivity(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity schedule updated successfully"));
    }

    @PatchMapping({"/api/v1/wbs-activities/{id}/location", "/api/v1/activities/{id}/location"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Relocate Activity in Physical Construction Hierarchy",
            description = "Updates physical location linkage (Site, Building, Floor, Zone) for this activity."
    )
    public ResponseEntity<ApiResponse<WbsActivityResponse>> relocateActivity(
            @PathVariable Long id,
            @Valid @RequestBody RelocateActivityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WbsActivityResponse updated = wbsActivityService.relocateActivity(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity location updated successfully"));
    }

    @DeleteMapping({"/api/v1/wbs-activities/{id}", "/api/v1/activities/{id}"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete Activity",
            description = "Permanently deletes an activity. If it has child activities, cascades and removes all sub-tasks cleanly."
    )
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        wbsActivityService.deleteActivity(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Activity deleted successfully"));
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
            description = "Returns complete hierarchical breakdown: Project -> Work Packages -> Parent Activities -> Nested Sub-Activities with quantities and progress percentages."
    )
    public ResponseEntity<ApiResponse<WbsTreeResponse>> getWbsTree(@PathVariable Long projectId) {
        WbsTreeResponse tree = wbsActivityService.getWbsTree(projectId);
        return ResponseEntity.ok(ApiResponse.success(tree, "Project WBS tree breakdown retrieved successfully"));
    }

    @GetMapping("/api/v1/work-packages/{workPackageId}/wbs-tree")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Work Package WBS Tree",
            description = "Returns hierarchical breakdown of a single work package with nested parent-child activity nodes."
    )
    public ResponseEntity<ApiResponse<WbsTreeResponse.WorkPackageNode>> getWorkPackageWbsTree(@PathVariable Long workPackageId) {
        WbsTreeResponse.WorkPackageNode node = wbsActivityService.getWorkPackageWbsTree(workPackageId);
        return ResponseEntity.ok(ApiResponse.success(node, "Work package WBS tree retrieved successfully"));
    }
}
