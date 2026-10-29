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
@Tag(name = "15. Executive Reports & Analytics Backend", description = "Consolidated Weekly Reports, Inventory Summary, Cost-Quality Dashboards & Composite Health Scores (Epic 7)")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Weekly Project Executive Report (Week 10 Day 1)", description = "Consolidates progress, labour, materials, procurement, cost/EVM and unresolved quality snags.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<WeeklyProjectReportDto>> getWeeklyProjectReport(
            @PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "10") Integer weekNumber) {
        WeeklyProjectReportDto report = reportService.generateWeeklyProjectReport(projectId, weekNumber);
        return ResponseEntity.ok(ApiResponse.success(report, "Weekly project report generated successfully"));
    }

    @GetMapping("/inventory/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Material & Inventory Report (Week 10 Day 2)", description = "Inventory valuation, total consumption cost, and itemized critical low stock list.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<InventoryReportDto>> getInventoryReport(@PathVariable Long projectId) {
        InventoryReportDto report = reportService.generateInventoryReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Material inventory report generated successfully"));
    }

    @GetMapping("/procurement-labour/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Procurement & Labour Unified Report (Week 10 Day 3)", description = "Combines vendor PO liabilities, GRN deliveries, contractor daily headcount, and wage accruals.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProcurementLabourReportDto>> getProcurementLabourReport(@PathVariable Long projectId) {
        ProcurementLabourReportDto report = reportService.generateProcurementLabourReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Procurement and labour report generated successfully"));
    }

    @GetMapping("/cost-quality/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Cost & Quality Control Unified Report (Week 10 Day 4)", description = "Cross-analyzes budget variance, EVM performance indices (CPI/SPI), active NCRs, and site delay days.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostQualityReportDto>> getCostQualityReport(@PathVariable Long projectId) {
        CostQualityReportDto report = reportService.generateCostQualityReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Cost and quality report generated successfully"));
    }

    @GetMapping("/analytics/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Generate Executive Analytics & Composite Health Score (Week 10 Day 5)", description = "Calculates automated 0-100 composite project health score across budget, schedule, inventory, and quality.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<AnalyticsAggregationDto>> getAnalyticsAggregation(@PathVariable Long projectId) {
        AnalyticsAggregationDto report = reportService.generateAnalyticsAggregation(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Project analytics aggregation generated successfully"));
    }
}
