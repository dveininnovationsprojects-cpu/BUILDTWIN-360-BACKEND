package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import com.example.BuildTwin._0.domain.cost.repository.BudgetRepository;
import com.example.BuildTwin._0.domain.cost.repository.CostTransactionRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Epic4CostServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CostTransactionRepository costTransactionRepository;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository labourDailyRepository;

    @Mock
    private com.example.BuildTwin._0.domain.procurement.repository.GrnRepository grnRepository;

    @InjectMocks
    private CostServiceImpl costService;

    @Test
    @DisplayName("Module 1: Should set and update CBS Baseline Budget & Authorized Revision")
    void testBudgetRevisions() {
        Budget existing = Budget.builder()
                .id(1L)
                .projectId(101L)
                .costCode("CIV_RCC_01")
                .costHead(CostHead.MATERIAL)
                .baselineAmount(new BigDecimal("1000000.00"))
                .revisedAmount(BigDecimal.ZERO)
                .status("APPROVED")
                .build();

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetRevisionDto revisionDto = BudgetRevisionDto.builder()
                .revisedAmount(new BigDecimal("1250000.00"))
                .revisionReason("Extra reinforcement for seismic zone upgrades")
                .approvedBy("Director_Ramesh")
                .build();

        Budget updated = costService.authorizeBudgetRevision(1L, revisionDto);

        assertNotNull(updated);
        assertEquals(new BigDecimal("1250000.00"), updated.getRevisedAmount());
        assertEquals("Extra reinforcement for seismic zone upgrades", updated.getRevisionReason());
        assertEquals("Director_Ramesh", updated.getApprovedBy());
        assertEquals("APPROVED_REVISED", updated.getStatus());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Module 2: Should track Material & Labour Cost breakdown across all Cost Heads")
    void testMaterialAndLabourCostTracking() {
        Long projectId = 101L;

        Budget materialBudget = Budget.builder()
                .id(1L).projectId(projectId).costCode("MAT_CEMENT").costHead(CostHead.MATERIAL)
                .baselineAmount(new BigDecimal("500000.00")).revisedAmount(new BigDecimal("600000.00")).build();
        Budget labourBudget = Budget.builder()
                .id(2L).projectId(projectId).costCode("LAB_DIRECT").costHead(CostHead.LABOUR)
                .baselineAmount(new BigDecimal("300000.00")).revisedAmount(BigDecimal.ZERO).build();

        when(budgetRepository.findByProjectId(projectId)).thenReturn(List.of(materialBudget, labourBudget));

        CostTransaction labourTx = CostTransaction.builder()
                .id(10L).projectId(projectId).costCode("LAB_DIRECT").costHead(CostHead.LABOUR)
                .sourceType(CostSourceType.LABOUR_WAGES).amount(new BigDecimal("180000.00")).build();

        when(costTransactionRepository.findByProjectId(projectId)).thenReturn(List.of(labourTx));
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId)).thenReturn(new BigDecimal("250000.00"));

        PurchaseOrder po = PurchaseOrder.builder()
                .id(50L).projectId(projectId).amount(new BigDecimal("150000.00")).status("ISSUED").build();
        when(purchaseOrderRepository.findByProjectId(projectId)).thenReturn(List.of(po));

        ProjectCostTrackingReportDto report = costService.getProjectCostTracking(projectId);

        assertNotNull(report);
        assertEquals(projectId, report.getProjectId());
        assertEquals(new BigDecimal("800000.00"), report.getTotalBaselineBudget());
        assertEquals(new BigDecimal("900000.00"), report.getTotalApprovedBudget());
        assertEquals(new BigDecimal("430000.00"), report.getTotalActualExpense()); // 180k labour + 250k material
        assertEquals(new BigDecimal("150000.00"), report.getTotalCommittedCost()); // 150k PO

        CostHeadTrackingDto materialTracking = report.getCostHeadBreakdown().stream()
                .filter(b -> b.getCostHead() == CostHead.MATERIAL).findFirst().orElseThrow();
        assertEquals(new BigDecimal("250000.00"), materialTracking.getActualIncurred());
        assertEquals(new BigDecimal("150000.00"), materialTracking.getCommittedAmount());
        assertEquals(new BigDecimal("400000.00"), materialTracking.getTotalForecasted());

        CostHeadTrackingDto labourTracking = report.getCostHeadBreakdown().stream()
                .filter(b -> b.getCostHead() == CostHead.LABOUR).findFirst().orElseThrow();
        assertEquals(new BigDecimal("180000.00"), labourTracking.getActualIncurred());
    }

    @Test
    @DisplayName("Module 3: Should compute Procurement Commitments from Purchase Orders accurately")
    void testProcurementCommitments() {
        Long projectId = 101L;

        PurchaseOrder po1 = PurchaseOrder.builder()
                .id(1L).projectId(projectId).poNumber("PO-001").amount(new BigDecimal("200000.00")).status("ISSUED").build();
        PurchaseOrder po2 = PurchaseOrder.builder()
                .id(2L).projectId(projectId).poNumber("PO-002").amount(new BigDecimal("100000.00")).status("PARTIALLY_FULFILLED").build();
        PurchaseOrder po3 = PurchaseOrder.builder()
                .id(3L).projectId(projectId).poNumber("PO-003").amount(new BigDecimal("350000.00")).status("FULFILLED").build();
        PurchaseOrder po4 = PurchaseOrder.builder()
                .id(4L).projectId(projectId).poNumber("PO-004").amount(new BigDecimal("50000.00")).status("CANCELLED").build();

        when(purchaseOrderRepository.findByProjectId(projectId)).thenReturn(List.of(po1, po2, po3, po4));

        ProcurementCommitmentDto commitments = costService.getProcurementCommitments(projectId);

        assertNotNull(commitments);
        assertEquals(new BigDecimal("300000.00"), commitments.getTotalCommittedAmount()); // PO-001 + PO-002
        assertEquals(new BigDecimal("350000.00"), commitments.getTotalFulfilledAmount());
        assertEquals(new BigDecimal("50000.00"), commitments.getTotalCancelledAmount());
        assertEquals(2, commitments.getOpenOrdersCount());
    }

    @Test
    @DisplayName("Module 4: Should generate Baseline Budget vs Actual Expense Variance Report")
    void testBudgetVarianceReport() {
        Long projectId = 101L;

        Project project = Project.builder().id(projectId).name("Cyber Metro Station").build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Budget b1 = Budget.builder()
                .id(1L).projectId(projectId).costCode("CIV_01").costHead(CostHead.MATERIAL)
                .description("Structural Steel").baselineAmount(new BigDecimal("1000000.00")).revisedAmount(new BigDecimal("1100000.00")).build();

        when(budgetRepository.findByProjectId(projectId)).thenReturn(List.of(b1));

        CostTransaction t1 = CostTransaction.builder()
                .id(1L).projectId(projectId).costCode("CIV_01").amount(new BigDecimal("800000.00")).build();
        when(costTransactionRepository.findByProjectId(projectId)).thenReturn(List.of(t1));

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L).projectId(projectId).amount(new BigDecimal("200000.00")).status("ISSUED").build();
        when(purchaseOrderRepository.findByProjectId(projectId)).thenReturn(List.of(po));

        BudgetVarianceReportDto report = costService.getBudgetVarianceReport(projectId);

        assertNotNull(report);
        assertEquals("Cyber Metro Station", report.getProjectName());
        assertEquals(new BigDecimal("1000000.00"), report.getTotalBaselineBudget());
        assertEquals(new BigDecimal("1100000.00"), report.getTotalRevisedBudget());
        assertEquals(new BigDecimal("800000.00"), report.getTotalActualExpense());
        assertEquals(new BigDecimal("200000.00"), report.getTotalCommittedExpense());
        assertEquals(new BigDecimal("1000000.00"), report.getTotalProjectedCost());
        assertEquals(new BigDecimal("100000.00"), report.getNetVariance());
        assertEquals("ON_BUDGET", report.getOverallStatus()); // 1M / 1.1M is ~90.9%
    }

    @Test
    @DisplayName("Module 5: Should calculate EVM with Planned and Actual Progress inputs & TCPI")
    void testEvmWithProgress() {
        Long projectId = 101L;
        BigDecimal bac = new BigDecimal("2000000.00");

        when(budgetRepository.getTotalApprovedBudgetByProjectId(projectId)).thenReturn(bac);
        when(budgetRepository.getTotalBaselineBudgetByProjectId(projectId)).thenReturn(bac);
        when(costTransactionRepository.getTotalActualCostByProjectId(projectId)).thenReturn(new BigDecimal("900000.00"));
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId)).thenReturn(BigDecimal.ZERO);

        // 50% planned, 60% actual completed
        EvmMetricsDto metrics = costService.calculateEvmMetricsWithProgress(projectId, 50.0, 60.0);

        assertNotNull(metrics);
        assertEquals(new BigDecimal("1000000.00"), metrics.getPlannedValue()); // 50% of 2M
        assertEquals(new BigDecimal("1200000.00"), metrics.getEarnedValue());  // 60% of 2M
        assertEquals(new BigDecimal("900000.00"), metrics.getActualCost());
        assertEquals(new BigDecimal("200000.00"), metrics.getScheduleVariance()); // 1.2M - 1M
        assertEquals(new BigDecimal("300000.00"), metrics.getCostVariance());     // 1.2M - 900k
        assertEquals("ON_SCHEDULE", metrics.getSpiStatus());
        assertEquals("UNDER_BUDGET", metrics.getCpiStatus());
        assertNotNull(metrics.getToCompletePerformanceIndex());
    }

    @Test
    @DisplayName("Day 2: Should retrieve material actual cost summary from stock ledger")
    void testMaterialActualCostSummary() {
        Long projectId = 101L;
        when(stockLedgerRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(costTransactionRepository.findByProjectIdAndCostHead(projectId, CostHead.MATERIAL)).thenReturn(List.of(
                CostTransaction.builder().costCode("MAT-01").amount(new BigDecimal("75000.00")).transactionDate(LocalDate.now()).build()
        ));

        MaterialActualCostSummaryDto summary = costService.getMaterialActualCostSummary(projectId);

        assertNotNull(summary);
        assertEquals(new BigDecimal("75000.00"), summary.getTotalMaterialCost());
        assertEquals(1, summary.getEntriesCount());
    }

    @Test
    @DisplayName("Day 2: Should retrieve labour actual cost summary from daily muster records")
    void testLabourActualCostSummary() {
        Long projectId = 101L;
        when(labourDailyRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(costTransactionRepository.findByProjectIdAndCostHead(projectId, CostHead.LABOUR)).thenReturn(List.of(
                CostTransaction.builder().costCode("LAB-01").amount(new BigDecimal("40000.00")).transactionDate(LocalDate.now()).build()
        ));

        LabourActualCostSummaryDto summary = costService.getLabourActualCostSummary(projectId);

        assertNotNull(summary);
        assertEquals(new BigDecimal("40000.00"), summary.getTotalLabourCost());
    }

    @Test
    @DisplayName("Day 2: Should synchronize ground consumptions into Cost Ledger")
    void testSyncActualCosts() {
        Long projectId = 101L;
        when(stockLedgerRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(labourDailyRepository.findByProjectId(projectId)).thenReturn(List.of());

        CostSyncResultDto result = costService.syncActualCosts(projectId);

        assertNotNull(result);
        assertEquals(0, result.getMaterialTransactionsSynced());
        assertEquals(0, result.getLabourTransactionsSynced());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("Day 3: Should compute Procurement Expenditure Trail with incurred GRN bill values")
    void testProcurementExpenditureTrail() {
        Long projectId = 101L;
        PurchaseOrder po = PurchaseOrder.builder().id(1L).amount(new BigDecimal("100000.00")).unitRate(new BigDecimal("500.00")).status("ISSUED").build();
        Grn grn = Grn.builder().id(10L).poId(1L).acceptedQty(new BigDecimal("100.00")).status("COMPLETED").build();

        when(purchaseOrderRepository.findByProjectId(projectId)).thenReturn(List.of(po));
        when(grnRepository.findByProjectId(projectId)).thenReturn(List.of(grn));
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        ProcurementExpenditureTrailDto trail = costService.getProcurementExpenditureTrail(projectId);

        assertNotNull(trail);
        assertEquals(new BigDecimal("100000.00"), trail.getCommittedCost());
        assertEquals(new BigDecimal("50000.00"), trail.getIncurredCost()); // 100 * 500 = 50,000
        assertEquals(new BigDecimal("50000.00"), trail.getOutstandingLiability());
    }

    @Test
    @DisplayName("Day 4: Should detect Budget Overrun alerts and 90% threshold warnings")
    void testBudgetOverruns() {
        Long projectId = 101L;
        Budget b1 = Budget.builder().costCode("MAT-STEEL").baselineAmount(new BigDecimal("100000.00")).build();
        CostTransaction tx1 = CostTransaction.builder().costCode("MAT-STEEL").amount(new BigDecimal("120000.00")).build();

        when(budgetRepository.findByProjectId(projectId)).thenReturn(List.of(b1));
        when(costTransactionRepository.findByProjectId(projectId)).thenReturn(List.of(tx1));

        BudgetOverrunReportDto report = costService.getBudgetOverruns(projectId);

        assertNotNull(report);
        assertEquals(1, report.getOverrunCount());
        assertEquals(new BigDecimal("20000.00"), report.getTotalOverrunAmount());
        assertEquals("CRITICAL_OVERRUN", report.getOverruns().get(0).getAlertSeverity());
    }

    @Test
    @DisplayName("Day 5: Should generate EVM Forecast with EAC auto-calculation")
    void testEvmForecast() {
        Long projectId = 101L;
        BigDecimal bac = new BigDecimal("1000000.00");

        when(budgetRepository.getTotalApprovedBudgetByProjectId(projectId)).thenReturn(bac);
        when(budgetRepository.getTotalBaselineBudgetByProjectId(projectId)).thenReturn(bac);
        when(costTransactionRepository.getTotalActualCostByProjectId(projectId)).thenReturn(new BigDecimal("500000.00"));
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId)).thenReturn(BigDecimal.ZERO);

        EvmForecastDto forecast = costService.getEvmForecast(projectId);

        assertNotNull(forecast);
        assertEquals(bac, forecast.getBudgetAtCompletion());
        assertNotNull(forecast.getEstimateAtCompletion());
        assertNotNull(forecast.getForecastSummary());
    }

    @Test
    @DisplayName("Step 1: Should retrieve WBS activity level budget breakdown")
    void testGetBudgetsByProjectAndActivity() {
        Long projectId = 101L;
        Long activityId = 55L;
        Budget b1 = Budget.builder().projectId(projectId).activityId(activityId).costCode("SLAB_MAT").costHead(CostHead.MATERIAL).baselineAmount(new BigDecimal("500000.00")).build();
        Budget b2 = Budget.builder().projectId(projectId).activityId(activityId).costCode("SLAB_LAB").costHead(CostHead.LABOUR).baselineAmount(new BigDecimal("200000.00")).build();

        when(budgetRepository.findByProjectIdAndActivityId(projectId, activityId)).thenReturn(List.of(b1, b2));

        List<Budget> result = costService.getBudgetsByProjectAndActivity(projectId, activityId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SLAB_MAT", result.get(0).getCostCode());
        assertEquals("SLAB_LAB", result.get(1).getCostCode());
    }

    @Test
    @DisplayName("Step 4: Should trigger WARNING_80_PERCENT alert when budget utilization crosses 80%")
    void testBudgetOverrun80PercentWarning() {
        Long projectId = 101L;
        Budget b = Budget.builder().costCode("CIV_RCC").baselineAmount(new BigDecimal("100000.00")).build();
        CostTransaction tx = CostTransaction.builder().costCode("CIV_RCC").amount(new BigDecimal("85000.00")).build(); // 85%

        when(budgetRepository.findByProjectId(projectId)).thenReturn(List.of(b));
        when(costTransactionRepository.findByProjectId(projectId)).thenReturn(List.of(tx));

        BudgetOverrunReportDto report = costService.getBudgetOverruns(projectId);

        assertNotNull(report);
        assertEquals(1, report.getOverrunCount());
        assertEquals("WARNING_80_PERCENT", report.getOverruns().get(0).getAlertSeverity());
        assertTrue(report.getOverruns().get(0).getAlertMessage().contains("80% threshold"));
    }

    @Test
    @DisplayName("Step 4: Should execute daily background budget scan across projects")
    void testRunDailyBudgetOverrunCheck() {
        com.example.BuildTwin._0.domain.projects.model.Project proj = new com.example.BuildTwin._0.domain.projects.model.Project();
        proj.setId(101L);
        proj.setName("Metro Line 4");

        Budget b = Budget.builder().costCode("STEEL_01").baselineAmount(new BigDecimal("100000.00")).build();
        CostTransaction tx = CostTransaction.builder().costCode("STEEL_01").amount(new BigDecimal("95000.00")).build();

        when(projectRepository.findAll()).thenReturn(List.of(proj));
        when(budgetRepository.findByProjectId(101L)).thenReturn(List.of(b));
        when(costTransactionRepository.findByProjectId(101L)).thenReturn(List.of(tx));

        assertDoesNotThrow(() -> costService.runDailyBudgetOverrunCheck());
    }
}
