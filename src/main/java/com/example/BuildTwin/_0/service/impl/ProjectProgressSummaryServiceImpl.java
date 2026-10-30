package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.summary.DisciplineProgressSummaryDto;
import com.example.BuildTwin._0.dto.summary.PortfolioProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.WorkPackageProgressSummaryDto;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.model.WbsActivity;
import com.example.BuildTwin._0.model.WorkPackage;
import com.example.BuildTwin._0.repository.ActivityProgressLogRepository;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.ProjectProgressSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectProgressSummaryServiceImpl implements ProjectProgressSummaryService {

    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ActivityProgressLogRepository activityProgressLogRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectProgressSummaryResponse getProjectProgressSummary(Long projectId) {
        log.info("Calculating comprehensive progress summary for projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WbsActivity> allActivities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);

        // Filter leaf activities for physical progress calculations
        List<WbsActivity> leafActivities = allActivities.stream()
                .filter(act -> !act.hasChildren())
                .collect(Collectors.toList());
        if (leafActivities.isEmpty()) {
            leafActivities = allActivities;
        }

        LocalDate today = LocalDate.now();

        // 1. Activity Status Counts
        long totalActivities = allActivities.size();
        long completedActivities = 0;
        long inProgressActivities = 0;
        long plannedActivities = 0;
        long delayedActivities = 0;
        long onHoldActivities = 0;

        for (WbsActivity act : allActivities) {
            String status = act.getStatus() != null ? act.getStatus().toUpperCase() : "PLANNED";
            double pct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;

            if ("COMPLETED".equals(status) || pct >= 100.0) {
                completedActivities++;
            } else if ("IN_PROGRESS".equals(status) || pct > 0.0) {
                inProgressActivities++;
            } else if ("ON_HOLD".equals(status)) {
                onHoldActivities++;
            } else {
                plannedActivities++;
            }

            // Check if delayed
            if (!"COMPLETED".equals(status) && act.getPlannedEndDate() != null && today.isAfter(act.getPlannedEndDate())) {
                delayedActivities++;
            }
        }

        double completionRate = totalActivities > 0
                ? round2(((double) completedActivities / totalActivities) * 100.0)
                : 0.0;

        // 2. Physical Quantities Calculation
        double totalPlannedQuantity = 0.0;
        double totalCompletedQuantity = 0.0;

        for (WbsActivity act : leafActivities) {
            totalPlannedQuantity += (act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0);
            totalCompletedQuantity += (act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0);
        }

        totalPlannedQuantity = round2(totalPlannedQuantity);
        totalCompletedQuantity = round2(totalCompletedQuantity);
        double totalRemainingQuantity = Math.max(0.0, round2(totalPlannedQuantity - totalCompletedQuantity));
        double quantityProgressPercentage = totalPlannedQuantity > 0.0
                ? round2((totalCompletedQuantity / totalPlannedQuantity) * 100.0)
                : 0.0;

        // 3. Overall Weighted Progress & Planned Progress
        double weightedActualSum = 0.0;
        double weightedPlannedSum = 0.0;
        double totalWeight = 0.0;

        for (WbsActivity act : leafActivities) {
            double weight = act.getWeightage() != null && act.getWeightage() > 0 ? act.getWeightage() : 1.0;
            double actualPct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
            double plannedPct = calculatePlannedPercentage(act, today);

            weightedActualSum += (actualPct * weight);
            weightedPlannedSum += (plannedPct * weight);
            totalWeight += weight;
        }

        double overallActualPct = totalWeight > 0 ? round2(weightedActualSum / totalWeight) : 0.0;
        double overallPlannedPct = totalWeight > 0 ? round2(weightedPlannedSum / totalWeight) : 0.0;
        double progressVariance = round2(overallActualPct - overallPlannedPct);
        String scheduleHealth = determineScheduleHealth(overallActualPct, overallPlannedPct);

        // 4. EVM Value Metrics
        BigDecimal estimatedBudget = project.getEstimatedBudget() != null ? project.getEstimatedBudget() : BigDecimal.ZERO;
        BigDecimal earnedValue = estimatedBudget.multiply(BigDecimal.valueOf(overallActualPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal plannedValue = estimatedBudget.multiply(BigDecimal.valueOf(overallPlannedPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
        double spi = plannedValue.compareTo(BigDecimal.ZERO) > 0
                ? round2(earnedValue.doubleValue() / plannedValue.doubleValue())
                : 1.0;

        // 5. Recent Progress Log Info
        LocalDate lastLogDate = activityProgressLogRepository.findMaxLogDateByProjectId(projectId);
        Long totalProgressEntries = activityProgressLogRepository.countByProjectId(projectId);
        Long activeBlockers = activityProgressLogRepository.countBlockersByProjectId(projectId);

        // 6. Work Package Summaries
        List<WorkPackageProgressSummaryDto> wpSummaries = buildWorkPackageSummaries(workPackages, allActivities, today);

        // 7. Discipline Summaries
        List<DisciplineProgressSummaryDto> disciplineSummaries = buildDisciplineSummaries(leafActivities, today);

        return ProjectProgressSummaryResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .projectStatus(project.getStatus())
                .projectType(project.getProjectType())
                .clientName(project.getClientName())
                .location(project.getLocation())
                .plannedStartDate(project.getPlannedStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .actualStartDate(project.getActualStartDate())
                .actualEndDate(project.getActualEndDate())
                .estimatedBudget(estimatedBudget)
                .currency(project.getCurrency() != null ? project.getCurrency() : "INR")
                .overallProgressPercentage(overallActualPct)
                .plannedProgressPercentage(overallPlannedPct)
                .progressVariance(progressVariance)
                .scheduleHealth(scheduleHealth)
                .earnedValue(earnedValue)
                .plannedValue(plannedValue)
                .schedulePerformanceIndex(spi)
                .totalActivities(totalActivities)
                .completedActivities(completedActivities)
                .inProgressActivities(inProgressActivities)
                .plannedActivities(plannedActivities)
                .delayedActivities(delayedActivities)
                .onHoldActivities(onHoldActivities)
                .completionRate(completionRate)
                .totalPlannedQuantity(totalPlannedQuantity)
                .totalCompletedQuantity(totalCompletedQuantity)
                .totalRemainingQuantity(totalRemainingQuantity)
                .quantityProgressPercentage(quantityProgressPercentage)
                .lastProgressUpdateDate(lastLogDate)
                .totalProgressEntriesCount(totalProgressEntries != null ? totalProgressEntries : 0L)
                .activeBlockersCount(activeBlockers != null ? activeBlockers : 0L)
                .workPackages(wpSummaries)
                .disciplines(disciplineSummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProgressSummaryResponse getPortfolioProgressSummary() {
        log.info("Calculating executive portfolio progress summary across all projects");

        List<Project> projects = projectRepository.findAll();
        List<ProjectProgressSummaryResponse> summaries = new ArrayList<>();

        long activeCount = 0;
        long completedCount = 0;
        long plannedOrOnHoldCount = 0;
        double sumActualPct = 0.0;
        double sumPlannedPct = 0.0;
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalEarnedValue = BigDecimal.ZERO;
        long totalActivities = 0;
        long totalCompletedActivities = 0;

        for (Project prj : projects) {
            ProjectProgressSummaryResponse summary = getProjectProgressSummary(prj.getId());
            summaries.add(summary);

            String status = prj.getStatus() != null ? prj.getStatus().toUpperCase() : "PLANNED";
            if ("ACTIVE".equals(status)) {
                activeCount++;
            } else if ("COMPLETED".equals(status)) {
                completedCount++;
            } else {
                plannedOrOnHoldCount++;
            }

            sumActualPct += summary.getOverallProgressPercentage();
            sumPlannedPct += summary.getPlannedProgressPercentage();
            totalBudget = totalBudget.add(summary.getEstimatedBudget() != null ? summary.getEstimatedBudget() : BigDecimal.ZERO);
            totalEarnedValue = totalEarnedValue.add(summary.getEarnedValue() != null ? summary.getEarnedValue() : BigDecimal.ZERO);
            totalActivities += summary.getTotalActivities();
            totalCompletedActivities += summary.getCompletedActivities();
        }

        double avgActualPct = !projects.isEmpty() ? round2(sumActualPct / projects.size()) : 0.0;
        double avgPlannedPct = !projects.isEmpty() ? round2(sumPlannedPct / projects.size()) : 0.0;

        return PortfolioProgressSummaryResponse.builder()
                .totalProjects((long) projects.size())
                .activeProjects(activeCount)
                .completedProjects(completedCount)
                .plannedOrOnHoldProjects(plannedOrOnHoldCount)
                .averageProgressPercentage(avgActualPct)
                .averagePlannedProgressPercentage(avgPlannedPct)
                .totalPortfolioBudget(totalBudget)
                .totalPortfolioEarnedValue(totalEarnedValue)
                .totalActivities(totalActivities)
                .totalCompletedActivities(totalCompletedActivities)
                .projectSummaries(summaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkPackageProgressSummaryDto getWorkPackageProgressSummary(Long workPackageId) {
        log.info("Calculating progress summary for workPackageId={}", workPackageId);

        WorkPackage wp = workPackageRepository.findById(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", workPackageId));

        List<WbsActivity> activities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(workPackageId);
        return mapWorkPackageSummary(wp, activities, LocalDate.now());
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private List<WorkPackageProgressSummaryDto> buildWorkPackageSummaries(
            List<WorkPackage> workPackages, List<WbsActivity> allActivities, LocalDate today) {

        Map<Long, List<WbsActivity>> activitiesByWp = allActivities.stream()
                .filter(act -> act.getWorkPackage() != null)
                .collect(Collectors.groupingBy(act -> act.getWorkPackage().getId()));

        List<WorkPackageProgressSummaryDto> list = new ArrayList<>();
        for (WorkPackage wp : workPackages) {
            List<WbsActivity> wpActs = activitiesByWp.getOrDefault(wp.getId(), Collections.emptyList());
            list.add(mapWorkPackageSummary(wp, wpActs, today));
        }
        return list;
    }

    private WorkPackageProgressSummaryDto mapWorkPackageSummary(
            WorkPackage wp, List<WbsActivity> activities, LocalDate today) {

        long total = activities.size();
        long completed = 0;
        long inProgress = 0;
        long planned = 0;

        double weightedActual = 0.0;
        double weightedPlanned = 0.0;
        double totalWeight = 0.0;

        for (WbsActivity act : activities) {
            String status = act.getStatus() != null ? act.getStatus().toUpperCase() : "PLANNED";
            double actualPct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
            double plannedPct = calculatePlannedPercentage(act, today);
            double weight = act.getWeightage() != null && act.getWeightage() > 0 ? act.getWeightage() : 1.0;

            if ("COMPLETED".equals(status) || actualPct >= 100.0) {
                completed++;
            } else if ("IN_PROGRESS".equals(status) || actualPct > 0.0) {
                inProgress++;
            } else {
                planned++;
            }

            weightedActual += (actualPct * weight);
            weightedPlanned += (plannedPct * weight);
            totalWeight += weight;
        }

        double actualProgress = totalWeight > 0 ? round2(weightedActual / totalWeight) : 0.0;
        double plannedProgress = totalWeight > 0 ? round2(weightedPlanned / totalWeight) : 0.0;
        double variance = round2(actualProgress - plannedProgress);

        return WorkPackageProgressSummaryDto.builder()
                .workPackageId(wp.getId())
                .code(wp.getCode())
                .name(wp.getName())
                .discipline(wp.getDiscipline())
                .status(wp.getStatus())
                .progressPercentage(actualProgress)
                .plannedProgressPercentage(plannedProgress)
                .progressVariance(variance)
                .totalActivities(total)
                .completedActivities(completed)
                .inProgressActivities(inProgress)
                .plannedActivities(planned)
                .budgetAmount(wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO)
                .build();
    }

    private List<DisciplineProgressSummaryDto> buildDisciplineSummaries(
            List<WbsActivity> activities, LocalDate today) {

        Map<String, List<WbsActivity>> byDiscipline = activities.stream()
                .collect(Collectors.groupingBy(
                        act -> act.getDiscipline() != null && !act.getDiscipline().isBlank()
                                ? act.getDiscipline().toUpperCase()
                                : "GENERAL"));

        List<DisciplineProgressSummaryDto> list = new ArrayList<>();

        for (Map.Entry<String, List<WbsActivity>> entry : byDiscipline.entrySet()) {
            String discipline = entry.getKey();
            List<WbsActivity> acts = entry.getValue();

            long total = acts.size();
            long completed = 0;
            long inProgress = 0;

            double weightedActual = 0.0;
            double weightedPlanned = 0.0;
            double totalWeight = 0.0;

            for (WbsActivity act : acts) {
                String status = act.getStatus() != null ? act.getStatus().toUpperCase() : "PLANNED";
                double actualPct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
                double plannedPct = calculatePlannedPercentage(act, today);
                double weight = act.getWeightage() != null && act.getWeightage() > 0 ? act.getWeightage() : 1.0;

                if ("COMPLETED".equals(status) || actualPct >= 100.0) {
                    completed++;
                } else if ("IN_PROGRESS".equals(status) || actualPct > 0.0) {
                    inProgress++;
                }

                weightedActual += (actualPct * weight);
                weightedPlanned += (plannedPct * weight);
                totalWeight += weight;
            }

            double actualProgress = totalWeight > 0 ? round2(weightedActual / totalWeight) : 0.0;
            double plannedProgress = totalWeight > 0 ? round2(weightedPlanned / totalWeight) : 0.0;
            double variance = round2(actualProgress - plannedProgress);

            list.add(DisciplineProgressSummaryDto.builder()
                    .discipline(discipline)
                    .totalActivities(total)
                    .completedActivities(completed)
                    .inProgressActivities(inProgress)
                    .actualProgressPercentage(actualProgress)
                    .plannedProgressPercentage(plannedProgress)
                    .progressVariance(variance)
                    .build());
        }

        list.sort(Comparator.comparing(DisciplineProgressSummaryDto::getDiscipline));
        return list;
    }

    private double calculatePlannedPercentage(WbsActivity act, LocalDate asOfDate) {
        if (asOfDate == null) asOfDate = LocalDate.now();
        LocalDate start = act.getPlannedStartDate();
        LocalDate end = act.getPlannedEndDate();

        if (start == null || end == null) return 0.0;
        if (asOfDate.isBefore(start)) return 0.0;
        if (!asOfDate.isAfter(end)) {
            long totalDays = Math.max(1L, ChronoUnit.DAYS.between(start, end) + 1);
            long elapsedDays = Math.max(1L, ChronoUnit.DAYS.between(start, asOfDate) + 1);
            return Math.min(100.0, round2(((double) elapsedDays / totalDays) * 100.0));
        }
        return 100.0;
    }

    private String determineScheduleHealth(double actual, double planned) {
        if (actual >= 100.0) return "COMPLETED";
        double variance = actual - planned;
        if (variance >= 5.0) return "AHEAD";
        if (variance >= -5.0) return "ON_TRACK";
        if (variance >= -15.0) return "AT_RISK";
        return "DELAYED";
    }

    private double round2(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
