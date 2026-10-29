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
import com.example.BuildTwin._0.domain.labour.service.LabourService;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.service.MaterialService;
import com.example.BuildTwin._0.domain.notifications.repository.NotificationRepository;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.domain.reports.dto.*;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private GrnRepository grnRepository;

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
                .estimatedBudget(new BigDecimal("45000000.00"))
                .build();
    }

    @Test
    @DisplayName("Unit Test: Generate Weekly Project Report Successfully")
    void generateWeeklyProjectReport_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        EvmMetricsDto evm = EvmMetricsDto.builder()
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
                .build();
        when(labourService.getLabourHourSummary(eq(1L), any(), any(), any())).thenReturn(labour);

        LabourActualCostSummaryDto labourCost = LabourActualCostSummaryDto.builder()
                .totalLabourCost(new BigDecimal("95000.00"))
                .build();
        when(costService.getLabourActualCostSummary(1L)).thenReturn(labourCost);

        when(materialRepository.findAll()).thenReturn(List.of());
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
        when(projectIssueService.getIssueSummaryByProject(1L)).thenReturn(quality);
        when(issueBlockerRepository.findByProjectId(1L)).thenReturn(List.of());
        when(notificationRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        WeeklyProjectReportDto report = reportService.generateWeeklyProjectReport(1L, 10);

        assertNotNull(report);
        assertEquals(1L, report.getProjectId());
        assertEquals("Ashok Grandeur - Padur, Chennai", report.getProjectName());
        assertEquals(10, report.getWeekNumber());
        assertEquals("ON_SCHEDULE", report.getScheduleStatus());
        assertEquals(45, report.getTotalHeadcount());
        assertEquals(new BigDecimal("95000.00"), report.getTotalLabourCost());
    }

    @Test
    @DisplayName("Unit Test: Generate Inventory Report Successfully")
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

        when(materialRepository.findAll()).thenReturn(List.of(mat));
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
    @DisplayName("Unit Test: Project Not Found Throws ResourceNotFoundException")
    void generateWeeklyReport_NotFound_ThrowsException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.generateWeeklyProjectReport(999L, 1));
    }
}
