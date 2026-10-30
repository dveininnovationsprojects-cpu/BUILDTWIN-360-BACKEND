package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.dpr.*;
import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import com.example.BuildTwin._0.model.DprStatus;
import com.example.BuildTwin._0.service.DailyProgressReportService;
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

@RestController
@RequiredArgsConstructor
@Tag(
        name = "7. Daily Progress Report (DPR) Module",
        description = "Comprehensive Site Execution DPR Engine: Header shift tracking, weather, safety incidents, physical WBS progress line items, manpower headcount, equipment hours, bulk material receipts/consumption, geotagged photo attachments, review workflow (Draft, Submit, Approve with automatic WBS cascade, Reject), Executive KPI Dashboards, and Activity Quantity Tracking."
)
@SecurityRequirement(name = "BearerAuth")
public class DailyProgressReportController {

    private final DailyProgressReportService dprService;

    // =========================================================================
    // 1. DPR CORE OPERATIONS
    // =========================================================================

    @PostMapping("/api/v1/projects/{projectId}/dpr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Create Draft Daily Progress Report (DPR)",
            description = "Creates a new daily progress report for the site shift. Can include initial activities, manpower, equipment, materials, and photo attachments. Report begins in DRAFT status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "DPR draft created successfully",
                    content = @Content(schema = @Schema(implementation = DailyProgressReportResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or activity not belonging to project"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "DPR already exists for this project, date, and shift")
    })
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> createDpr(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateDprRequest request,
            Authentication authentication) {
        String currentUser = authentication != null ? authentication.getName() : "ANONYMOUS";
        DailyProgressReportResponse response = dprService.createDpr(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Daily Progress Report draft created successfully with code: " + response.getReportCode()));
    }

    @GetMapping("/api/v1/projects/{projectId}/dpr")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Daily Progress Reports for Project",
            description = "Retrieves all DPRs for a project with optional filtering by date range (startDate, endDate) or lifecycle status (DRAFT, SUBMITTED, APPROVED, REJECTED)."
    )
    public ResponseEntity<ApiResponse<List<DailyProgressReportResponse>>> getDprsByProject(
            @PathVariable Long projectId,
            @Parameter(description = "Filter start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by status: DRAFT, SUBMITTED, APPROVED, REJECTED")
            @RequestParam(required = false) DprStatus status) {
        List<DailyProgressReportResponse> list = dprService.getDprsByProject(projectId, startDate, endDate, status);
        return ResponseEntity.ok(ApiResponse.success(list, "Found " + list.size() + " Daily Progress Reports"));
    }

    @GetMapping("/api/v1/projects/{projectId}/dpr/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Executive DPR Summary & Project KPIs",
            description = "Returns high-level project KPIs: Total filed DPRs, breakdown by Draft/Submitted/Approved/Rejected, cumulative manpower logged, and total safety incidents."
    )
    public ResponseEntity<ApiResponse<DprSummaryResponse>> getDprSummary(
            @PathVariable Long projectId) {
        DprSummaryResponse summary = dprService.getDprSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "DPR Executive Summary retrieved successfully"));
    }

    @GetMapping("/api/v1/dpr/{dprId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Daily Progress Report Details",
            description = "Returns complete DPR details including all activity line items, labor headcount, equipment usage, materials, and photos."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> getDprById(
            @PathVariable Long dprId) {
        DailyProgressReportResponse response = dprService.getDprById(dprId);
        return ResponseEntity.ok(ApiResponse.success(response, "DPR retrieved successfully"));
    }

    @PutMapping("/api/v1/dpr/{dprId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Update DPR Header Metadata",
            description = "Updates DPR shift, weather, temperature, notes, safety observations, and hindrances. Applicable only for DRAFT or REJECTED reports."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> updateDpr(
            @PathVariable Long dprId,
            @Valid @RequestBody UpdateDprRequest request,
            Authentication authentication) {
        String currentUser = authentication != null ? authentication.getName() : "ANONYMOUS";
        DailyProgressReportResponse response = dprService.updateDpr(dprId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Daily Progress Report header updated successfully"));
    }

    @DeleteMapping("/api/v1/dpr/{dprId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete Daily Progress Report",
            description = "Deletes a DRAFT or REJECTED DPR. APPROVED DPRs cannot be deleted to maintain WBS integrity."
    )
    public ResponseEntity<ApiResponse<Void>> deleteDpr(
            @PathVariable Long dprId) {
        dprService.deleteDpr(dprId);
        return ResponseEntity.ok(ApiResponse.success(null, "Daily Progress Report ID " + dprId + " deleted successfully"));
    }

    // =========================================================================
    // 2. DPR ACTIVITY ITEMS MANAGEMENT
    // =========================================================================

    @PostMapping("/api/v1/dpr/{dprId}/activities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Add WBS Activity Progress Item",
            description = "Appends a physical activity progress line item to the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> addActivityItem(
            @PathVariable Long dprId,
            @Valid @RequestBody DprActivityItemRequest request) {
        DailyProgressReportResponse response = dprService.addActivityItem(dprId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity progress item added to DPR"));
    }

    @GetMapping("/api/v1/dpr/{dprId}/activities")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Activity Items for DPR",
            description = "Retrieves all WBS activity progress line items logged for this specific Daily Progress Report."
    )
    public ResponseEntity<ApiResponse<List<DprActivityItemResponse>>> getActivityItems(
            @PathVariable Long dprId) {
        List<DprActivityItemResponse> items = dprService.getActivityItems(dprId);
        return ResponseEntity.ok(ApiResponse.success(items, "Found " + items.size() + " activity items in DPR"));
    }

    @GetMapping("/api/v1/dpr/{dprId}/activities/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get DPR Activity Item by ID",
            description = "Retrieves a single WBS activity progress line item by its item ID."
    )
    public ResponseEntity<ApiResponse<DprActivityItemResponse>> getActivityItemById(
            @PathVariable Long dprId,
            @PathVariable Long itemId) {
        DprActivityItemResponse response = dprService.getActivityItemById(dprId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity item retrieved successfully"));
    }

    @PutMapping("/api/v1/dpr/{dprId}/activities/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Update DPR Activity Item",
            description = "Updates quantity today, location reference, and remarks on an activity progress line item in a draft DPR."
    )
    public ResponseEntity<ApiResponse<DprActivityItemResponse>> updateActivityItem(
            @PathVariable Long dprId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateDprActivityItemRequest request) {
        DprActivityItemResponse response = dprService.updateActivityItem(dprId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity item updated successfully"));
    }

    @DeleteMapping("/api/v1/dpr/{dprId}/activities/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Remove WBS Activity Progress Item",
            description = "Removes an activity progress line item from the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> removeActivityItem(
            @PathVariable Long dprId,
            @PathVariable Long itemId) {
        DailyProgressReportResponse response = dprService.removeActivityItem(dprId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity item removed from DPR"));
    }

    @GetMapping("/api/v1/activities/{activityId}/dpr-progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get WBS Activity DPR Progress History",
            description = "Retrieves all historical DPR activity progress logs recorded across all dates for a specific WBS activity."
    )
    public ResponseEntity<ApiResponse<List<DprActivityItemResponse>>> getActivityDprHistory(
            @PathVariable Long activityId) {
        List<DprActivityItemResponse> history = dprService.getActivityDprHistory(activityId);
        return ResponseEntity.ok(ApiResponse.success(history, "Retrieved " + history.size() + " DPR entries for activity ID " + activityId));
    }

    // =========================================================================
    // 3. ACTIVITY QUANTITY TRACKING OPERATIONS
    // =========================================================================

    @GetMapping("/api/v1/dpr/{dprId}/quantity-tracking")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get DPR Activity Quantity Tracking Summary",
            description = "Retrieves complete quantity execution metrics for all activities in the given DPR, including planned vs completed today, cumulative quantity, balance remaining, required run rate, and over-execution alerts."
    )
    public ResponseEntity<ApiResponse<DprQuantityTrackingSummaryResponse>> getDprQuantityTracking(
            @PathVariable Long dprId) {
        DprQuantityTrackingSummaryResponse response = dprService.getDprQuantityTracking(dprId);
        return ResponseEntity.ok(ApiResponse.success(response, "DPR quantity tracking summary retrieved successfully"));
    }

    @GetMapping("/api/v1/activities/{activityId}/quantity-tracking")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Activity Quantity Tracking Analytics",
            description = "Retrieves deep physical quantity analytics for a specific WBS activity across all logged DPR entries, including planned, cumulative, balance, run rates, completion percentage, and historical journal entries."
    )
    public ResponseEntity<ApiResponse<ActivityQuantityTrackingResponse>> getActivityQuantityTracking(
            @PathVariable Long activityId) {
        ActivityQuantityTrackingResponse response = dprService.getActivityQuantityTracking(activityId);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity quantity tracking analytics retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/activity-quantity-tracking")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project-Wide Activity Quantity Tracking Dashboard",
            description = "Provides executive project-wide activity quantity tracking with discipline-level rollups (CIVIL, STRUCTURAL, MEP, FINISHING), execution status breakdown, over-executed activity flags, and required run rates."
    )
    public ResponseEntity<ApiResponse<ProjectQuantityTrackingResponse>> getProjectQuantityTracking(
            @PathVariable Long projectId,
            @Parameter(description = "Optional filter by discipline (e.g., CIVIL, STRUCTURAL, MEP, FINISHING)")
            @RequestParam(required = false) String discipline,
            @Parameter(description = "Optional filter by execution status (e.g., AHEAD, ON_TRACK, BEHIND, OVER_EXECUTED, COMPLETED)")
            @RequestParam(required = false) String status) {
        ProjectQuantityTrackingResponse response = dprService.getProjectQuantityTracking(projectId, discipline, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Project activity quantity tracking retrieved successfully"));
    }

    // =========================================================================
    // 4. OTHER LINE ITEMS MANAGEMENT
    // =========================================================================

    @PostMapping("/api/v1/dpr/{dprId}/labor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Add Manpower Headcount Item",
            description = "Appends a trade labor headcount record to the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> addLaborItem(
            @PathVariable Long dprId,
            @Valid @RequestBody DprLaborItemRequest request) {
        DailyProgressReportResponse response = dprService.addLaborItem(dprId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Labor headcount item added to DPR"));
    }

    @DeleteMapping("/api/v1/dpr/{dprId}/labor/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Remove Manpower Headcount Item",
            description = "Removes a labor headcount record from the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> removeLaborItem(
            @PathVariable Long dprId,
            @PathVariable Long itemId) {
        DailyProgressReportResponse response = dprService.removeLaborItem(dprId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Labor item removed from DPR"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/equipment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Add Equipment Deployment Item",
            description = "Records machinery operating hours, idle hours, and fuel consumption."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> addEquipmentItem(
            @PathVariable Long dprId,
            @Valid @RequestBody DprEquipmentItemRequest request) {
        DailyProgressReportResponse response = dprService.addEquipmentItem(dprId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Equipment item added to DPR"));
    }

    @DeleteMapping("/api/v1/dpr/{dprId}/equipment/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Remove Equipment Deployment Item",
            description = "Removes an equipment deployment record from the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> removeEquipmentItem(
            @PathVariable Long dprId,
            @PathVariable Long itemId) {
        DailyProgressReportResponse response = dprService.removeEquipmentItem(dprId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Equipment item removed from DPR"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/materials")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Add Material Receipt & Consumption Item",
            description = "Logs material received, quantities consumed, challan numbers, and suppliers."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> addMaterialItem(
            @PathVariable Long dprId,
            @Valid @RequestBody DprMaterialItemRequest request) {
        DailyProgressReportResponse response = dprService.addMaterialItem(dprId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Material item added to DPR"));
    }

    @DeleteMapping("/api/v1/dpr/{dprId}/materials/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Remove Material Item",
            description = "Removes a material item from the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> removeMaterialItem(
            @PathVariable Long dprId,
            @PathVariable Long itemId) {
        DailyProgressReportResponse response = dprService.removeMaterialItem(dprId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Material item removed from DPR"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/photos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Attach Site Photo",
            description = "Attaches a site visual progress or quality inspection photo to the DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> addPhotoAttachment(
            @PathVariable Long dprId,
            @Valid @RequestBody DprPhotoAttachmentRequest request) {
        DailyProgressReportResponse response = dprService.addPhotoAttachment(dprId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Photo attachment added to DPR"));
    }

    @DeleteMapping("/api/v1/dpr/{dprId}/photos/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Remove Photo Attachment",
            description = "Removes a photo attachment from the draft DPR."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> removePhotoAttachment(
            @PathVariable Long dprId,
            @PathVariable Long attachmentId) {
        DailyProgressReportResponse response = dprService.removePhotoAttachment(dprId, attachmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Photo attachment removed from DPR"));
    }

    // =========================================================================
    // 5. WORKFLOW TRANSITIONS (DRAFT, SUBMIT, WITHDRAW, APPROVE, REJECT)
    // =========================================================================

    @PostMapping("/api/v1/dpr/{dprId}/submit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Submit DPR for Managerial Review",
            description = "Transitions DPR status from DRAFT or REJECTED to SUBMITTED. Locks draft editing until review is finalized. Optional submission handover notes can be passed."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> submitDpr(
            @PathVariable Long dprId,
            @RequestBody(required = false) SubmitDprRequest request,
            Authentication authentication) {
        String submitter = authentication != null ? authentication.getName() : "SITE_ENGINEER";
        DailyProgressReportResponse response = dprService.submitDpr(dprId, request, submitter);
        return ResponseEntity.ok(ApiResponse.success(response, "DPR submitted for approval successfully"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/withdraw")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Withdraw Submitted DPR back to DRAFT",
            description = "Reverts a SUBMITTED DPR back to DRAFT status allowing site engineers to make adjustments or add attachments before review."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> withdrawDpr(
            @PathVariable Long dprId,
            @RequestBody(required = false) WithdrawDprRequest request,
            Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "SITE_ENGINEER";
        DailyProgressReportResponse response = dprService.withdrawDpr(dprId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "DPR withdrawn back to DRAFT successfully"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Approve Daily Progress Report (Cascades to WBS)",
            description = "Approves the DPR. Automatically calculates and cascades today's completed physical quantities into active WBS activities, recalculates progress percentages, sets actual start/completion dates, and writes immutable records into ActivityProgressLog. Optional approval comments can be included."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> approveDpr(
            @PathVariable Long dprId,
            @RequestBody(required = false) ApproveDprRequest request,
            Authentication authentication) {
        String approver = authentication != null ? authentication.getName() : "PROJECT_MANAGER";
        DailyProgressReportResponse response = dprService.approveDpr(dprId, request, approver);
        return ResponseEntity.ok(ApiResponse.success(response, "DPR approved successfully and progress cascaded to WBS activities"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Reject Daily Progress Report",
            description = "Rejects a submitted DPR with documented justification and returns it to the site team for rectification."
    )
    public ResponseEntity<ApiResponse<DailyProgressReportResponse>> rejectDpr(
            @PathVariable Long dprId,
            @Valid @RequestBody RejectDprRequest request,
            Authentication authentication) {
        String reviewer = authentication != null ? authentication.getName() : "PROJECT_MANAGER";
        DailyProgressReportResponse response = dprService.rejectDpr(dprId, request, reviewer);
        return ResponseEntity.ok(ApiResponse.success(response, "DPR rejected and returned to site engineer for revisions"));
    }

    // =========================================================================
    // 5. BLOCKER & REMARKS SUPPORT OPERATIONS
    // =========================================================================

    @GetMapping("/api/v1/dpr/{dprId}/blockers")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Blockers for DPR",
            description = "Retrieves all activity items flagged as blockers/hindrances in the specified Daily Progress Report, including root causes, severity, impact hours, resolution status, and remarks."
    )
    public ResponseEntity<ApiResponse<List<DprBlockerItemResponse>>> getDprBlockers(
            @PathVariable Long dprId) {
        List<DprBlockerItemResponse> blockers = dprService.getDprBlockers(dprId);
        return ResponseEntity.ok(ApiResponse.success(blockers, "Retrieved " + blockers.size() + " blockers for DPR ID " + dprId));
    }

    @GetMapping("/api/v1/projects/{projectId}/dpr/blockers")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Blocker & Remarks Register",
            description = "Enterprise project-wide blocker register across all Daily Progress Reports. Supports filtering by status (OPEN, IN_PROGRESS, RESOLVED, WAIVED), category (MATERIAL_SHORTAGE, DESIGN_CLARIFICATION, etc.), severity (CRITICAL, HIGH, etc.), and activityId."
    )
    public ResponseEntity<ApiResponse<List<DprBlockerItemResponse>>> getProjectDprBlockers(
            @PathVariable Long projectId,
            @RequestParam(required = false) BlockerStatus status,
            @RequestParam(required = false) BlockerCategory category,
            @RequestParam(required = false) BlockerSeverity severity,
            @RequestParam(required = false) Long activityId) {
        List<DprBlockerItemResponse> blockers = dprService.getProjectDprBlockers(projectId, status, category, severity, activityId);
        return ResponseEntity.ok(ApiResponse.success(blockers, "Retrieved " + blockers.size() + " project blockers matching criteria"));
    }

    @GetMapping("/api/v1/projects/{projectId}/dpr/blockers/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Project Blocker Summary & Analytics Dashboard",
            description = "Calculates executive blocker KPIs for a project: total blockers, active open blockers, resolved blockers, critical count, total delay impact hours, and category/severity/status distributions."
    )
    public ResponseEntity<ApiResponse<DprBlockerSummaryResponse>> getProjectBlockerSummary(
            @PathVariable Long projectId) {
        DprBlockerSummaryResponse summary = dprService.getProjectBlockerSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Project blocker summary retrieved successfully"));
    }

    @PostMapping("/api/v1/dpr/{dprId}/activities/{itemId}/resolve-blocker")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Resolve or Update Blocker on DPR Activity",
            description = "Updates the blocker resolution status (RESOLVED, IN_PROGRESS, WAIVED), adds resolution notes, and updates operational remarks."
    )
    public ResponseEntity<ApiResponse<DprBlockerItemResponse>> resolveBlocker(
            @PathVariable Long dprId,
            @PathVariable Long itemId,
            @Valid @RequestBody ResolveDprBlockerRequest request,
            Authentication authentication) {
        String resolver = authentication != null ? authentication.getName() : "SITE_ENGINEER";
        DprBlockerItemResponse response = dprService.resolveBlocker(dprId, itemId, request, resolver);
        return ResponseEntity.ok(ApiResponse.success(response, "Blocker updated successfully to " + response.getBlockerStatus()));
    }
}
