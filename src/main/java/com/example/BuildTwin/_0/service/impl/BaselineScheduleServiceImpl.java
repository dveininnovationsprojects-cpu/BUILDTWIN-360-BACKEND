package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.baseline.*;
import com.example.BuildTwin._0.dto.wbs.UpdateWbsActivityProgressRequest;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.BaselineScheduleService;
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
public class BaselineScheduleServiceImpl implements BaselineScheduleService {

    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ProjectBaselineRepository projectBaselineRepository;
    private final BaselineActivitySnapshotRepository baselineActivitySnapshotRepository;
    private final ActivityProgressLogRepository activityProgressLogRepository;
    private final WbsActivityService wbsActivityService;
    private final AuditService auditService;

    @Override
    @Transactional
    public ProjectBaselineResponse createBaseline(Long projectId, CreateProjectBaselineRequest request, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String name = request.getName().trim();
        if (projectBaselineRepository.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new DuplicateResourceException("ProjectBaseline", "name", name + " in Project " + project.getName());
        }

        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);

        if (activities.isEmpty()) {
            throw new BadRequestException("Cannot create a baseline schedule: Project '" + project.getName() + "' has no activities defined.");
        }

        // Determine next version number
        int nextVersion = projectBaselineRepository.findTopByProjectIdOrderByVersionDesc(projectId)
                .map(b -> b.getVersion() + 1)
                .orElse(1);

        // If requested to be active, deactivate existing active baselines
        if (Boolean.TRUE.equals(request.getSetActive())) {
            projectBaselineRepository.findByProjectIdAndIsActiveTrue(projectId).ifPresent(active -> {
                active.setIsActive(false);
                projectBaselineRepository.save(active);
            });
        }

        BigDecimal totalBudget = workPackages.stream()
                .map(wp -> wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate minStart = activities.stream()
                .map(WbsActivity::getPlannedStartDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(project.getPlannedStartDate());

        LocalDate maxEnd = activities.stream()
                .map(WbsActivity::getPlannedEndDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(project.getPlannedEndDate());

        ProjectBaseline baseline = ProjectBaseline.builder()
                .project(project)
                .name(name)
                .version(nextVersion)
                .description(request.getDescription())
                .isActive(Boolean.TRUE.equals(request.getSetActive()))
                .createdBy(performedBy)
                .totalActivities(activities.size())
                .totalWorkPackages(workPackages.size())
                .totalBudgetAmount(totalBudget)
                .baselineStartDate(minStart)
                .baselineEndDate(maxEnd)
                .build();

        ProjectBaseline savedBaseline = projectBaselineRepository.save(baseline);

        // Snapshot all activities
        List<BaselineActivitySnapshot> snapshots = new ArrayList<>();
        for (WbsActivity act : activities) {
            int durationDays = 0;
            if (act.getPlannedStartDate() != null && act.getPlannedEndDate() != null) {
                durationDays = (int) ChronoUnit.DAYS.between(act.getPlannedStartDate(), act.getPlannedEndDate());
            }

            BaselineActivitySnapshot snapshot = BaselineActivitySnapshot.builder()
                    .baseline(savedBaseline)
                    .activityId(act.getId())
                    .activityCode(act.getCode())
                    .activityName(act.getName())
                    .workPackageId(act.getWorkPackage() != null ? act.getWorkPackage().getId() : null)
                    .workPackageName(act.getWorkPackage() != null ? act.getWorkPackage().getName() : null)
                    .discipline(act.getDiscipline())
                    .uom(act.getUom())
                    .plannedQuantity(act.getPlannedQuantity())
                    .plannedStartDate(act.getPlannedStartDate())
                    .plannedEndDate(act.getPlannedEndDate())
                    .plannedDurationDays(durationDays)
                    .weightage(act.getWeightage())
                    .assignedContractor(act.getAssignedContractor())
                    .build();
            snapshots.add(snapshot);
        }
        baselineActivitySnapshotRepository.saveAll(snapshots);

        auditService.logAction(
                performedBy,
                "CREATE_BASELINE_SCHEDULE",
                "PROJECT_BASELINE",
                String.valueOf(savedBaseline.getId()),
                "Created Baseline Schedule '" + savedBaseline.getName() + "' (v" + savedBaseline.getVersion() + ") with " + snapshots.size() + " activity snapshots.",
                null
        );

        return mapToBaselineResponse(savedBaseline);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectBaselineResponse> getBaselinesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return projectBaselineRepository.findByProjectIdOrderByVersionDesc(projectId).stream()
                .map(this::mapToBaselineResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectBaselineResponse getBaselineById(Long baselineId) {
        ProjectBaseline baseline = projectBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectBaseline", "id", baselineId));
        return mapToBaselineResponse(baseline);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaselineActivitySnapshotResponse> getBaselineSnapshots(Long baselineId) {
        if (!projectBaselineRepository.existsById(baselineId)) {
            throw new ResourceNotFoundException("ProjectBaseline", "id", baselineId);
        }
        return baselineActivitySnapshotRepository.findByBaselineIdOrderByActivityCodeAsc(baselineId).stream()
                .map(this::mapToSnapshotResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectBaselineResponse setActiveBaseline(Long projectId, Long baselineId, String performedBy) {
        ProjectBaseline target = projectBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectBaseline", "id", baselineId));

        if (!target.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Baseline ID " + baselineId + " does not belong to Project ID " + projectId);
        }

        // Deactivate all others for this project
        List<ProjectBaseline> all = projectBaselineRepository.findByProjectIdOrderByVersionDesc(projectId);
        for (ProjectBaseline b : all) {
            b.setIsActive(b.getId().equals(baselineId));
            projectBaselineRepository.save(b);
        }

        auditService.logAction(
                performedBy,
                "SET_ACTIVE_BASELINE",
                "PROJECT_BASELINE",
                String.valueOf(baselineId),
                "Set Baseline '" + target.getName() + "' (v" + target.getVersion() + ") as active comparison baseline.",
                null
        );

        return mapToBaselineResponse(target);
    }

    @Override
    @Transactional
    public ProjectBaselineResponse approveBaseline(Long baselineId, String approvedBy) {
        ProjectBaseline baseline = projectBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectBaseline", "id", baselineId));

        baseline.setApprovedBy(approvedBy);
        baseline.setApprovedAt(LocalDateTime.now());
        ProjectBaseline saved = projectBaselineRepository.save(baseline);

        auditService.logAction(
                approvedBy,
                "APPROVE_BASELINE",
                "PROJECT_BASELINE",
                String.valueOf(baselineId),
                "Approved Baseline '" + saved.getName() + "' as official Contract Baseline.",
                null
        );

        return mapToBaselineResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBaseline(Long baselineId, String performedBy) {
        ProjectBaseline baseline = projectBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectBaseline", "id", baselineId));

        String name = baseline.getName();
        baselineActivitySnapshotRepository.deleteByBaselineId(baselineId);
        projectBaselineRepository.delete(baseline);

        auditService.logAction(
                performedBy,
                "DELETE_BASELINE",
                "PROJECT_BASELINE",
                String.valueOf(baselineId),
                "Deleted Baseline Schedule '" + name + "'.",
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectScheduleVarianceReport getProjectScheduleVarianceReport(Long projectId, Long optionalBaselineId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        ProjectBaseline baseline;
        if (optionalBaselineId != null) {
            baseline = projectBaselineRepository.findById(optionalBaselineId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProjectBaseline", "id", optionalBaselineId));
        } else {
            baseline = projectBaselineRepository.findByProjectIdAndIsActiveTrue(projectId)
                    .orElseThrow(() -> new BadRequestException("No active baseline found for project '" +
                            project.getName() + "'. Please create or activate a baseline schedule first."));
        }

        List<BaselineActivitySnapshot> snapshots = baselineActivitySnapshotRepository.findByBaselineIdOrderByActivityCodeAsc(baseline.getId());
        List<ActivityScheduleVarianceResponse> variances = new ArrayList<>();

        int onTrack = 0;
        int slightDelay = 0;
        int criticalDelay = 0;
        int ahead = 0;
        long totalDelayDays = 0;
        int delayedTasksCount = 0;

        for (BaselineActivitySnapshot snap : snapshots) {
            Optional<WbsActivity> currentActOpt = wbsActivityRepository.findById(snap.getActivityId());
            if (currentActOpt.isEmpty()) continue;

            WbsActivity curr = currentActOpt.get();
            ActivityScheduleVarianceResponse variance = computeActivityVariance(snap, curr);
            variances.add(variance);

            switch (variance.getHealthStatus()) {
                case "ON_TRACK":
                    onTrack++;
                    break;
                case "SLIGHT_DELAY":
                    slightDelay++;
                    if (variance.getFinishVarianceDays() > 0) {
                        totalDelayDays += variance.getFinishVarianceDays();
                        delayedTasksCount++;
                    }
                    break;
                case "CRITICAL_DELAY":
                    criticalDelay++;
                    if (variance.getFinishVarianceDays() > 0) {
                        totalDelayDays += variance.getFinishVarianceDays();
                        delayedTasksCount++;
                    }
                    break;
                case "AHEAD":
                    ahead++;
                    break;
            }
        }

        double avgSlippage = delayedTasksCount > 0 ? ((double) totalDelayDays / delayedTasksCount) : 0.0;
        avgSlippage = BigDecimal.valueOf(avgSlippage).setScale(1, RoundingMode.HALF_UP).doubleValue();

        String overallHealth;
        if (criticalDelay > 0 || avgSlippage > 7.0) {
            overallHealth = "HIGH_RISK";
        } else if (slightDelay > 0) {
            overallHealth = "MODERATE_RISK";
        } else {
            overallHealth = "HEALTHY";
        }

        return ProjectScheduleVarianceReport.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .baselineId(baseline.getId())
                .baselineName(baseline.getName())
                .totalActivities(variances.size())
                .onTrackActivitiesCount(onTrack)
                .slightDelayActivitiesCount(slightDelay)
                .criticalDelayActivitiesCount(criticalDelay)
                .aheadActivitiesCount(ahead)
                .averageSlippageDays(avgSlippage)
                .overallProjectHealth(overallHealth)
                .activityVariances(variances)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityScheduleVarianceResponse getActivityScheduleVariance(Long activityId, Long optionalBaselineId) {
        WbsActivity curr = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        ProjectBaseline baseline;
        if (optionalBaselineId != null) {
            baseline = projectBaselineRepository.findById(optionalBaselineId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProjectBaseline", "id", optionalBaselineId));
        } else {
            baseline = projectBaselineRepository.findByProjectIdAndIsActiveTrue(curr.getProject().getId())
                    .orElseThrow(() -> new BadRequestException("No active baseline found for project '" +
                            curr.getProject().getName() + "'. Please create or activate a baseline schedule first."));
        }

        BaselineActivitySnapshot snapshot = baselineActivitySnapshotRepository
                .findByBaselineIdAndActivityId(baseline.getId(), activityId)
                .orElseThrow(() -> new ResourceNotFoundException("BaselineActivitySnapshot for activity ID " + activityId, "baselineId", baseline.getId()));

        return computeActivityVariance(snapshot, curr);
    }

    @Override
    @Transactional
    public ActivityProgressLogResponse logDailyProgress(Long activityId, LogDailyProgressRequest request, String recordedBy) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        LocalDate date = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();
        double quantityToday = request.getQuantityCompletedToday();

        double prevCumulative = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
        double newCumulative = prevCumulative + quantityToday;

        if (act.getPlannedQuantity() != null && act.getPlannedQuantity() > 0) {
            newCumulative = Math.min(act.getPlannedQuantity(), newCumulative);
        }

        double newProgress = (act.getPlannedQuantity() != null && act.getPlannedQuantity() > 0)
                ? (newCumulative / act.getPlannedQuantity()) * 100.0
                : 0.0;
        newProgress = BigDecimal.valueOf(Math.min(100.0, Math.max(0.0, newProgress)))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        // Update activity progress via WbsActivityService (which performs status and dependency predecessor validations)
        UpdateWbsActivityProgressRequest progressReq = UpdateWbsActivityProgressRequest.builder()
                .completedQuantity(newCumulative)
                .progressPercentage(newProgress)
                .actualStartDate(act.getActualStartDate() != null ? act.getActualStartDate() : date)
                .actualEndDate(newProgress >= 100.0 ? date : null)
                .build();
        wbsActivityService.updateActivityProgress(activityId, progressReq, recordedBy);

        ActivityProgressLog logEntry = ActivityProgressLog.builder()
                .activity(act)
                .logDate(date)
                .quantityCompletedToday(quantityToday)
                .cumulativeQuantityCompleted(newCumulative)
                .cumulativeProgressPercentage(newProgress)
                .manpowerCount(request.getManpowerCount())
                .equipmentUsed(request.getEquipmentUsed())
                .siteHindranceNotes(request.getSiteHindranceNotes())
                .weatherCondition(request.getWeatherCondition())
                .recordedBy(recordedBy)
                .build();

        ActivityProgressLog savedLog = activityProgressLogRepository.save(logEntry);

        auditService.logAction(
                recordedBy,
                "LOG_DAILY_PROGRESS",
                "WBS_ACTIVITY",
                String.valueOf(activityId),
                "Logged Daily Progress: +" + quantityToday + " " + act.getUom() + " on " + date +
                        " (Cumulative: " + newCumulative + "/" + act.getPlannedQuantity() + " " + act.getUom() + ", " + newProgress + "%)",
                null
        );

        return mapToProgressLogResponse(savedLog, act);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityProgressLogResponse> getActivityProgressLogs(Long activityId) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        return activityProgressLogRepository.findByActivityIdOrderByLogDateDesc(activityId).stream()
                .map(log -> mapToProgressLogResponse(log, act))
                .collect(Collectors.toList());
    }

    private ActivityScheduleVarianceResponse computeActivityVariance(BaselineActivitySnapshot snap, WbsActivity curr) {
        LocalDate baseStart = snap.getPlannedStartDate();
        LocalDate baseEnd = snap.getPlannedEndDate();

        LocalDate currStart = curr.getActualStartDate() != null ? curr.getActualStartDate() : curr.getPlannedStartDate();
        LocalDate currEnd = curr.getActualEndDate() != null ? curr.getActualEndDate() : curr.getPlannedEndDate();

        int baseDuration = (baseStart != null && baseEnd != null) ? (int) ChronoUnit.DAYS.between(baseStart, baseEnd) : 0;
        int currDuration = (currStart != null && currEnd != null) ? (int) ChronoUnit.DAYS.between(currStart, currEnd) : 0;

        long startVariance = (baseStart != null && currStart != null) ? ChronoUnit.DAYS.between(baseStart, currStart) : 0;
        long finishVariance = (baseEnd != null && currEnd != null) ? ChronoUnit.DAYS.between(baseEnd, currEnd) : 0;

        String health;
        if (finishVariance > 5) {
            health = "CRITICAL_DELAY";
        } else if (finishVariance > 0) {
            health = "SLIGHT_DELAY";
        } else if (finishVariance < 0) {
            health = "AHEAD";
        } else {
            health = "ON_TRACK";
        }

        return ActivityScheduleVarianceResponse.builder()
                .activityId(curr.getId())
                .activityCode(curr.getCode())
                .activityName(curr.getName())
                .discipline(curr.getDiscipline())
                .currentStatus(curr.getStatus())
                .baselineStartDate(baseStart)
                .baselineEndDate(baseEnd)
                .baselineDurationDays(baseDuration)
                .currentStartDate(currStart)
                .currentEndDate(currEnd)
                .currentDurationDays(currDuration)
                .startVarianceDays(startVariance)
                .finishVarianceDays(finishVariance)
                .currentProgressPercentage(curr.getProgressPercentage())
                .healthStatus(health)
                .build();
    }

    private ProjectBaselineResponse mapToBaselineResponse(ProjectBaseline b) {
        return ProjectBaselineResponse.builder()
                .id(b.getId())
                .projectId(b.getProject() != null ? b.getProject().getId() : null)
                .projectName(b.getProject() != null ? b.getProject().getName() : null)
                .name(b.getName())
                .version(b.getVersion())
                .description(b.getDescription())
                .isActive(b.getIsActive())
                .createdBy(b.getCreatedBy())
                .approvedBy(b.getApprovedBy())
                .approvedAt(b.getApprovedAt())
                .totalActivities(b.getTotalActivities())
                .totalWorkPackages(b.getTotalWorkPackages())
                .totalBudgetAmount(b.getTotalBudgetAmount())
                .baselineStartDate(b.getBaselineStartDate())
                .baselineEndDate(b.getBaselineEndDate())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private BaselineActivitySnapshotResponse mapToSnapshotResponse(BaselineActivitySnapshot s) {
        return BaselineActivitySnapshotResponse.builder()
                .id(s.getId())
                .baselineId(s.getBaseline().getId())
                .activityId(s.getActivityId())
                .activityCode(s.getActivityCode())
                .activityName(s.getActivityName())
                .workPackageId(s.getWorkPackageId())
                .workPackageName(s.getWorkPackageName())
                .discipline(s.getDiscipline())
                .uom(s.getUom())
                .plannedQuantity(s.getPlannedQuantity())
                .plannedStartDate(s.getPlannedStartDate())
                .plannedEndDate(s.getPlannedEndDate())
                .plannedDurationDays(s.getPlannedDurationDays())
                .weightage(s.getWeightage())
                .assignedContractor(s.getAssignedContractor())
                .build();
    }

    private ActivityProgressLogResponse mapToProgressLogResponse(ActivityProgressLog log, WbsActivity act) {
        return ActivityProgressLogResponse.builder()
                .id(log.getId())
                .activityId(act.getId())
                .activityCode(act.getCode())
                .activityName(act.getName())
                .uom(act.getUom())
                .logDate(log.getLogDate())
                .quantityCompletedToday(log.getQuantityCompletedToday())
                .cumulativeQuantityCompleted(log.getCumulativeQuantityCompleted())
                .cumulativeProgressPercentage(log.getCumulativeProgressPercentage())
                .manpowerCount(log.getManpowerCount())
                .equipmentUsed(log.getEquipmentUsed())
                .siteHindranceNotes(log.getSiteHindranceNotes())
                .weatherCondition(log.getWeatherCondition())
                .recordedBy(log.getRecordedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
