package com.example.BuildTwin._0.domain.reports.service;

import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.domain.issues.dto.ProjectIssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.service.ProjectIssueService;
import com.example.BuildTwin._0.domain.labour.dto.LabourHourSummaryDto;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.service.LabourService;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.service.MaterialService;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.repository.NotificationRepository;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.domain.reports.dto.*;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ProjectRepository projectRepository;
    private final CostService costService;
    private final LabourService labourService;
    private final MaterialService materialService;
    private final MaterialRepository materialRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ProjectIssueService projectIssueService;
    private final IssueBlockerRepository issueBlockerRepository;
    private final NotificationRepository notificationRepository;
    private final ContractorRepository contractorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GrnRepository grnRepository;

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
    }

    @Override
    public WeeklyProjectReportDto generateWeeklyProjectReport(Long projectId, Integer weekNumber) {
        Project project = getProjectOrThrow(projectId);
        int week = weekNumber != null ? weekNumber : 10;

        // 1. Cost & EVM Metrics
        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);
        BudgetVarianceReportDto variance = costService.getBudgetVarianceReport(projectId);

        // 2. Labour Metrics
        LabourHourSummaryDto labour = labourService.getLabourHourSummary(projectId, null, null, null);
        LabourActualCostSummaryDto labourCost = costService.getLabourActualCostSummary(projectId);

        // 3. Material & Inventory
        List<Material> allMaterials = materialRepository.findAll();
        List<Material> lowStockList = materialService.getLowStockMaterials(projectId);
        BigDecimal materialConsumptionCost = stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId);
        if (materialConsumptionCost == null) materialConsumptionCost = BigDecimal.ZERO;

        List<StockLedger> projectLedgers = stockLedgerRepository.findByProjectId(projectId);
        java.util.Set<Long> projectMaterialIds = projectLedgers.stream()
                .filter(sl -> sl.getMaterial() != null)
                .map(sl -> sl.getMaterial().getId())
                .collect(java.util.stream.Collectors.toSet());
        List<Material> projectMaterials = projectMaterialIds.isEmpty() ? allMaterials :
                allMaterials.stream().filter(m -> projectMaterialIds.contains(m.getId())).toList();

        // 4. Procurement
        ProcurementExpenditureTrailDto procurement = costService.getProcurementExpenditureTrail(projectId);

        // 5. Quality & Issues
        ProjectIssueSummaryDto quality = projectIssueService.getProjectIssueSummary(projectId);
        int totalDelayDays = issueBlockerRepository.findByProjectId(projectId).stream()
                .mapToInt(b -> b.getDaysDelayed() != null ? b.getDaysDelayed() : 0)
                .sum();

        // 6. Unread Notifications
        long unreadAlerts = notificationRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .count();

        // Progress Calculations
        BigDecimal plannedProgress = new BigDecimal("85.00");
        if (evm.getBudgetAtCompletion() != null && evm.getBudgetAtCompletion().compareTo(BigDecimal.ZERO) > 0 && evm.getPlannedValue() != null) {
            plannedProgress = evm.getPlannedValue().multiply(new BigDecimal("100")).divide(evm.getBudgetAtCompletion(), 2, RoundingMode.HALF_UP);
        }

        BigDecimal actualProgress = evm.getCostPerformanceIndex().compareTo(BigDecimal.ONE) >= 0 ? new BigDecimal("82.50") : new BigDecimal("74.00");
        if (evm.getBudgetAtCompletion() != null && evm.getBudgetAtCompletion().compareTo(BigDecimal.ZERO) > 0 && evm.getEarnedValue() != null) {
            actualProgress = evm.getEarnedValue().multiply(new BigDecimal("100")).divide(evm.getBudgetAtCompletion(), 2, RoundingMode.HALF_UP);
        }
        String scheduleStatus = evm.getSchedulePerformanceIndex().compareTo(BigDecimal.ONE) >= 0 ? "ON_SCHEDULE" : "BEHIND_SCHEDULE";

        return WeeklyProjectReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .projectCode(project.getCode())
                .weekNumber(week)
                .generatedAt(LocalDateTime.now())
                .plannedProgressPercentage(plannedProgress)
                .actualProgressPercentage(actualProgress)
                .scheduleStatus(scheduleStatus)
                .totalHeadcount(labour.getTotalHeadcount())
                .totalStandardHours(labour.getTotalStandardHours())
                .totalOvertimeHours(labour.getTotalOvertimeHours())
                .totalLabourCost(labourCost.getTotalLabourCost())
                .materialsTrackedCount(projectMaterials.size())
                .lowStockAlertsCount(lowStockList.size())
                .totalMaterialConsumptionCost(materialConsumptionCost)
                .totalPurchaseOrdersCount(procurement.getTotalPoCount())
                .committedProcurementCost(procurement.getCommittedCost())
                .incurredGrnCost(procurement.getIncurredCost())
                .outstandingLiability(procurement.getOutstandingLiability())
                .baselineBudget(variance.getTotalBaselineBudget())
                .actualExpense(variance.getTotalActualExpense())
                .costVariance(variance.getNetVariance())
                .costPerformanceIndex(evm.getCostPerformanceIndex())
                .schedulePerformanceIndex(evm.getSchedulePerformanceIndex())
                .estimateAtCompletion(evm.getEstimateAtCompletion())
                .costHealthStatus(variance.getOverallStatus())
                .openSnagsCount(quality.getSnagCount())
                .activeNcrsCount(quality.getNcrCount())
                .criticalBlockersCount(quality.getBlockerCount())
                .totalSiteDelayDays(totalDelayDays)
                .unreadAlertsCount(unreadAlerts)
                .build();
    }

    @Override
    public InventoryReportDto generateInventoryReport(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        List<Material> allMaterials = materialRepository.findAll();
        List<Material> lowStockList = materialService.getLowStockMaterials(projectId);

        // Project isolation: filter materials relevant to this project if project has specific ledger activity
        List<StockLedger> projectLedgers = stockLedgerRepository.findByProjectId(projectId);
        java.util.Set<Long> projectMaterialIds = projectLedgers.stream()
                .filter(sl -> sl.getMaterial() != null)
                .map(sl -> sl.getMaterial().getId())
                .collect(java.util.stream.Collectors.toSet());
        List<Material> targetMaterials = projectMaterialIds.isEmpty() ? allMaterials :
                allMaterials.stream().filter(m -> projectMaterialIds.contains(m.getId())).toList();

        BigDecimal totalValuation = BigDecimal.ZERO;
        for (Material m : targetMaterials) {
            BigDecimal stock = m.getCurrentStock() != null ? m.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal rate = m.getStandardRate() != null ? m.getStandardRate() : BigDecimal.ZERO;
            totalValuation = totalValuation.add(stock.multiply(rate));
        }
        totalValuation = totalValuation.setScale(2, RoundingMode.HALF_UP);

        MaterialActualCostSummaryDto matCostSummary = costService.getMaterialActualCostSummary(projectId);

        List<InventoryReportDto.LowStockItemDto> lowStockItems = new ArrayList<>();
        for (Material m : lowStockList) {
            BigDecimal current = m.getCurrentStock() != null ? m.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal reorder = m.getReorderLevel() != null ? m.getReorderLevel() : BigDecimal.ZERO;
            BigDecimal deficit = reorder.subtract(current).max(BigDecimal.ZERO);

            lowStockItems.add(InventoryReportDto.LowStockItemDto.builder()
                    .materialId(m.getId())
                    .materialCode(m.getMaterialCode())
                    .materialName(m.getName())
                    .unit(m.getUnit() != null ? m.getUnit().name() : "UNITS")
                    .currentStock(current)
                    .reorderLevel(reorder)
                    .standardRate(m.getStandardRate())
                    .stockDeficit(deficit)
                    .build());
        }

        return InventoryReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .generatedAt(LocalDateTime.now())
                .totalMaterialsCataloged(allMaterials.size())
                .totalInventoryValuation(totalValuation)
                .lowStockItemsCount(lowStockItems.size())
                .totalConsumedQuantity(matCostSummary.getTotalQuantityConsumed())
                .totalConsumptionCost(matCostSummary.getTotalMaterialCost())
                .criticalLowStockItems(lowStockItems)
                .build();
    }

    @Override
    public ProcurementLabourReportDto generateProcurementLabourReport(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        ProcurementExpenditureTrailDto trail = costService.getProcurementExpenditureTrail(projectId);
        LabourHourSummaryDto labour = labourService.getLabourHourSummary(projectId, null, null, null);
        LabourActualCostSummaryDto labourCost = costService.getLabourActualCostSummary(projectId);

        int contractorCount = contractorRepository.findByStatus("ACTIVE").size();
        int totalPos = purchaseOrderRepository.findByProjectId(projectId).size();
        int totalGrns = grnRepository.findByProjectId(projectId).size();

        return ProcurementLabourReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .generatedAt(LocalDateTime.now())
                .totalPurchaseOrders(totalPos)
                .openCommitmentOrdersCount(trail.getOpenCommitments() != null ? trail.getOpenCommitments().size() : 0)
                .totalCommittedAmount(trail.getCommittedCost())
                .totalIncurredGrnCost(trail.getIncurredCost())
                .outstandingLiability(trail.getOutstandingLiability())
                .totalGrnReceiptsLogged(totalGrns)
                .activeContractorsCount(contractorCount)
                .totalHeadcountDeployed(labour.getTotalHeadcount())
                .totalStandardHours(labour.getTotalStandardHours())
                .totalOvertimeHours(labour.getTotalOvertimeHours())
                .totalLabourWageCost(labourCost.getTotalLabourCost())
                .totalDailyAttendanceLogs(labour.getTotalRecordsLogged())
                .build();
    }

    @Override
    public CostQualityReportDto generateCostQualityReport(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        BudgetVarianceReportDto variance = costService.getBudgetVarianceReport(projectId);
        BudgetOverrunReportDto overruns = costService.getBudgetOverruns(projectId);
        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);
        ProjectIssueSummaryDto quality = projectIssueService.getProjectIssueSummary(projectId);

        int totalDelayDays = issueBlockerRepository.findByProjectId(projectId).stream()
                .mapToInt(b -> b.getDaysDelayed() != null ? b.getDaysDelayed() : 0)
                .sum();

        return CostQualityReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .generatedAt(LocalDateTime.now())
                .baselineBudget(variance.getTotalBaselineBudget())
                .revisedBudget(variance.getTotalRevisedBudget())
                .actualCostIncurred(variance.getTotalActualExpense())
                .committedCost(variance.getTotalCommittedExpense())
                .projectedTotalCost(variance.getTotalProjectedCost())
                .netBudgetVariance(variance.getNetVariance())
                .budgetStatus(variance.getOverallStatus())
                .activeBudgetOverrunsCount(overruns.getOverrunCount())
                .totalOverrunAmount(overruns.getTotalOverrunAmount())
                .costPerformanceIndex(evm.getCostPerformanceIndex())
                .schedulePerformanceIndex(evm.getSchedulePerformanceIndex())
                .estimateAtCompletion(evm.getEstimateAtCompletion())
                .varianceAtCompletion(evm.getVarianceAtCompletion())
                .totalIssuesReported(quality.getTotalIssues())
                .openSnagsCount(quality.getSnagCount())
                .activeNcrsCount(quality.getNcrCount())
                .criticalBlockersCount(quality.getBlockerCount())
                .overdueIssuesCount(quality.getOverdueCount())
                .qualityResolutionPercentage(quality.getResolutionPercentage())
                .totalSiteDelayDays(totalDelayDays)
                .build();
    }

    @Override
    public AnalyticsAggregationDto generateAnalyticsAggregation(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);
        ProjectIssueSummaryDto quality = projectIssueService.getIssueSummaryByProject(projectId);
        List<Material> lowStockList = materialService.getLowStockMaterials(projectId);

        long unreadUrgentAlerts = notificationRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD && n.getPriority() == NotificationPriority.URGENT)
                .count();

        // Individual Health Categorizations
        String bHealth = evm.getCostPerformanceIndex().compareTo(new BigDecimal("1.00")) >= 0 ? "ON_BUDGET"
                : (evm.getCostPerformanceIndex().compareTo(new BigDecimal("0.85")) >= 0 ? "WARNING" : "CRITICAL");

        String sHealth = evm.getSchedulePerformanceIndex().compareTo(new BigDecimal("1.00")) >= 0 ? "ON_SCHEDULE" : "DELAYED";

        String iHealth = lowStockList.isEmpty() ? "SUFFICIENT" : "LOW_STOCK";

        String qHealth = (quality.getBlockerCount() == 0 && quality.getOverdueCount() == 0) ? "PASSING" : "AUDIT_FAILURES";

        // Composite Health Calculation (0 to 100)
        double score = 100.0;
        if ("WARNING".equals(bHealth)) score -= 15;
        if ("CRITICAL".equals(bHealth)) score -= 30;
        if ("DELAYED".equals(sHealth)) score -= 15;
        if ("LOW_STOCK".equals(iHealth)) score -= 10;
        if (quality.getBlockerCount() > 0) score -= (quality.getBlockerCount() * 5);
        if (quality.getOverdueCount() > 0) score -= (quality.getOverdueCount() * 5);
        score = Math.max(score, 20.0);

        String overallStatus = score >= 85 ? "OPTIMAL" : (score >= 70 ? "GOOD" : (score >= 50 ? "ATTENTION_REQUIRED" : "CRITICAL"));

        return AnalyticsAggregationDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .projectCode(project.getCode())
                .generatedAt(LocalDateTime.now())
                .overallHealthStatus(overallStatus)
                .compositeHealthScore(score)
                .budgetHealth(bHealth)
                .scheduleHealth(sHealth)
                .inventoryHealth(iHealth)
                .qualityHealth(qHealth)
                .cpi(evm.getCostPerformanceIndex())
                .spi(evm.getSchedulePerformanceIndex())
                .qualityResolutionRate(quality.getResolutionPercentage())
                .activeBlockersCount((int) quality.getBlockerCount())
                .unreadCriticalAlertsCount((int) unreadUrgentAlerts)
                .build();
    }
}
