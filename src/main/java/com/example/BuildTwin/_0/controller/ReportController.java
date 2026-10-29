package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.reports.dto.*;
import com.example.BuildTwin._0.domain.reports.service.ReportService;
import com.example.BuildTwin._0.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "15. Executive Reports & Analytics Engine (Week 10)", description = "Weekly Project Executive Reports, Inventory Valuation, Procurement & Labour Reports, Cost & Quality Analysis, Analytics Aggregation APIs (Week 10)")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Weekly Project Comprehensive Report (Week 10 Day 1)", description = "Aggregates planned vs actual progress, labour hours, material consumption, PO liability, budget variance, EVM metrics, and open snags/blockers into an executive weekly briefing.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<WeeklyProjectReportDto>> getWeeklyProjectReport(
            @PathVariable Long projectId,
            @RequestParam(required = false) Integer weekNumber) {
        WeeklyProjectReportDto report = reportService.generateWeeklyProjectReport(projectId, weekNumber);
        return ResponseEntity.ok(ApiResponse.success(report, "Weekly project report generated successfully"));
    }

    @GetMapping("/inventory/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Material & Inventory Report (Week 10 Day 2)", description = "Provides inventory valuation, current stock levels, low-stock threshold deficits, and material consumption costs.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<InventoryReportDto>> getInventoryReport(@PathVariable Long projectId) {
        InventoryReportDto report = reportService.generateInventoryReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Material and inventory report generated successfully"));
    }

    @GetMapping("/procurement-labour/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Procurement & Labour Report (Week 10 Day 3)", description = "Aggregates open PO commitments, GRN accepted receipts, contractor gang headcount deployments, and standard/overtime wage costs.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProcurementLabourReportDto>> getProcurementLabourReport(@PathVariable Long projectId) {
        ProcurementLabourReportDto report = reportService.generateProcurementLabourReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Procurement and labour report generated successfully"));
    }

    @GetMapping("/cost-quality/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Cost & Quality Report (Week 10 Day 4)", description = "Combines Cost Breakdown Structure baseline vs actual variances, active 80%/90% overrun warnings, EVM CPI/SPI metrics, and quality snag/NCR/blocker resolution rates.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostQualityReportDto>> getCostQualityReport(@PathVariable Long projectId) {
        CostQualityReportDto report = reportService.generateCostQualityReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Cost and quality report generated successfully"));
    }

    @GetMapping("/analytics/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Executive Analytics Aggregation & Health Score (Week 10 Day 5)", description = "Calculates composite 0-100 project health score, overall status (OPTIMAL, GOOD, ATTENTION_REQUIRED, CRITICAL), and component health indicators.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<AnalyticsAggregationDto>> getAnalyticsAggregation(@PathVariable Long projectId) {
        AnalyticsAggregationDto report = reportService.generateAnalyticsAggregation(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Project analytics aggregation generated successfully"));
    }
}
