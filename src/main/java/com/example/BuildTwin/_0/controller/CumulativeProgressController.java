package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.cumulative.*;
import com.example.BuildTwin._0.service.CumulativeProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import com.example.BuildTwin._0.domain.projects.model.Project;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "9. Calculate Cumulative Progress Engine",
        description = "Enterprise Cumulative Progress Engine: Multi-Model Weighting (Weightage Factor, Budget/Cost, Duration, Equal), EVM Variance (BCWP/BCWS, SPI), Hierarchical Rollup, Point-in-Time As-of-Date Reconstruction, S-Curve Generation, and Data Integrity Reconciliation."
)
@SecurityRequirement(name = "BearerAuth")
public class CumulativeProgressController {

    private final CumulativeProgressService cumulativeProgressService;

    // =========================================================================
    // 1. PROJECT CUMULATIVE PROGRESS DASHBOARD
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/cumulative-progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Cumulative Progress Dashboard",
            description = "Calculates overall project cumulative progress across multiple weighting models (Weightage factor, Budget/Cost, Duration, Equal), EVM metrics (Earned Value, Planned Value, SPI), physical quantity totals, status distribution, and multi-dimensional rollups by discipline, work package, and location."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cumulative progress calculated successfully",
                    content = @Content(schema = @Schema(implementation = CumulativeProgressResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<CumulativeProgressResponse>> getProjectCumulativeProgress(
            @PathVariable Long projectId,
            @Parameter(description = "Weighting methodology: WEIGHTAGE_FACTOR, BUDGET_WEIGHTED, DURATION_WEIGHTED, EQUAL_WEIGHTED")
            @RequestParam(required = false, defaultValue = "WEIGHTAGE_FACTOR") WeightingModel weightingModel,
            @Parameter(description = "Optional filter by discipline (e.g., CIVIL, STRUCTURAL, MEP, FINISHING)")
            @RequestParam(required = false) String discipline,
            @Parameter(description = "Optional filter by specific work package ID")
            @RequestParam(required = false) Long workPackageId) {

        CumulativeProgressResponse response = cumulativeProgressService.getProjectCumulativeProgress(
                projectId, weightingModel, discipline, workPackageId);
        return ResponseEntity.ok(ApiResponse.success(response, "Project cumulative progress calculated successfully"));
    }

    // =========================================================================
    // 2. WORK PACKAGE CUMULATIVE PROGRESS
    // =========================================================================

    @GetMapping("/api/v1/work-packages/{workPackageId}/cumulative-progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Work Package Cumulative Progress",
            description = "Calculates cumulative progress, EVM values, weightage rollups, and activity-level contributions for a specific work package."
    )
    public ResponseEntity<ApiResponse<WorkPackageCumulativeProgressResponse>> getWorkPackageCumulativeProgress(
            @PathVariable Long workPackageId,
            @RequestParam(required = false, defaultValue = "WEIGHTAGE_FACTOR") WeightingModel weightingModel) {

        WorkPackageCumulativeProgressResponse response = cumulativeProgressService.getWorkPackageCumulativeProgress(
                workPackageId, weightingModel);
        return ResponseEntity.ok(ApiResponse.success(response, "Work package cumulative progress calculated successfully"));
    }

    // =========================================================================
    // 3. ACTIVITY CUMULATIVE PROGRESS
    // =========================================================================

    @GetMapping({"/api/v1/activities/{activityId}/cumulative-progress", "/api/v1/wbs-activities/{activityId}/cumulative-progress"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Activity Cumulative Progress",
            description = "Retrieves cumulative actual progress %, planned progress %, progress variance, and recursive child task rollups for a WBS activity."
    )
    public ResponseEntity<ApiResponse<ActivityCumulativeProgressResponse>> getActivityCumulativeProgress(
            @PathVariable Long activityId) {

        ActivityCumulativeProgressResponse response = cumulativeProgressService.getActivityCumulativeProgress(activityId);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity cumulative progress calculated successfully"));
    }

    // =========================================================================
    // 4. POINT-IN-TIME AS-OF-DATE ENGINE
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/cumulative-progress/as-of-date")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Calculate Historical Cumulative Progress As-of-Date",
            description = "Point-in-Time Reconstruction Engine: Recalculates exact cumulative output, completed quantities, earned value, and planned progress % across the project as of any given historical cutoff date."
    )
    public ResponseEntity<ApiResponse<AsOfDateProgressResponse>> getAsOfDateCumulativeProgress(
            @PathVariable Long projectId,
            @Parameter(description = "Historical cutoff date (yyyy-MM-dd). Defaults to today if omitted.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {

        AsOfDateProgressResponse response = cumulativeProgressService.getAsOfDateCumulativeProgress(projectId, cutoffDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Historical as-of-date cumulative progress calculated successfully for " + response.getCutoffDate()));
    }

    @GetMapping({"/api/v1/activities/{activityId}/cumulative-progress/as-of-date", "/api/v1/wbs-activities/{activityId}/cumulative-progress/as-of-date"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Calculate Activity Cumulative Progress As-of-Date",
            description = "Calculates cumulative progress completed on or before the specified historical cutoff date for an individual activity."
    )
    public ResponseEntity<ApiResponse<AsOfDateProgressResponse.ActivityAsOfDateSnapshot>> getActivityAsOfDateProgress(
            @PathVariable Long activityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {

        AsOfDateProgressResponse.ActivityAsOfDateSnapshot response = cumulativeProgressService.getActivityAsOfDateProgress(activityId, cutoffDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity as-of-date cumulative progress snapshot retrieved successfully"));
    }

    // =========================================================================
    // 5. CUMULATIVE S-CURVE GENERATION ENGINE
    // =========================================================================

        @GetMapping({
            "/api/v1/projects/{projectId}/cumulative-progress/s-curve",
            "/api/v1/projects/{projectId}/progress-timeline"
    })
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Generate Project Cumulative S-Curve Timeline",
            description = "Generates periodic time-series S-Curve progression data points comparing Planned Cumulative % (BCWS) vs Actual Cumulative % (BCWP), Planned Value, Earned Value, and SPI."
    )
    public ResponseEntity<ApiResponse<CumulativeSCurveResponse>> getProjectCumulativeSCurve(
            @PathVariable Long projectId,
            @Parameter(description = "Time-series interval: DAILY, WEEKLY (default), or MONTHLY")
            @RequestParam(required = false, defaultValue = "WEEKLY") String interval) {

        CumulativeSCurveResponse response = cumulativeProgressService.getProjectCumulativeSCurve(projectId, interval);
        return ResponseEntity.ok(ApiResponse.success(response, "Project cumulative S-curve generated successfully"));
    }

        @GetMapping({
            "/api/v1/activities/{activityId}/cumulative-progress/s-curve",
            "/api/v1/wbs-activities/{activityId}/cumulative-progress/s-curve",
            "/api/v1/activities/{activityId}/cumulative-progress/timeline"
    })
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Generate Activity Cumulative S-Curve Timeline",
            description = "Generates periodic S-curve time-series data points for an individual WBS activity."
    )
    public ResponseEntity<ApiResponse<CumulativeSCurveResponse>> getActivityCumulativeSCurve(
            @PathVariable Long activityId,
            @RequestParam(required = false, defaultValue = "WEEKLY") String interval) {

        CumulativeSCurveResponse response = cumulativeProgressService.getActivityCumulativeSCurve(activityId, interval);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity cumulative S-curve generated successfully"));
    }

    // =========================================================================
    // 6. ON-DEMAND HIERARCHICAL RECALCULATION & SYNC ENGINE
    // =========================================================================

    @PostMapping("/api/v1/projects/{projectId}/cumulative-progress/recalculate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Recalculate & Synchronize Project Cumulative Progress",
            description = "Triggers an on-demand bottom-up hierarchical recalculation of cumulative output across all leaf activities, parent tasks, work packages, and the project. Auto-syncs dates and statuses."
    )
    public ResponseEntity<ApiResponse<CumulativeProgressResponse>> recalculateProjectCumulativeProgress(
            @PathVariable Long projectId,
            @RequestBody(required = false) RecalculateProgressRequest request,
            Authentication authentication) {

        String user = authentication != null ? authentication.getName() : "ADMIN";
        CumulativeProgressResponse response = cumulativeProgressService.recalculateProjectCumulativeProgress(projectId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Project cumulative progress recalculated and synchronized successfully"));
    }

    @PostMapping("/api/v1/work-packages/{workPackageId}/cumulative-progress/recalculate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Recalculate Work Package Cumulative Progress",
            description = "Recalculates cumulative output and progress % for a work package and its activities."
    )
    public ResponseEntity<ApiResponse<WorkPackageCumulativeProgressResponse>> recalculateWorkPackageCumulativeProgress(
            @PathVariable Long workPackageId,
            @RequestBody(required = false) RecalculateProgressRequest request,
            Authentication authentication) {

        String user = authentication != null ? authentication.getName() : "PROJECT_MANAGER";
        WorkPackageCumulativeProgressResponse response = cumulativeProgressService.recalculateWorkPackageCumulativeProgress(workPackageId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Work package cumulative progress recalculated successfully"));
    }

    @PostMapping({"/api/v1/activities/{activityId}/cumulative-progress/recalculate", "/api/v1/wbs-activities/{activityId}/cumulative-progress/recalculate"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Recalculate Activity Cumulative Progress",
            description = "Recalculates cumulative quantity and progress % for an individual activity and its sub-tree."
    )
    public ResponseEntity<ApiResponse<ActivityCumulativeProgressResponse>> recalculateActivityCumulativeProgress(
            @PathVariable Long activityId,
            Authentication authentication) {

        String user = authentication != null ? authentication.getName() : "SITE_ENGINEER";
        ActivityCumulativeProgressResponse response = cumulativeProgressService.recalculateActivityCumulativeProgress(activityId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity cumulative progress recalculated successfully"));
    }

    // =========================================================================
    // 7. CUMULATIVE PROGRESS AUDIT & RECONCILIATION ENGINE
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/cumulative-progress/audit")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Audit Cumulative Progress Data Integrity",
            description = "Scans all activities across a project, compares stored cumulative quantities against verified progress history logs, checks percentage formulas, and flags discrepancies."
    )
    public ResponseEntity<ApiResponse<ProgressAuditReportResponse>> auditCumulativeProgress(
            @PathVariable Long projectId) {

        ProgressAuditReportResponse response = cumulativeProgressService.auditCumulativeProgress(projectId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cumulative progress audit completed. Status: " + response.getIntegrityStatus()));
    }

    @PostMapping("/api/v1/projects/{projectId}/cumulative-progress/reconcile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Reconcile & Auto-Heal Cumulative Progress Discrepancies",
            description = "Automatically heals all detected cumulative progress discrepancies, resets entity values to match verified progress journal logs, cascades rollups to parents, and creates audit logs."
    )
    public ResponseEntity<ApiResponse<ProgressReconciliationResponse>> reconcileCumulativeProgress(
            @PathVariable Long projectId,
            Authentication authentication) {

        String user = authentication != null ? authentication.getName() : "ADMIN";
        ProgressReconciliationResponse response = cumulativeProgressService.reconcileCumulativeProgress(projectId, user);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }
}
