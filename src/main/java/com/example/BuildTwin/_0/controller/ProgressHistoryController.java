package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.progress.*;
import com.example.BuildTwin._0.service.ProgressHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import com.example.BuildTwin._0.domain.projects.model.Project;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "8. Progress History Module",
        description = "Enterprise Chronological Site Progress History, Production Journals, Activity Velocity, S-Curve Analytics, Blocker Logs, and Rollup Cascade."
)
@SecurityRequirement(name = "BearerAuth")
public class ProgressHistoryController {

    private final ProgressHistoryService progressHistoryService;

    // =========================================================================
    // 1. ACTIVITY PROGRESS HISTORY
    // =========================================================================

    @GetMapping({"/api/v1/activities/{activityId}/progress-history", "/api/v1/wbs-activities/{activityId}/progress-history"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Complete Progress History for WBS Activity",
            description = "Retrieves full chronological progress journal, cumulative quantity completed, burn rate statistics, and all historical daily entries for an activity. Supports date filtering, blocker filter, and sort order."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Activity progress history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ActivityProgressHistoryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<ApiResponse<ActivityProgressHistoryResponse>> getActivityProgressHistory(
            @PathVariable Long activityId,
            @Parameter(description = "Filter start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter entries by blocker flag (true/false)")
            @RequestParam(required = false) Boolean isBlocker,
            @Parameter(description = "Filter by source origin: DPR or MANUAL_LOG")
            @RequestParam(required = false) String source,
            @Parameter(description = "Sort direction: desc (newest first, default) or asc (oldest first)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        ActivityProgressHistoryResponse response = progressHistoryService.getActivityProgressHistory(
                activityId, startDate, endDate, isBlocker, source, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity progress history retrieved successfully"));
    }

    @GetMapping({"/api/v1/activities/{activityId}/progress-history/paginated", "/api/v1/wbs-activities/{activityId}/progress-history/paginated"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Paginated Progress History Items for Activity",
            description = "Retrieves paginated daily progress history records for a specific activity with filtering and custom page size."
    )
    public ResponseEntity<ApiResponse<PageResponse<ProgressHistoryItemResponse>>> getActivityProgressHistoryPaginated(
            @PathVariable Long activityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean isBlocker,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<ProgressHistoryItemResponse> response = progressHistoryService.getActivityProgressHistoryPaginated(
                activityId, startDate, endDate, isBlocker, source, page, size, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Paginated activity progress history retrieved successfully"));
    }

        @GetMapping({
            "/api/v1/activities/{activityId}/progress-history/analytics",
            "/api/v1/wbs-activities/{activityId}/progress-history/analytics",
            "/api/v1/activities/{activityId}/progress-timeline",
            "/api/v1/wbs-activities/{activityId}/progress-timeline"
    })
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Activity Progress Velocity & S-Curve Analytics",
            description = "Retrieves deep physical quantity analytics, time-series progression data points for S-Curve graphing, required daily run rate vs actual achieved rate, days remaining, and velocity trend status."
    )
    public ResponseEntity<ApiResponse<ProgressAnalyticsResponse>> getActivityProgressAnalytics(
            @PathVariable Long activityId) {

        ProgressAnalyticsResponse response = progressHistoryService.getActivityProgressAnalytics(activityId);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity progress analytics retrieved successfully"));
    }

    // =========================================================================
    // 2. PROJECT-WIDE PROGRESS HISTORY REGISTER
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/progress-history")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project-Wide Progress History Register & Dashboard",
            description = "Master project progress journal aggregating all daily logs across all work packages and activities. Includes high-level KPIs (total entries, cumulative quantity, unique activities logged, manpower sum, blocker count) with multi-criteria filtering."
    )
    public ResponseEntity<ApiResponse<ProjectProgressHistoryResponse>> getProjectProgressHistory(
            @PathVariable Long projectId,
            @Parameter(description = "Optional filter by Work Package ID")
            @RequestParam(required = false) Long workPackageId,
            @Parameter(description = "Optional filter by Activity ID")
            @RequestParam(required = false) Long activityId,
            @Parameter(description = "Optional filter by discipline (e.g. CIVIL, STRUCTURAL, MEP, FINISHING)")
            @RequestParam(required = false) String discipline,
            @Parameter(description = "Filter start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by blocker flag (true/false)")
            @RequestParam(required = false) Boolean isBlocker,
            @Parameter(description = "Filter by origin source (DPR / MANUAL_LOG)")
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "logDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ProjectProgressHistoryResponse response = progressHistoryService.getProjectProgressHistory(
                projectId, workPackageId, activityId, discipline, startDate, endDate, isBlocker, source, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Project progress history register retrieved successfully"));
    }

    // =========================================================================
    // 3. WORK PACKAGE PROGRESS HISTORY
    // =========================================================================

    @GetMapping("/api/v1/work-packages/{workPackageId}/progress-history")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Work Package Progress History",
            description = "Retrieves chronological progress journal entries and rolled-up performance metrics for all activities configured under a specific Work Package."
    )
    public ResponseEntity<ApiResponse<WorkPackageProgressHistoryResponse>> getWorkPackageProgressHistory(
            @PathVariable Long workPackageId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean isBlocker,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {

        WorkPackageProgressHistoryResponse response = progressHistoryService.getWorkPackageProgressHistory(
                workPackageId, startDate, endDate, isBlocker, source, page, size, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Work Package progress history retrieved successfully"));
    }

    // =========================================================================
    // 4. GET SINGLE PROGRESS LOG ENTRY
    // =========================================================================

    @GetMapping("/api/v1/progress-history/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Progress History Log Entry by ID",
            description = "Retrieves full details of a specific daily progress log entry including activity details, quantities, manpower, equipment, and remarks."
    )
    public ResponseEntity<ApiResponse<ProgressHistoryItemResponse>> getProgressHistoryItemById(
            @PathVariable Long id) {

        ProgressHistoryItemResponse response = progressHistoryService.getProgressHistoryItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Progress history entry retrieved successfully"));
    }

    // =========================================================================
    // 5. DIRECT SITE PROGRESS LOGGING VIA PROGRESS HISTORY API
    // =========================================================================

    @PostMapping({"/api/v1/activities/{activityId}/progress-history", "/api/v1/wbs-activities/{activityId}/progress-history"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Log Physical Site Progress Entry for Activity",
            description = "Records daily work output, manpower count, equipment deployed, weather, and hindrance remarks. Automatically advances activity cumulative quantity, recalculates progress %, updates actual dates, rolls up to parent tasks, and saves to the audit trail."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Site progress history entry logged successfully",
                    content = @Content(schema = @Schema(implementation = ProgressHistoryItemResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<ApiResponse<ProgressHistoryItemResponse>> logProgress(
            @PathVariable Long activityId,
            @Valid @RequestBody LogProgressHistoryRequest request,
            Authentication authentication) {

        String recordedBy = authentication != null ? authentication.getName() : "SITE_ENGINEER";
        ProgressHistoryItemResponse response = progressHistoryService.logProgress(activityId, request, recordedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Progress history entry recorded successfully"));
    }

    // =========================================================================
    // 6. UPDATE PROGRESS HISTORY ENTRY
    // =========================================================================

    @PutMapping("/api/v1/progress-history/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Update Progress History Log Entry",
            description = "Updates remarks, hindrance notes, weather condition, manpower, equipment, or adjusts quantity completed on an existing progress history record with automatic recalculation."
    )
    public ResponseEntity<ApiResponse<ProgressHistoryItemResponse>> updateProgressLog(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProgressHistoryRequest request,
            Authentication authentication) {

        String performedBy = authentication != null ? authentication.getName() : "SITE_ENGINEER";
        ProgressHistoryItemResponse response = progressHistoryService.updateProgressLog(id, request, performedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Progress history entry updated successfully"));
    }

    // =========================================================================
    // 7. DELETE PROGRESS HISTORY ENTRY
    // =========================================================================

    @DeleteMapping("/api/v1/progress-history/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete Progress History Log Entry",
            description = "Permanently deletes a progress history journal entry. Automatically recalculates the activity's cumulative completed quantity and progress percentage from remaining records, cascades updates, and logs an audit trail."
    )
    public ResponseEntity<ApiResponse<Void>> deleteProgressLog(
            @PathVariable Long id,
            Authentication authentication) {

        String performedBy = authentication != null ? authentication.getName() : "ADMIN";
        progressHistoryService.deleteProgressLog(id, performedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Progress history entry ID " + id + " deleted and activity metrics recalculated successfully"));
    }
}
