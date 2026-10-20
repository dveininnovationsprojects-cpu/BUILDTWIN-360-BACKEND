package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.baseline.*;
import com.example.BuildTwin._0.service.BaselineScheduleService;
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
@Tag(name = "5. Baseline Schedule & Progress Tracking Module",
     description = "Master Baseline Schedules (Plan of Record), Schedule Freeze, Daily Site Progress Logging, EVM Variance Analysis (SV, Slippage) & Project Health")
@SecurityRequirement(name = "BearerAuth")
public class BaselineScheduleController {

    private final BaselineScheduleService baselineScheduleService;

    @PostMapping("/api/v1/projects/{projectId}/baselines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create & Freeze Project Baseline Schedule",
            description = "Freezes the current project WBS, activity dates, quantities, and budgets into an immutable master baseline version for contract benchmarking and variance tracking."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Baseline Schedule created and frozen successfully",
                    content = @Content(schema = @Schema(implementation = ProjectBaselineResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or no activities in project"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Baseline name already exists for this project")
    })
    public ResponseEntity<ApiResponse<ProjectBaselineResponse>> createBaseline(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectBaselineRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectBaselineResponse created = baselineScheduleService.createBaseline(projectId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Project baseline schedule created and frozen successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/projects/{projectId}/baselines")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List All Baselines for a Project",
            description = "Retrieves all frozen baseline versions for a project ordered by newest version first."
    )
    public ResponseEntity<ApiResponse<List<ProjectBaselineResponse>>> getBaselinesByProject(
            @PathVariable Long projectId) {
        List<ProjectBaselineResponse> baselines = baselineScheduleService.getBaselinesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(baselines, "Project baselines retrieved successfully"));
    }

    @GetMapping("/api/v1/baselines/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Baseline Details by ID",
            description = "Retrieves details of a specific frozen baseline version including version number, activity count, and approval status."
    )
    public ResponseEntity<ApiResponse<ProjectBaselineResponse>> getBaselineById(
            @PathVariable Long id) {
        ProjectBaselineResponse baseline = baselineScheduleService.getBaselineById(id);
        return ResponseEntity.ok(ApiResponse.success(baseline, "Baseline details retrieved successfully"));
    }

    @GetMapping("/api/v1/baselines/{id}/snapshots")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get All Activity Snapshots in a Baseline",
            description = "Retrieves the frozen planned dates, quantities, weightages, and contractors for all activities locked inside this baseline version."
    )
    public ResponseEntity<ApiResponse<List<BaselineActivitySnapshotResponse>>> getBaselineSnapshots(
            @PathVariable Long id) {
        List<BaselineActivitySnapshotResponse> snapshots = baselineScheduleService.getBaselineSnapshots(id);
        return ResponseEntity.ok(ApiResponse.success(snapshots, "Baseline activity snapshots retrieved successfully"));
    }

    @PatchMapping("/api/v1/projects/{projectId}/baselines/{baselineId}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Set Active Comparison Baseline",
            description = "Switches the active reference baseline used for computing project schedule variance (SV) and slippage."
    )
    public ResponseEntity<ApiResponse<ProjectBaselineResponse>> setActiveBaseline(
            @PathVariable Long projectId,
            @PathVariable Long baselineId,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectBaselineResponse updated = baselineScheduleService.setActiveBaseline(projectId, baselineId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Active baseline set successfully"));
    }

    @PatchMapping("/api/v1/baselines/{baselineId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Approve Baseline Schedule",
            description = "Formally signs off and approves a baseline schedule as the contractually binding master program."
    )
    public ResponseEntity<ApiResponse<ProjectBaselineResponse>> approveBaseline(
            @PathVariable Long baselineId,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectBaselineResponse approved = baselineScheduleService.approveBaseline(baselineId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(approved, "Baseline approved successfully"));
    }

    @DeleteMapping("/api/v1/baselines/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete Baseline Schedule",
            description = "Permanently deletes a baseline schedule and its associated activity snapshots."
    )
    public ResponseEntity<ApiResponse<Void>> deleteBaseline(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        baselineScheduleService.deleteBaseline(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Baseline deleted successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/schedule-variance-report")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Schedule Variance & Health Report",
            description = "Comprehensive report analyzing every activity's current dates vs active baseline dates. Calculates Schedule Variance (SV in days), task health (ON_TRACK, SLIGHT_DELAY, CRITICAL_DELAY, AHEAD), and overall project health score."
    )
    public ResponseEntity<ApiResponse<ProjectScheduleVarianceReport>> getProjectScheduleVarianceReport(
            @PathVariable Long projectId,
            @Parameter(description = "Optional specific baseline ID to compare against. If omitted, uses active baseline.")
            @RequestParam(required = false) Long baselineId) {
        ProjectScheduleVarianceReport report = baselineScheduleService.getProjectScheduleVarianceReport(projectId, baselineId);
        return ResponseEntity.ok(ApiResponse.success(report, "Project schedule variance report generated successfully"));
    }

    @GetMapping("/api/v1/activities/{activityId}/schedule-variance")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Single Activity Schedule Variance",
            description = "Calculates start variance and finish variance (days) for a single activity against the baseline."
    )
    public ResponseEntity<ApiResponse<ActivityScheduleVarianceResponse>> getActivityScheduleVariance(
            @PathVariable Long activityId,
            @RequestParam(required = false) Long baselineId) {
        ActivityScheduleVarianceResponse variance = baselineScheduleService.getActivityScheduleVariance(activityId, baselineId);
        return ResponseEntity.ok(ApiResponse.success(variance, "Activity schedule variance retrieved successfully"));
    }

    @PostMapping("/api/v1/activities/{activityId}/daily-progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Log Daily Site Progress & Production Output",
            description = "Site engineers log physical quantity completed today along with manpower count, machinery utilized, weather, and site hindrance notes. Automatically advances cumulative quantity, progress %, and cascades rollups."
    )
    public ResponseEntity<ApiResponse<ActivityProgressLogResponse>> logDailyProgress(
            @PathVariable Long activityId,
            @Valid @RequestBody LogDailyProgressRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ActivityProgressLogResponse response = baselineScheduleService.logDailyProgress(activityId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(response, "Daily site progress logged successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/activities/{activityId}/daily-progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Daily Progress History for Activity",
            description = "Retrieves the chronological journal of all daily progress logs recorded on site for this activity."
    )
    public ResponseEntity<ApiResponse<List<ActivityProgressLogResponse>>> getActivityProgressLogs(
            @PathVariable Long activityId) {
        List<ActivityProgressLogResponse> logs = baselineScheduleService.getActivityProgressLogs(activityId);
        return ResponseEntity.ok(ApiResponse.success(logs, "Activity daily progress logs retrieved successfully"));
    }
}
