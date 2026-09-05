package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.cost.dto.BudgetRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.CostTransactionRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/cost")
@RequiredArgsConstructor
@Tag(name = "7. Cost Heads, Budget & EVM Analytics", description = "Cost Breakdown Structure (CBS), Baseline/Authorized Revisions, Actual Expenses & Earned Value Management (FR-070 - FR-073)")
public class CostController {

    private final CostService costService;

    @PostMapping("/budgets")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Set Baseline or Authorized Revised Budget (FR-070, FR-071)", description = "Standardized cost codes tied to project work packages with support for Original Baseline Budget and Authorized Revisions.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Budget>> createOrUpdateBudget(@Valid @RequestBody BudgetRequestDto request) {
        Budget budget = costService.createOrUpdateBudget(request);
        return new ResponseEntity<>(ApiResponse.created(budget, "Budget saved successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/budgets/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Budgets By Project", description = "Retrieves baseline and revised budgets for a given project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Budget>>> getBudgetsByProject(@PathVariable Long projectId) {
        List<Budget> budgets = costService.getBudgetsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(budgets, "Project budgets fetched successfully"));
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR')")
    @Operation(summary = "Record Cost Transaction (FR-072)", description = "Tracks PO commitments, actual invoice/GRN expenses, and subcontract payouts against budget cost codes.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<CostTransaction>> recordCostTransaction(@Valid @RequestBody CostTransactionRequestDto request) {
        CostTransaction transaction = costService.recordCostTransaction(request);
        return new ResponseEntity<>(ApiResponse.created(transaction, "Cost transaction recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/transactions/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Cost Transactions By Project", description = "Retrieves all cost commitment & actual expense transactions for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<CostTransaction>>> getCostTransactionsByProject(@PathVariable Long projectId) {
        List<CostTransaction> transactions = costService.getCostTransactionsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Cost transactions fetched successfully"));
    }

    @GetMapping("/evm/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'QUANTITY_COST_COORDINATOR', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Earned Value Management (EVM) Analytics (FR-073)", description = "Calculates Planned Value (PV), Earned Value (EV), Actual Cost (AC), Schedule Performance Index (SPI), and Cost Performance Index (CPI).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<EvmMetricsDto>> getEvmMetrics(@PathVariable Long projectId) {
        EvmMetricsDto metrics = costService.calculateEvmMetrics(projectId);
        return ResponseEntity.ok(ApiResponse.success(metrics, "EVM analytics computed successfully"));
    }
}
