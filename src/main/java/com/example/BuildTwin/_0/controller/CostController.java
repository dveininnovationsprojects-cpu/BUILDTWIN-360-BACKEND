package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostActualLedger;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import com.example.BuildTwin._0.domain.cost.model.ProjectBudget;
import com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord;
import com.example.BuildTwin._0.domain.cost.service.CostService;
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

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@RestController
@RequestMapping({"/api/v1/costs", "/api/v1/cost"})
@RequiredArgsConstructor
@Tag(name = "7. Cost Heads, Budget & EVM Analytics", description = "Cost Breakdown Structure (CBS), Baseline/Authorized Revisions, Material & Labour Cost Tracking, Procurement Commitments, Variance Analysis & Earned Value Management (FR-070 - FR-073)")
public class CostController {

    private final CostService costService;

    // ==========================================
    // Day 1: Project Cost Structure Setup (CBS)
    // ==========================================

    @PostMapping("/budgets")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Set Project Baseline Budget (FR-070)", description = "Sets baseline budget heads tied to project and WBS activities (Direct Cost, Indirect Cost, Contingency, Material, Labour, etc.).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Budget>> createOrUpdateBudget(@Valid @RequestBody BudgetRequestDto request) {
        Budget budget = costService.createOrUpdateBudget(request);
        return new ResponseEntity<>(ApiResponse.created(budget, "Budget saved successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/budgets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Update / Revise Budget Allocation (FR-071)", description = "Updates budget allocation or records authorized revision for a specific cost head.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Budget>> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetRequestDto request) {
        Budget updated = costService.updateBudget(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Budget updated successfully"));
    }

    @PutMapping("/budgets/{id}/revision")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Authorize Budget Revision with Justification (FR-071)", description = "Records authorized budget revision amount along with audit reason and approving officer.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Budget>> authorizeBudgetRevision(@PathVariable Long id, @Valid @RequestBody BudgetRevisionDto revisionDto) {
        Budget revised = costService.authorizeBudgetRevision(id, revisionDto);
        return ResponseEntity.ok(ApiResponse.success(revised, "Budget revision authorized successfully"));
    }

    @GetMapping("/budgets")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Budgets", description = "Retrieves all baseline and revised budgets across projects or filtered by optional projectId query parameter.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Budget>>> getAllBudgets(@RequestParam(required = false) Long projectId) {
        List<Budget> budgets = projectId != null
                ? costService.getBudgetsByProject(projectId)
                : costService.getAllBudgets();
        return ResponseEntity.ok(ApiResponse.success(budgets, "Budgets fetched successfully"));
    }

    @GetMapping("/budgets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Budget By ID", description = "Retrieves budget details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Budget>> getBudgetById(@PathVariable Long id) {
        Budget budget = costService.getBudgetById(id);
        return ResponseEntity.ok(ApiResponse.success(budget, "Budget details fetched successfully"));
    }

    @GetMapping("/budgets/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Project Budget Breakdown", description = "Retrieves baseline and revised budget breakdown for a given project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Budget>>> getBudgetsByProject(@PathVariable Long projectId) {
        List<Budget> budgets = costService.getBudgetsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(budgets, "Project budgets fetched successfully"));
    }

    @GetMapping("/budgets/project/{projectId}/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get WBS Activity Budget Breakdown", description = "Retrieves budget breakdown allocated directly to a specific WBS activity (e.g. Slab Casting Material ₹5L, Labour ₹2L).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Budget>>> getBudgetsByProjectAndActivity(
            @PathVariable Long projectId,
            @PathVariable Long activityId) {
        List<Budget> budgets = costService.getBudgetsByProjectAndActivity(projectId, activityId);
        return ResponseEntity.ok(ApiResponse.success(budgets, "WBS activity budget breakdown fetched successfully"));
    }

    @DeleteMapping("/budgets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Delete Budget Head", description = "Deletes budget record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        costService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }

    // ==========================================
    // Day 2: Material Cost + Labour Cost Tracking
    // ==========================================

    @GetMapping("/actual/material/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Project Material Actual Cost Summary", description = "Calculates actual material cost from Stock Ledger CONSUMPTION transactions (Qty × Unit Rate).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialActualCostSummaryDto>> getMaterialActualCostSummary(@PathVariable Long projectId) {
        MaterialActualCostSummaryDto summary = costService.getMaterialActualCostSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Project material actual cost summary fetched successfully"));
    }

    @GetMapping("/actual/labour/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Labour Man-Hours Cost Breakdown", description = "Calculates actual labour expense from daily muster records and allocations (man-hours × hourly wage rates).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<LabourActualCostSummaryDto>> getLabourActualCostSummary(@PathVariable Long projectId) {
        LabourActualCostSummaryDto summary = costService.getLabourActualCostSummary(projectId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Labour actual cost breakdown fetched successfully"));
    }

    @PostMapping({"/actual/sync", "/actuals/sync"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Sync Stock & Labour Consumption to Cost Ledger", description = "Synchronizes ground material consumptions and daily labour records into the central cost transactions ledger.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncActualCosts(@RequestParam(required = false, defaultValue = "1") Long projectId) {
        CostSyncResultDto result = costService.syncActualCosts(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync/{projectId}", "/actuals/sync/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Sync Stock & Labour Consumption to Cost Ledger By Project Path", description = "Synchronizes ground material consumptions and daily labour records into the central cost transactions ledger.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncActualCostsByPath(@PathVariable Long projectId) {
        CostSyncResultDto result = costService.syncActualCosts(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync-material", "/actuals/sync-material"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Sync Material Stock Consumption to Cost Ledger (FR-072)", description = "Pulls Stock Ledger consumption entries and writes them into Cost Actual Ledger.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncMaterialCost(@RequestParam(required = false, defaultValue = "1") Long projectId) {
        CostSyncResultDto result = costService.syncMaterialCost(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync-material/{projectId}", "/actuals/sync-material/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Sync Material Stock Consumption to Cost Ledger By Project Path", description = "Pulls Stock Ledger consumption entries and writes them into Cost Actual Ledger.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncMaterialCostByPath(@PathVariable Long projectId) {
        CostSyncResultDto result = costService.syncMaterialCost(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync-labour", "/actuals/sync-labour"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Sync Contractor Labour Timesheet to Cost Ledger (FR-072)", description = "Syncs daily muster attendance and overtime hours into Cost Actual Ledger.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncLabourCost(@RequestParam(required = false, defaultValue = "1") Long projectId) {
        CostSyncResultDto result = costService.syncLabourCost(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync-labour/{projectId}", "/actuals/sync-labour/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Sync Contractor Labour Timesheet to Cost Ledger By Project Path", description = "Syncs daily muster attendance and overtime hours into Cost Actual Ledger.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncLabourCostByPath(@PathVariable Long projectId) {
        CostSyncResultDto result = costService.syncLabourCost(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync-equipment", "/actuals/sync-equipment", "/ledger/sync/equipment"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE', 'SITE_ENGINEER')")
    @Operation(summary = "Sync Equipment Fleet Usage to Cost Ledger", description = "Syncs daily equipment usage hours into Cost Actual Ledger with costCode=EQUIPMENT.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncEquipmentCost(@RequestParam(required = false, defaultValue = "1") Long projectId) {
        CostSyncResultDto result = costService.syncEquipmentCost(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping({"/actual/sync-equipment/{projectId}", "/actuals/sync-equipment/{projectId}", "/ledger/sync/equipment/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE', 'SITE_ENGINEER')")
    @Operation(summary = "Sync Equipment Fleet Usage to Cost Ledger By Path", description = "Syncs daily equipment usage hours into Cost Actual Ledger with costCode=EQUIPMENT.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostSyncResultDto>> syncEquipmentCostByPath(@PathVariable Long projectId) {
        CostSyncResultDto result = costService.syncEquipmentCost(projectId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }


    @GetMapping({"/tracking/project/{projectId}", "/actuals/project/{projectId}/breakdown"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material & Labour Cost Tracking Report", description = "Itemized breakdown across Material, Labour, Equipment, Subcontractor, and Overhead cost heads with utilization metrics.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectCostTrackingReportDto>> getProjectCostTracking(@PathVariable Long projectId) {
        ProjectCostTrackingReportDto report = costService.getProjectCostTracking(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Project cost tracking report fetched successfully"));
    }

    // ==========================================
    // Day 3: Procurement Commitments & Costing
    // ==========================================

    @GetMapping({"/commitments/project/{projectId}", "/procurement/commitments/project/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Committed PO Liability Report", description = "Tracks open Purchase Order liabilities (ISSUED status) where financial commitment is locked but cash is not yet disbursed.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProcurementCommitmentDto>> getProcurementCommitments(@PathVariable Long projectId) {
        ProcurementCommitmentDto commitments = costService.getProcurementCommitments(projectId);
        return ResponseEntity.ok(ApiResponse.success(commitments, "Procurement commitments fetched successfully"));
    }

    @GetMapping({"/procurement/summary/{projectId}", "/procurement/liability-gap/project/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Procurement Total Expenditure Trail & Gap", description = "Committed Cost (issued POs) vs Incurred Cost (GRN accepted receipts) and outstanding liability.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProcurementExpenditureTrailDto>> getProcurementExpenditureTrail(@PathVariable Long projectId) {
        ProcurementExpenditureTrailDto trail = costService.getProcurementExpenditureTrail(projectId);
        return ResponseEntity.ok(ApiResponse.success(trail, "Procurement expenditure trail fetched successfully"));
    }

    // ==========================================
    // Day 4: Baseline Budget vs Actual Expense
    // ==========================================

    @GetMapping({"/variance/project/{projectId}", "/variance/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Budget vs Actual Variance Matrix", description = "Cost Variance (CV = Allocated Budget - Actual Expense) with 90% threshold alert tracking.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<BudgetVarianceReportDto>> getBudgetVarianceReport(@PathVariable Long projectId) {
        BudgetVarianceReportDto varianceReport = costService.getBudgetVarianceReport(projectId);
        return ResponseEntity.ok(ApiResponse.success(varianceReport, "Budget variance report fetched successfully"));
    }

    @GetMapping({"/overruns/project/{projectId}", "/alerts/project/{projectId}", "/overruns/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Budget Limit Exceeded Activities List", description = "Retrieves WBS activities and Cost Codes that have exceeded or crossed 90% of allocated budget with severity tags.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<BudgetOverrunReportDto>> getBudgetOverruns(@PathVariable Long projectId) {
        BudgetOverrunReportDto overruns = costService.getBudgetOverruns(projectId);
        return ResponseEntity.ok(ApiResponse.success(overruns, "Budget overrun report fetched successfully"));
    }

    @PostMapping("/overruns/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Trigger Daily Budget Overrun Check Job (FR-072)", description = "Executes the daily automated scan checking Allocated Budget - (Actual Cost + Committed Cost) across active projects.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<String>> triggerDailyBudgetOverrunCheck() {
        costService.runDailyBudgetOverrunCheck();
        return ResponseEntity.ok(ApiResponse.success("Daily automated budget overrun check executed successfully", "Budget overrun check completed"));
    }

    // ==========================================
    // Day 5: EVM Backend Support + Forecast
    // ==========================================

    @PostMapping({"/evm/calculate/project/{projectId}", "/evm/snapshot/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Generate On-Demand EVM Snapshot (FR-073)", description = "Generates and persists a historical EVM snapshot record in project_evm_records table.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectEVMRecord>> calculateEvmSnapshot(
            @PathVariable Long projectId,
            @RequestParam(required = false) Double plannedProgress,
            @RequestParam(required = false) Double actualProgress) {
        ProjectEVMRecord snapshot = costService.calculateAndSaveEvmSnapshot(projectId, plannedProgress, actualProgress);
        return ResponseEntity.ok(ApiResponse.success(snapshot, "EVM snapshot generated and recorded successfully"));
    }

    @GetMapping({"/evm/project/{projectId}", "/evm/dashboard/project/{projectId}", "/evm/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Live EVM KPIs & Dashboard (FR-073)", description = "Calculates Planned Value (PV), Earned Value (EV), Actual Cost (AC), CPI, and SPI with dynamic schedule parameters.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<EvmMetricsDto>> getEvmMetrics(
            @PathVariable Long projectId,
            @RequestParam(required = false) Double plannedProgress,
            @RequestParam(required = false) Double actualProgress) {
        EvmMetricsDto metrics = costService.calculateEvmMetricsWithProgress(projectId, plannedProgress, actualProgress);
        return ResponseEntity.ok(ApiResponse.success(metrics, "EVM KPIs calculated successfully"));
    }

    @GetMapping({"/evm/trends/project/{projectId}", "/evm/trends/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Historical Week-over-Week EVM Trends (FR-073)", description = "Retrieves chronological historical EVM performance records from project_evm_records table with optional startDate and endDate filters.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectEVMRecord>>> getEvmTrends(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ProjectEVMRecord> trends = (startDate != null && endDate != null)
                ? costService.getEvmTrends(projectId, startDate, endDate)
                : costService.getEvmTrends(projectId);
        return ResponseEntity.ok(ApiResponse.success(trends, "Historical EVM trends fetched successfully"));
    }

    @GetMapping({"/evm/forecast/{projectId}", "/evm/{projectId}/forecast"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Estimate At Completion (EAC) Auto Calculation", description = "Calculates project EAC (BAC / CPI), ETC, VAC, and provides predictive budget forecasts.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<EvmForecastDto>> getEvmForecast(@PathVariable Long projectId) {
        EvmForecastDto forecast = costService.getEvmForecast(projectId);
        return ResponseEntity.ok(ApiResponse.success(forecast, "EVM forecast computed successfully"));
    }

    // ==========================================
    // Direct Cost Transactions
    // ==========================================

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'PROCUREMENT_STORE', 'SITE_ENGINEER')")
    @Operation(summary = "Record Cost Transaction (FR-072)", description = "Tracks PO commitments, actual invoices, labor wages, equipment hire, and subcontract payouts.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostTransaction>> recordCostTransaction(@Valid @RequestBody CostTransactionRequestDto request) {
        CostTransaction transaction = costService.recordCostTransaction(request);
        return new ResponseEntity<>(ApiResponse.created(transaction, "Cost transaction recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Cost Transactions", description = "Retrieves all cost transactions across projects or filtered by optional projectId query parameter.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<CostTransaction>>> getAllCostTransactions(@RequestParam(required = false) Long projectId) {
        List<CostTransaction> transactions = projectId != null
                ? costService.getCostTransactionsByProject(projectId)
                : costService.getAllCostTransactions();
        return ResponseEntity.ok(ApiResponse.success(transactions, "Cost transactions fetched successfully"));
    }

    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Cost Transaction By ID", description = "Retrieves specific cost transaction details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostTransaction>> getCostTransactionById(@PathVariable Long id) {
        CostTransaction transaction = costService.getCostTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Cost transaction fetched successfully"));
    }

    @GetMapping("/transactions/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Cost Transactions By Project", description = "Retrieves all cost transactions for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<CostTransaction>>> getCostTransactionsByProject(@PathVariable Long projectId) {
        List<CostTransaction> transactions = costService.getCostTransactionsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Cost transactions fetched successfully"));
    }

    @PutMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Update Cost Transaction (FR-072)", description = "Updates cost transaction details (amount, reference, payee, or remarks).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostTransaction>> updateCostTransaction(@PathVariable Long id, @Valid @RequestBody CostTransactionRequestDto request) {
        CostTransaction updated = costService.updateCostTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cost transaction updated successfully"));
    }

    @DeleteMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Delete Cost Transaction", description = "Deletes a cost transaction record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteCostTransaction(@PathVariable Long id) {
        costService.deleteCostTransaction(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cost transaction deleted successfully"));
    }

    @GetMapping("/actuals/ledger/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Raw Cost Actual Ledger Entries (FR-072)", description = "Retrieves itemized entries synced into Cost Actual Ledger for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<CostActualLedger>>> getCostActualLedgersByProject(@PathVariable Long projectId) {
        List<CostActualLedger> ledgers = costService.getCostActualLedgersByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(ledgers, "Cost actual ledger entries fetched successfully"));
    }

    @DeleteMapping("/actuals/ledger/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Delete Cost Actual Ledger Entry", description = "Deletes an itemized cost actual ledger entry by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteCostActualLedger(@PathVariable Long id) {
        costService.deleteCostActualLedger(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cost actual ledger entry deleted successfully"));
    }

    @GetMapping("/evm/records/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get EVM Historical Record By ID", description = "Retrieves specific EVM historical performance record details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectEVMRecord>> getEvmRecordById(@PathVariable Long id) {
        ProjectEVMRecord record = costService.getEvmRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(record, "EVM record fetched successfully"));
    }

    @DeleteMapping("/evm/records/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Delete EVM Historical Record", description = "Deletes an EVM historical snapshot record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteEvmRecord(@PathVariable Long id) {
        costService.deleteEvmRecord(id);
        return ResponseEntity.ok(ApiResponse.success(null, "EVM historical record deleted successfully"));
    }

    @GetMapping("/project-budgets/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Mirrored Project Budgets By Project", description = "Retrieves mirrored ProjectBudget entities by project ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectBudget>>> getProjectBudgetsByProject(@PathVariable Long projectId) {
        List<ProjectBudget> pbs = costService.getProjectBudgetsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(pbs, "Project budgets fetched successfully"));
    }
}
