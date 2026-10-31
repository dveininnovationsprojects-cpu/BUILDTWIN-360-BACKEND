package com.example.BuildTwin._0.domain.reports.service;

import com.example.BuildTwin._0.domain.cost.dto.BudgetVarianceReportDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.dto.LabourActualCostSummaryDto;
import com.example.BuildTwin._0.domain.cost.dto.MaterialActualCostSummaryDto;
import com.example.BuildTwin._0.domain.cost.dto.ProcurementExpenditureTrailDto;
import com.example.BuildTwin._0.domain.cost.dto.BudgetOverrunReportDto;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.domain.issues.dto.ProjectIssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.service.ProjectIssueService;
import com.example.BuildTwin._0.domain.labour.dto.LabourHourSummaryDto;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.labour.service.LabourService;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.service.MaterialService;
import com.example.BuildTwin._0.domain.notifications.repository.NotificationRepository;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.domain.reports.dto.*;
import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CostService costService;

    @Mock
    private LabourService labourService;

    @Mock
    private MaterialService materialService;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @Mock
    private ProjectIssueService projectIssueService;

    @Mock
    private IssueBlockerRepository issueBlockerRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private LabourDailyRepository labourDailyRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private GrnRepository grnRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .id(1L)
                .name("Ashok Grandeur - Padur, Chennai")
                .code("PADUR-AG-01")
                .status("ACTIVE")
                .actualStartDate(LocalDate.of(2026, 1, 1))
                .estimatedBudget(new BigDecimal("45000000.00"))
                .build();
    }

    @Test
    @DisplayName("Unit Test: Generate Weekly Project Report Successfully")
    void generateWeeklyProjectReport_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        EvmMetricsDto evm = EvmMetricsDto.builder()
                .budgetAtCompletion(new BigDecimal("45000000.00"))
                .plannedValue(new BigDecimal("38250000.00"))
                .earnedValue(new BigDecimal("37125000.00"))
                .costPerformanceIndex(new BigDecimal("1.05"))
                .schedulePerformanceIndex(new BigDecimal("1.02"))
                .estimateAtCompletion(new BigDecimal("42000000.00"))
                .build();
        when(costService.calculateEvmMetrics(1L)).thenReturn(evm);

        BudgetVarianceReportDto variance = BudgetVarianceReportDto.builder()
                .totalBaselineBudget(new BigDecimal("45000000.00"))
                .totalActualExpense(new BigDecimal("15000000.00"))
                .netVariance(new BigDecimal("30000000.00"))
                .overallStatus("UNDER_BUDGET")
                .build();
        when(costService.getBudgetVarianceReport(1L)).thenReturn(variance);

        LabourHourSummaryDto labour = LabourHourSummaryDto.builder()
                .totalHeadcount(45)
                .totalStandardHours(new BigDecimal("360.00"))
                .totalOvertimeHours(new BigDecimal("40.00"))
                .totalRecordsLogged(12)
                .build();
        when(labourService.getLabourHourSummary(eq(1L), any(), any(), any())).thenReturn(labour);

        LabourActualCostSummaryDto labourCost = LabourActualCostSummaryDto.builder()
                .totalLabourCost(new BigDecimal("95000.00"))
                .build();
        when(costService.getLabourActualCostSummary(1L)).thenReturn(labourCost);

        when(stockLedgerRepository.findDistinctMaterialsByProjectId(1L)).thenReturn(List.of());
        when(materialService.getLowStockMaterials(1L)).thenReturn(List.of());
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(1L)).thenReturn(new BigDecimal("500000.00"));

        ProcurementExpenditureTrailDto trail = ProcurementExpenditureTrailDto.builder()
                .totalPoCount(5)
                .committedCost(new BigDecimal("2000000.00"))
                .incurredCost(new BigDecimal("1200000.00"))
                .outstandingLiability(new BigDecimal("800000.00"))
                .build();
        when(costService.getProcurementExpenditureTrail(1L)).thenReturn(trail);

        ProjectIssueSummaryDto quality = ProjectIssueSummaryDto.builder()
                .snagCount(2L)
                .ncrCount(1L)
                .blockerCount(0L)
                .build();
        when(projectIssueService.getProjectIssueSummary(1L)).thenReturn(quality);
        when(issueBlockerRepository.sumDaysDelayedByProjectId(1L)).thenReturn(0);
        when(notificationRepository.countByProjectIdAndStatus(1L, com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus.UNREAD)).thenReturn(0L);
        when(activityRepository.findByProjectId(1L)).thenReturn(List.of());

        WeeklyProjectReportDto report = reportService.generateWeeklyProjectReport(1L, 10);

        assertNotNull(report);
        assertEquals(1L, report.getProjectId());
        assertEquals("Ashok Grandeur - Padur, Chennai", report.getProjectName());
        assertEquals(10, report.getWeekNumber());
        assertEquals("ON_SCHEDULE", report.getScheduleStatus());
        assertEquals(45, report.getTotalHeadcount());
        assertEquals(new BigDecimal("95000.00"), report.getTotalLabourCost());
        assertEquals(new BigDecimal("85.00"), report.getPlannedProgressPercentage());
        assertEquals(new BigDecimal("82.50"), report.getActualProgressPercentage());
    }

    @Test
    @DisplayName("Unit Test: WBS Weightage-based Physical Progress Calculation (Gap 1 Fix)")
    void generateWeeklyProjectReport_WithWbsWeightageProgress_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        EvmMetricsDto evm = EvmMetricsDto.builder()
                .costPerformanceIndex(new BigDecimal("1.05"))
                .schedulePerformanceIndex(new BigDecimal("1.02"))
                .build();
        when(costService.calculateEvmMetrics(1L)).thenReturn(evm);

        BudgetVarianceReportDto variance = BudgetVarianceReportDto.builder()
                .totalBaselineBudget(new BigDecimal("45000000.00"))
                .totalActualExpense(new BigDecimal("15000000.00"))
                .netVariance(new BigDecimal("30000000.00"))
                .overallStatus("UNDER_BUDGET")
                .build();
        when(costService.getBudgetVarianceReport(1L)).thenReturn(variance);

        LabourHourSummaryDto labour = LabourHourSummaryDto.builder().totalHeadcount(30).totalRecordsLogged(10).build();
        when(labourService.getLabourHourSummary(eq(1L), any(), any(), any())).thenReturn(labour);
        when(costService.getLabourActualCostSummary(1L)).thenReturn(LabourActualCostSummaryDto.builder().totalLabourCost(BigDecimal.ZERO).build());
        when(stockLedgerRepository.findDistinctMaterialsByProjectId(1L)).thenReturn(List.of());
        when(materialService.getLowStockMaterials(1L)).thenReturn(List.of());
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(1L)).thenReturn(BigDecimal.ZERO);
        when(costService.getProcurementExpenditureTrail(1L)).thenReturn(ProcurementExpenditureTrailDto.builder().totalPoCount(0).build());
        when(projectIssueService.getProjectIssueSummary(1L)).thenReturn(ProjectIssueSummaryDto.builder().build());
        when(issueBlockerRepository.sumDaysDelayedByProjectId(1L)).thenReturn(0);
        when(notificationRepository.countByProjectIdAndStatus(1L, com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus.UNREAD)).thenReturn(0L);

        // 2 WBS Activities with weightage: Act1 (60%, COMPLETED = 100%), Act2 (40%, IN_PROGRESS = 50%)
        // Actual Progress = (100 * 60 + 50 * 40) / 100 = (6000 + 2000) / 100 = 80.00%
        Activity act1 = Activity.builder()
                .id(101L)
                .name("Foundation Pile Works")
                .status("COMPLETED")
                .weightage(new BigDecimal("60.00"))
                .plannedStartDate(LocalDate.now().minusDays(30))
                .plannedEndDate(LocalDate.now().minusDays(5))
                .build();
        Activity act2 = Activity.builder()
                .id(102L)
                .name("Basement RCC Retaining Wall")
                .status("IN_PROGRESS")
                .weightage(new BigDecimal("40.00"))
                .plannedStartDate(LocalDate.now().minusDays(5))
                .plannedEndDate(LocalDate.now().plusDays(25))
                .build();
        when(activityRepository.findByProjectId(1L)).thenReturn(List.of(act1, act2));

        WeeklyProjectReportDto report = reportService.generateWeeklyProjectReport(1L, 10);

        assertNotNull(report);
        assertEquals(new BigDecimal("80.00"), report.getActualProgressPercentage());
    }

    @Test
    @DisplayName("Unit Test: Generate Inventory Report with Strict Project Isolation (Gap 2 Fix)")
    void generateInventoryReport_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        Material mat = Material.builder()
                .id(1L)
                .materialCode("MAT-CEM-01")
                .name("UltraTech Cement")
                .unit(MaterialUnit.BAGS)
                .currentStock(new BigDecimal("200.00"))
                .reorderLevel(new BigDecimal("500.00"))
                .standardRate(new BigDecimal("380.00"))
                .build();

        // Project-isolated materials only!
        when(stockLedgerRepository.findDistinctMaterialsByProjectId(1L)).thenReturn(List.of(mat));
        when(materialService.getLowStockMaterials(1L)).thenReturn(List.of(mat));

        MaterialActualCostSummaryDto matCost = MaterialActualCostSummaryDto.builder()
                .totalQuantityConsumed(new BigDecimal("150.00"))
                .totalMaterialCost(new BigDecimal("57000.00"))
                .build();
        when(costService.getMaterialActualCostSummary(1L)).thenReturn(matCost);

        InventoryReportDto report = reportService.generateInventoryReport(1L);

        assertNotNull(report);
        assertEquals(1, report.getTotalMaterialsCataloged());
        assertEquals(1, report.getLowStockItemsCount());
        assertEquals(new BigDecimal("76000.00"), report.getTotalInventoryValuation());
        assertEquals(1, report.getCriticalLowStockItems().size());
        assertEquals(new BigDecimal("300.00"), report.getCriticalLowStockItems().get(0).getStockDeficit());
    }

    @Test
    @DisplayName("Unit Test: Generate Procurement and Labour Report with Project Contractor Isolation (Gap 5 Fix)")
    void generateProcurementLabourReport_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        ProcurementExpenditureTrailDto trail = ProcurementExpenditureTrailDto.builder()
                .totalPoCount(12)
                .committedCost(new BigDecimal("4500000.00"))
                .incurredCost(new BigDecimal("2800000.00"))
                .outstandingLiability(new BigDecimal("1700000.00"))
                .openCommitments(List.of())
                .build();
        when(costService.getProcurementExpenditureTrail(1L)).thenReturn(trail);

        LabourHourSummaryDto labour = LabourHourSummaryDto.builder()
                .totalHeadcount(68)
                .totalStandardHours(new BigDecimal("544.00"))
                .totalOvertimeHours(new BigDecimal("60.00"))
                .totalRecordsLogged(35)
                .build();
        when(labourService.getLabourHourSummary(1L, null, null, null)).thenReturn(labour);

        LabourActualCostSummaryDto labourCost = LabourActualCostSummaryDto.builder()
                .totalLabourCost(new BigDecimal("145000.00"))
                .build();
        when(costService.getLabourActualCostSummary(1L)).thenReturn(labourCost);

        // Project-only contractor count!
        when(labourDailyRepository.countDistinctContractorsByProjectId(1L)).thenReturn(5);
        when(purchaseOrderRepository.countByProjectId(1L)).thenReturn(12L);
        when(grnRepository.countByProjectId(1L)).thenReturn(18L);

        ProcurementLabourReportDto report = reportService.generateProcurementLabourReport(1L);

        assertNotNull(report);
        assertEquals(1L, report.getProjectId());
        assertEquals(12, report.getTotalPurchaseOrders());
        assertEquals(18, report.getTotalGrnReceiptsLogged());
        assertEquals(5, report.getActiveContractorsCount());
        assertEquals(68, report.getTotalHeadcountDeployed());
        assertEquals(new BigDecimal("145000.00"), report.getTotalLabourWageCost());
    }

    @Test
    @DisplayName("Unit Test: Generate Cost and Quality Unified Report Successfully")
    void generateCostQualityReport_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        BudgetVarianceReportDto variance = BudgetVarianceReportDto.builder()
                .totalBaselineBudget(new BigDecimal("45000000.00"))
                .totalRevisedBudget(new BigDecimal("45000000.00"))
                .totalActualExpense(new BigDecimal("12000000.00"))
                .totalCommittedExpense(new BigDecimal("3000000.00"))
                .totalProjectedCost(new BigDecimal("15000000.00"))
                .netVariance(new BigDecimal("30000000.00"))
                .overallStatus("UNDER_BUDGET")
                .build();
        when(costService.getBudgetVarianceReport(1L)).thenReturn(variance);

        BudgetOverrunReportDto overruns = BudgetOverrunReportDto.builder()
                .overrunCount(0)
                .totalOverrunAmount(BigDecimal.ZERO)
                .build();
        when(costService.getBudgetOverruns(1L)).thenReturn(overruns);

        EvmMetricsDto evm = EvmMetricsDto.builder()
                .costPerformanceIndex(new BigDecimal("1.05"))
                .schedulePerformanceIndex(new BigDecimal("1.00"))
                .estimateAtCompletion(new BigDecimal("42857142.86"))
                .varianceAtCompletion(new BigDecimal("2142857.14"))
                .build();
        when(costService.calculateEvmMetrics(1L)).thenReturn(evm);

        ProjectIssueSummaryDto quality = ProjectIssueSummaryDto.builder()
                .totalIssues(8L)
                .snagCount(2L)
                .ncrCount(1L)
                .blockerCount(0L)
                .overdueCount(0L)
                .resolutionPercentage(75.0)
                .build();
        when(projectIssueService.getProjectIssueSummary(1L)).thenReturn(quality);
        when(issueBlockerRepository.sumDaysDelayedByProjectId(1L)).thenReturn(0);

        CostQualityReportDto report = reportService.generateCostQualityReport(1L);

        assertNotNull(report);
        assertEquals("UNDER_BUDGET", report.getBudgetStatus());
        assertEquals(8L, report.getTotalIssuesReported());
        assertEquals(75.0, report.getQualityResolutionPercentage());
        assertEquals(0, report.getActiveBudgetOverrunsCount());
    }

    @Test
    @DisplayName("Unit Test: Generate Executive Analytics and Composite Health Score Successfully")
    void generateAnalyticsAggregation_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        EvmMetricsDto evm = EvmMetricsDto.builder()
                .costPerformanceIndex(new BigDecimal("1.05"))
                .schedulePerformanceIndex(new BigDecimal("1.00"))
                .build();
        when(costService.calculateEvmMetrics(1L)).thenReturn(evm);

        ProjectIssueSummaryDto quality = ProjectIssueSummaryDto.builder()
                .blockerCount(0L)
                .overdueCount(0L)
                .resolutionPercentage(85.0)
                .build();
        when(projectIssueService.getProjectIssueSummary(1L)).thenReturn(quality);
        when(materialService.getLowStockMaterials(1L)).thenReturn(List.of());
        when(notificationRepository.countByProjectIdAndStatusAndPriority(
                1L,
                com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus.UNREAD,
                com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority.URGENT)).thenReturn(0L);

        AnalyticsAggregationDto report = reportService.generateAnalyticsAggregation(1L);

        assertNotNull(report);
        assertEquals("OPTIMAL", report.getOverallHealthStatus());
        assertEquals(100.0, report.getCompositeHealthScore());
        assertEquals("ON_BUDGET", report.getBudgetHealth());
        assertEquals("ON_SCHEDULE", report.getScheduleHealth());
        assertEquals("SUFFICIENT", report.getInventoryHealth());
        assertEquals("PASSING", report.getQualityHealth());
    }

    @Test
    @DisplayName("Unit Test: Edge Case - Critical Delays and Overruns Clamped Safely without NPE")
    void generateAnalyticsAggregation_CriticalOverruns_ClampedSafely() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        EvmMetricsDto evm = EvmMetricsDto.builder()
                .costPerformanceIndex(new BigDecimal("0.70"))
                .schedulePerformanceIndex(new BigDecimal("0.80"))
                .build();
        when(costService.calculateEvmMetrics(1L)).thenReturn(evm);

        ProjectIssueSummaryDto quality = ProjectIssueSummaryDto.builder()
                .blockerCount(10L)
                .overdueCount(10L)
                .resolutionPercentage(20.0)
                .build();
        when(projectIssueService.getProjectIssueSummary(1L)).thenReturn(quality);

        Material mat = Material.builder().id(1L).build();
        when(materialService.getLowStockMaterials(1L)).thenReturn(List.of(mat));
        when(notificationRepository.countByProjectIdAndStatusAndPriority(
                1L,
                com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus.UNREAD,
                com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority.URGENT)).thenReturn(4L);

        AnalyticsAggregationDto report = reportService.generateAnalyticsAggregation(1L);

        assertNotNull(report);
        assertEquals("CRITICAL", report.getBudgetHealth());
        assertEquals("DELAYED", report.getScheduleHealth());
        assertEquals("LOW_STOCK", report.getInventoryHealth());
        assertEquals("AUDIT_FAILURES", report.getQualityHealth());
        assertEquals("CRITICAL", report.getOverallHealthStatus());
        assertTrue(report.getCompositeHealthScore() >= 0.0);
        assertEquals(10, report.getActiveBlockersCount());
        assertEquals(4, report.getUnreadCriticalAlertsCount());
    }

    @Test
    @DisplayName("Unit Test: Project Not Found Throws ResourceNotFoundException")
    void generateWeeklyReport_NotFound_ThrowsException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.generateWeeklyProjectReport(999L, 1));
    }
}
