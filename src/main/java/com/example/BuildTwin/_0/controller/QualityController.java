package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
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
    @Operation(summary = "Create Snag / NCR Issue (FR-080, FR-081)", description = "Creates a quality defect or Non-Conformance Report (NCR) record with category, severity, owner, location, and due date.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> createQualityIssue(@Valid @RequestBody QualityIssue issue) {
        QualityIssue created = qualityService.createQualityIssue(issue);
        return new ResponseEntity<>(ApiResponse.created(created, "Quality issue recorded successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/issues/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Quality Issue Details", description = "Updates details of a quality defect or NCR.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> updateQualityIssue(@PathVariable Long id, @Valid @RequestBody QualityIssue issue) {
        QualityIssue updated = qualityService.updateQualityIssue(id, issue);
        return ResponseEntity.ok(ApiResponse.success(updated, "Quality issue updated successfully"));
    }

    @GetMapping("/issues/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Quality Issue Details", description = "Retrieves quality defect details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> getQualityIssueById(@PathVariable Long id) {
        QualityIssue issue = qualityService.getQualityIssueById(id);
        return ResponseEntity.ok(ApiResponse.success(issue, "Quality issue details fetched successfully"));
    }

    @GetMapping("/issues/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Quality Issues By Project", description = "Retrieves quality snags/NCRs for a project with optional filters for status, severity, and issueType.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<QualityIssue>>> getQualityIssuesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String issueType) {
        List<QualityIssue> issues = qualityService.getQualityIssuesByProject(projectId, status, severity, issueType);
        return ResponseEntity.ok(ApiResponse.success(issues, "Quality issues fetched successfully"));
    }

    @PatchMapping("/issues/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Quality Workflow Status (FR-082, FR-083)", description = "Transitions status: OPEN -> ASSIGNED -> RECTIFICATION_SUBMITTED -> VERIFIED -> CLOSED / REOPENED.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        QualityIssue updated = qualityService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Quality issue status updated successfully"));
    }

    @PostMapping("/issues/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER', 'SITE_SUPERVISOR')")
    @Operation(summary = "Submit Rectification Resolution (FR-082)", description = "Submits corrective action notes and marks status as RECTIFICATION_SUBMITTED awaiting verification.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> resolveQualityIssue(@PathVariable Long id, @Valid @RequestBody QualityResolutionDto resolutionDto) {
        QualityIssue resolved = qualityService.resolveQualityIssue(id, resolutionDto);
        return ResponseEntity.ok(ApiResponse.success(resolved, "Rectification resolution submitted successfully"));
    }

    @PostMapping("/issues/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER')")
    @Operation(summary = "Verify Rectification & Close NCR / Snag (FR-083, FR-084)", description = "Performs QA closure verification. Approving marks issue CLOSED; rejecting reopens it.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityIssue>> verifyQualityIssue(@PathVariable Long id, @Valid @RequestBody QualityVerificationDto verificationDto) {
        QualityIssue verified = qualityService.verifyQualityIssue(id, verificationDto);
        return ResponseEntity.ok(ApiResponse.success(verified, "Quality issue verification processed successfully"));
    }

    @DeleteMapping("/issues/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER')")
    @Operation(summary = "Delete Quality Issue / Snag", description = "Deletes a quality issue record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteQualityIssue(@PathVariable Long id) {
        qualityService.deleteQualityIssue(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Quality issue deleted successfully"));
    }

    @PostMapping("/evidence")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER', 'SITE_SUPERVISOR')")
    @Operation(summary = "Attach Quality Evidence Photo (FR-083)", description = "Attaches defect capture or rectification closure proof photos.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualityEvidence>> addEvidence(@Valid @RequestBody QualityEvidence evidence) {
        QualityEvidence saved = qualityService.addEvidence(evidence);
        return new ResponseEntity<>(ApiResponse.created(saved, "Quality evidence attached successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/issues/{id}/evidence")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Evidence Photos for Quality Issue", description = "Retrieves all defect capture and rectification evidence photos for a specific quality issue.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<QualityEvidence>>> getEvidenceByQualityIssueId(@PathVariable Long id) {
        List<QualityEvidence> evidenceList = qualityService.getEvidenceByQualityIssueId(id);
        return ResponseEntity.ok(ApiResponse.success(evidenceList, "Quality evidence photos fetched successfully"));
    }

    @GetMapping("/summary/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Project Quality & NCR KPI Summary", description = "Returns aggregated quality metrics: total snags, open NCRs, pending verifications, closed counts, and severity metrics.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<QualitySummaryDto>> getQualitySummary(@PathVariable Long projectId) {
        QualitySummaryDto summary = qualityService.getQualitySummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Quality KPI summary fetched successfully"));
    }
}
