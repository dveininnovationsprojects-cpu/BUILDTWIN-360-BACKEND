package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.cumulative.*;
import com.example.BuildTwin._0.dto.wbs.UpdateWbsActivityProgressRequest;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.ActivityProgressLogRepository;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.CumulativeProgressService;
import com.example.BuildTwin._0.service.WbsActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CumulativeProgressServiceImpl implements CumulativeProgressService {

    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ActivityProgressLogRepository progressLogRepository;
    private final WbsActivityService wbsActivityService;
    private final AuditService auditService;

    // =========================================================================
    // 1. MASTER PROJECT CUMULATIVE PROGRESS ENGINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public CumulativeProgressResponse getProjectCumulativeProgress(
            Long projectId,
            WeightingModel weightingModel,
            String discipline,
            Long workPackageId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        WeightingModel model = weightingModel != null ? weightingModel : WeightingModel.WEIGHTAGE_FACTOR;

        List<WbsActivity> allActivities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);

        // Apply optional discipline or work package filters
        List<WbsActivity> filtered = allActivities.stream()
                .filter(a -> discipline == null || a.getDiscipline().equalsIgnoreCase(discipline))
                .filter(a -> workPackageId == null || (a.getWorkPackage() != null && a.getWorkPackage().getId().equals(workPackageId)))
                .collect(Collectors.toList());

        List<WbsActivity> leafActivities = filtered.stream()
                .filter(a -> !a.hasChildren())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();

        // Calculate Multi-Weighting Models
        double weightFactorActual = calculateWeightedProgress(leafActivities, WeightingModel.WEIGHTAGE_FACTOR, true, today);
        double weightFactorPlanned = calculateWeightedProgress(leafActivities, WeightingModel.WEIGHTAGE_FACTOR, false, today);

        double budgetWeightedActual = calculateWeightedProgress(leafActivities, WeightingModel.BUDGET_WEIGHTED, true, today);
        double durationWeightedActual = calculateWeightedProgress(leafActivities, WeightingModel.DURATION_WEIGHTED, true, today);
        double equalWeightedActual = calculateWeightedProgress(leafActivities, WeightingModel.EQUAL_WEIGHTED, true, today);

        // Primary selected model results
        double primaryActual = calculateWeightedProgress(leafActivities, model, true, today);
        double primaryPlanned = calculateWeightedProgress(leafActivities, model, false, today);
        double variance = round2(primaryActual - primaryPlanned);

        String overallStatus = determinePerformanceStatus(primaryActual, primaryPlanned);

        // Physical Quantities Rollup
        double totalPlannedQty = round2(leafActivities.stream()
                .mapToDouble(a -> a.getPlannedQuantity() != null ? a.getPlannedQuantity() : 0.0)
                .sum());
        double totalCompletedQty = round2(leafActivities.stream()
                .mapToDouble(a -> a.getCompletedQuantity() != null ? a.getCompletedQuantity() : 0.0)
                .sum());
        double totalBalanceQty = Math.max(0.0, round2(totalPlannedQty - totalCompletedQty));

        // Financial & EVM Calculations
        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        BigDecimal totalBudget = workPackages.stream()
                .map(wp -> wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal earnedValue = totalBudget.multiply(BigDecimal.valueOf(primaryActual / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal plannedValue = totalBudget.multiply(BigDecimal.valueOf(primaryPlanned / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal svAmount = earnedValue.subtract(plannedValue);

        double spi = plannedValue.compareTo(BigDecimal.ZERO) > 0
                ? round2(earnedValue.doubleValue() / plannedValue.doubleValue())
                : 1.0;

        // Activity Status Counts
        int completedCount = 0;
        int inProgressCount = 0;
        int plannedCount = 0;
        int delayedCount = 0;
        int aheadCount = 0;
        int onTrackCount = 0;
        int behindCount = 0;

        for (WbsActivity act : leafActivities) {
            String st = act.getStatus() != null ? act.getStatus().toUpperCase() : "PLANNED";
            if ("COMPLETED".equals(st)) completedCount++;
            else if ("IN_PROGRESS".equals(st)) inProgressCount++;
            else plannedCount++;

            double aPct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
            double pPct = calculatePlannedPercentage(act, today);
            double vPct = aPct - pPct;

            if (vPct >= 5.0) aheadCount++;
            else if (vPct < -5.0) {
                behindCount++;
                delayedCount++;
            } else {
                onTrackCount++;
            }
        }

        // Multi-dimensional Rollups
        List<CumulativeProgressResponse.DisciplineCumulativeRollupDto> disciplineRollup = buildDisciplineRollup(leafActivities, model, today);
        List<CumulativeProgressResponse.WorkPackageCumulativeRollupDto> wpRollup = buildWorkPackageRollup(workPackages, allActivities, model, today);
        List<CumulativeProgressResponse.LocationCumulativeRollupDto> locationRollup = buildLocationRollup(leafActivities, model, today);

        return CumulativeProgressResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .projectStatus(project.getStatus())
                .plannedStartDate(project.getPlannedStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .actualStartDate(project.getActualStartDate())
                .weightingModel(model)
                .overallActualProgressPercentage(primaryActual)
                .overallPlannedProgressPercentage(primaryPlanned)
                .overallProgressVariancePercentage(variance)
                .overallPerformanceStatus(overallStatus)
                .weightageFactorProgressPercentage(weightFactorActual)
                .budgetWeightedProgressPercentage(budgetWeightedActual)
                .durationWeightedProgressPercentage(durationWeightedActual)
                .equalWeightedProgressPercentage(equalWeightedActual)
                .totalProjectBudget(totalBudget)
                .earnedValue(earnedValue)
                .plannedValue(plannedValue)
                .scheduleVarianceAmount(svAmount)
                .schedulePerformanceIndex(spi)
                .totalPlannedQuantity(totalPlannedQty)
                .totalCompletedQuantity(totalCompletedQty)
                .totalBalanceQuantity(totalBalanceQty)
                .totalActivities(leafActivities.size())
                .completedActivities(completedCount)
                .inProgressActivities(inProgressCount)
                .plannedActivities(plannedCount)
                .delayedActivities(delayedCount)
                .aheadActivities(aheadCount)
                .onTrackActivities(onTrackCount)
                .behindActivities(behindCount)
                .disciplineRollup(disciplineRollup)
                .workPackageRollup(wpRollup)
                .locationRollup(locationRollup)
                .build();
    }

    // =========================================================================
    // 2. WORK PACKAGE CUMULATIVE PROGRESS ENGINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public WorkPackageCumulativeProgressResponse getWorkPackageCumulativeProgress(
            Long workPackageId,
            WeightingModel weightingModel) {

        WorkPackage wp = workPackageRepository.findById(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", workPackageId));

        WeightingModel model = weightingModel != null ? weightingModel : WeightingModel.WEIGHTAGE_FACTOR;

        List<WbsActivity> activities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(workPackageId);
        List<WbsActivity> leafActivities = activities.stream()
                .filter(a -> !a.hasChildren())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        double actualPct = calculateWeightedProgress(leafActivities, model, true, today);
        double plannedPct = calculateWeightedProgress(leafActivities, model, false, today);
        double variance = round2(actualPct - plannedPct);
        String perfStatus = determinePerformanceStatus(actualPct, plannedPct);

        BigDecimal budget = wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO;
        BigDecimal earnedValue = budget.multiply(BigDecimal.valueOf(actualPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal plannedValue = budget.multiply(BigDecimal.valueOf(plannedPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
        double spi = plannedValue.compareTo(BigDecimal.ZERO) > 0
                ? round2(earnedValue.doubleValue() / plannedValue.doubleValue())
                : 1.0;

        int completed = (int) leafActivities.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        int inProgress = (int) leafActivities.stream().filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus())).count();
        int planned = leafActivities.size() - completed - inProgress;

        List<ActivityCumulativeProgressResponse> actResponses = activities.stream()
                .filter(a -> a.getParent() == null) // Root tasks in package
                .map(this::mapActivityCumulative)
                .collect(Collectors.toList());

        Project prj = wp.getProject();

        return WorkPackageCumulativeProgressResponse.builder()
                .workPackageId(wp.getId())
                .workPackageCode(wp.getCode())
                .workPackageName(wp.getName())
                .discipline(wp.getDiscipline())
                .projectId(prj != null ? prj.getId() : null)
                .projectName(prj != null ? prj.getName() : null)
                .weightingModel(model)
                .cumulativeActualProgressPercentage(actualPct)
                .cumulativePlannedProgressPercentage(plannedPct)
                .progressVariancePercentage(variance)
                .performanceStatus(perfStatus)
                .budgetAmount(budget)
                .earnedValue(earnedValue)
                .plannedValue(plannedValue)
                .spi(spi)
                .totalActivities(leafActivities.size())
                .completedActivities(completed)
                .inProgressActivities(inProgress)
                .plannedActivities(planned)
                .plannedStartDate(wp.getPlannedStartDate())
                .plannedEndDate(wp.getPlannedEndDate())
                .actualStartDate(wp.getActualStartDate())
                .actualEndDate(wp.getActualEndDate())
                .activities(actResponses)
                .build();
    }

    // =========================================================================
    // 3. ACTIVITY CUMULATIVE PROGRESS ENGINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ActivityCumulativeProgressResponse getActivityCumulativeProgress(Long activityId) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));
        return mapActivityCumulative(act);
    }

    // =========================================================================
    // 4. HISTORICAL AS-OF-DATE CUMULATIVE PROGRESS ENGINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public AsOfDateProgressResponse getAsOfDateCumulativeProgress(Long projectId, LocalDate cutoffDate) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        LocalDate targetDate = cutoffDate != null ? cutoffDate : LocalDate.now();

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<WbsActivity> leafActivities = activities.stream()
                .filter(a -> !a.hasChildren())
                .collect(Collectors.toList());

        List<AsOfDateProgressResponse.ActivityAsOfDateSnapshot> snapshots = new ArrayList<>();
        double weightedActualSum = 0.0;
        double weightedPlannedSum = 0.0;
        double totalWeight = 0.0;
        double totalCompletedQty = 0.0;
        int completedCount = 0;
        int inProgressCount = 0;

        for (WbsActivity act : leafActivities) {
            Double completedAtCutoff = progressLogRepository.sumQuantityByActivityIdAndLogDateLessThanEqual(act.getId(), targetDate);
            if (completedAtCutoff == null) completedAtCutoff = 0.0;

            double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
            double actualPct = planned > 0.0 ? Math.min(100.0, round2((completedAtCutoff / planned) * 100.0)) : 0.0;
            double plannedPct = calculatePlannedPercentage(act, targetDate);
            double variancePct = round2(actualPct - plannedPct);

            double weight = act.getWeightage() != null && act.getWeightage() > 0 ? act.getWeightage() : 1.0;
            weightedActualSum += actualPct * weight;
            weightedPlannedSum += plannedPct * weight;
            totalWeight += weight;
            totalCompletedQty += completedAtCutoff;

            String statusAtDate;
            if (actualPct >= 100.0) {
                statusAtDate = "COMPLETED";
                completedCount++;
            } else if (completedAtCutoff > 0.0 || (act.getActualStartDate() != null && !act.getActualStartDate().isAfter(targetDate))) {
                statusAtDate = "IN_PROGRESS";
                inProgressCount++;
            } else {
                statusAtDate = "PLANNED";
            }

            snapshots.add(AsOfDateProgressResponse.ActivityAsOfDateSnapshot.builder()
                    .activityId(act.getId())
                    .activityCode(act.getCode())
                    .activityName(act.getName())
                    .discipline(act.getDiscipline())
                    .uom(act.getUom())
                    .plannedQuantity(planned)
                    .cumulativeCompletedQuantity(round2(completedAtCutoff))
                    .actualProgressPercentage(actualPct)
                    .plannedProgressPercentage(plannedPct)
                    .variancePercentage(variancePct)
                    .status(statusAtDate)
                    .build());
        }

        double overallActualPct = totalWeight > 0 ? round2(weightedActualSum / totalWeight) : 0.0;
        double overallPlannedPct = totalWeight > 0 ? round2(weightedPlannedSum / totalWeight) : 0.0;
        double overallVariancePct = round2(overallActualPct - overallPlannedPct);
        String perfStatus = determinePerformanceStatus(overallActualPct, overallPlannedPct);

        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        BigDecimal totalBudget = workPackages.stream()
                .map(wp -> wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ev = totalBudget.multiply(BigDecimal.valueOf(overallActualPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pv = totalBudget.multiply(BigDecimal.valueOf(overallPlannedPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
        double spi = pv.compareTo(BigDecimal.ZERO) > 0 ? round2(ev.doubleValue() / pv.doubleValue()) : 1.0;

        List<ActivityProgressLog> logsUpToCutoff = progressLogRepository.findByProjectIdAndLogDateLessThanEqualOrderByLogDateAsc(projectId, targetDate);

        return AsOfDateProgressResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .cutoffDate(targetDate)
                .cumulativeActualProgressPercentage(overallActualPct)
                .cumulativePlannedProgressPercentage(overallPlannedPct)
                .progressVariancePercentage(overallVariancePct)
                .performanceStatus(perfStatus)
                .totalCompletedQuantityAsOfDate(round2(totalCompletedQty))
                .earnedValueAsOfDate(ev)
                .plannedValueAsOfDate(pv)
                .spiAsOfDate(spi)
                .completedActivitiesCount(completedCount)
                .inProgressActivitiesCount(inProgressCount)
                .totalLogsUpToCutoffDate((long) logsUpToCutoff.size())
                .activitySnapshots(snapshots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AsOfDateProgressResponse.ActivityAsOfDateSnapshot getActivityAsOfDateProgress(Long activityId, LocalDate cutoffDate) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        LocalDate targetDate = cutoffDate != null ? cutoffDate : LocalDate.now();

        Double completedAtCutoff = progressLogRepository.sumQuantityByActivityIdAndLogDateLessThanEqual(activityId, targetDate);
        if (completedAtCutoff == null) completedAtCutoff = 0.0;

        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double actualPct = planned > 0.0 ? Math.min(100.0, round2((completedAtCutoff / planned) * 100.0)) : 0.0;
        double plannedPct = calculatePlannedPercentage(act, targetDate);
        double variancePct = round2(actualPct - plannedPct);

        String statusAtDate;
        if (actualPct >= 100.0) statusAtDate = "COMPLETED";
        else if (completedAtCutoff > 0.0 || (act.getActualStartDate() != null && !act.getActualStartDate().isAfter(targetDate))) statusAtDate = "IN_PROGRESS";
        else statusAtDate = "PLANNED";

        return AsOfDateProgressResponse.ActivityAsOfDateSnapshot.builder()
                .activityId(act.getId())
                .activityCode(act.getCode())
                .activityName(act.getName())
                .discipline(act.getDiscipline())
                .uom(act.getUom())
                .plannedQuantity(planned)
                .cumulativeCompletedQuantity(round2(completedAtCutoff))
                .actualProgressPercentage(actualPct)
                .plannedProgressPercentage(plannedPct)
                .variancePercentage(variancePct)
                .status(statusAtDate)
                .build();
    }

    // =========================================================================
    // 5. CUMULATIVE S-CURVE GENERATION ENGINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public CumulativeSCurveResponse getProjectCumulativeSCurve(Long projectId, String interval) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String timeStep = (interval != null && !interval.isBlank()) ? interval.toUpperCase() : "WEEKLY";

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<WbsActivity> leafActivities = activities.stream()
                .filter(a -> !a.hasChildren())
                .collect(Collectors.toList());

        LocalDate start = project.getPlannedStartDate();
        LocalDate end = project.getPlannedEndDate();

        if (start == null || end == null) {
            start = leafActivities.stream()
                    .map(WbsActivity::getPlannedStartDate)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDate.now().minusMonths(1));
            end = leafActivities.stream()
                    .map(WbsActivity::getPlannedEndDate)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(LocalDate.now().plusMonths(3));
        }

        List<LocalDate> bucketDates = generateTimelineBuckets(start, end, timeStep);

        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        BigDecimal totalBudget = workPackages.stream()
                .map(wp -> wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SCurveDataPointDto> points = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (LocalDate d : bucketDates) {
            double plannedPct = calculateWeightedProgress(leafActivities, WeightingModel.WEIGHTAGE_FACTOR, false, d);

            Double actualPct = null;
            BigDecimal ev = null;
            Double spi = null;
            Double cumQty = null;

            // Only calculate actual progress for dates up to today or if progress logs exist
            if (!d.isAfter(today.plusDays(3))) {
                double weightedSum = 0.0;
                double totalWeight = 0.0;
                double qtySum = 0.0;

                for (WbsActivity act : leafActivities) {
                    Double qtyAtD = progressLogRepository.sumQuantityByActivityIdAndLogDateLessThanEqual(act.getId(), d);
                    double q = qtyAtD != null ? qtyAtD : 0.0;
                    qtySum += q;
                    double p = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
                    double actPct = p > 0 ? Math.min(100.0, (q / p) * 100.0) : 0.0;
                    double w = act.getWeightage() != null && act.getWeightage() > 0 ? act.getWeightage() : 1.0;
                    weightedSum += actPct * w;
                    totalWeight += w;
                }
                actualPct = totalWeight > 0 ? round2(weightedSum / totalWeight) : 0.0;
                cumQty = round2(qtySum);
                ev = totalBudget.multiply(BigDecimal.valueOf(actualPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal pv = totalBudget.multiply(BigDecimal.valueOf(plannedPct / 100.0)).setScale(2, RoundingMode.HALF_UP);

            if (actualPct != null && pv.compareTo(BigDecimal.ZERO) > 0 && ev != null) {
                spi = round2(ev.doubleValue() / pv.doubleValue());
            }

            Double varPct = actualPct != null ? round2(actualPct - plannedPct) : null;
            String st = actualPct != null ? determinePerformanceStatus(actualPct, plannedPct) : "PROJECTED";

            points.add(SCurveDataPointDto.builder()
                    .date(d)
                    .plannedProgressPercentage(plannedPct)
                    .actualProgressPercentage(actualPct)
                    .progressVariancePercentage(varPct)
                    .plannedValue(pv)
                    .earnedValue(ev)
                    .schedulePerformanceIndex(spi)
                    .cumulativeQuantityCompleted(cumQty)
                    .status(st)
                    .build());
        }

        double curActual = calculateWeightedProgress(leafActivities, WeightingModel.WEIGHTAGE_FACTOR, true, today);
        double curPlanned = calculateWeightedProgress(leafActivities, WeightingModel.WEIGHTAGE_FACTOR, false, today);
        BigDecimal curEv = totalBudget.multiply(BigDecimal.valueOf(curActual / 100.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal curPv = totalBudget.multiply(BigDecimal.valueOf(curPlanned / 100.0)).setScale(2, RoundingMode.HALF_UP);
        double curSpi = curPv.compareTo(BigDecimal.ZERO) > 0 ? round2(curEv.doubleValue() / curPv.doubleValue()) : 1.0;

        return CumulativeSCurveResponse.builder()
                .scope("PROJECT")
                .entityId(project.getId())
                .entityName(project.getName())
                .entityCode(project.getCode())
                .interval(timeStep)
                .timelineStartDate(start)
                .timelineEndDate(end)
                .currentActualPercentage(curActual)
                .currentPlannedPercentage(curPlanned)
                .totalBudget(totalBudget)
                .totalEarnedValue(curEv)
                .totalPlannedValue(curPv)
                .currentSpi(curSpi)
                .overallStatus(determinePerformanceStatus(curActual, curPlanned))
                .curveData(points)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CumulativeSCurveResponse getActivityCumulativeSCurve(Long activityId, String interval) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        String timeStep = (interval != null && !interval.isBlank()) ? interval.toUpperCase() : "WEEKLY";

        LocalDate start = act.getPlannedStartDate() != null ? act.getPlannedStartDate() : LocalDate.now().minusWeeks(2);
        LocalDate end = act.getPlannedEndDate() != null ? act.getPlannedEndDate() : LocalDate.now().plusWeeks(4);

        List<LocalDate> bucketDates = generateTimelineBuckets(start, end, timeStep);
        List<SCurveDataPointDto> points = new ArrayList<>();
        LocalDate today = LocalDate.now();

        double plannedQty = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;

        for (LocalDate d : bucketDates) {
            double plannedPct = calculatePlannedPercentage(act, d);
            Double actualPct = null;
            Double cumQty = null;

            if (!d.isAfter(today.plusDays(3))) {
                Double q = progressLogRepository.sumQuantityByActivityIdAndLogDateLessThanEqual(activityId, d);
                cumQty = q != null ? round2(q) : 0.0;
                actualPct = plannedQty > 0 ? Math.min(100.0, round2((cumQty / plannedQty) * 100.0)) : 0.0;
            }

            Double varPct = actualPct != null ? round2(actualPct - plannedPct) : null;
            String st = actualPct != null ? determinePerformanceStatus(actualPct, plannedPct) : "PROJECTED";

            points.add(SCurveDataPointDto.builder()
                    .date(d)
                    .date(d)
                    .plannedProgressPercentage(plannedPct)
                    .actualProgressPercentage(actualPct)
                    .progressVariancePercentage(varPct)
                    .cumulativeQuantityCompleted(cumQty)
                    .status(st)
                    .build());
        }

        double curActual = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
        double curPlanned = calculatePlannedPercentage(act, today);

        return CumulativeSCurveResponse.builder()
                .scope("ACTIVITY")
                .entityId(act.getId())
                .entityName(act.getName())
                .entityCode(act.getCode())
                .interval(timeStep)
                .timelineStartDate(start)
                .timelineEndDate(end)
                .currentActualPercentage(curActual)
                .currentPlannedPercentage(curPlanned)
                .overallStatus(determinePerformanceStatus(curActual, curPlanned))
                .curveData(points)
                .build();
    }

    // =========================================================================
    // 6. ON-DEMAND HIERARCHICAL RECALCULATION & SYNC ENGINE
    // =========================================================================

    @Override
    @Transactional
    public CumulativeProgressResponse recalculateProjectCumulativeProgress(
            Long projectId,
            RecalculateProgressRequest request,
            String performedBy) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        boolean syncDates = request == null || request.getSyncActualDates() == null || request.getSyncActualDates();
        boolean autoComplete = request == null || request.getAutoCompleteStatus() == null || request.getAutoCompleteStatus();
        WeightingModel model = (request != null && request.getWeightingModel() != null)
                ? request.getWeightingModel() : WeightingModel.WEIGHTAGE_FACTOR;

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);

        // 1. Recalculate all leaf tasks first from their verified progress logs
        for (WbsActivity act : activities) {
            if (!act.hasChildren()) {
                recalculateLeafActivity(act, syncDates, autoComplete);
            }
        }

        // 2. Perform recursive bottom-up rollup for parent tasks
        recalculateParentTasks(activities);

        // 3. Save all updated activities
        wbsActivityRepository.saveAll(activities);

        // 4. Update Work Packages actual dates and statuses
        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        for (WorkPackage wp : workPackages) {
            List<WbsActivity> wpActs = activities.stream()
                    .filter(a -> a.getWorkPackage() != null && a.getWorkPackage().getId().equals(wp.getId()))
                    .collect(Collectors.toList());

            if (!wpActs.isEmpty()) {
                boolean allCompleted = wpActs.stream().allMatch(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()));
                boolean anyStarted = wpActs.stream().anyMatch(a -> !"PLANNED".equalsIgnoreCase(a.getStatus()));

                if (allCompleted) wp.setStatus("COMPLETED");
                else if (anyStarted) wp.setStatus("IN_PROGRESS");
                else wp.setStatus("PLANNED");

                LocalDate minStart = wpActs.stream()
                        .map(WbsActivity::getActualStartDate)
                        .filter(Objects::nonNull)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
                if (minStart != null) wp.setActualStartDate(minStart);

                if (allCompleted) {
                    LocalDate maxEnd = wpActs.stream()
                            .map(WbsActivity::getActualEndDate)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(LocalDate.now());
                    wp.setActualEndDate(maxEnd);
                }
            }
        }
        workPackageRepository.saveAll(workPackages);

        auditService.logAction(
                performedBy != null ? performedBy : "SYSTEM",
                "RECALCULATE_CUMULATIVE_PROGRESS",
                "PROJECT",
                String.valueOf(projectId),
                "Recalculated and synchronized cumulative progress for Project " + project.getCode() + " across " + activities.size() + " activities",
                null
        );

        return getProjectCumulativeProgress(projectId, model, null, null);
    }

    @Override
    @Transactional
    public WorkPackageCumulativeProgressResponse recalculateWorkPackageCumulativeProgress(
            Long workPackageId,
            RecalculateProgressRequest request,
            String performedBy) {

        WorkPackage wp = workPackageRepository.findById(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", workPackageId));

        boolean syncDates = request == null || request.getSyncActualDates() == null || request.getSyncActualDates();
        boolean autoComplete = request == null || request.getAutoCompleteStatus() == null || request.getAutoCompleteStatus();
        WeightingModel model = (request != null && request.getWeightingModel() != null)
                ? request.getWeightingModel() : WeightingModel.WEIGHTAGE_FACTOR;

        List<WbsActivity> activities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(workPackageId);

        for (WbsActivity act : activities) {
            if (!act.hasChildren()) {
                recalculateLeafActivity(act, syncDates, autoComplete);
            }
        }
        recalculateParentTasks(activities);
        wbsActivityRepository.saveAll(activities);

        auditService.logAction(
                performedBy != null ? performedBy : "SYSTEM",
                "RECALCULATE_CUMULATIVE_PROGRESS",
                "WORK_PACKAGE",
                String.valueOf(workPackageId),
                "Recalculated cumulative progress for Work Package " + wp.getCode(),
                null
        );

        return getWorkPackageCumulativeProgress(workPackageId, model);
    }

    @Override
    @Transactional
    public ActivityCumulativeProgressResponse recalculateActivityCumulativeProgress(Long activityId, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        if (!act.hasChildren()) {
            recalculateLeafActivity(act, true, true);
        } else {
            // Recalculate subtasks tree
            List<WbsActivity> children = wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(activityId);
            for (WbsActivity c : children) {
                if (!c.hasChildren()) recalculateLeafActivity(c, true, true);
            }
            wbsActivityRepository.saveAll(children);
            recalculateParentTasks(List.of(act));
        }

        WbsActivity saved = wbsActivityRepository.save(act);

        auditService.logAction(
                performedBy != null ? performedBy : "SYSTEM",
                "RECALCULATE_CUMULATIVE_PROGRESS",
                "WBS_ACTIVITY",
                String.valueOf(activityId),
                "Recalculated cumulative progress for Activity " + saved.getCode(),
                null
        );

        return mapActivityCumulative(saved);
    }

    // =========================================================================
    // 7. CUMULATIVE PROGRESS AUDIT & RECONCILIATION ENGINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ProgressAuditReportResponse auditCumulativeProgress(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<WbsActivity> leafActivities = activities.stream()
                .filter(a -> !a.hasChildren())
                .collect(Collectors.toList());

        List<ProgressAuditItemDto> discrepancies = new ArrayList<>();
        int cleanCount = 0;
        int qtyMismatchCount = 0;
        int pctMismatchCount = 0;
        int overExecutedCount = 0;

        for (WbsActivity act : leafActivities) {
            Double verifiedQty = progressLogRepository.sumQuantityByActivityId(act.getId());
            if (verifiedQty == null) verifiedQty = 0.0;
            verifiedQty = round2(verifiedQty);

            double storedQty = act.getCompletedQuantity() != null ? round2(act.getCompletedQuantity()) : 0.0;
            double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
            double storedPct = act.getProgressPercentage() != null ? round2(act.getProgressPercentage()) : 0.0;
            double calculatedPct = planned > 0 ? Math.min(100.0, round2((verifiedQty / planned) * 100.0)) : 100.0;

            double qtyDelta = round2(verifiedQty - storedQty);
            double pctDelta = round2(calculatedPct - storedPct);

            boolean hasQtyMismatch = Math.abs(qtyDelta) > 0.01;
            boolean hasPctMismatch = Math.abs(pctDelta) > 0.1;
            boolean isOverExecuted = planned > 0 && verifiedQty > planned + 0.01;

            if (hasQtyMismatch || hasPctMismatch || isOverExecuted) {
                String type;
                String desc;
                String severity;

                if (isOverExecuted) {
                    type = "OVER_EXECUTED";
                    desc = "Completed output (" + verifiedQty + " " + act.getUom() + ") exceeds planned baseline (" + planned + " " + act.getUom() + ") by " + round2(verifiedQty - planned) + " " + act.getUom();
                    severity = "HIGH";
                    overExecutedCount++;
                } else if (hasQtyMismatch) {
                    type = "QUANTITY_MISMATCH";
                    desc = "Stored activity quantity (" + storedQty + ") does not match verified progress journal logs sum (" + verifiedQty + "). Delta: " + qtyDelta;
                    severity = "MEDIUM";
                    qtyMismatchCount++;
                } else {
                    type = "PERCENTAGE_MISMATCH";
                    desc = "Stored progress percentage (" + storedPct + "%) differs from computed percentage (" + calculatedPct + "%). Delta: " + pctDelta + "%";
                    severity = "LOW";
                    pctMismatchCount++;
                }

                discrepancies.add(ProgressAuditItemDto.builder()
                        .activityId(act.getId())
                        .activityCode(act.getCode())
                        .activityName(act.getName())
                        .workPackageCode(act.getWorkPackage() != null ? act.getWorkPackage().getCode() : null)
                        .storedCompletedQuantity(storedQty)
                        .verifiedCompletedQuantityFromLogs(verifiedQty)
                        .storedProgressPercentage(storedPct)
                        .calculatedProgressPercentage(calculatedPct)
                        .quantityDelta(qtyDelta)
                        .percentageDelta(pctDelta)
                        .discrepancyType(type)
                        .description(desc)
                        .severity(severity)
                        .build());
            } else {
                cleanCount++;
            }
        }

        String integrityStatus;
        if (discrepancies.isEmpty()) {
            integrityStatus = "HEALTHY";
        } else if (overExecutedCount > 0 || qtyMismatchCount > 5) {
            integrityStatus = "ACTION_REQUIRED";
        } else {
            integrityStatus = "MINOR_DISCREPANCIES";
        }

        return ProgressAuditReportResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .auditTimestamp(LocalDateTime.now())
                .totalActivitiesAudited(leafActivities.size())
                .cleanActivitiesCount(cleanCount)
                .discrepantActivitiesCount(discrepancies.size())
                .quantityMismatchesCount(qtyMismatchCount)
                .percentageMismatchesCount(pctMismatchCount)
                .overExecutedActivitiesCount(overExecutedCount)
                .integrityStatus(integrityStatus)
                .discrepancies(discrepancies)
                .build();
    }

    @Override
    @Transactional
    public ProgressReconciliationResponse reconcileCumulativeProgress(Long projectId, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<WbsActivity> leafActivities = activities.stream()
                .filter(a -> !a.hasChildren())
                .collect(Collectors.toList());

        List<String> repairedCodes = new ArrayList<>();

        for (WbsActivity act : leafActivities) {
            Double verifiedQty = progressLogRepository.sumQuantityByActivityId(act.getId());
            if (verifiedQty == null) verifiedQty = 0.0;
            verifiedQty = round2(verifiedQty);

            double storedQty = act.getCompletedQuantity() != null ? round2(act.getCompletedQuantity()) : 0.0;
            double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
            double storedPct = act.getProgressPercentage() != null ? round2(act.getProgressPercentage()) : 0.0;
            double calculatedPct = planned > 0 ? Math.min(100.0, round2((verifiedQty / planned) * 100.0)) : 100.0;

            if (Math.abs(verifiedQty - storedQty) > 0.01 || Math.abs(calculatedPct - storedPct) > 0.1) {
                act.setCompletedQuantity(verifiedQty);
                act.setProgressPercentage(calculatedPct);

                if (calculatedPct >= 100.0) {
                    act.setStatus("COMPLETED");
                    if (act.getActualEndDate() == null) {
                        LocalDate lastLog = progressLogRepository.findMaxLogDateByActivityId(act.getId());
                        act.setActualEndDate(lastLog != null ? lastLog : LocalDate.now());
                    }
                } else if (verifiedQty > 0.0) {
                    act.setStatus("IN_PROGRESS");
                    if (act.getActualStartDate() == null) {
                        LocalDate firstLog = progressLogRepository.findMinLogDateByActivityId(act.getId());
                        act.setActualStartDate(firstLog != null ? firstLog : LocalDate.now());
                    }
                }
                repairedCodes.add(act.getCode());
            }
        }

        recalculateParentTasks(activities);
        wbsActivityRepository.saveAll(activities);

        auditService.logAction(
                performedBy != null ? performedBy : "SYSTEM",
                "RECONCILE_CUMULATIVE_PROGRESS",
                "PROJECT",
                String.valueOf(projectId),
                "Reconciled cumulative progress for Project " + project.getCode() + ". Repaired " + repairedCodes.size() + " activities",
                null
        );

        return ProgressReconciliationResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .reconciliationTimestamp(LocalDateTime.now())
                .totalActivitiesScanned(leafActivities.size())
                .activitiesRepairedCount(repairedCodes.size())
                .parentTasksUpdatedCount(activities.size() - leafActivities.size())
                .repairedActivityCodes(repairedCodes)
                .reconciledBy(performedBy != null ? performedBy : "SYSTEM")
                .message("Cumulative progress successfully reconciled and synchronized with verified progress history logs.")
                .build();
    }

    // =========================================================================
    // CALCULATION HELPER METHODS
    // =========================================================================

    private double calculateWeightedProgress(List<WbsActivity> activities, WeightingModel model, boolean isActual, LocalDate asOfDate) {
        if (activities == null || activities.isEmpty()) return 0.0;

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (WbsActivity act : activities) {
            double progress = isActual
                    ? (act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0)
                    : calculatePlannedPercentage(act, asOfDate);

            double weight = getWeight(act, model);
            weightedSum += progress * weight;
            totalWeight += weight;
        }

        return totalWeight > 0.0 ? round2(weightedSum / totalWeight) : 0.0;
    }

    private double getWeight(WbsActivity act, WeightingModel model) {
        switch (model) {
            case EQUAL_WEIGHTED:
                return 1.0;
            case DURATION_WEIGHTED:
                if (act.getPlannedStartDate() != null && act.getPlannedEndDate() != null) {
                    return Math.max(1.0, ChronoUnit.DAYS.between(act.getPlannedStartDate(), act.getPlannedEndDate()) + 1);
                }
                return 1.0;
            case BUDGET_WEIGHTED:
            case QUANTITY_WEIGHTED:
                if (act.getPlannedQuantity() != null && act.getPlannedQuantity() > 0) {
                    return act.getPlannedQuantity();
                }
                return 1.0;
            case WEIGHTAGE_FACTOR:
            default:
                return (act.getWeightage() != null && act.getWeightage() > 0) ? act.getWeightage() : 1.0;
        }
    }

    private double calculatePlannedPercentage(WbsActivity act, LocalDate asOfDate) {
        if (asOfDate == null) asOfDate = LocalDate.now();
        LocalDate start = act.getPlannedStartDate();
        LocalDate end = act.getPlannedEndDate();

        if (start == null || end == null) return 0.0;
        if (asOfDate.isBefore(start)) return 0.0;
        if (!asOfDate.isBefore(end)) return 100.0;

        long totalDays = Math.max(1L, ChronoUnit.DAYS.between(start, end) + 1);
        long elapsedDays = Math.max(1L, ChronoUnit.DAYS.between(start, asOfDate) + 1);

        return Math.min(100.0, round2(((double) elapsedDays / totalDays) * 100.0));
    }

    private String determinePerformanceStatus(double actual, double planned) {
        if (actual >= 100.0) return "COMPLETED";
        if (actual == 0.0 && planned == 0.0) return "NOT_STARTED";
        double diff = actual - planned;
        if (diff >= 5.0) return "AHEAD";
        if (diff < -5.0) return "BEHIND";
        return "ON_TRACK";
    }

    private void recalculateLeafActivity(WbsActivity act, boolean syncDates, boolean autoComplete) {
        Double sumQty = progressLogRepository.sumQuantityByActivityId(act.getId());
        double verifiedQty = sumQty != null ? round2(sumQty) : 0.0;

        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double newPct = planned > 0.0 ? Math.min(100.0, round2((verifiedQty / planned) * 100.0)) : 100.0;

        act.setCompletedQuantity(verifiedQty);
        act.setProgressPercentage(newPct);

        if (syncDates) {
            LocalDate firstLog = progressLogRepository.findMinLogDateByActivityId(act.getId());
            LocalDate lastLog = progressLogRepository.findMaxLogDateByActivityId(act.getId());

            if (firstLog != null && act.getActualStartDate() == null) {
                act.setActualStartDate(firstLog);
            }
            if (newPct >= 100.0) {
                if (autoComplete) act.setStatus("COMPLETED");
                if (act.getActualEndDate() == null) {
                    act.setActualEndDate(lastLog != null ? lastLog : LocalDate.now());
                }
            } else if (verifiedQty > 0.0) {
                if ("PLANNED".equalsIgnoreCase(act.getStatus())) act.setStatus("IN_PROGRESS");
            }
        }
    }

    private void recalculateParentTasks(List<WbsActivity> activities) {
        // Group by parent and process bottom-up
        List<WbsActivity> parents = activities.stream()
                .filter(WbsActivity::hasChildren)
                .sorted((a, b) -> Integer.compare(b.getLevel() != null ? b.getLevel() : 1, a.getLevel() != null ? a.getLevel() : 1))
                .collect(Collectors.toList());

        for (WbsActivity parent : parents) {
            List<WbsActivity> children = parent.getChildren();
            if (children == null || children.isEmpty()) continue;

            double weightedSum = 0.0;
            double totalWeight = 0.0;
            double totalCompleted = 0.0;
            boolean allCompleted = true;
            boolean anyStarted = false;

            for (WbsActivity child : children) {
                double pct = child.getProgressPercentage() != null ? child.getProgressPercentage() : 0.0;
                double w = child.getWeightage() != null && child.getWeightage() > 0 ? child.getWeightage() : 1.0;
                weightedSum += pct * w;
                totalWeight += w;
                if (child.getCompletedQuantity() != null) totalCompleted += child.getCompletedQuantity();

                if (!"COMPLETED".equalsIgnoreCase(child.getStatus())) allCompleted = false;
                if (!"PLANNED".equalsIgnoreCase(child.getStatus()) || pct > 0.0) anyStarted = true;
            }

            double parentPct = totalWeight > 0 ? round2(weightedSum / totalWeight) : 0.0;
            parent.setProgressPercentage(parentPct);
            parent.setCompletedQuantity(round2(totalCompleted));

            if (allCompleted || parentPct >= 100.0) {
                parent.setStatus("COMPLETED");
            } else if (anyStarted) {
                parent.setStatus("IN_PROGRESS");
            } else {
                parent.setStatus("PLANNED");
            }
        }
    }

    private List<CumulativeProgressResponse.DisciplineCumulativeRollupDto> buildDisciplineRollup(
            List<WbsActivity> activities, WeightingModel model, LocalDate asOfDate) {

        Map<String, List<WbsActivity>> grouped = activities.stream()
                .collect(Collectors.groupingBy(a -> a.getDiscipline() != null ? a.getDiscipline().toUpperCase() : "GENERAL"));

        List<CumulativeProgressResponse.DisciplineCumulativeRollupDto> list = new ArrayList<>();
        for (Map.Entry<String, List<WbsActivity>> entry : grouped.entrySet()) {
            List<WbsActivity> acts = entry.getValue();
            double actual = calculateWeightedProgress(acts, model, true, asOfDate);
            double planned = calculateWeightedProgress(acts, model, false, asOfDate);
            double var = round2(actual - planned);

            int completed = (int) acts.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
            int inProgress = (int) acts.stream().filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus())).count();

            list.add(CumulativeProgressResponse.DisciplineCumulativeRollupDto.builder()
                    .discipline(entry.getKey())
                    .totalActivities(acts.size())
                    .completedActivities(completed)
                    .inProgressActivities(inProgress)
                    .actualPercentage(actual)
                    .plannedPercentage(planned)
                    .variancePercentage(var)
                    .performanceStatus(determinePerformanceStatus(actual, planned))
                    .build());
        }
        return list;
    }

    private List<CumulativeProgressResponse.WorkPackageCumulativeRollupDto> buildWorkPackageRollup(
            List<WorkPackage> workPackages, List<WbsActivity> allActivities, WeightingModel model, LocalDate asOfDate) {

        List<CumulativeProgressResponse.WorkPackageCumulativeRollupDto> list = new ArrayList<>();
        for (WorkPackage wp : workPackages) {
            List<WbsActivity> acts = allActivities.stream()
                    .filter(a -> a.getWorkPackage() != null && a.getWorkPackage().getId().equals(wp.getId()))
                    .filter(a -> !a.hasChildren())
                    .collect(Collectors.toList());

            double actual = calculateWeightedProgress(acts, model, true, asOfDate);
            double planned = calculateWeightedProgress(acts, model, false, asOfDate);
            double var = round2(actual - planned);

            list.add(CumulativeProgressResponse.WorkPackageCumulativeRollupDto.builder()
                    .workPackageId(wp.getId())
                    .workPackageCode(wp.getCode())
                    .workPackageName(wp.getName())
                    .discipline(wp.getDiscipline())
                    .status(wp.getStatus())
                    .budgetAmount(wp.getBudgetAmount())
                    .totalActivities(acts.size())
                    .actualPercentage(actual)
                    .plannedPercentage(planned)
                    .variancePercentage(var)
                    .performanceStatus(determinePerformanceStatus(actual, planned))
                    .build());
        }
        return list;
    }

    private List<CumulativeProgressResponse.LocationCumulativeRollupDto> buildLocationRollup(
            List<WbsActivity> activities, WeightingModel model, LocalDate asOfDate) {

        Map<Long, List<WbsActivity>> grouped = activities.stream()
                .filter(a -> a.getBuilding() != null)
                .collect(Collectors.groupingBy(a -> a.getBuilding().getId()));

        List<CumulativeProgressResponse.LocationCumulativeRollupDto> list = new ArrayList<>();
        for (Map.Entry<Long, List<WbsActivity>> entry : grouped.entrySet()) {
            List<WbsActivity> acts = entry.getValue();
            Building b = acts.get(0).getBuilding();
            double actual = calculateWeightedProgress(acts, model, true, asOfDate);
            double planned = calculateWeightedProgress(acts, model, false, asOfDate);
            double var = round2(actual - planned);

            list.add(CumulativeProgressResponse.LocationCumulativeRollupDto.builder()
                    .buildingId(b.getId())
                    .buildingName(b.getName())
                    .totalActivities(acts.size())
                    .actualPercentage(actual)
                    .plannedPercentage(planned)
                    .variancePercentage(var)
                    .performanceStatus(determinePerformanceStatus(actual, planned))
                    .build());
        }
        return list;
    }

    private List<LocalDate> generateTimelineBuckets(LocalDate start, LocalDate end, String interval) {
        List<LocalDate> buckets = new ArrayList<>();
        LocalDate curr = start;

        int stepDays = "DAILY".equalsIgnoreCase(interval) ? 1 : ("MONTHLY".equalsIgnoreCase(interval) ? 30 : 7);

        while (!curr.isAfter(end)) {
            buckets.add(curr);
            curr = curr.plusDays(stepDays);
        }
        if (!buckets.contains(end)) {
            buckets.add(end);
        }
        return buckets;
    }

    private ActivityCumulativeProgressResponse mapActivityCumulative(WbsActivity act) {
        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double completed = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
        double balance = Math.max(0.0, round2(planned - completed));

        double actualPct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
        double plannedPct = calculatePlannedPercentage(act, LocalDate.now());
        double variance = round2(actualPct - plannedPct);
        String perf = determinePerformanceStatus(actualPct, plannedPct);

        double weight = act.getWeightage() != null ? act.getWeightage() : 1.0;
        double contribution = round2(actualPct * weight);

        List<ActivityCumulativeProgressResponse> subtasks = new ArrayList<>();
        if (act.hasChildren()) {
            subtasks = act.getChildren().stream()
                    .map(this::mapActivityCumulative)
                    .collect(Collectors.toList());
        }

        WorkPackage wp = act.getWorkPackage();
        Project prj = act.getProject();

        return ActivityCumulativeProgressResponse.builder()
                .activityId(act.getId())
                .activityCode(act.getCode())
                .activityName(act.getName())
                .discipline(act.getDiscipline())
                .workPackageId(wp != null ? wp.getId() : null)
                .workPackageCode(wp != null ? wp.getCode() : null)
                .workPackageName(wp != null ? wp.getName() : null)
                .projectId(prj != null ? prj.getId() : null)
                .projectName(prj != null ? prj.getName() : null)
                .uom(act.getUom())
                .plannedQuantity(planned)
                .completedQuantity(completed)
                .balanceQuantity(balance)
                .actualProgressPercentage(actualPct)
                .plannedProgressPercentage(plannedPct)
                .progressVariancePercentage(variance)
                .performanceStatus(perf)
                .weightage(weight)
                .weightedContribution(contribution)
                .plannedStartDate(act.getPlannedStartDate())
                .plannedEndDate(act.getPlannedEndDate())
                .actualStartDate(act.getActualStartDate())
                .actualEndDate(act.getActualEndDate())
                .status(act.getStatus())
                .assignedContractor(act.getAssignedContractor())
                .childCount(act.getChildren() != null ? act.getChildren().size() : 0)
                .subtasks(subtasks)
                .build();
    }

    private double round2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
