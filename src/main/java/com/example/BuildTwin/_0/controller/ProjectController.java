package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.project.*;
import com.example.BuildTwin._0.service.ProjectService;
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

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "2. Project & Site Master Setup", description = "Project Master Registry, Construction Sites, Geo-Locations, Budgets & Portfolio Metrics")
@SecurityRequirement(name = "BearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Create construction project",
            description = "Creates a new construction project master record with code, client details, budget, and timeline."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or DIRECTOR role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Project code already exists")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectResponse created = projectService.createProject(request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Project created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List projects with search, filter, and pagination",
            description = "Retrieves projects list with optional search query (name/code/location), status filter, project type filter, and pagination."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    })
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getAllProjects(
            @Parameter(description = "Search in project name, code, or location") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status: PLANNED, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by project type: RESIDENTIAL, COMMERCIAL, INFRASTRUCTURE") @RequestParam(required = false) String projectType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<ProjectResponse> projects = projectService.getAllProjects(search, status, projectType, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects retrieved successfully"));
    }

    @GetMapping("/metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get project portfolio metrics",
            description = "Provides high-level executive counts of total projects, active projects, status breakdown, total budget, and sites."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Portfolio metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectMetricsResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ProjectMetricsResponse>> getProjectMetrics() {
        ProjectMetricsResponse metrics = projectService.getProjectMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics, "Project metrics retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get project details by ID",
            description = "Retrieves comprehensive project information including all registered sites and assigned team members."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectById(@PathVariable Long id) {
        ProjectDetailResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project, "Project details retrieved successfully"));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get project details by Code",
            description = "Finds project by its unique alphanumeric code (e.g. PADUR-AG-01)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectByCode(@PathVariable String code) {
        ProjectDetailResponse project = projectService.getProjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(project, "Project details retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Update project",
            description = "Updates project master details such as budget, client, scope, timelines, or manager."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Project code in use by another project")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectResponse updated = projectService.updateProject(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Project updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Update project status",
            description = "Updates project lifecycle state (PLANNED, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project status updated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectResponse updated = projectService.updateProjectStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Project status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete project",
            description = "Permanently deletes a project and cascades deletion to child sites and team role allocations."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        projectService.deleteProject(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }
}
