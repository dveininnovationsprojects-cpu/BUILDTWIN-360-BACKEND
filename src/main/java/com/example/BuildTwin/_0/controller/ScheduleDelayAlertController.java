package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.alert.*;
import com.example.BuildTwin._0.model.DelayAlertStatus;
import com.example.BuildTwin._0.model.DelayAlertType;
import com.example.BuildTwin._0.model.OverdueAgingBucket;
import com.example.BuildTwin._0.model.OverdueSeverity;
import com.example.BuildTwin._0.service.ScheduleDelayAlertService;
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
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "10. Schedule Delay Alert Support Module",
        description = "Automated Schedule Delay Detection Engine, Severity Escalation, Cascading Bottleneck Warnings, Alert Acknowledgment Workflow, Catch-Up Mitigation Plans, and Executive KPI Dashboards."
)
@SecurityRequirement(name = "BearerAuth")
public class ScheduleDelayAlertController {

    private final ScheduleDelayAlertService scheduleDelayAlertService;

    // =========================================================================
    // 1. AUTOMATED SCAN & GENERATION ENGINE
    // =========================================================================

    @PostMapping("/api/v1/projects/{projectId}/schedule-alerts/scan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Scan Project & Generate Schedule Delay Alerts",
            description = "Triggers automated delay scanning algorithm across all WBS activities for the project. Analyzes planned vs actual dates, progress slippage, successor dependencies, and uncompleted predecessor bottlenecks. Automatically creates new alerts, updates existing active alerts, and auto-resolves alerts for completed activities."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Schedule delay alert scan executed successfully",
                    content = @Content(schema = @Schema(implementation = ScanDelayAlertsResultResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ScanDelayAlertsResultResponse>> scanAndGenerateAlerts(
            @PathVariable Long projectId,
            @RequestBody(required = false) ScanDelayAlertsRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        ScanDelayAlertsResultResponse result = scheduleDelayAlertService.scanAndGenerateAlerts(
                projectId, request, authentication != null ? authentication.getName() : "SYSTEM"
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Schedule delay alert scan completed successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/schedule-alerts")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Schedule Delay Alerts for Project",
            description = "Retrieves delay alerts for a project with multi-dimensional filtering by status (ACTIVE, ACKNOWLEDGED, IN_MITIGATION, RESOLVED, DISMISSED), severity (CRITICAL, HIGH, MEDIUM, LOW), alertType, agingBucket, discipline, and contractor."
    )
    public ResponseEntity<ApiResponse<List<ScheduleDelayAlertResponse>>> getAlerts(
            @PathVariable Long projectId,
            @RequestParam(required = false) DelayAlertStatus status,
            @RequestParam(required = false) OverdueSeverity severity,
            @RequestParam(required = false) DelayAlertType alertType,
            @RequestParam(required = false) OverdueAgingBucket agingBucket,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String contractor) {

        List<ScheduleDelayAlertResponse> alerts = scheduleDelayAlertService.getAlerts(
                projectId, status, severity, alertType, agingBucket, discipline, contractor
        );
        return ResponseEntity.ok(ApiResponse.success(alerts, "Schedule delay alerts retrieved successfully"));
    }

    // =========================================================================
    // 3. EXECUTIVE KPI SUMMARY & METRICS
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/schedule-alerts/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Schedule Delay Alerts Executive KPI Summary",
            description = "Provides executive management metrics on total active alerts, unacknowledged backlog, critical bottlenecks, average delay days, severity distribution, contractor delay exposure, and strategic recommendations."
    )
    public ResponseEntity<ApiResponse<ScheduleDelayAlertSummaryResponse>> getAlertSummary(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        ScheduleDelayAlertSummaryResponse summary = scheduleDelayAlertService.getAlertSummary(projectId, asOfDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Schedule delay alert summary retrieved successfully"));
    }

    // =========================================================================
    // 4. EARLY WARNING DELAY RISK PREVIEW
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/schedule-alerts/early-warnings")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Early Warning Delay Risks (Horizon Screening)",
            description = "Detects tasks approaching scheduled start or finish within the next 3 to 7 days that are in jeopardy due to incomplete predecessors or unresolved field constraints (materials, drawings, equipment, permits)."
    )
    public ResponseEntity<ApiResponse<List<EarlyWarningDelayRiskResponse>>> getEarlyWarnings(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(defaultValue = "7") Integer horizonDays) {

        List<EarlyWarningDelayRiskResponse> earlyWarnings = scheduleDelayAlertService.getEarlyWarnings(projectId, asOfDate, horizonDays);
        return ResponseEntity.ok(ApiResponse.success(earlyWarnings, "Early warning delay risks retrieved successfully"));
    }

    // =========================================================================
    // 5. GET SINGLE ALERT DETAILS
    // =========================================================================

    @GetMapping("/api/v1/schedule-alerts/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Delay Alert Details by ID",
            description = "Retrieves full detailed breakdown of a single schedule delay alert including impacted successor codes, dates, quantities, and current mitigation progress."
    )
    public ResponseEntity<ApiResponse<ScheduleDelayAlertResponse>> getAlertById(
            @PathVariable Long id) {

        ScheduleDelayAlertResponse alert = scheduleDelayAlertService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.success(alert, "Delay alert retrieved successfully"));
    }

    // =========================================================================
    // 6. ACKNOWLEDGE ALERT (PUT)
    // =========================================================================

    @PutMapping("/api/v1/schedule-alerts/{id}/acknowledge")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Acknowledge Delay Alert",
            description = "Transitions an alert from ACTIVE to ACKNOWLEDGED status. Records site engineer remarks, initial delay assessment, and estimated target recovery date."
    )
    public ResponseEntity<ApiResponse<ScheduleDelayAlertResponse>> acknowledgeAlert(
            @PathVariable Long id,
            @Valid @RequestBody AcknowledgeAlertRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        ScheduleDelayAlertResponse response = scheduleDelayAlertService.acknowledgeAlert(
                id, request, authentication != null ? authentication.getName() : "USER"
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Delay alert acknowledged successfully"));
    }

    // =========================================================================
    // 7. MITIGATE / RECORD CATCH-UP PLAN (PUT)
    // =========================================================================

        @PutMapping({
            "/api/v1/schedule-alerts/{id}/mitigate",
            "/api/v1/schedule-alerts/{id}/delay-cause"
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Record Delay Mitigation & Catch-Up Plan",
            description = "Transitions alert status to IN_MITIGATION. Formally documents root cause category (material shortage, labor, drawings, weather), concrete recovery action plan, revised completion date, and additional labor allocation."
    )
    public ResponseEntity<ApiResponse<ScheduleDelayAlertResponse>> mitigateAlert(
            @PathVariable Long id,
            @Valid @RequestBody MitigateAlertRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        ScheduleDelayAlertResponse response = scheduleDelayAlertService.mitigateAlert(
                id, request, authentication != null ? authentication.getName() : "USER"
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Delay mitigation catch-up plan recorded successfully"));
    }

    // =========================================================================
    // 8. RESOLVE ALERT (PUT)
    // =========================================================================

    @PutMapping("/api/v1/schedule-alerts/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Resolve Delay Alert",
            description = "Marks a delay alert as RESOLVED once the activity has caught up with schedule, physical work is completed, or schedule revision has been formally approved."
    )
    public ResponseEntity<ApiResponse<ScheduleDelayAlertResponse>> resolveAlert(
            @PathVariable Long id,
            @Valid @RequestBody ResolveAlertRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        ScheduleDelayAlertResponse response = scheduleDelayAlertService.resolveAlert(
                id, request, authentication != null ? authentication.getName() : "USER"
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Delay alert resolved successfully"));
    }

    // =========================================================================
    // 9. DISMISS ALERT (PUT)
    // =========================================================================

    @PutMapping("/api/v1/schedule-alerts/{id}/dismiss")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Dismiss Delay Alert",
            description = "Suppresses or dismisses an alert with mandatory documented justification (e.g., activity scope descoped under Change Order or false positive alert)."
    )
    public ResponseEntity<ApiResponse<ScheduleDelayAlertResponse>> dismissAlert(
            @PathVariable Long id,
            @Valid @RequestBody DismissAlertRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        ScheduleDelayAlertResponse response = scheduleDelayAlertService.dismissAlert(
                id, request, authentication != null ? authentication.getName() : "USER"
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Delay alert dismissed successfully"));
    }

    // =========================================================================
    // 10. DELETE ALERT (DELETE)
    // =========================================================================

    @DeleteMapping("/api/v1/schedule-alerts/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete Delay Alert",
            description = "Permanently removes a delay alert record from the system. Restricted to Project Managers, Directors, and Administrators."
    )
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {

        scheduleDelayAlertService.deleteAlert(id, authentication != null ? authentication.getName() : "USER");
        return ResponseEntity.ok(ApiResponse.success(null, "Delay alert deleted successfully"));
    }
}
