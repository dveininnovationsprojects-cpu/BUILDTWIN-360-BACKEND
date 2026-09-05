package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.dpr.model.*;
import com.example.BuildTwin._0.domain.dpr.service.DprService;
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
@RequestMapping("/api/v1/dpr")
@RequiredArgsConstructor
@Tag(name = "10. Daily Progress Report (DPR)", description = "Daily Progress Reports, measured actual quantities, photos, and multi-stage review workflows (FR-030 - FR-035)")
public class DprController {

    private final DprService dprService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Create DPR Draft (FR-030, FR-031)", description = "Creates a site Daily Progress Report draft with weather, date, site, and submitted by remarks.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DprHeader>> createDpr(@Valid @RequestBody DprHeader dprHeader) {
        DprHeader created = dprService.createDpr(dprHeader);
        return new ResponseEntity<>(ApiResponse.created(created, "DPR draft created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get DPR Details", description = "Retrieves DPR header record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DprHeader>> getDprById(@PathVariable Long id) {
        DprHeader dpr = dprService.getDprById(id);
        return ResponseEntity.ok(ApiResponse.success(dpr, "DPR details fetched successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get DPR Logs By Project", description = "Retrieves all DPR records for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<DprHeader>>> getDprsByProject(@PathVariable Long projectId) {
        List<DprHeader> dprs = dprService.getDprsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(dprs, "Project DPR logs fetched successfully"));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Submit DPR for Approval (FR-033)", description = "Submits a DPR draft for site manager review.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DprHeader>> submitDpr(@PathVariable Long id) {
        DprHeader submitted = dprService.submitDpr(id);
        return ResponseEntity.ok(ApiResponse.success(submitted, "DPR submitted for approval successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Approve DPR (FR-033, FR-035)", description = "Approves a DPR and updates cumulative project progress.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DprHeader>> approveDpr(@PathVariable Long id) {
        DprHeader approved = dprService.approveDpr(id);
        return ResponseEntity.ok(ApiResponse.success(approved, "DPR approved successfully"));
    }

    @PostMapping("/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Record Activity Actual Quantity (FR-031, FR-032)", description = "Records actual work quantity completed for an activity in today's DPR.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DprActivityProgress>> addActivityProgress(@Valid @RequestBody DprActivityProgress progress) {
        DprActivityProgress saved = dprService.addActivityProgress(progress);
        return new ResponseEntity<>(ApiResponse.created(saved, "Activity progress recorded successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/photos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Upload Site Progress Photo Metadata (FR-031)", description = "Attaches site evidence photos tagged to project date and activity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DprPhoto>> addPhoto(@Valid @RequestBody DprPhoto photo) {
        DprPhoto saved = dprService.addPhoto(photo);
        return new ResponseEntity<>(ApiResponse.created(saved, "DPR photo recorded successfully"), HttpStatus.CREATED);
    }
}
