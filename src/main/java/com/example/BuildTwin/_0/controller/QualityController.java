package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.quality.model.*;
import com.example.BuildTwin._0.domain.quality.service.QualityService;
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
@RequestMapping("/api/v1/quality")
@RequiredArgsConstructor
@Tag(name = "12. Quality, Snag & NCR Workflow", description = "Quality Inspections, NCR/Snag management, Rectification proof & verifier closure workflows (FR-080 - FR-084)")
public class QualityController {

    private final QualityService qualityService;

    @PostMapping("/issues")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER')")
    @Operation(summary = "Create Snag / NCR Issue (FR-080, FR-081)", description = "Creates a quality defect or Non-Conformance Report (NCR) record with category, severity, location, and due date.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> createQualityIssue(@Valid @RequestBody QualityIssue issue) {
        QualityIssue created = qualityService.createQualityIssue(issue);
        return new ResponseEntity<>(ApiResponse.created(created, "Quality issue recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/issues/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Quality Issue Details", description = "Retrieves quality defect details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> getQualityIssueById(@PathVariable Long id) {
        QualityIssue issue = qualityService.getQualityIssueById(id);
        return ResponseEntity.ok(ApiResponse.success(issue, "Quality issue details fetched successfully"));
    }

    @GetMapping("/issues/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Quality Issues By Project", description = "Retrieves all open and closed quality snags/NCRs for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<QualityIssue>>> getQualityIssuesByProject(@PathVariable Long projectId) {
        List<QualityIssue> issues = qualityService.getQualityIssuesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(issues, "Quality issues fetched successfully"));
    }

    @PatchMapping("/issues/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Quality Workflow Status (FR-082, FR-083)", description = "Transitions status: Open -> Assigned -> Rectification Submitted -> Verification -> Closed / Reopened.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        QualityIssue updated = qualityService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Quality issue status updated successfully"));
    }

    @PostMapping("/evidence")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER')")
    @Operation(summary = "Attach Quality Evidence Photo (FR-083)", description = "Attaches defect capture or rectification closure proof photos.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityEvidence>> addEvidence(@Valid @RequestBody QualityEvidence evidence) {
        QualityEvidence saved = qualityService.addEvidence(evidence);
        return new ResponseEntity<>(ApiResponse.created(saved, "Quality evidence attached successfully"), HttpStatus.CREATED);
    }
}
