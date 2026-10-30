package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.issues.dto.*;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.service.ProjectIssueService;
import com.example.BuildTwin._0.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project-issues")
@RequiredArgsConstructor
@Tag(name = "17. Project Issue Lifecycle (5-Step Master Workflow)", description = "End-to-end master workflow: Step 1 (Capture/Snag/NCR/Blocker) -> Step 2 (PM Assign & SLA) -> Step 3 (Contractor Fix & Proof) -> Step 4 (QA Audit Pass/Fail) -> Step 5 (Overdue Red Alert)")
public class ProjectIssueController {

    private final ProjectIssueService projectIssueService;

    // =========================================================================
    // STEP 1: Identification & Logging (Capture Phase)
    // =========================================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'QUALITY_ENGINEER')")
    @Operation(summary = "Step 1: Identify & Log Issue Ticket (Capture Phase)", description = "Site Engineer or Quality Inspector records a defect/problem with photo, WBS activity tag, severity, and type (SNAG, NCR, ISSUE, RISK, BLOCKER). Status defaults to OPEN.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueResponseDto>> createIssue(@Valid @RequestBody CreateProjectIssueDto dto) {
        ProjectIssueResponseDto created = projectIssueService.createIssue(dto);
        return new ResponseEntity<>(ApiResponse.created(created, "Issue ticket logged successfully with status OPEN"), HttpStatus.CREATED);
    }

    // =========================================================================
    // STEP 2: Assignment & SLA Setting (Allocation Phase)
    // =========================================================================
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Step 2: Assign Issue & Set SLA Target Date (Allocation Phase)", description = "Project Manager assigns the issue to a contractor or engineer and sets the Target Resolution Date (SLA). Status updates to ASSIGNED.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueResponseDto>> assignIssue(@PathVariable Long id, @Valid @RequestBody AssignProjectIssueDto dto) {
        ProjectIssueResponseDto assigned = projectIssueService.assignIssue(id, dto);
        return ResponseEntity.ok(ApiResponse.success(assigned, "Issue assigned to contractor/engineer with SLA target date"));
    }

    // =========================================================================
    // STEP 3: Action & Rectification (Fixing Phase)
    // =========================================================================
    @PatchMapping("/{id}/start-progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    @Operation(summary = "Step 3A: Mark Issue In-Progress (Contractor Starts Work)", description = "Assigned contractor or engineer opens the issue and starts rectification. Status updates to IN_PROGRESS.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueResponseDto>> startProgress(@PathVariable Long id) {
        ProjectIssueResponseDto inProgress = projectIssueService.startProgress(id);
        return ResponseEntity.ok(ApiResponse.success(inProgress, "Issue marked as IN_PROGRESS by contractor"));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    @Operation(summary = "Step 3B: Resolve Issue with Proof Photo (Rectification Phase)", description = "Contractor completes work, uploads resolution proof photo, writes resolution remarks (e.g. 'Crack filled with epoxy grout'), and sets status to RESOLVED.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueResponseDto>> resolveIssue(@PathVariable Long id, @Valid @RequestBody ResolveProjectIssueDto dto) {
        ProjectIssueResponseDto resolved = projectIssueService.resolveIssue(id, dto);
        return ResponseEntity.ok(ApiResponse.success(resolved, "Issue resolved by contractor and submitted for QA inspection"));
    }

    // =========================================================================
    // STEP 4: Quality Verification & Closure (Audit Phase)
    // =========================================================================
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER')")
    @Operation(summary = "Step 4: Audit Verification Sign-off (Pass -> CLOSED, Fail -> Reverts to OPEN)", description = "Quality Inspector conducts site audit. If approved, status becomes CLOSED. If rejected, status reverts to OPEN for re-rectification.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueResponseDto>> verifyIssue(@PathVariable Long id, @Valid @RequestBody VerifyProjectIssueDto dto) {
        ProjectIssueResponseDto verified = projectIssueService.verifyIssue(id, dto);
        String msg = dto.isApproved() ? "Issue verified and CLOSED successfully" : "Issue rejected by auditor - reverted to OPEN for contractor rework";
        return ResponseEntity.ok(ApiResponse.success(verified, msg));
    }

    // =========================================================================
    // Retrieval & Queries
    // =========================================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR', 'CONTRACTOR')")
    @Operation(summary = "Get Project Issue By ID", description = "Retrieves full details of a project issue including SLA, proof photos, and audit log.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueResponseDto>> getIssueById(@PathVariable Long id) {
        ProjectIssueResponseDto issue = projectIssueService.getIssueById(id);
        return ResponseEntity.ok(ApiResponse.success(issue, "Project issue fetched successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR', 'CONTRACTOR')")
    @Operation(summary = "Get Project Issues By Project with Filters", description = "Retrieves project issues filtered by optional type (SNAG, NCR, ISSUE, RISK, BLOCKER), status, and severity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectIssueResponseDto>>> getIssuesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueType type,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssueSeverity severity) {
        List<ProjectIssueResponseDto> list = projectIssueService.getIssuesByProject(projectId, type, status, severity);
        return ResponseEntity.ok(ApiResponse.success(list, "Project issues fetched successfully"));
    }

    // =========================================================================
    // STEP 5: Overdue Escalation & Monitoring (Analytics Phase)
    // =========================================================================
    @GetMapping("/overdue/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR', 'CONTRACTOR')")
    @Operation(summary = "Step 5A: Get Overdue Issues Exceeding SLA Target Date", description = "Returns all unresolved issues past SLA Target Resolution Date for executive Red Alert.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectIssueResponseDto>>> getOverdueIssuesByProject(@PathVariable Long projectId) {
        List<ProjectIssueResponseDto> overdues = projectIssueService.getOverdueIssuesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(overdues, "Overdue project issues fetched successfully"));
    }

    @GetMapping("/summary/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR', 'CONTRACTOR')")
    @Operation(summary = "Step 5B: Project Issue Analytics KPI Summary", description = "Returns total counts across statuses (OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED), types (SNAG, NCR, RISK, BLOCKER), severities, overdue counts, and resolution rate.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectIssueSummaryDto>> getIssueSummaryByProject(@PathVariable Long projectId) {
        ProjectIssueSummaryDto summary = projectIssueService.getIssueSummaryByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Project issue summary metrics fetched successfully"));
    }
}
