package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.revision.*;
import com.example.BuildTwin._0.model.ScheduleRevisionStatus;
import com.example.BuildTwin._0.service.ScheduleRevisionService;
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
@Tag(name = "6. Schedule Revision Tracking Module",
     description = "Draft Schedule Revisions, Extension of Time (EOT) Management, Activity Schedule Adjustments, Impact & Variance Analysis, and Formal Revision Sign-off")
@SecurityRequirement(name = "BearerAuth")
public class ScheduleRevisionController {

    private final ScheduleRevisionService scheduleRevisionService;

    @PostMapping("/api/v1/projects/{projectId}/schedule-revisions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Create Draft Schedule Revision",
            description = "Initiates a new schedule revision package (REV-01, REV-02, etc.) in DRAFT status. Can optionally include initial activity date adjustments, narrative reason, and requested time extensions."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Draft Schedule Revision created successfully",
                    content = @Content(schema = @Schema(implementation = ScheduleRevisionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid activity linkage"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Revision code already exists for this project")
    })
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> createDraftRevision(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateScheduleRevisionRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.createDraftRevision(projectId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(response, "Draft schedule revision created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/projects/{projectId}/schedule-revisions")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List All Schedule Revisions for Project",
            description = "Retrieves all schedule revision proposals for a project, with optional filtering by status (DRAFT, SUBMITTED, APPROVED, REJECTED, APPLIED)."
    )
    public ResponseEntity<ApiResponse<List<ScheduleRevisionResponse>>> getRevisionsByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) ScheduleRevisionStatus status) {
        List<ScheduleRevisionResponse> list = scheduleRevisionService.getRevisionsByProject(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(list, "Project schedule revisions retrieved successfully"));
    }

    @GetMapping("/api/v1/schedule-revisions/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Schedule Revision Details by ID",
            description = "Retrieves comprehensive details of a specific schedule revision package, including all adjusted activity items, dates, and delays."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> getRevisionById(@PathVariable Long id) {
        ScheduleRevisionResponse response = scheduleRevisionService.getRevisionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule revision details retrieved successfully"));
    }

    @PutMapping("/api/v1/schedule-revisions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update Draft Schedule Revision Metadata",
            description = "Modifies the name, narrative reason, projected end date, or time extension parameters of a DRAFT schedule revision."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> updateRevision(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRevisionRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.updateRevision(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule revision updated successfully"));
    }

    @PostMapping("/api/v1/schedule-revisions/{id}/items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Add or Update Activity Change in Revision",
            description = "Adds a revised schedule adjustment for an individual WBS activity to the draft revision package. Automatically calculates variance days and revised duration."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> addOrUpdateRevisionItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateScheduleRevisionItemRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.addOrUpdateRevisionItem(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Activity schedule change added to revision successfully"));
    }

    @DeleteMapping("/api/v1/schedule-revisions/{id}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Remove Activity Change from Revision",
            description = "Deletes an individual activity schedule adjustment item from a DRAFT revision package."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> removeRevisionItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.removeRevisionItem(id, itemId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Activity removed from schedule revision successfully"));
    }

    @PatchMapping("/api/v1/schedule-revisions/{id}/submit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Submit Schedule Revision for Approval",
            description = "Submits a DRAFT schedule revision to management (Director / Project Manager) for review and formal approval."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> submitRevision(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.submitRevision(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule revision submitted for approval successfully"));
    }

    @PatchMapping("/api/v1/schedule-revisions/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Approve Schedule Revision",
            description = "Formally signs off and approves the schedule revision. If applyToActivities is true (default), automatically writes the revised planned dates directly into the project's active WBS activities."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> approveRevision(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean applyToActivities,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.approveRevision(id, applyToActivities, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule revision approved and applied successfully"));
    }

    @PatchMapping("/api/v1/schedule-revisions/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Reject Schedule Revision",
            description = "Rejects a submitted schedule revision with a mandatory explanation reason."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionResponse>> rejectRevision(
            @PathVariable Long id,
            @Valid @RequestBody RejectScheduleRevisionRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ScheduleRevisionResponse response = scheduleRevisionService.rejectRevision(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule revision rejected successfully"));
    }

    @GetMapping("/api/v1/schedule-revisions/{id}/impact-analysis")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Schedule Revision Impact Analysis",
            description = "Analyzes the overall impact of the revision package, including max activity delay, net time extension requested, new projected project end date, and cost impact."
    )
    public ResponseEntity<ApiResponse<ScheduleRevisionImpactAnalysisResponse>> getImpactAnalysis(@PathVariable Long id) {
        ScheduleRevisionImpactAnalysisResponse response = scheduleRevisionService.getImpactAnalysis(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule revision impact analysis generated successfully"));
    }

    @DeleteMapping("/api/v1/schedule-revisions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete Draft Schedule Revision",
            description = "Permanently deletes a DRAFT or REJECTED schedule revision package."
    )
    public ResponseEntity<ApiResponse<Void>> deleteRevision(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        scheduleRevisionService.deleteRevision(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule revision deleted successfully"));
    }
}
