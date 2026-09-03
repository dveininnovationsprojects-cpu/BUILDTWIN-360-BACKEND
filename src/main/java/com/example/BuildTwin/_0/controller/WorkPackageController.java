package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.CreateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageStatusRequest;
import com.example.BuildTwin._0.dto.wbs.WorkPackageResponse;
import com.example.BuildTwin._0.service.WorkPackageService;
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
@RequiredArgsConstructor
@Tag(name = "3. Work Package & WBS Module", description = "Work Breakdown Structure (WBS), Trades (Civil, RCC, MEP, Electrical), Packages, Budgets & Contractor Allocations")
@SecurityRequirement(name = "BearerAuth")
public class WorkPackageController {

    private final WorkPackageService workPackageService;

    @PostMapping("/api/v1/projects/{projectId}/work-packages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create work package under project",
            description = "Creates a construction work package (e.g. Substructure, RCC Framing, MEP, Finishing) under a project with trade discipline, budget, and contractor."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Work package created successfully",
                    content = @Content(schema = @Schema(implementation = WorkPackageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or Site not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Work package code already exists in this project")
    })
    public ResponseEntity<ApiResponse<WorkPackageResponse>> createWorkPackage(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateWorkPackageRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WorkPackageResponse created = workPackageService.createWorkPackage(projectId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Work package created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/projects/{projectId}/work-packages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List work packages under project",
            description = "Retrieves work packages with filtering by status and discipline (CIVIL, MEP, etc.) and pagination."
    )
    public ResponseEntity<ApiResponse<PageResponse<WorkPackageResponse>>> getWorkPackages(
            @PathVariable Long projectId,
            @Parameter(description = "Filter by status: PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by discipline: CIVIL, STRUCTURAL, MEP, ELECTRICAL, etc.") @RequestParam(required = false) String discipline,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<WorkPackageResponse> packages = workPackageService.getWorkPackagesByProjectId(
                projectId, status, discipline, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(packages, "Work packages retrieved successfully"));
    }

    @GetMapping("/api/v1/work-packages/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get work package details by ID",
            description = "Retrieves detailed work package info including contractor and incharge engineer."
    )
    public ResponseEntity<ApiResponse<WorkPackageResponse>> getWorkPackageById(@PathVariable Long id) {
        WorkPackageResponse wp = workPackageService.getWorkPackageById(id);
        return ResponseEntity.ok(ApiResponse.success(wp, "Work package retrieved successfully"));
    }

    @PutMapping("/api/v1/work-packages/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Update work package",
            description = "Updates work package scope, contractor, dates, or budget amount."
    )
    public ResponseEntity<ApiResponse<WorkPackageResponse>> updateWorkPackage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkPackageRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WorkPackageResponse updated = workPackageService.updateWorkPackage(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Work package updated successfully"));
    }

    @PatchMapping("/api/v1/work-packages/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update work package status",
            description = "Updates work package execution status (PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED)."
    )
    public ResponseEntity<ApiResponse<WorkPackageResponse>> updateWorkPackageStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkPackageStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        WorkPackageResponse updated = workPackageService.updateWorkPackageStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Work package status updated successfully"));
    }

    @DeleteMapping("/api/v1/work-packages/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete work package",
            description = "Permanently deletes a work package."
    )
    public ResponseEntity<ApiResponse<Void>> deleteWorkPackage(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        workPackageService.deleteWorkPackage(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Work package deleted successfully"));
    }
}
