package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.IssueEvidence;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.service.IssueService;
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
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
@Tag(name = "13. Issue, Blocker & Risk Register", description = "Construction site blockers, escalation, risk register, severity, owner & resolution tracking (FR-090 - FR-093)")
public class IssueController {

    private final IssueService issueService;

    // ==========================================
    // Site Blocker & Issue Management
    // ==========================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'QUALITY_ENGINEER')")
    @Operation(summary = "Record Site Blocker / Issue (FR-090)", description = "Creates a site blocker or general issue storing title, impact area, priority/severity, owner, due date, days delayed, and cause code.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> createIssue(@Valid @RequestBody IssueBlocker issue) {
        IssueBlocker created = issueService.createIssue(issue);
        return new ResponseEntity<>(ApiResponse.created(created, "Site issue recorded successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Site Blocker / Issue", description = "Updates details, severity, owner or impact area of an issue or blocker.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> updateIssue(@PathVariable Long id, @Valid @RequestBody IssueBlocker issue) {
        IssueBlocker updated = issueService.updateIssue(id, issue);
        return ResponseEntity.ok(ApiResponse.success(updated, "Site issue updated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Issues & Blockers", description = "Retrieves all site blockers and issues across projects or filtered by optional projectId, issueType, status, and severity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueBlocker>>> getAllIssues(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        List<IssueBlocker> issues = projectId != null
                ? issueService.getIssuesByProject(projectId, issueType, status, severity)
                : issueService.getAllIssues(issueType, status, severity);
        return ResponseEntity.ok(ApiResponse.success(issues, "Issues fetched successfully"));
    }

    @GetMapping("/blockers")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Active Site Blockers", description = "Retrieves all critical obstacles and site blockers across projects or for a specific project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueBlocker>>> getAllBlockers(@RequestParam(required = false) Long projectId) {
        List<IssueBlocker> blockers = projectId != null
                ? issueService.getBlockersByProject(projectId)
                : issueService.getAllBlockers();
        return ResponseEntity.ok(ApiResponse.success(blockers, "Active site blockers fetched successfully"));
    }

    @PostMapping("/evidence")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'QUALITY_ENGINEER')")
    @Operation(summary = "Attach Issue Proof Evidence Photo", description = "Attaches blocker proof or resolution evidence photo.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueEvidence>> addEvidence(@Valid @RequestBody IssueEvidence evidence) {
        IssueEvidence saved = issueService.addEvidence(evidence);
        return new ResponseEntity<>(ApiResponse.created(saved, "Issue evidence attached successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/evidence")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Evidence Photos for Issue / Blocker", description = "Retrieves all proof evidence photos attached to an issue or blocker.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueEvidence>>> getEvidenceByIssueId(@PathVariable Long id) {
        List<IssueEvidence> evidenceList = issueService.getEvidenceByIssueId(id);
        return ResponseEntity.ok(ApiResponse.success(evidenceList, "Issue evidence photos fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Issue Blocker By ID", description = "Retrieves site blocker issue details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> getIssueById(@PathVariable Long id) {
        IssueBlocker issue = issueService.getIssueById(id);
        return ResponseEntity.ok(ApiResponse.success(issue, "Site issue fetched successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Issues By Project", description = "Retrieves site blockers and issues for a project with optional filters for issueType, status, and severity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueBlocker>>> getIssuesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        List<IssueBlocker> issues = issueService.getIssuesByProject(projectId, issueType, status, severity);
        return ResponseEntity.ok(ApiResponse.success(issues, "Project issues fetched successfully"));
    }

    @GetMapping("/blockers/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Active Site Blockers (FR-090, FR-091)", description = "Retrieves all critical obstacles and site blockers delaying progress on site.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueBlocker>>> getBlockersByProject(@PathVariable Long projectId) {
        List<IssueBlocker> blockers = issueService.getBlockersByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(blockers, "Active project blockers fetched successfully"));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'QUALITY_ENGINEER')")
    @Operation(summary = "Resolve Issue / Unblock Work (FR-090)", description = "Marks a site blocker or issue as resolved with resolution notes and resolver details.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> resolveIssue(
            @PathVariable Long id,
            @RequestBody(required = false) IssueResolutionDto resolutionDto) {
        IssueBlocker resolved = (resolutionDto != null && resolutionDto.getResolutionNotes() != null)
                ? issueService.resolveIssue(id, resolutionDto)
                : issueService.resolveIssue(id);
        return ResponseEntity.ok(ApiResponse.success(resolved, "Issue marked as resolved"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Issue Status", description = "Transitions status: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        IssueBlocker updated = issueService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Issue status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Delete Issue Blocker", description = "Deletes a site blocker issue by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Site issue deleted successfully"));
    }

    @GetMapping("/summary/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Issue & Blocker KPI Summary", description = "Retrieves aggregated blocker count, cumulative delay days, critical issue breakdown and resolution metrics.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueSummaryDto>> getIssueSummary(@PathVariable Long projectId) {
        IssueSummaryDto summary = issueService.getIssueSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Issue & blocker KPI summary fetched successfully"));
    }

    @GetMapping("/overdue/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Overdue Site Blockers & Issues", description = "Retrieves active site blockers and issues that have passed their due date.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueBlocker>>> getOverdueBlockersByProject(@PathVariable Long projectId) {
        List<IssueBlocker> overdue = issueService.getOverdueBlockersByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(overdue, "Overdue site blockers fetched successfully"));
    }

    // ==========================================
    // Project Risk Register
    // ==========================================

    @PostMapping("/risks")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Record Project Risk (FR-092)", description = "Registers a project risk with probability, impact rating, auto severity/score calculation, and mitigation plan.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectRisk>> createRisk(@Valid @RequestBody ProjectRisk risk) {
        ProjectRisk created = issueService.createRisk(risk);
        return new ResponseEntity<>(ApiResponse.created(created, "Project risk registered successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/risks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Project Risk Details", description = "Updates probability, impact, severity, owner, mitigation or contingency plan.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectRisk>> updateRisk(@PathVariable Long id, @Valid @RequestBody ProjectRisk risk) {
        ProjectRisk updated = issueService.updateRisk(id, risk);
        return ResponseEntity.ok(ApiResponse.success(updated, "Project risk updated successfully"));
    }

    @PatchMapping("/risks/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Risk Mitigation Status (FR-092)", description = "Transitions status: IDENTIFIED -> MONITORED -> MITIGATED -> CLOSED with resolution notes.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectRisk>> updateRiskStatus(@PathVariable Long id, @Valid @RequestBody RiskStatusUpdateDto statusDto) {
        ProjectRisk updated = issueService.updateRiskStatus(id, statusDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Risk status updated successfully"));
    }

    @GetMapping("/risks")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Project Risks", description = "Retrieves complete risk registers across projects or filtered by optional projectId, status, and severity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectRisk>>> getAllRisks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        List<ProjectRisk> risks = projectId != null
                ? issueService.getRisksByProject(projectId, status, severity)
                : issueService.getAllRisks(status, severity);
        return ResponseEntity.ok(ApiResponse.success(risks, "Project risks fetched successfully"));
    }

    @GetMapping("/risks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Project Risk By ID", description = "Retrieves project risk details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectRisk>> getRiskById(@PathVariable Long id) {
        ProjectRisk risk = issueService.getRiskById(id);
        return ResponseEntity.ok(ApiResponse.success(risk, "Project risk fetched successfully"));
    }

    @GetMapping("/risks/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Project Risk Register (FR-092)", description = "Retrieves complete risk register for a project with optional filters for status and severity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectRisk>>> getRisksByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        List<ProjectRisk> risks = issueService.getRisksByProject(projectId, status, severity);
        return ResponseEntity.ok(ApiResponse.success(risks, "Project risks fetched successfully"));
    }

    @DeleteMapping("/risks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Delete Project Risk", description = "Deletes a project risk by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteRisk(@PathVariable Long id) {
        issueService.deleteRisk(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project risk deleted successfully"));
    }

    @GetMapping("/risks/summary/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Project Risk Register KPI Summary", description = "Retrieves risk count metrics by probability, impact, critical/high severity distribution and mitigation status.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<RiskSummaryDto>> getRiskSummary(@PathVariable Long projectId) {
        RiskSummaryDto summary = issueService.getRiskSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Risk KPI summary fetched successfully"));
    }
}
