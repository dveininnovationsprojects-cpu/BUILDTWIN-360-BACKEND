package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.dependency.*;
import com.example.BuildTwin._0.service.ActivityDependencyService;
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
@Tag(name = "4. Activity Dependency & CPM Network Module",
     description = "Activity Precedence Relationships (FS, SS, FF, SF), Lag Days, Circular Dependency Prevention, Readiness Tracking & Project CPM Network Diagrams")
@SecurityRequirement(name = "BearerAuth")
public class
ActivityDependencyController {

    private final ActivityDependencyService activityDependencyService;

    @PostMapping("/api/v1/activity-dependencies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Create Activity Precedence Dependency",
            description = "Links a predecessor activity to a successor activity with dependency type (FS, SS, FF, SF) and lead/lag days. Automatically validates against circular loops (Directed Cycles) and duplicates."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Activity dependency created successfully",
                    content = @Content(schema = @Schema(implementation = ActivityDependencyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error, self-dependency, hierarchy conflict, or circular loop detected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Predecessor or Successor activity not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Precedence link already exists between these activities")
    })
    public ResponseEntity<ApiResponse<ActivityDependencyResponse>> createDependency(
            @Valid @RequestBody CreateActivityDependencyRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ActivityDependencyResponse created = activityDependencyService.createDependency(request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Activity precedence dependency created successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/activity-dependencies/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Bulk Create Activity Dependencies",
            description = "Creates multiple activity dependency relationships in batch. Useful during initial project scheduling setup."
    )
    public ResponseEntity<ApiResponse<List<ActivityDependencyResponse>>> createBatchDependencies(
            @Valid @RequestBody BatchCreateActivityDependencyRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        List<ActivityDependencyResponse> createdList = activityDependencyService.createBatchDependencies(request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(createdList, "Batch activity dependencies created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/activity-dependencies/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Activity Dependency by ID",
            description = "Retrieves details of a specific activity precedence relationship, including schedule conflict diagnostics."
    )
    public ResponseEntity<ApiResponse<ActivityDependencyResponse>> getDependencyById(
            @PathVariable Long id) {
        ActivityDependencyResponse dependency = activityDependencyService.getDependencyById(id);
        return ResponseEntity.ok(ApiResponse.success(dependency, "Activity dependency retrieved successfully"));
    }

    @PutMapping("/api/v1/activity-dependencies/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update Activity Dependency",
            description = "Modifies dependency type (FS, SS, FF, SF), lead/lag days, or engineering remarks."
    )
    public ResponseEntity<ApiResponse<ActivityDependencyResponse>> updateDependency(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivityDependencyRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ActivityDependencyResponse updated = activityDependencyService.updateDependency(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Activity dependency updated successfully"));
    }

    @DeleteMapping("/api/v1/activity-dependencies/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete Activity Dependency Link",
            description = "Removes the precedence constraint between two activities without deleting the activities themselves."
    )
    public ResponseEntity<ApiResponse<Void>> deleteDependency(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        activityDependencyService.deleteDependency(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Activity dependency removed successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/activity-dependencies")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all Activity Dependencies in a Project",
            description = "Retrieves all precedence links configured across the entire project schedule."
    )
    public ResponseEntity<ApiResponse<List<ActivityDependencyResponse>>> getDependenciesByProject(
            @PathVariable Long projectId) {
        List<ActivityDependencyResponse> dependencies = activityDependencyService.getDependenciesByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(dependencies, "Project activity dependencies retrieved successfully"));
    }

    @GetMapping("/api/v1/work-packages/{workPackageId}/activity-dependencies")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Activity Dependencies for a Work Package",
            description = "Retrieves all precedence links where either predecessor or successor belongs to this work package."
    )
    public ResponseEntity<ApiResponse<List<ActivityDependencyResponse>>> getDependenciesByWorkPackage(
            @PathVariable Long workPackageId) {
        List<ActivityDependencyResponse> dependencies = activityDependencyService.getDependenciesByWorkPackageId(workPackageId);
        return ResponseEntity.ok(ApiResponse.success(dependencies, "Work package activity dependencies retrieved successfully"));
    }

    @GetMapping("/api/v1/activities/{activityId}/predecessors")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Predecessors of an Activity (Incoming Dependencies)",
            description = "Retrieves all activities that must be executed or started prior to this activity."
    )
    public ResponseEntity<ApiResponse<List<ActivityDependencyResponse>>> getPredecessors(
            @PathVariable Long activityId) {
        List<ActivityDependencyResponse> predecessors = activityDependencyService.getPredecessors(activityId);
        return ResponseEntity.ok(ApiResponse.success(predecessors, "Activity predecessors retrieved successfully"));
    }

    @GetMapping("/api/v1/activities/{activityId}/successors")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Successors of an Activity (Outgoing Dependencies)",
            description = "Retrieves all activities that depend on this activity and will be unlocked once this activity completes."
    )
    public ResponseEntity<ApiResponse<List<ActivityDependencyResponse>>> getSuccessors(
            @PathVariable Long activityId) {
        List<ActivityDependencyResponse> successors = activityDependencyService.getSuccessors(activityId);
        return ResponseEntity.ok(ApiResponse.success(successors, "Activity successors retrieved successfully"));
    }

    @GetMapping("/api/v1/activities/{activityId}/dependency-chain")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Activity Dependency Chain & Execution Readiness",
            description = "Diagnostic overview of incoming predecessors, outgoing successors, readiness flag (isReadyToStart), and blocking predecessor activities."
    )
    public ResponseEntity<ApiResponse<ActivityDependencyChainResponse>> getDependencyChain(
            @PathVariable Long activityId) {
        ActivityDependencyChainResponse chain = activityDependencyService.getDependencyChain(activityId);
        return ResponseEntity.ok(ApiResponse.success(chain, "Activity dependency chain and readiness retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/activity-dependencies/network")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project CPM Dependency Network Graph",
            description = "Returns all activity nodes and precedence edges for the project, formatted for Gantt chart, CPM Critical Path analysis, and interactive visual graph libraries (Mermaid, Vis.js, React Flow)."
    )
    public ResponseEntity<ApiResponse<ProjectDependencyNetworkResponse>> getProjectDependencyNetwork(
            @PathVariable Long projectId) {
        ProjectDependencyNetworkResponse network = activityDependencyService.getProjectDependencyNetwork(projectId);
        return ResponseEntity.ok(ApiResponse.success(network, "Project dependency network retrieved successfully"));
    }
    @DeleteMapping("/api/v1/activities/{activityId}/dependencies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete All Dependencies for an Activity",
            description = "Clears all incoming and outgoing precedence links associated with the specified activity."
    )

    public ResponseEntity<ApiResponse<Void>> deleteDependenciesByActivity(
            @PathVariable Long activityId,
            @Parameter(hidden = true) Authentication authentication) {
        activityDependencyService.deleteDependenciesByActivityId(activityId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "All dependencies for activity removed successfully"));
    }
}
