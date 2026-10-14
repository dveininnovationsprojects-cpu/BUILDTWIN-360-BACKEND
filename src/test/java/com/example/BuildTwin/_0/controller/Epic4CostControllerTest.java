package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostActualLedger;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import com.example.BuildTwin._0.domain.cost.model.ProjectBudget;
import com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CostController.class)
@AutoConfigureMockMvc(addFilters = false)
class Epic4CostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CostService costService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/cost/budgets - Set Baseline Budget")
    void testCreateBudget() throws Exception {
        BudgetRequestDto dto = BudgetRequestDto.builder()
                .projectId(100L)
                .costCode("CIV_RCC")
                .costHead(CostHead.MATERIAL)
                .baselineAmount(new BigDecimal("750000.00"))
                .build();

        Budget saved = Budget.builder()
                .id(1L)
                .projectId(100L)
                .costCode("CIV_RCC")
                .costHead(CostHead.MATERIAL)
                .baselineAmount(new BigDecimal("750000.00"))
                .build();

        when(costService.createOrUpdateBudget(any(BudgetRequestDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/cost/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.costCode").value("CIV_RCC"));
    }

    @Test
    @DisplayName("PUT /api/v1/cost/budgets/{id}/revision - Authorize Budget Revision")
    void testAuthorizeBudgetRevision() throws Exception {
        BudgetRevisionDto revisionDto = BudgetRevisionDto.builder()
                .revisedAmount(new BigDecimal("900000.00"))
                .revisionReason("Scope increase approved per change order #12")
                .approvedBy("PM_Selvam")
                .build();

        Budget updated = Budget.builder()
                .id(1L)
                .projectId(100L)
                .costCode("CIV_RCC")
                .revisedAmount(new BigDecimal("900000.00"))
                .revisionReason("Scope increase approved per change order #12")
                .status("APPROVED_REVISED")
                .build();

        when(costService.authorizeBudgetRevision(eq(1L), any(BudgetRevisionDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/cost/budgets/1/revision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revisionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revisedAmount").value(900000.00))
                .andExpect(jsonPath("$.data.status").value("APPROVED_REVISED"));
    }

    @Test
    @DisplayName("GET /api/v1/cost/budgets/project/{projectId} - Get project budgets")
    void testGetBudgetsByProject() throws Exception {
        Budget b = Budget.builder().id(1L).projectId(100L).costCode("CIV_01").baselineAmount(new BigDecimal("500000.00")).build();
        when(costService.getBudgetsByProject(100L)).thenReturn(List.of(b));

        mockMvc.perform(get("/api/v1/cost/budgets/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].costCode").value("CIV_01"));
    }

    @Test
    @DisplayName("POST /api/v1/cost/transactions - Record Cost Transaction")
    void testRecordTransaction() throws Exception {
        CostTransactionRequestDto dto = CostTransactionRequestDto.builder()
                .projectId(100L)
                .costCode("LAB_01")
                .costHead(CostHead.LABOUR)
                .sourceType(CostSourceType.LABOUR_WAGES)
                .amount(new BigDecimal("50000.00"))
                .transactionDate(LocalDate.now())
                .referenceNumber("MUSTER-001")
                .build();

        CostTransaction saved = CostTransaction.builder()
                .id(10L)
                .projectId(100L)
                .costCode("LAB_01")
                .amount(new BigDecimal("50000.00"))
                .build();

        when(costService.recordCostTransaction(any(CostTransactionRequestDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/cost/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/cost/tracking/project/{projectId} - Material & Labour Cost Tracking")
    void testGetCostTracking() throws Exception {
        ProjectCostTrackingReportDto report = ProjectCostTrackingReportDto.builder()
                .projectId(100L)
                .totalApprovedBudget(new BigDecimal("1000000.00"))
                .totalActualExpense(new BigDecimal("450000.00"))
                .overallStatus("UNDER_BUDGET")
                .costHeadBreakdown(List.of(
                        CostHeadTrackingDto.builder().costHead(CostHead.MATERIAL).actualIncurred(new BigDecimal("250000.00")).build(),
                        CostHeadTrackingDto.builder().costHead(CostHead.LABOUR).actualIncurred(new BigDecimal("200000.00")).build()
                ))
                .build();

        when(costService.getProjectCostTracking(100L)).thenReturn(report);

        mockMvc.perform(get("/api/v1/cost/tracking/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalApprovedBudget").value(1000000.00))
                .andExpect(jsonPath("$.data.overallStatus").value("UNDER_BUDGET"))
                .andExpect(jsonPath("$.data.costHeadBreakdown[0].costHead").value("MATERIAL"));
    }

    @Test
    @DisplayName("GET /api/v1/cost/commitments/project/{projectId} - Procurement Commitments")
    void testGetProcurementCommitments() throws Exception {
        ProcurementCommitmentDto commitments = ProcurementCommitmentDto.builder()
                .projectId(100L)
                .totalCommittedAmount(new BigDecimal("300000.00"))
                .openOrdersCount(2)
                .build();

        when(costService.getProcurementCommitments(100L)).thenReturn(commitments);

        mockMvc.perform(get("/api/v1/cost/commitments/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCommittedAmount").value(300000.00))
                .andExpect(jsonPath("$.data.openOrdersCount").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/cost/variance/project/{projectId} - Budget Variance Report")
    void testGetBudgetVarianceReport() throws Exception {
        BudgetVarianceReportDto varianceReport = BudgetVarianceReportDto.builder()
                .projectId(100L)
                .projectName("Skyline Tower")
                .totalRevisedBudget(new BigDecimal("1500000.00"))
                .totalActualExpense(new BigDecimal("900000.00"))
                .netVariance(new BigDecimal("600000.00"))
                .overallStatus("UNDER_BUDGET")
                .build();

        when(costService.getBudgetVarianceReport(100L)).thenReturn(varianceReport);

        mockMvc.perform(get("/api/v1/cost/variance/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectName").value("Skyline Tower"))
                .andExpect(jsonPath("$.data.overallStatus").value("UNDER_BUDGET"));
    }

    @Test
    @DisplayName("GET /api/v1/cost/evm/project/{projectId} - EVM Analytics")
    void testGetEvmMetrics() throws Exception {
        EvmMetricsDto evm = EvmMetricsDto.builder()
                .projectId(100L)
                .budgetAtCompletion(new BigDecimal("1000000.00"))
                .plannedValue(new BigDecimal("500000.00"))
                .earnedValue(new BigDecimal("550000.00"))
                .actualCost(new BigDecimal("480000.00"))
                .spiStatus("ON_SCHEDULE")
                .cpiStatus("UNDER_BUDGET")
                .build();

        when(costService.calculateEvmMetricsWithProgress(eq(100L), any(), any())).thenReturn(evm);

        mockMvc.perform(get("/api/v1/cost/evm/project/100?plannedProgress=50&actualProgress=55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.spiStatus").value("ON_SCHEDULE"))
                .andExpect(jsonPath("$.data.cpiStatus").value("UNDER_BUDGET"));
    }

    @Test
    @DisplayName("GET /api/v1/costs/actual/material/project/{projectId} - Material Actual Cost Summary")
    void testGetMaterialActualCostSummary() throws Exception {
        MaterialActualCostSummaryDto dto = MaterialActualCostSummaryDto.builder()
                .projectId(100L)
                .totalMaterialCost(new BigDecimal("120000.00"))
                .totalQuantityConsumed(new BigDecimal("500.00"))
                .entriesCount(3)
                .build();

        when(costService.getMaterialActualCostSummary(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/costs/actual/material/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMaterialCost").value(120000.00));
    }

    @Test
    @DisplayName("GET /api/v1/costs/actual/labour/project/{projectId} - Labour Man-Hours Cost Breakdown")
    void testGetLabourActualCostSummary() throws Exception {
        LabourActualCostSummaryDto dto = LabourActualCostSummaryDto.builder()
                .projectId(100L)
                .totalLabourCost(new BigDecimal("85000.00"))
                .totalManHours(new BigDecimal("400.00"))
                .build();

        when(costService.getLabourActualCostSummary(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/costs/actual/labour/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalLabourCost").value(85000.00));
    }

    @Test
    @DisplayName("POST /api/v1/costs/actual/sync - Sync Stock & Labour Consumption to Cost Ledger")
    void testSyncActualCosts() throws Exception {
        CostSyncResultDto dto = CostSyncResultDto.builder()
                .projectId(100L)
                .materialTransactionsSynced(5)
                .labourTransactionsSynced(3)
                .totalSyncedAmount(new BigDecimal("150000.00"))
                .message("Sync successful")
                .build();

        when(costService.syncActualCosts(100L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/costs/actual/sync?projectId=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.materialTransactionsSynced").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/costs/procurement/summary/{projectId} - Procurement Total Expenditure Trail")
    void testGetProcurementExpenditureTrail() throws Exception {
        ProcurementExpenditureTrailDto dto = ProcurementExpenditureTrailDto.builder()
                .projectId(100L)
                .committedCost(new BigDecimal("250000.00"))
                .incurredCost(new BigDecimal("180000.00"))
                .outstandingLiability(new BigDecimal("70000.00"))
                .build();

        when(costService.getProcurementExpenditureTrail(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/costs/procurement/summary/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.committedCost").value(250000.00))
                .andExpect(jsonPath("$.data.incurredCost").value(180000.00));
    }

    @Test
    @DisplayName("GET /api/v1/costs/overruns/project/{projectId} - Budget Limit Exceeded WBS Activities")
    void testGetBudgetOverruns() throws Exception {
        BudgetOverrunReportDto dto = BudgetOverrunReportDto.builder()
                .projectId(100L)
                .overrunCount(1)
                .totalOverrunAmount(new BigDecimal("25000.00"))
                .overruns(List.of(
                        BudgetOverrunReportDto.BudgetOverrunItemDto.builder()
                                .costCode("CIV_01")
                                .alertSeverity("CRITICAL_OVERRUN")
                                .alertMessage("Budget exceeded")
                                .build()
                ))
                .build();

        when(costService.getBudgetOverruns(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/costs/overruns/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overrunCount").value(1))
                .andExpect(jsonPath("$.data.overruns[0].alertSeverity").value("CRITICAL_OVERRUN"));
    }

    @Test
    @DisplayName("GET /api/v1/costs/evm/forecast/{projectId} - Estimate at Completion (EAC) Forecast")
    void testGetEvmForecast() throws Exception {
        EvmForecastDto dto = EvmForecastDto.builder()
                .projectId(100L)
                .budgetAtCompletion(new BigDecimal("1000000.00"))
                .estimateAtCompletion(new BigDecimal("1050000.00"))
                .costPerformanceIndex(new BigDecimal("0.9524"))
                .forecastSummary("Forecast summary test")
                .build();

        when(costService.getEvmForecast(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/costs/evm/forecast/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.budgetAtCompletion").value(1000000.00))
                .andExpect(jsonPath("$.data.estimateAtCompletion").value(1050000.00));
    }

    @Test
    @DisplayName("GET /api/v1/costs/budgets/project/{projectId}/activity/{activityId} - WBS Activity Budget Breakdown")
    void testGetBudgetsByProjectAndActivity() throws Exception {
        Budget b = Budget.builder()
                .id(1L)
                .projectId(100L)
                .activityId(50L)
                .costCode("SLAB_CONCRETE")
                .costHead(CostHead.MATERIAL)
                .baselineAmount(new BigDecimal("500000.00"))
                .build();

        when(costService.getBudgetsByProjectAndActivity(100L, 50L)).thenReturn(List.of(b));

        mockMvc.perform(get("/api/v1/costs/budgets/project/100/activity/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].costCode").value("SLAB_CONCRETE"))
                .andExpect(jsonPath("$.data[0].baselineAmount").value(500000.00));
    }

    @Test
    @DisplayName("POST /api/v1/costs/overruns/check - Trigger Daily Budget Overrun Check Job")
    void testTriggerDailyBudgetOverrunCheck() throws Exception {
        doNothing().when(costService).runDailyBudgetOverrunCheck();

        mockMvc.perform(post("/api/v1/costs/overruns/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Daily automated budget overrun check executed successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/costs/actuals/sync-material - Sync Material Stock Consumption")
    void testSyncMaterialCost() throws Exception {
        CostSyncResultDto result = CostSyncResultDto.builder()
                .projectId(100L)
                .materialTransactionsSynced(4)
                .labourTransactionsSynced(0)
                .totalSyncedAmount(new BigDecimal("120000.00"))
                .message("Material consumption successfully synchronized into Cost Actual Ledger")
                .build();

        when(costService.syncMaterialCost(100L)).thenReturn(result);

        mockMvc.perform(post("/api/v1/costs/actuals/sync-material?projectId=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.materialTransactionsSynced").value(4));
    }

    @Test
    @DisplayName("POST /api/v1/costs/actuals/sync-labour - Sync Contractor Labour Timesheet")
    void testSyncLabourCost() throws Exception {
        CostSyncResultDto result = CostSyncResultDto.builder()
                .projectId(100L)
                .materialTransactionsSynced(0)
                .labourTransactionsSynced(6)
                .totalSyncedAmount(new BigDecimal("95000.00"))
                .message("Contractor labour timesheet attendance successfully synchronized into Cost Actual Ledger")
                .build();

        when(costService.syncLabourCost(100L)).thenReturn(result);

        mockMvc.perform(post("/api/v1/costs/actuals/sync-labour?projectId=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.labourTransactionsSynced").value(6));
    }

    @Test
    @DisplayName("GET /api/v1/costs/actuals/project/{projectId}/breakdown - Itemized Cost Breakdown")
    void testGetActualSpendBreakdown() throws Exception {
        ProjectCostTrackingReportDto report = ProjectCostTrackingReportDto.builder()
                .projectId(100L)
                .totalApprovedBudget(new BigDecimal("1000000.00"))
                .totalActualExpense(new BigDecimal("450000.00"))
                .overallStatus("UNDER_BUDGET")
                .build();

        when(costService.getProjectCostTracking(100L)).thenReturn(report);

        mockMvc.perform(get("/api/v1/costs/actuals/project/100/breakdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalApprovedBudget").value(1000000.00));
    }

    @Test
    @DisplayName("GET /api/v1/costs/procurement/liability-gap/project/{projectId} - Procurement Liability Gap")
    void testGetProcurementLiabilityGap() throws Exception {
        ProcurementExpenditureTrailDto dto = ProcurementExpenditureTrailDto.builder()
                .projectId(100L)
                .committedCost(new BigDecimal("250000.00"))
                .incurredCost(new BigDecimal("180000.00"))
                .outstandingLiability(new BigDecimal("70000.00"))
                .build();

        when(costService.getProcurementExpenditureTrail(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/costs/procurement/liability-gap/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.outstandingLiability").value(70000.00));
    }

    @Test
    @DisplayName("POST /api/v1/costs/evm/calculate/project/{projectId} - Generate EVM Snapshot")
    void testCalculateEvmSnapshot() throws Exception {
        ProjectEVMRecord record = ProjectEVMRecord.builder()
                .id(1L)
                .projectId(100L)
                .evaluationDate(LocalDate.now())
                .plannedValue(new BigDecimal("500000.00"))
                .earnedValue(new BigDecimal("550000.00"))
                .actualCost(new BigDecimal("480000.00"))
                .budgetAtCompletion(new BigDecimal("1000000.00"))
                .cpi(new BigDecimal("1.146"))
                .spi(new BigDecimal("1.100"))
                .estimateAtCompletion(new BigDecimal("872600.35"))
                .costStatus("UNDER_BUDGET")
                .scheduleStatus("AHEAD_OF_SCHEDULE")
                .build();

        when(costService.calculateAndSaveEvmSnapshot(eq(100L), any(), any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/costs/evm/calculate/project/100?plannedProgress=50&actualProgress=55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.costStatus").value("UNDER_BUDGET"));
    }

    @Test
    @DisplayName("GET /api/v1/costs/evm/trends/project/{projectId} - Historical EVM Trends")
    void testGetEvmTrends() throws Exception {
        ProjectEVMRecord record = ProjectEVMRecord.builder()
                .id(1L)
                .projectId(100L)
                .evaluationDate(LocalDate.now())
                .cpi(new BigDecimal("1.05"))
                .spi(new BigDecimal("1.02"))
                .build();

        when(costService.getEvmTrends(100L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/costs/evm/trends/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].cpi").value(1.05));
    }

    @Test
    @DisplayName("GET /api/v1/costs/evm/dashboard/project/{projectId} - EVM Dashboard")
    void testGetEvmDashboard() throws Exception {
        EvmMetricsDto evm = EvmMetricsDto.builder()
                .projectId(100L)
                .budgetAtCompletion(new BigDecimal("1000000.00"))
                .spiStatus("ON_SCHEDULE")
                .cpiStatus("UNDER_BUDGET")
                .build();

        when(costService.calculateEvmMetricsWithProgress(eq(100L), any(), any())).thenReturn(evm);

        mockMvc.perform(get("/api/v1/costs/evm/dashboard/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.spiStatus").value("ON_SCHEDULE"));
    }

    @Test
    @DisplayName("PUT /api/v1/costs/transactions/10 - Update Cost Transaction")
    void testUpdateCostTransaction() throws Exception {
        CostTransactionRequestDto dto = CostTransactionRequestDto.builder()
                .projectId(100L)
                .costCode("LAB_01")
                .costHead(CostHead.LABOUR)
                .sourceType(CostSourceType.LABOUR_WAGES)
                .amount(new BigDecimal("60000.00"))
                .transactionDate(LocalDate.now())
                .referenceNumber("MUSTER-002")
                .build();

        CostTransaction updated = CostTransaction.builder()
                .id(10L)
                .projectId(100L)
                .amount(new BigDecimal("60000.00"))
                .build();

        when(costService.updateCostTransaction(eq(10L), any(CostTransactionRequestDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/costs/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(60000.00));
    }

    @Test
    @DisplayName("GET /api/v1/costs/actuals/ledger/project/100 - Get Raw Cost Actual Ledger Entries")
    void testGetCostActualLedgersByProject() throws Exception {
        CostActualLedger ledger = CostActualLedger.builder()
                .id(1L)
                .projectId(100L)
                .sourceType("STOCK_CONSUMPTION")
                .costCode("MATERIAL")
                .incurredQty(new BigDecimal("10.00"))
                .unitRate(new BigDecimal("500.00"))
                .totalCost(new BigDecimal("5000.00"))
                .expenseDate(LocalDate.now())
                .build();

        when(costService.getCostActualLedgersByProject(100L)).thenReturn(List.of(ledger));

        mockMvc.perform(get("/api/v1/costs/actuals/ledger/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].totalCost").value(5000.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/costs/actuals/ledger/1 - Delete Cost Actual Ledger Entry")
    void testDeleteCostActualLedger() throws Exception {
        doNothing().when(costService).deleteCostActualLedger(1L);

        mockMvc.perform(delete("/api/v1/costs/actuals/ledger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/costs/evm/records/1 - Get EVM Snapshot By ID")
    void testGetEvmRecordById() throws Exception {
        ProjectEVMRecord record = ProjectEVMRecord.builder().id(1L).projectId(100L).cpi(new BigDecimal("1.10")).build();
        when(costService.getEvmRecordById(1L)).thenReturn(record);

        mockMvc.perform(get("/api/v1/costs/evm/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cpi").value(1.10));
    }

    @Test
    @DisplayName("DELETE /api/v1/costs/evm/records/1 - Delete EVM Snapshot Record")
    void testDeleteEvmRecord() throws Exception {
        doNothing().when(costService).deleteEvmRecord(1L);

        mockMvc.perform(delete("/api/v1/costs/evm/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/costs/project-budgets/project/100 - Get Mirrored Project Budgets")
    void testGetProjectBudgetsByProject() throws Exception {
        ProjectBudget pb = ProjectBudget.builder().id(1L).projectId(100L).costCode("MAT_01").allocatedAmount(new BigDecimal("50000.00")).build();
        when(costService.getProjectBudgetsByProject(100L)).thenReturn(List.of(pb));

        mockMvc.perform(get("/api/v1/costs/project-budgets/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].allocatedAmount").value(50000.00));
    }

    @Test
    @DisplayName("POST /api/v1/cost/ledger/sync/equipment/100 - Sync Equipment Fleet Usage to Cost Ledger")
    void testSyncEquipmentCost() throws Exception {
        CostSyncResultDto res = CostSyncResultDto.builder()
                .projectId(100L)
                .equipmentTransactionsSynced(3)
                .totalSyncedAmount(new BigDecimal("36000.00"))
                .message("Equipment fleet usage hours successfully synchronized into Cost Actual Ledger")
                .build();
        when(costService.syncEquipmentCost(100L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/cost/ledger/sync/equipment/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.equipmentTransactionsSynced").value(3))
                .andExpect(jsonPath("$.data.totalSyncedAmount").value(36000.00));
    }
}

