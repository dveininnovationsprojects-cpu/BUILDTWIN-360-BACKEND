package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.labour.dto.*;
import com.example.BuildTwin._0.domain.labour.model.LabourAllocation;
import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import com.example.BuildTwin._0.domain.labour.service.LabourService;
import com.example.BuildTwin._0.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/labour")
@RequiredArgsConstructor
@Tag(name = "3. Labour Management", description = "Daily site-wise, contractor-wise & trade-wise headcount, hours worked, productivity, and task allocations (FR-041)")
public class LabourController {

    private final LabourService labourService;

    @PostMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Record Daily Labour & Task Allocations (FR-041)", description = "Captures site-wise, contractor-wise, and trade-wise daily headcount, total hours worked, and activity allocations.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<LabourDaily>> recordDailyLabour(@Valid @RequestBody LabourDailyRecordDto request) {
        LabourDaily record = labourService.recordDailyLabour(request);
        return new ResponseEntity<>(ApiResponse.created(record, "Daily labour recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/daily/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Daily Labour Log By ID", description = "Retrieves specific daily labour record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<LabourDaily>> getLabourRecordById(@PathVariable Long id) {
        LabourDaily record = labourService.getLabourRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(record, "Daily labour log fetched successfully"));
    }

    @PutMapping("/daily/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Daily Labour Record", description = "Updates daily headcount, standard hours, overtime hours, and task allocations.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<LabourDaily>> updateDailyLabour(@PathVariable Long id, @Valid @RequestBody LabourDailyRecordDto request) {
        LabourDaily updated = labourService.updateDailyLabour(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Daily labour record updated successfully"));
    }

    @GetMapping("/daily/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Daily Labour Logs By Project", description = "Retrieves all daily labour records for a specific project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<LabourDaily>>> getLabourByProject(@PathVariable Long projectId) {
        List<LabourDaily> records = labourService.getLabourRecordsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(records, "Labour logs fetched successfully"));
    }

    @GetMapping("/daily/contractor/{contractorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Daily Labour Logs By Contractor", description = "Retrieves all daily labour records associated with a specific contractor.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<LabourDaily>>> getLabourByContractor(@PathVariable Long contractorId) {
        List<LabourDaily> records = labourService.getLabourRecordsByContractor(contractorId);
        return ResponseEntity.ok(ApiResponse.success(records, "Contractor labour logs fetched successfully"));
    }

    @GetMapping("/allocations/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Task Allocations By WBS Activity", description = "Retrieves daily labour hours and activity allocations for a specific WBS activity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<LabourAllocation>>> getLabourAllocationsByActivity(@PathVariable Long activityId) {
        List<LabourAllocation> allocations = labourService.getLabourAllocationsByActivity(activityId);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Activity labour allocations fetched successfully"));
    }

    @GetMapping("/hours-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Implement Labour Hour Tracking API", description = "Aggregates headcount, standard hours, and overtime hours across project, contractor, and date range.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<LabourHourSummaryDto>> getLabourHourSummary(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long contractorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LabourHourSummaryDto summary = labourService.getLabourHourSummary(projectId, contractorId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Labour hour summary fetched successfully"));
    }

    @GetMapping("/productivity/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Implement Productivity Calculation API", description = "Calculates productivity based on configured activity unit and labour hours for a WBS activity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProductivityResponseDto>> getProductivityForActivity(
            @PathVariable Long activityId,
            @RequestParam(required = false) BigDecimal completedQty) {
        ProductivityResponseDto result = labourService.calculateActivityProductivity(activityId, completedQty);
        return ResponseEntity.ok(ApiResponse.success(result, "Activity productivity calculated successfully"));
    }

    @PostMapping("/productivity/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "On-Demand Productivity Calculator API", description = "Calculates output per hour and man-hours per unit for given completed quantity and labour hours.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProductivityResponseDto>> calculateCustomProductivity(@Valid @RequestBody ProductivityRequestDto request) {
        ProductivityResponseDto result = labourService.calculateCustomProductivity(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Custom productivity calculated successfully"));
    }
}
