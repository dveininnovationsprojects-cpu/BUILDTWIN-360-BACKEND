package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.integration.*;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.ModuleIntegrationService;
import com.example.BuildTwin._0.service.ProjectProgressSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleIntegrationServiceImpl implements ModuleIntegrationService {

    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ZoneRepository zoneRepository;
    private final WorkPackageRepository workPackageRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ProjectBaselineRepository projectBaselineRepository;
    private final BaselineActivitySnapshotRepository baselineActivitySnapshotRepository;
    private final ScheduleRevisionRepository scheduleRevisionRepository;
    private final ScheduleDelayAlertRepository scheduleDelayAlertRepository;
    private final ActivityDependencyRepository activityDependencyRepository;
    private final LookaheadCommitmentRepository lookaheadCommitmentRepository;
    private final DailyProgressReportRepository dailyProgressReportRepository;
    private final ActivityProgressLogRepository activityProgressLogRepository;
    private final ProjectProgressSummaryService projectProgressSummaryService;

    @Override
    @Transactional(readOnly = true)
    public ProjectModuleIntegrationSummaryResponse getProjectModuleIntegrationSummary(Long projectId) {
        log.info("Calculating module integration summary for projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // 1. Spatial Hierarchy Metrics
        long siteCount = siteRepository.countByProjectId(projectId);
        List<Site> sites = siteRepository.findByProjectId(projectId);
        long buildingCount = 0;
        long floorCount = 0;
        long zoneCount = 0;
        for (Site site : sites) {
            List<Building> buildings = buildingRepository.findBySiteId(site.getId());
            buildingCount += buildings.size();
            for (Building b : buildings) {
                List<Floor> floors = floorRepository.findByBuildingIdOrderByFloorNumberAsc(b.getId());
                floorCount += floors.size();
                for (Floor f : floors) {
                    zoneCount += zoneRepository.countByFloorId(f.getId());
                }
            }
        }

        String hierarchyStatus = (siteCount > 0 && zoneCount > 0) ? "CONFIGURED" : (siteCount > 0 ? "PARTIAL" : "UNCONFIGURED");
        HierarchyIntegrationDto hierarchyDto = HierarchyIntegrationDto.builder()
                .siteCount(siteCount)
                .buildingCount(buildingCount)
                .floorCount(floorCount)
                .zoneCount(zoneCount)
                .status(hierarchyStatus)
                .build();

        // 2. WBS & Activity Metrics
        long workPackageCount = workPackageRepository.countByProjectId(projectId);
        long activityCount = wbsActivityRepository.countByProjectId(projectId);
        long completedActivities = wbsActivityRepository.countByProjectIdAndStatus(projectId, "COMPLETED");
        long inProgressActivities = wbsActivityRepository.countByProjectIdAndStatus(projectId, "IN_PROGRESS");
        long plannedActivities = wbsActivityRepository.countByProjectIdAndStatus(projectId, "PLANNED");
        List<WbsActivity> allActivities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        long milestoneCount = allActivities.stream()
                .filter(a -> a.getPlannedQuantity() != null && a.getPlannedQuantity() == 0.0)
                .count();

        String wbsStatus = (activityCount > 0) ? "ACTIVE" : (workPackageCount > 0 ? "STRUCTURE_DEFINED" : "EMPTY");
        WbsIntegrationDto wbsDto = WbsIntegrationDto.builder()
                .workPackageCount(workPackageCount)
                .activityCount(activityCount)
                .completedActivitiesCount(completedActivities)
                .inProgressActivitiesCount(inProgressActivities)
                .plannedActivitiesCount(plannedActivities)
                .milestoneCount(milestoneCount)
                .status(wbsStatus)
                .build();

        // 3. Baseline & Schedule Metrics
        Optional<ProjectBaseline> activeBaselineOpt = projectBaselineRepository.findByProjectIdAndIsActiveTrue(projectId);
        Integer activeBaselineVersion = activeBaselineOpt.map(ProjectBaseline::getVersion).orElse(null);
        String activeBaselineName = activeBaselineOpt.map(ProjectBaseline::getName).orElse(null);
        Boolean isBaselineApproved = activeBaselineOpt.map(b -> b.getApprovedAt() != null).orElse(false);
        long scheduleRevisionsCount = scheduleRevisionRepository.countByProjectId(projectId);
        long delayAlertsCount = scheduleDelayAlertRepository.countByProjectId(projectId);
        long dependencyCount = activityDependencyRepository.findByProjectId(projectId).size();

        String baselineStatus = activeBaselineOpt.isPresent()
                ? (Boolean.TRUE.equals(isBaselineApproved) ? "APPROVED_FROZEN" : "ACTIVE_DRAFT")
                : "NO_ACTIVE_BASELINE";
        ScheduleBaselineIntegrationDto scheduleBaselineDto = ScheduleBaselineIntegrationDto.builder()
                .activeBaselineVersion(activeBaselineVersion)
                .activeBaselineName(activeBaselineName)
                .isBaselineApproved(isBaselineApproved)
                .scheduleRevisionsCount(scheduleRevisionsCount)
                .delayAlertsCount(delayAlertsCount)
                .dependencyCount(dependencyCount)
                .status(baselineStatus)
                .build();

        // 4. Lookahead Planning Metrics
        long totalCommitments = lookaheadCommitmentRepository.countByProjectId(projectId);
        long activeCommitments = lookaheadCommitmentRepository.countByProjectIdAndStatus(projectId, CommitmentStatus.COMMITTED);
        long completedCommitments = lookaheadCommitmentRepository.countByProjectIdAndStatus(projectId, CommitmentStatus.COMPLETED);
        long overdueCommitments = lookaheadCommitmentRepository.countByProjectIdAndStatus(projectId, CommitmentStatus.BLOCKED)
                + lookaheadCommitmentRepository.countByProjectIdAndStatus(projectId, CommitmentStatus.NOT_ACHIEVED);

        String lookaheadStatus = totalCommitments > 0 ? "SYNCHRONIZED" : "STANDBY";
        LookaheadIntegrationDto lookaheadDto = LookaheadIntegrationDto.builder()
                .totalCommitments(totalCommitments)
                .activeCommitments(activeCommitments)
                .completedCommitments(completedCommitments)
                .overdueCommitments(overdueCommitments)
                .status(lookaheadStatus)
                .build();

        // 5. Daily Progress Report (DPR) Metrics
        Long totalDprs = dailyProgressReportRepository.countByProjectId(projectId);
        Long approvedDprs = dailyProgressReportRepository.countByProjectIdAndStatus(projectId, DprStatus.APPROVED);
        Long pendingDprs = (totalDprs != null ? totalDprs : 0L) - (approvedDprs != null ? approvedDprs : 0L);
        Optional<DailyProgressReport> latestDprOpt = dailyProgressReportRepository.findTopByProjectIdOrderByReportDateDesc(projectId);
        LocalDate latestReportDate = latestDprOpt.map(DailyProgressReport::getReportDate).orElse(null);
        String latestWorkShift = latestDprOpt.map(d -> d.getShift() != null ? d.getShift().name() : null).orElse(null);
        Long totalManpowerHeadcount = dailyProgressReportRepository.sumManpowerByProjectId(projectId);
        Long totalSafetyIncidents = dailyProgressReportRepository.sumSafetyIncidentsByProjectId(projectId);

        String dprStatusStr = totalDprs != null && totalDprs > 0 ? "FIELD_ACTIVE" : "NO_REPORTS_FILED";
        DprIntegrationDto dprDto = DprIntegrationDto.builder()
                .totalDprs(totalDprs != null ? totalDprs : 0L)
                .approvedDprs(approvedDprs != null ? approvedDprs : 0L)
                .pendingDprs(pendingDprs)
                .latestReportDate(latestReportDate)
                .latestWorkShift(latestWorkShift)
                .totalManpowerHeadcount(totalManpowerHeadcount != null ? totalManpowerHeadcount : 0L)
                .totalSafetyIncidents(totalSafetyIncidents != null ? totalSafetyIncidents : 0L)
                .status(dprStatusStr)
                .build();

        // 6. Progress Engine & Summary Metrics
        Long totalProgressLogs = activityProgressLogRepository.countByProjectId(projectId);
        LocalDate lastProgressUpdateDate = activityProgressLogRepository.findMaxLogDateByProjectId(projectId);

        Double cumulativeProgressPercentage = 0.0;
        Double plannedProgressPercentage = 0.0;
        Double progressVariance = 0.0;
        String scheduleHealth = "NOT_STARTED";

        try {
            ProjectProgressSummaryResponse summary = projectProgressSummaryService.getProjectProgressSummary(projectId);
            if (summary != null) {
                cumulativeProgressPercentage = summary.getOverallProgressPercentage();
                plannedProgressPercentage = summary.getPlannedProgressPercentage();
                progressVariance = summary.getProgressVariance();
                scheduleHealth = summary.getScheduleHealth();
            }
        } catch (Exception e) {
            log.warn("Could not fetch progress summary for projectId={}: {}", projectId, e.getMessage());
        }

        String progressEngineStatus = totalProgressLogs != null && totalProgressLogs > 0 ? "SYNCHRONIZED" : "STANDBY";
        ProgressEngineIntegrationDto progressEngineDto = ProgressEngineIntegrationDto.builder()
                .totalProgressLogs(totalProgressLogs != null ? totalProgressLogs : 0L)
                .lastProgressUpdateDate(lastProgressUpdateDate)
                .cumulativeProgressPercentage(cumulativeProgressPercentage)
                .plannedProgressPercentage(plannedProgressPercentage)
                .progressVariance(progressVariance)
                .calculationModel("WEIGHTAGE_FACTOR")
                .scheduleHealth(scheduleHealth)
                .status(progressEngineStatus)
                .build();

        // 7. Compute Readiness Score & Overall Integration Status
        int readinessScore = 0;
        List<String> integratedModules = new ArrayList<>();

        if (siteCount > 0) {
            readinessScore += 15;
            integratedModules.add("SPATIAL_HIERARCHY");
        }
        if (workPackageCount > 0 && activityCount > 0) {
            readinessScore += 20;
            integratedModules.add("WBS_ACTIVITIES");
        }
        if (activeBaselineOpt.isPresent()) {
            readinessScore += 20;
            integratedModules.add("BASELINE_SCHEDULE");
        }
        if (totalCommitments > 0) {
            readinessScore += 15;
            integratedModules.add("LOOKAHEAD_PLANNING");
        }
        if (totalDprs != null && totalDprs > 0) {
            readinessScore += 15;
            integratedModules.add("DPR_FIELD_TRACKING");
        }
        if (totalProgressLogs != null && totalProgressLogs > 0) {
            readinessScore += 15;
            integratedModules.add("PROGRESS_ENGINE");
        }

        String overallIntegrationStatus;
        if (readinessScore >= 70) {
            overallIntegrationStatus = "FULLY_INTEGRATED";
        } else if (readinessScore > 0) {
            overallIntegrationStatus = "PARTIALLY_INTEGRATED";
        } else {
            overallIntegrationStatus = "INITIALIZED";
        }

        return ProjectModuleIntegrationSummaryResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .projectStatus(project.getStatus())
                .estimatedBudget(project.getEstimatedBudget())
                .integrationStatus(overallIntegrationStatus)
                .readinessScore(readinessScore)
                .hierarchy(hierarchyDto)
                .wbs(wbsDto)
                .scheduleBaseline(scheduleBaselineDto)
                .lookahead(lookaheadDto)
                .dpr(dprDto)
                .progressEngine(progressEngineDto)
                .integratedModules(integratedModules)
                .syncedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectModulesListResponse getProjectModulesList(Long projectId) {
        log.info("Listing registered modules for projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<ModuleDescriptorDto> modules = new ArrayList<>();

        // 1. Hierarchy
        long siteCount = siteRepository.countByProjectId(projectId);
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("HIERARCHY")
                .moduleName("Spatial Hierarchy & Construction Structure")
                .basePath("/api/v1/hierarchy")
                .version("1.0.0")
                .status(siteCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(siteCount)
                .health("HEALTHY")
                .build());

        // 2. WBS
        long activityCount = wbsActivityRepository.countByProjectId(projectId);
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("WBS")
                .moduleName("Work Breakdown Structure & Activity Management")
                .basePath("/api/v1/activities")
                .version("1.0.0")
                .status(activityCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(activityCount)
                .health("HEALTHY")
                .build());

        // 3. Baseline & Dependencies
        long baselineCount = projectBaselineRepository.findByProjectIdOrderByVersionDesc(projectId).size();
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("BASELINE_SCHEDULE")
                .moduleName("Master Schedule Baseline & Revision Engine")
                .basePath("/api/v1/projects/" + projectId + "/baselines")
                .version("1.0.0")
                .status(baselineCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(baselineCount)
                .health("HEALTHY")
                .build());

        // 4. Lookahead Planning
        long commitmentsCount = lookaheadCommitmentRepository.countByProjectId(projectId);
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("LOOKAHEAD_PLANNING")
                .moduleName("7 & 14-Day Lookahead Planning & Commitment Tracking")
                .basePath("/api/v1/projects/" + projectId + "/lookahead")
                .version("1.0.0")
                .status(commitmentsCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(commitmentsCount)
                .health("HEALTHY")
                .build());

        // 5. DPR
        Long dprCount = dailyProgressReportRepository.countByProjectId(projectId);
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("DPR_FIELD_TRACKING")
                .moduleName("Daily Progress Reporting (DPR) Field Execution")
                .basePath("/api/v1/dpr")
                .version("1.0.0")
                .status(dprCount != null && dprCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(dprCount != null ? dprCount : 0L)
                .health("HEALTHY")
                .build());

        // 6. Progress History
        Long progressLogCount = activityProgressLogRepository.countByProjectId(projectId);
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("PROGRESS_HISTORY")
                .moduleName("Activity Progress Journal & Audit Trail")
                .basePath("/api/v1/projects/" + projectId + "/progress-history")
                .version("1.0.0")
                .status(progressLogCount != null && progressLogCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(progressLogCount != null ? progressLogCount : 0L)
                .health("HEALTHY")
                .build());

        // 7. Cumulative Progress Engine
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("CUMULATIVE_PROGRESS")
                .moduleName("Cumulative Progress Calculation & S-Curve Engine")
                .basePath("/api/v1/projects/" + projectId + "/cumulative-progress")
                .version("1.0.0")
                .status(activityCount > 0 ? "SYNCHRONIZED" : "STANDBY")
                .entityCount(activityCount)
                .health("HEALTHY")
                .build());

        // 8. Project Progress Summary Dashboard
        modules.add(ModuleDescriptorDto.builder()
                .moduleKey("SUMMARY_DASHBOARD")
                .moduleName("Executive Project Progress Summary Dashboard")
                .basePath("/api/v1/projects/" + projectId + "/progress-summary")
                .version("1.0.0")
                .status("SYNCHRONIZED")
                .entityCount(1L)
                .health("HEALTHY")
                .build());

        int activeCount = (int) modules.stream().filter(m -> "SYNCHRONIZED".equalsIgnoreCase(m.getStatus())).count();

        return ProjectModulesListResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .totalModules(modules.size())
                .activeModules(activeCount)
                .modules(modules)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectCrossReferenceResponse getProjectCrossReferences(Long projectId) {
        log.info("Fetching cross-module entity references for projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<ActivityCrossReferenceDto> dtoList = new ArrayList<>();

        for (WbsActivity act : activities) {
            String syncStatus = ("COMPLETED".equalsIgnoreCase(act.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(act.getStatus()))
                    ? "SYNCED"
                    : "PENDING";

            ActivityCrossReferenceDto dto = ActivityCrossReferenceDto.builder()
                    .activityId(act.getId())
                    .activityCode(act.getCode())
                    .activityName(act.getName())
                    .activityStatus(act.getStatus())
                    .discipline(act.getDiscipline())
                    .workPackageId(act.getWorkPackage() != null ? act.getWorkPackage().getId() : null)
                    .workPackageCode(act.getWorkPackage() != null ? act.getWorkPackage().getCode() : null)
                    .workPackageName(act.getWorkPackage() != null ? act.getWorkPackage().getName() : null)
                    .siteId(act.getSite() != null ? act.getSite().getId() : null)
                    .siteName(act.getSite() != null ? act.getSite().getName() : null)
                    .buildingName(act.getBuilding() != null ? act.getBuilding().getName() : null)
                    .floorName(act.getFloor() != null ? act.getFloor().getFloorName() : null)
                    .zoneId(act.getZone() != null ? act.getZone().getId() : null)
                    .zoneName(act.getZone() != null ? act.getZone().getName() : null)
                    .progressPercentage(act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0)
                    .assignedContractor(act.getAssignedContractor())
                    .syncStatus(syncStatus)
                    .build();

            dtoList.add(dto);
        }

        return ProjectCrossReferenceResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .totalActivities(dtoList.size())
                .activities(dtoList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityIntegrationDetailsResponse getActivityIntegrationDetails(Long activityId) {
        log.info("Fetching deep activity integration details for activityId={}", activityId);

        WbsActivity activity = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        Project project = activity.getProject();
        WorkPackage workPackage = activity.getWorkPackage();
        Site site = activity.getSite();
        Building building = activity.getBuilding();
        Floor floor = activity.getFloor();
        Zone zone = activity.getZone();

        // Dependencies
        int predecessorCount = activityDependencyRepository.findBySuccessorId(activityId).size();
        int successorCount = activityDependencyRepository.findByPredecessorId(activityId).size();

        // Active baseline snapshot
        Integer activeBaselineVersion = null;
        LocalDate baselineStartDate = null;
        LocalDate baselineEndDate = null;
        Integer scheduleVarianceDays = null;

        if (project != null) {
            Optional<ProjectBaseline> activeBaseline = projectBaselineRepository.findByProjectIdAndIsActiveTrue(project.getId());
            if (activeBaseline.isPresent()) {
                activeBaselineVersion = activeBaseline.get().getVersion();
                Optional<BaselineActivitySnapshot> snapshotOpt =
                        baselineActivitySnapshotRepository.findByBaselineIdAndActivityId(activeBaseline.get().getId(), activityId);
                if (snapshotOpt.isPresent()) {
                    BaselineActivitySnapshot snapshot = snapshotOpt.get();
                    baselineStartDate = snapshot.getPlannedStartDate();
                    baselineEndDate = snapshot.getPlannedEndDate();
                    if (baselineEndDate != null && activity.getPlannedEndDate() != null) {
                        scheduleVarianceDays = (int) ChronoUnit.DAYS.between(baselineEndDate, activity.getPlannedEndDate());
                    }
                }
            }
        }

        // Duration days
        Integer durationDays = null;
        if (activity.getPlannedStartDate() != null && activity.getPlannedEndDate() != null) {
            durationDays = (int) ChronoUnit.DAYS.between(activity.getPlannedStartDate(), activity.getPlannedEndDate()) + 1;
        }

        // Progress logs
        Optional<ActivityProgressLog> latestLog = activityProgressLogRepository.findTopByActivityIdOrderByLogDateDesc(activityId);
        LocalDate lastProgressUpdateDate = latestLog.map(ActivityProgressLog::getLogDate).orElse(null);

        String integrationStatus;
        if (site != null && workPackage != null && activeBaselineVersion != null) {
            integrationStatus = "FULLY_INTEGRATED";
        } else if (workPackage != null) {
            integrationStatus = "PARTIALLY_INTEGRATED";
        } else {
            integrationStatus = "BASIC";
        }

        return ActivityIntegrationDetailsResponse.builder()
                .activityId(activity.getId())
                .activityCode(activity.getCode())
                .activityName(activity.getName())
                .discipline(activity.getDiscipline())
                .status(activity.getStatus())
                .plannedStartDate(activity.getPlannedStartDate())
                .plannedEndDate(activity.getPlannedEndDate())
                .actualStartDate(activity.getActualStartDate())
                .actualEndDate(activity.getActualEndDate())
                .durationDays(durationDays)
                .weightage(activity.getWeightage())
                .projectId(project != null ? project.getId() : null)
                .projectCode(project != null ? project.getCode() : null)
                .workPackageId(workPackage != null ? workPackage.getId() : null)
                .workPackageName(workPackage != null ? workPackage.getName() : null)
                .siteId(site != null ? site.getId() : null)
                .siteName(site != null ? site.getName() : null)
                .buildingId(building != null ? building.getId() : null)
                .buildingName(building != null ? building.getName() : null)
                .floorId(floor != null ? floor.getId() : null)
                .floorName(floor != null ? floor.getFloorName() : null)
                .zoneId(zone != null ? zone.getId() : null)
                .zoneName(zone != null ? zone.getName() : null)
                .predecessorCount(predecessorCount)
                .successorCount(successorCount)
                .activeBaselineVersion(activeBaselineVersion)
                .baselineStartDate(baselineStartDate)
                .baselineEndDate(baselineEndDate)
                .scheduleVarianceDays(scheduleVarianceDays != null ? scheduleVarianceDays : 0)
                .plannedQuantity(activity.getPlannedQuantity())
                .completedQuantity(activity.getCompletedQuantity())
                .uom(activity.getUom())
                .progressPercentage(activity.getProgressPercentage() != null ? activity.getProgressPercentage() : 0.0)
                .lastProgressUpdateDate(lastProgressUpdateDate)
                .assignedContractor(activity.getAssignedContractor())
                .integrationStatus(integrationStatus)
                .build();
    }

    @Override
    public ModuleHealthResponse getSystemIntegrationHealth() {
        Map<String, String> subsystems = new LinkedHashMap<>();
        subsystems.put("SPATIAL_HIERARCHY", "UP");
        subsystems.put("WBS_ACTIVITIES", "UP");
        subsystems.put("SCHEDULE_BASELINE", "UP");
        subsystems.put("LOOKAHEAD_PLANNING", "UP");
        subsystems.put("DAILY_PROGRESS_REPORT", "UP");
        subsystems.put("PROGRESS_HISTORY", "UP");
        subsystems.put("CUMULATIVE_PROGRESS_ENGINE", "UP");
        subsystems.put("PROGRESS_SUMMARY_DASHBOARD", "UP");

        return ModuleHealthResponse.builder()
                .status("UP")
                .version("1.0.0-ENTERPRISE")
                .timestamp(LocalDateTime.now())
                .subsystemStatus(subsystems)
                .build();
    }
}
