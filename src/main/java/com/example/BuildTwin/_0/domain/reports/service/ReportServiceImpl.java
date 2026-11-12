package com.example.BuildTwin._0.domain.reports.service;

import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.domain.issues.dto.ProjectIssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.service.ProjectIssueService;
import com.example.BuildTwin._0.domain.labour.dto.LabourHourSummaryDto;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.labour.service.LabourService;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.service.MaterialService;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import com.example.BuildTwin._0.domain.notifications.repository.NotificationRepository;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.domain.reports.dto.*;
import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
    private final LabourDailyRepository labourDailyRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GrnRepository grnRepository;
    private final ActivityRepository activityRepository;

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
    }

    @Override
    public WeeklyProjectReportDto generateWeeklyProjectReport(Long projectId, Integer weekNumber) {
        Project project = getProjectOrThrow(projectId);
        int week = weekNumber != null && weekNumber > 0 ? weekNumber : 10;

        // 1. Cost & EVM Metrics
        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);
        BudgetVarianceReportDto variance = costService.getBudgetVarianceReport(projectId);

        // 2. Labour Metrics - Calculate week-specific bounds with safe fallback
        LocalDate baseDate = project.getActualStartDate() != null ? project.getActualStartDate()
                : (project.getPlannedStartDate() != null ? project.getPlannedStartDate()
                : (project.getCreatedAt() != null ? project.getCreatedAt().toLocalDate() : LocalDate.now()));
        LocalDate weekStartDate = baseDate.plusWeeks(week - 1);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        LabourHourSummaryDto labour = labourService.getLabourHourSummary(projectId, null, weekStartDate, weekEndDate);
        if (labour == null || labour.getTotalRecordsLogged() == 0) {
            // Fallback to all-time cumulative summary if weekly attendance records are not partitioned
            labour = labourService.getLabourHourSummary(projectId, null, null, null);
        }
        LabourActualCostSummaryDto labourCost = costService.getLabourActualCostSummary(projectId);

        // 3. Material & Inventory Metrics (Strict Project Isolation via Stock Ledger)
        List<Material> projectMaterials = stockLedgerRepository.findDistinctMaterialsByProjectId(projectId);
        if (projectMaterials == null || projectMaterials.isEmpty()) {
            List<StockLedger> projectLedgers = stockLedgerRepository.findByProjectId(projectId);
            if (projectLedgers != null && !projectLedgers.isEmpty()) {
                projectMaterials = projectLedgers.stream()
                        .filter(sl -> sl.getMaterial() != null)
                        .map(StockLedger::getMaterial)
                        .distinct()
                        .toList();
            } else {
                projectMaterials = Collections.emptyList();
            }
        }
        int trackedMaterialsCount = projectMaterials.size();
        List<Material> lowStockList = materialService.getLowStockMaterials(projectId);

        BigDecimal materialConsumptionCost = stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId);
        if (materialConsumptionCost == null) {
            materialConsumptionCost = BigDecimal.ZERO;
        }

        // 4. Procurement
        ProcurementExpenditureTrailDto procurement = costService.getProcurementExpenditureTrail(projectId);

        // 5. Quality & Issues (Using DB Aggregate with in-memory fallback)
        ProjectIssueSummaryDto quality = projectIssueService.getProjectIssueSummary(projectId);
        int totalDelayDays = 0;
        try {
            totalDelayDays = issueBlockerRepository.sumDaysDelayedByProjectId(projectId);
        } catch (Exception e) {
            log.debug("Fallback to stream sum for delay days: {}", e.getMessage());
        }
        if (totalDelayDays == 0) {
            List<IssueBlocker> blockers = issueBlockerRepository.findByProjectId(projectId);
            if (blockers != null) {
                totalDelayDays = blockers.stream()
                        .mapToInt(b -> b.getDaysDelayed() != null ? b.getDaysDelayed() : 0)
                        .sum();
            }
        }

        // 6. Unread Notifications (Using DB Aggregate with fallback)
        long unreadAlerts = 0;
        try {
            unreadAlerts = notificationRepository.countByProjectIdAndStatus(projectId, NotificationStatus.UNREAD);
        } catch (Exception e) {
            log.debug("Fallback to stream count for unread alerts: {}", e.getMessage());
        }
        if (unreadAlerts == 0) {
            List<Notification> notifs = notificationRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
            if (notifs != null) {
                unreadAlerts = notifs.stream()
                        .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                        .count();
            }
        }

        // 7. WBS Weightage-based Physical Progress Calculation (Gap 1 Fix)
        BigDecimal plannedProgress = BigDecimal.ZERO;
        BigDecimal actualProgress = BigDecimal.ZERO;

        List<Activity> activities = activityRepository != null ? activityRepository.findByProjectId(projectId) : Collections.emptyList();
        if (activities != null && !activities.isEmpty()) {
            BigDecimal totalWeightage = BigDecimal.ZERO;
            BigDecimal weightedActualSum = BigDecimal.ZERO;
            BigDecimal weightedPlannedSum = BigDecimal.ZERO;
            LocalDate today = LocalDate.now();

            for (Activity act : activities) {
                BigDecimal weight = act.getWeightage() != null ? act.getWeightage() : BigDecimal.ONE;
                totalWeightage = totalWeightage.add(weight);

                // Activity actual completion progress %
                BigDecimal actActualPct = BigDecimal.ZERO;
                if ("COMPLETED".equalsIgnoreCase(act.getStatus())) {
                    actActualPct = new BigDecimal("100.00");
                } else if ("IN_PROGRESS".equalsIgnoreCase(act.getStatus())) {
                    actActualPct = new BigDecimal("50.00");
                }
                weightedActualSum = weightedActualSum.add(actActualPct.multiply(weight));

                // Activity planned schedule progress %
                BigDecimal actPlannedPct = BigDecimal.ZERO;
                if (act.getPlannedEndDate() != null && !today.isBefore(act.getPlannedEndDate())) {
                    actPlannedPct = new BigDecimal("100.00");
                } else if (act.getPlannedStartDate() != null && today.isBefore(act.getPlannedStartDate())) {
                    actPlannedPct = BigDecimal.ZERO;
                } else if (act.getPlannedStartDate() != null && act.getPlannedEndDate() != null
                        && !act.getPlannedEndDate().isEqual(act.getPlannedStartDate())) {
                    long totalDays = ChronoUnit.DAYS.between(act.getPlannedStartDate(), act.getPlannedEndDate());
                    long passedDays = ChronoUnit.DAYS.between(act.getPlannedStartDate(), today);
                    if (totalDays > 0) {
                        actPlannedPct = BigDecimal.valueOf(passedDays).multiply(new BigDecimal("100"))
                                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                                .max(BigDecimal.ZERO).min(new BigDecimal("100.00"));
                    }
                } else if ("COMPLETED".equalsIgnoreCase(act.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(act.getStatus())) {
                    actPlannedPct = new BigDecimal("100.00");
                }
                weightedPlannedSum = weightedPlannedSum.add(actPlannedPct.multiply(weight));
            }

            if (totalWeightage.compareTo(BigDecimal.ZERO) > 0) {
                actualProgress = weightedActualSum.divide(totalWeightage, 2, RoundingMode.HALF_UP);
                plannedProgress = weightedPlannedSum.divide(totalWeightage, 2, RoundingMode.HALF_UP);
            }
        }

        // Fallback to EVM Earned/Planned Value progress if WBS activities are not yet populated
        BigDecimal bac = evm != null && evm.getBudgetAtCompletion() != null ? evm.getBudgetAtCompletion() : BigDecimal.ZERO;
        if (plannedProgress.compareTo(BigDecimal.ZERO) == 0 && bac.compareTo(BigDecimal.ZERO) > 0 && evm != null && evm.getPlannedValue() != null) {
            plannedProgress = evm.getPlannedValue().multiply(new BigDecimal("100")).divide(bac, 2, RoundingMode.HALF_UP);
        }
        if (actualProgress.compareTo(BigDecimal.ZERO) == 0 && bac.compareTo(BigDecimal.ZERO) > 0 && evm != null && evm.getEarnedValue() != null) {
            actualProgress = evm.getEarnedValue().multiply(new BigDecimal("100")).divide(bac, 2, RoundingMode.HALF_UP);
        } else if (actualProgress.compareTo(BigDecimal.ZERO) == 0 && variance != null && variance.getTotalBaselineBudget() != null
                && variance.getTotalBaselineBudget().compareTo(BigDecimal.ZERO) > 0 && variance.getTotalActualExpense() != null) {
            actualProgress = variance.getTotalActualExpense().multiply(new BigDecimal("100")).divide(variance.getTotalBaselineBudget(), 2, RoundingMode.HALF_UP);
        }

        BigDecimal cpi = evm != null && evm.getCostPerformanceIndex() != null ? evm.getCostPerformanceIndex() : BigDecimal.ONE;
        BigDecimal spi = evm != null && evm.getSchedulePerformanceIndex() != null ? evm.getSchedulePerformanceIndex() : BigDecimal.ONE;
        String scheduleStatus = spi.compareTo(BigDecimal.ONE) >= 0 ? "ON_SCHEDULE" : "BEHIND_SCHEDULE";

        return WeeklyProjectReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .projectCode(project.getCode())
                .weekNumber(week)
                .generatedAt(LocalDateTime.now())
                .plannedProgressPercentage(plannedProgress)
                .actualProgressPercentage(actualProgress)
                .scheduleStatus(scheduleStatus)
                .totalHeadcount(labour != null ? labour.getTotalHeadcount() : 0)
                .totalStandardHours(labour != null ? labour.getTotalStandardHours() : BigDecimal.ZERO)
                .totalOvertimeHours(labour != null ? labour.getTotalOvertimeHours() : BigDecimal.ZERO)
                .totalLabourCost(labourCost != null ? labourCost.getTotalLabourCost() : BigDecimal.ZERO)
                .materialsTrackedCount(trackedMaterialsCount)
                .lowStockAlertsCount(lowStockList != null ? lowStockList.size() : 0)
                .totalMaterialConsumptionCost(materialConsumptionCost)
                .totalPurchaseOrdersCount(procurement != null ? procurement.getTotalPoCount() : 0)
                .committedProcurementCost(procurement != null ? procurement.getCommittedCost() : BigDecimal.ZERO)
                .incurredGrnCost(procurement != null ? procurement.getIncurredCost() : BigDecimal.ZERO)
                .outstandingLiability(procurement != null ? procurement.getOutstandingLiability() : BigDecimal.ZERO)
                .baselineBudget(variance != null ? variance.getTotalBaselineBudget() : BigDecimal.ZERO)
                .actualExpense(variance != null ? variance.getTotalActualExpense() : BigDecimal.ZERO)
                .costVariance(variance != null ? variance.getNetVariance() : BigDecimal.ZERO)
                .costPerformanceIndex(cpi)
                .schedulePerformanceIndex(spi)
                .estimateAtCompletion(evm != null ? evm.getEstimateAtCompletion() : BigDecimal.ZERO)
                .costHealthStatus(variance != null ? variance.getOverallStatus() : "ON_BUDGET")
                .openSnagsCount(quality != null ? quality.getSnagCount() : 0L)
                .activeNcrsCount(quality != null ? quality.getNcrCount() : 0L)
                .criticalBlockersCount(quality != null ? quality.getBlockerCount() : 0L)
                .totalSiteDelayDays(totalDelayDays)
                .unreadAlertsCount(unreadAlerts)
                .build();
    }

    @Override
    public InventoryReportDto generateInventoryReport(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        // Gap 2 Fix: Multi-Project Isolation Gap resolved by querying strictly project-scoped materials
        List<Material> targetMaterials = stockLedgerRepository.findDistinctMaterialsByProjectId(projectId);
        if (targetMaterials == null || targetMaterials.isEmpty()) {
            List<StockLedger> projectLedgers = stockLedgerRepository.findByProjectId(projectId);
            if (projectLedgers != null && !projectLedgers.isEmpty()) {
                targetMaterials = projectLedgers.stream()
                        .filter(sl -> sl.getMaterial() != null)
                        .map(StockLedger::getMaterial)
                        .distinct()
                        .toList();
            } else {
                targetMaterials = Collections.emptyList();
            }
        }

        List<Material> lowStockList = materialService.getLowStockMaterials(projectId);

        BigDecimal totalValuation = BigDecimal.ZERO;
        for (Material m : targetMaterials) {
            BigDecimal stock = m.getCurrentStock() != null ? m.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal rate = m.getStandardRate() != null ? m.getStandardRate() : BigDecimal.ZERO;
            totalValuation = totalValuation.add(stock.multiply(rate));
        }
        totalValuation = totalValuation.setScale(2, RoundingMode.HALF_UP);

        MaterialActualCostSummaryDto matCostSummary = costService.getMaterialActualCostSummary(projectId);
        BigDecimal consumedQty = matCostSummary != null && matCostSummary.getTotalQuantityConsumed() != null
                ? matCostSummary.getTotalQuantityConsumed() : BigDecimal.ZERO;
        BigDecimal consumedCost = matCostSummary != null && matCostSummary.getTotalMaterialCost() != null
                ? matCostSummary.getTotalMaterialCost() : BigDecimal.ZERO;

        List<InventoryReportDto.LowStockItemDto> lowStockItems = new ArrayList<>();
        if (lowStockList != null) {
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
        }

        return InventoryReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .generatedAt(LocalDateTime.now())
                .totalMaterialsCataloged(targetMaterials.size())
                .totalInventoryValuation(totalValuation)
                .lowStockItemsCount(lowStockItems.size())
                .totalConsumedQuantity(consumedQty)
                .totalConsumptionCost(consumedCost)
                .criticalLowStockItems(lowStockItems)
                .build();
    }

    @Override
    public ProcurementLabourReportDto generateProcurementLabourReport(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        ProcurementExpenditureTrailDto trail = costService.getProcurementExpenditureTrail(projectId);
        LabourHourSummaryDto labour = labourService.getLabourHourSummary(projectId, null, null, null);
        LabourActualCostSummaryDto labourCost = costService.getLabourActualCostSummary(projectId);

        // Gap 5 Fix: Strict Project-allocated contractor count (Never leak global contractors)
        int contractorCount = 0;
        if (labourDailyRepository != null) {
            try {
                contractorCount = labourDailyRepository.countDistinctContractorsByProjectId(projectId);
            } catch (Exception e) {
                log.debug("Error querying project contractors: {}", e.getMessage());
            }
        }

        // DB aggregate counts with in-memory list fallback
        int totalPos = 0;
        try {
            totalPos = (int) purchaseOrderRepository.countByProjectId(projectId);
        } catch (Exception e) {
            log.debug("Fallback to findByProjectId for PO count: {}", e.getMessage());
        }
        if (totalPos == 0) {
            List<PurchaseOrder> pos = purchaseOrderRepository.findByProjectId(projectId);
            totalPos = pos != null ? pos.size() : 0;
        }

        int totalGrns = 0;
        try {
            totalGrns = (int) grnRepository.countByProjectId(projectId);
        } catch (Exception e) {
            log.debug("Fallback to findByProjectId for GRN count: {}", e.getMessage());
        }
        if (totalGrns == 0) {
            List<Grn> grns = grnRepository.findByProjectId(projectId);
            totalGrns = grns != null ? grns.size() : 0;
        }

        return ProcurementLabourReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .generatedAt(LocalDateTime.now())
                .totalPurchaseOrders(totalPos)
                .openCommitmentOrdersCount(trail != null && trail.getOpenCommitments() != null ? trail.getOpenCommitments().size() : 0)
                .totalCommittedAmount(trail != null ? trail.getCommittedCost() : BigDecimal.ZERO)
                .totalIncurredGrnCost(trail != null ? trail.getIncurredCost() : BigDecimal.ZERO)
                .outstandingLiability(trail != null ? trail.getOutstandingLiability() : BigDecimal.ZERO)
                .totalGrnReceiptsLogged(totalGrns)
                .activeContractorsCount(contractorCount)
                .totalHeadcountDeployed(labour != null ? labour.getTotalHeadcount() : 0)
                .totalStandardHours(labour != null ? labour.getTotalStandardHours() : BigDecimal.ZERO)
                .totalOvertimeHours(labour != null ? labour.getTotalOvertimeHours() : BigDecimal.ZERO)
                .totalLabourWageCost(labourCost != null ? labourCost.getTotalLabourCost() : BigDecimal.ZERO)
                .totalDailyAttendanceLogs(labour != null ? labour.getTotalRecordsLogged() : 0)
                .build();
    }

    @Override
    public CostQualityReportDto generateCostQualityReport(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        BudgetVarianceReportDto variance = costService.getBudgetVarianceReport(projectId);
        BudgetOverrunReportDto overruns = costService.getBudgetOverruns(projectId);
        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);
        ProjectIssueSummaryDto quality = projectIssueService.getProjectIssueSummary(projectId);

        int totalDelayDays = 0;
        try {
            totalDelayDays = issueBlockerRepository.sumDaysDelayedByProjectId(projectId);
        } catch (Exception e) {
            log.debug("Fallback to findByProjectId for blocker delay days: {}", e.getMessage());
        }
        if (totalDelayDays == 0) {
            List<IssueBlocker> blockers = issueBlockerRepository.findByProjectId(projectId);
            if (blockers != null) {
                totalDelayDays = blockers.stream()
                        .mapToInt(b -> b.getDaysDelayed() != null ? b.getDaysDelayed() : 0)
                        .sum();
            }
        }

        BigDecimal cpi = evm != null && evm.getCostPerformanceIndex() != null ? evm.getCostPerformanceIndex() : BigDecimal.ONE;
        BigDecimal spi = evm != null && evm.getSchedulePerformanceIndex() != null ? evm.getSchedulePerformanceIndex() : BigDecimal.ONE;

        return CostQualityReportDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .generatedAt(LocalDateTime.now())
                .baselineBudget(variance != null ? variance.getTotalBaselineBudget() : BigDecimal.ZERO)
                .revisedBudget(variance != null ? variance.getTotalRevisedBudget() : BigDecimal.ZERO)
                .actualCostIncurred(variance != null ? variance.getTotalActualExpense() : BigDecimal.ZERO)
                .committedCost(variance != null ? variance.getTotalCommittedExpense() : BigDecimal.ZERO)
                .projectedTotalCost(variance != null ? variance.getTotalProjectedCost() : BigDecimal.ZERO)
                .netBudgetVariance(variance != null ? variance.getNetVariance() : BigDecimal.ZERO)
                .budgetStatus(variance != null ? variance.getOverallStatus() : "ON_BUDGET")
                .activeBudgetOverrunsCount(overruns != null ? overruns.getOverrunCount() : 0)
                .totalOverrunAmount(overruns != null ? overruns.getTotalOverrunAmount() : BigDecimal.ZERO)
                .costPerformanceIndex(cpi)
                .schedulePerformanceIndex(spi)
                .estimateAtCompletion(evm != null ? evm.getEstimateAtCompletion() : BigDecimal.ZERO)
                .varianceAtCompletion(evm != null ? evm.getVarianceAtCompletion() : BigDecimal.ZERO)
                .totalIssuesReported(quality != null ? quality.getTotalIssues() : 0L)
                .openSnagsCount(quality != null ? quality.getSnagCount() : 0L)
                .activeNcrsCount(quality != null ? quality.getNcrCount() : 0L)
                .criticalBlockersCount(quality != null ? quality.getBlockerCount() : 0L)
                .overdueIssuesCount(quality != null ? quality.getOverdueCount() : 0L)
                .qualityResolutionPercentage(quality != null ? quality.getResolutionPercentage() : 100.0)
                .totalSiteDelayDays(totalDelayDays)
                .build();
    }

    @Override
    public AnalyticsAggregationDto generateAnalyticsAggregation(Long projectId) {
        Project project = getProjectOrThrow(projectId);

        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);
        ProjectIssueSummaryDto quality = projectIssueService.getProjectIssueSummary(projectId);
        List<Material> lowStockList = materialService.getLowStockMaterials(projectId);

        long unreadUrgentAlerts = 0;
        try {
            unreadUrgentAlerts = notificationRepository.countByProjectIdAndStatusAndPriority(
                    projectId, NotificationStatus.UNREAD, NotificationPriority.URGENT);
        } catch (Exception e) {
            log.debug("Fallback to stream count for urgent notifications: {}", e.getMessage());
        }
        if (unreadUrgentAlerts == 0) {
            List<Notification> notifs = notificationRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
            if (notifs != null) {
                unreadUrgentAlerts = notifs.stream()
                        .filter(n -> n.getStatus() == NotificationStatus.UNREAD && n.getPriority() == NotificationPriority.URGENT)
                        .count();
            }
        }

        BigDecimal cpi = evm != null && evm.getCostPerformanceIndex() != null ? evm.getCostPerformanceIndex() : BigDecimal.ONE;
        BigDecimal spi = evm != null && evm.getSchedulePerformanceIndex() != null ? evm.getSchedulePerformanceIndex() : BigDecimal.ONE;

        // Individual Health Categorizations
        String bHealth = cpi.compareTo(new BigDecimal("1.00")) >= 0 ? "ON_BUDGET"
                : (cpi.compareTo(new BigDecimal("0.85")) >= 0 ? "WARNING" : "CRITICAL");

        String sHealth = spi.compareTo(new BigDecimal("1.00")) >= 0 ? "ON_SCHEDULE" : "DELAYED";

        String iHealth = (lowStockList == null || lowStockList.isEmpty()) ? "SUFFICIENT" : "LOW_STOCK";

        long blockerCount = quality != null ? quality.getBlockerCount() : 0L;
        long overdueCount = quality != null ? quality.getOverdueCount() : 0L;
        double resolutionPct = quality != null ? quality.getResolutionPercentage() : 100.0;

        String qHealth = (blockerCount == 0 && overdueCount == 0) ? "PASSING" : "AUDIT_FAILURES";

        // Composite Health Calculation (0 to 100)
        double score = 100.0;
        if ("WARNING".equals(bHealth)) score -= 15.0;
        if ("CRITICAL".equals(bHealth)) score -= 30.0;
        if ("DELAYED".equals(sHealth)) score -= 15.0;
        if ("LOW_STOCK".equals(iHealth)) score -= 10.0;
        if (blockerCount > 0) score -= Math.min(blockerCount * 5.0, 20.0);
        if (overdueCount > 0) score -= Math.min(overdueCount * 5.0, 20.0);
        score = Math.max(0.0, Math.min(score, 100.0));

        String overallStatus = score >= 85.0 ? "OPTIMAL" : (score >= 70.0 ? "GOOD" : (score >= 50.0 ? "ATTENTION_REQUIRED" : "CRITICAL"));

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
                .cpi(cpi)
                .spi(spi)
                .qualityResolutionRate(resolutionPct)
                .activeBlockersCount((int) blockerCount)
                .unreadCriticalAlertsCount((int) unreadUrgentAlerts)
                .build();
    }
}
