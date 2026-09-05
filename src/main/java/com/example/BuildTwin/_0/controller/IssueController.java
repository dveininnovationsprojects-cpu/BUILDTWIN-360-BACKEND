package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.issues.model.*;
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
@Tag(name = "13. Issue, Blocker & Risk Register", description = "Construction site blockers, escalation, risk register & delay cause tracking (FR-090 - FR-093)")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'QUALITY_ENGINEER')")
    @Operation(summary = "Record Site Blocker / Issue (FR-090)", description = "Creates a site blocker record storing impact area, priority, owner, due date, and linked activity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> createIssue(@Valid @RequestBody IssueBlocker issue) {
        IssueBlocker created = issueService.createIssue(issue);
        return new ResponseEntity<>(ApiResponse.created(created, "Site issue recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Issues By Project", description = "Retrieves all active site blockers and issues for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<IssueBlocker>>> getIssuesByProject(@PathVariable Long projectId) {
        List<IssueBlocker> issues = issueService.getIssuesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(issues, "Project issues fetched successfully"));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'QUALITY_ENGINEER')")
    @Operation(summary = "Resolve Issue (FR-090)", description = "Marks a site blocker or issue as resolved.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<IssueBlocker>> resolveIssue(@PathVariable Long id) {
        IssueBlocker resolved = issueService.resolveIssue(id);
        return ResponseEntity.ok(ApiResponse.success(resolved, "Issue marked as resolved"));
    }

    @PostMapping("/risks")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Record Project Risk (FR-092)", description = "Registers a project risk with probability, impact rating, and mitigation plan.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectRisk>> createRisk(@Valid @RequestBody ProjectRisk risk) {
        ProjectRisk created = issueService.createRisk(risk);
        return new ResponseEntity<>(ApiResponse.created(created, "Project risk registered successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/risks/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Project Risk Register (FR-092)", description = "Retrieves complete risk register for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectRisk>>> getRisksByProject(@PathVariable Long projectId) {
        List<ProjectRisk> risks = issueService.getRisksByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(risks, "Project risks fetched successfully"));
    }
}
