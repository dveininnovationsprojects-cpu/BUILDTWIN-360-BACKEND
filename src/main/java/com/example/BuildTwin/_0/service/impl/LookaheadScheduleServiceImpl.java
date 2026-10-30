package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.lookahead.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.ActivityDependencyRepository;
import com.example.BuildTwin._0.repository.LookaheadCommitmentRepository;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.LookaheadScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LookaheadScheduleServiceImpl implements LookaheadScheduleService {

    private final ProjectRepository projectRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ActivityDependencyRepository activityDependencyRepository;
    private final LookaheadCommitmentRepository lookaheadCommitmentRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public FourteenDayLookaheadResponse getFourteenDayLookaheadSchedule(
            Long projectId,
            LocalDate startDate,
            Long workPackageId,
            Long siteId,
            Long buildingId,
            String discipline,
            String status,
            Boolean includeOverdue) {

        LocalDate winStart = startDate != null ? startDate : LocalDate.now();
        LocalDate winEnd = winStart.plusDays(13); // 14 continuous calendar days

        LocalDate w1Start = winStart;
        LocalDate w1End = winStart.plusDays(6); // Days 1 to 7

        LocalDate w2Start = winStart.plusDays(7); // Days 8 to 14
        LocalDate w2End = winEnd;

        // 1. Get complete 14-day schedule
        LookaheadScheduleResponse fullSchedule = getLookaheadSchedule(
                projectId, winStart, 14, workPackageId, siteId, buildingId, discipline, status, includeOverdue
        );

        // 2. Get Week 1 schedule (Days 1 to 7) - Immediate commitment window
        LookaheadScheduleResponse week1Schedule = getLookaheadSchedule(
                projectId, w1Start, 7, workPackageId, siteId, buildingId, discipline, status, includeOverdue
        );

        // 3. Get Week 2 schedule (Days 8 to 14) - Lookahead make-ready window (exclude overdue since overdue is captured in Week 1 / backlog)
        LookaheadScheduleResponse week2Schedule = getLookaheadSchedule(
                projectId, w2Start, 7, workPackageId, siteId, buildingId, discipline, status, false
        );

        // 4. Get Week 2 constraints (hindrances that must be removed during Week 1 before Week 2 tasks commence)
        LookaheadConstraintSummaryResponse week2Constraints = getLookaheadConstraints(projectId, w2Start, 7);

        // 5. Refine categorization for 14-day window
        List<LookaheadActivityItemResponse> allActivities = new ArrayList<>(fullSchedule.getActivities());
        for (LookaheadActivityItemResponse item : allActivities) {
            LocalDate pStart = item.getPlannedStartDate();
            LocalDate pEnd = item.getPlannedEndDate();
            if (item.getCategory() == LookaheadCategory.OVERDUE) {
                continue; // keep OVERDUE
            }
            boolean startsW1 = pStart != null && !pStart.isBefore(w1Start) && !pStart.isAfter(w1End);
            boolean startsW2 = pStart != null && !pStart.isBefore(w2Start) && !pStart.isAfter(w2End);
            boolean endsW1 = pEnd != null && !pEnd.isBefore(w1Start) && !pEnd.isAfter(w1End);
            boolean endsW2 = pEnd != null && !pEnd.isBefore(w2Start) && !pEnd.isAfter(w2End);
            boolean spansBoth = pStart != null && pEnd != null && !pStart.isAfter(w1End) && !pEnd.isBefore(w2Start);

            if (spansBoth) {
                item.setCategory(LookaheadCategory.SPANNING_BOTH_WEEKS);
            } else if (startsW1 && endsW1) {
                item.setCategory(LookaheadCategory.STARTING_WEEK_1);
            } else if (startsW1) {
                item.setCategory(LookaheadCategory.STARTING_WEEK_1);
            } else if (startsW2) {
                item.setCategory(LookaheadCategory.STARTING_WEEK_2);
            } else if (endsW1) {
                item.setCategory(LookaheadCategory.FINISHING_WEEK_1);
            } else if (endsW2) {
                item.setCategory(LookaheadCategory.FINISHING_WEEK_2);
            }
        }

        // 6. Lookahead Health Assessment
        double overallReadiness = fullSchedule.getMetrics() != null && fullSchedule.getMetrics().getReadinessScorePercentage() != null
                ? fullSchedule.getMetrics().getReadinessScorePercentage() : 100.0;
        int totalBlocked = fullSchedule.getMetrics() != null && fullSchedule.getMetrics().getBlockedCount() != null
                ? fullSchedule.getMetrics().getBlockedCount() : 0;

        String healthStatus;
        if (overallReadiness >= 80.0 && totalBlocked <= 1) {
            healthStatus = "HEALTHY";
        } else if (overallReadiness >= 60.0 && totalBlocked <= 4) {
            healthStatus = "MODERATE_RISK";
        } else {
            healthStatus = "CRITICAL_ATTENTION_REQUIRED";
        }

        int w1Count = week1Schedule.getActivities().size();
        int w2Count = week2Schedule.getActivities().size();
        int w2Blockers = week2Constraints.getTotalBlockedActivities() != null ? week2Constraints.getTotalBlockedActivities() : 0;
        double w1Readiness = week1Schedule.getMetrics() != null && week1Schedule.getMetrics().getReadinessScorePercentage() != null
                ? week1Schedule.getMetrics().getReadinessScorePercentage() : 100.0;

        String plannerSummary = String.format(
                "14-Day Lookahead (Sprint) for %s: %d total activities scheduled. " +
                "Week 1 (Commitment Window: %s to %s) has %d activities (readiness: %.1f%%). " +
                "Week 2 (Lookahead Make-Ready Window: %s to %s) has %d activities with %d identified constraints requiring clearance.",
                fullSchedule.getProjectName(),
                allActivities.size(),
                w1Start, w1End, w1Count,
                w1Readiness,
                w2Start, w2End, w2Count, w2Blockers
        );

        return FourteenDayLookaheadResponse.builder()
                .projectId(fullSchedule.getProjectId())
                .projectCode(fullSchedule.getProjectCode())
                .projectName(fullSchedule.getProjectName())
                .windowStartDate(winStart)
                .windowEndDate(winEnd)
                .lookaheadDays(14)
                .week1StartDate(w1Start)
                .week1EndDate(w1End)
                .week1Metrics(week1Schedule.getMetrics())
                .week2StartDate(w2Start)
                .week2EndDate(w2End)
                .week2Metrics(week2Schedule.getMetrics())
                .overallMetrics(fullSchedule.getMetrics())
                .dailyBreakdown(fullSchedule.getDailyBreakdown())
                .week1Activities(week1Schedule.getActivities())
                .week2Activities(week2Schedule.getActivities())
                .allActivities(allActivities)
                .week2Constraints(week2Constraints)
                .lookaheadHealthStatus(healthStatus)
                .plannerSummary(plannerSummary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FourteenDayMetricsResponse getFourteenDayMetrics(Long projectId, LocalDate startDate) {
        FourteenDayLookaheadResponse schedule = getFourteenDayLookaheadSchedule(
                projectId, startDate, null, null, null, null, null, true
        );

        return FourteenDayMetricsResponse.builder()
                .projectId(schedule.getProjectId())
                .projectName(schedule.getProjectName())
                .windowStartDate(schedule.getWindowStartDate())
                .windowEndDate(schedule.getWindowEndDate())
                .overallMetrics(schedule.getOverallMetrics())
                .week1Metrics(schedule.getWeek1Metrics())
                .week2Metrics(schedule.getWeek2Metrics())
                .lookaheadHealthStatus(schedule.getLookaheadHealthStatus())
                .plannerSummary(schedule.getPlannerSummary())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LookaheadConstraintSummaryResponse getFourteenDayConstraints(Long projectId, LocalDate startDate) {
        return getLookaheadConstraints(projectId, startDate, 14);
    }

    @Override
    @Transactional(readOnly = true)
    public LookaheadScheduleResponse getLookaheadSchedule(
            Long projectId,
            LocalDate startDate,
            Integer days,
            Long workPackageId,
            Long siteId,
            Long buildingId,
            String discipline,
            String status,
            Boolean includeOverdue) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        LocalDate winStart = startDate != null ? startDate : LocalDate.now();
        int windowDays = (days != null && days > 0) ? Math.min(days, 60) : 7;
        LocalDate winEnd = winStart.plusDays(windowDays - 1);
        boolean checkOverdue = includeOverdue == null || includeOverdue;

        // 1. Fetch all activities for project
        List<WbsActivity> allActivities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);

        // 2. Fetch all commitments overlapping the window
        List<LookaheadCommitment> commitments = lookaheadCommitmentRepository.findByProjectIdAndDateRange(projectId, winStart, winEnd);
        Map<Long, LookaheadCommitment> commitmentByActivityId = new HashMap<>();
        for (LookaheadCommitment c : commitments) {
            if (c.getActivity() != null) {
                commitmentByActivityId.put(c.getActivity().getId(), c);
            }
        }

        // 3. Filter activities belonging to this Look-Ahead Window
        List<LookaheadActivityItemResponse> windowItems = new ArrayList<>();
        Set<String> disciplines = new HashSet<>();
        Set<Long> workPackageIds = new HashSet<>();

        int startingCount = 0;
        int finishingCount = 0;
        int ongoingCount = 0;
        int overdueCount = 0;
        int committedCount = 0;
        int blockedCount = 0;
        int readyCount = 0;
        int totalWorkloadDays = 0;

        for (WbsActivity act : allActivities) {
            // Apply optional property filters
            if (workPackageId != null && (act.getWorkPackage() == null || !workPackageId.equals(act.getWorkPackage().getId()))) {
                continue;
            }
            if (siteId != null && (act.getSite() == null || !siteId.equals(act.getSite().getId()))) {
                continue;
            }
            if (buildingId != null && (act.getBuilding() == null || !buildingId.equals(act.getBuilding().getId()))) {
                continue;
            }
            if (discipline != null && !discipline.trim().isEmpty() && (act.getDiscipline() == null || !act.getDiscipline().equalsIgnoreCase(discipline.trim()))) {
                continue;
            }
            if (status != null && !status.trim().isEmpty() && (act.getStatus() == null || !act.getStatus().equalsIgnoreCase(status.trim()))) {
                continue;
            }

            LocalDate pStart = act.getPlannedStartDate();
            LocalDate pEnd = act.getPlannedEndDate();

            if (pStart == null && pEnd == null) {
                continue;
            }

            boolean isCompleted = "COMPLETED".equalsIgnoreCase(act.getStatus());
            boolean isOverdue = checkOverdue && !isCompleted && pEnd != null && pEnd.isBefore(winStart);

            boolean isCommencingInWindow = pStart != null && !pStart.isBefore(winStart) && !pStart.isAfter(winEnd);
            boolean isFinishingInWindow = pEnd != null && !pEnd.isBefore(winStart) && !pEnd.isAfter(winEnd);
            boolean isSpanningWindow = pStart != null && pEnd != null && pStart.isBefore(winStart) && pEnd.isAfter(winEnd);

            if (!isOverdue && !isCommencingInWindow && !isFinishingInWindow && !isSpanningWindow) {
                continue; // Not active in this lookahead window
            }

            // Categorization
            LookaheadCategory category;
            long daysOverdue = 0;
            if (isOverdue) {
                category = LookaheadCategory.OVERDUE;
                daysOverdue = ChronoUnit.DAYS.between(pEnd, winStart);
                overdueCount++;
            } else if (isCommencingInWindow && isFinishingInWindow) {
                category = LookaheadCategory.STARTING_THIS_WEEK;
                startingCount++;
                finishingCount++;
            } else if (isCommencingInWindow) {
                category = LookaheadCategory.STARTING_THIS_WEEK;
                startingCount++;
            } else if (isFinishingInWindow) {
                category = LookaheadCategory.FINISHING_THIS_WEEK;
                finishingCount++;
            } else {
                category = LookaheadCategory.ONGOING;
                ongoingCount++;
            }

            // Calculate active calendar days in window
            List<LocalDate> activeDays = new ArrayList<>();
            LocalDate effectiveStart = pStart != null ? pStart : winStart;
            LocalDate effectiveEnd = pEnd != null ? pEnd : winEnd;

            LocalDate cur = winStart;
            while (!cur.isAfter(winEnd)) {
                if (isOverdue) {
                    activeDays.add(cur);
                } else if (!cur.isBefore(effectiveStart) && !cur.isAfter(effectiveEnd)) {
                    activeDays.add(cur);
                }
                cur = cur.plusDays(1);
            }
            totalWorkloadDays += activeDays.size();

            // Predecessors / Dependencies check
            List<ActivityDependency> dependencies = activityDependencyRepository.findBySuccessorId(act.getId());
            String predecessorStatus;
            List<String> unresolvedPredecessors = new ArrayList<>();
            if (dependencies.isEmpty()) {
                predecessorStatus = "NO_PREDECESSORS";
            } else {
                for (ActivityDependency dep : dependencies) {
                    WbsActivity pred = dep.getPredecessor();
                    if (pred != null && !"COMPLETED".equalsIgnoreCase(pred.getStatus())) {
                        unresolvedPredecessors.add(pred.getCode() + " - " + pred.getName() + " (" + pred.getStatus() + ")");
                    }
                }
                predecessorStatus = unresolvedPredecessors.isEmpty() ? "READY" : "PENDING_DEPENDENCIES";
            }

            // Weekly commitment details
            LookaheadCommitment commitment = commitmentByActivityId.get(act.getId());
            boolean isCommitted = commitment != null;
            if (isCommitted) {
                committedCount++;
            }

            // Readiness / Blocker determination
            String readinessStatus;
            if (isCompleted) {
                readinessStatus = "COMPLETED";
                readyCount++;
            } else if (commitment != null && commitment.getStatus() == CommitmentStatus.BLOCKED) {
                readinessStatus = "BLOCKED";
                blockedCount++;
            } else if (!unresolvedPredecessors.isEmpty()) {
                readinessStatus = "CONSTRAINED";
                blockedCount++;
            } else if (commitment != null && Boolean.FALSE.equals(isCommitmentConstraintFree(commitment))) {
                readinessStatus = "CONSTRAINED";
                blockedCount++;
            } else {
                readinessStatus = "READY_FOR_EXECUTION";
                readyCount++;
            }

            if (act.getDiscipline() != null) {
                disciplines.add(act.getDiscipline());
            }
            if (act.getWorkPackage() != null) {
                workPackageIds.add(act.getWorkPackage().getId());
            }

            int durationDays = 0;
            if (pStart != null && pEnd != null) {
                durationDays = (int) ChronoUnit.DAYS.between(pStart, pEnd);
            }

            LookaheadActivityItemResponse itemResponse = LookaheadActivityItemResponse.builder()
                    .activityId(act.getId())
                    .code(act.getCode())
                    .name(act.getName())
                    .discipline(act.getDiscipline())
                    .level(act.getLevel())
                    .wbsPath(act.getWbsPath())
                    .workPackageId(act.getWorkPackage() != null ? act.getWorkPackage().getId() : null)
                    .workPackageCode(act.getWorkPackage() != null ? act.getWorkPackage().getCode() : null)
                    .workPackageName(act.getWorkPackage() != null ? act.getWorkPackage().getName() : null)
                    .siteId(act.getSite() != null ? act.getSite().getId() : null)
                    .siteName(act.getSite() != null ? act.getSite().getName() : null)
                    .buildingId(act.getBuilding() != null ? act.getBuilding().getId() : null)
                    .buildingName(act.getBuilding() != null ? act.getBuilding().getName() : null)
                    .floorId(act.getFloor() != null ? act.getFloor().getId() : null)
                    .floorName(act.getFloor() != null ? act.getFloor().getFloorName() : null)
                    .zoneId(act.getZone() != null ? act.getZone().getId() : null)
                    .zoneName(act.getZone() != null ? act.getZone().getName() : null)
                    .status(act.getStatus())
                    .plannedStartDate(pStart)
                    .plannedEndDate(pEnd)
                    .actualStartDate(act.getActualStartDate())
                    .actualEndDate(act.getActualEndDate())
                    .durationDays(durationDays)
                    .plannedQuantity(act.getPlannedQuantity())
                    .completedQuantity(act.getCompletedQuantity())
                    .uom(act.getUom())
                    .progressPercentage(act.getProgressPercentage())
                    .assignedContractor(act.getAssignedContractor())
                    .inchargeUserId(act.getInchargeUserId())
                    .category(category)
                    .daysOverdue(daysOverdue)
                    .activeOnDays(activeDays)
                    .predecessorStatus(predecessorStatus)
                    .unresolvedPredecessorCount(unresolvedPredecessors.size())
                    .unresolvedPredecessors(unresolvedPredecessors)
                    .isCommitted(isCommitted)
                    .commitment(commitment != null ? mapCommitmentToResponse(commitment) : null)
                    .readinessStatus(readinessStatus)
                    .build();

            windowItems.add(itemResponse);
        }

        // 4. Build Day-by-Day workload distribution (Day 1 to Day N)
        List<LookaheadDailySummary> dailySummaries = new ArrayList<>();
        LocalDate dayIter = winStart;
        int dayIndex = 1;
        while (!dayIter.isAfter(winEnd)) {
            LocalDate currentDay = dayIter;
            List<String> activeCodes = new ArrayList<>();
            List<Long> activeIds = new ArrayList<>();
            int startingOnDay = 0;
            int finishingOnDay = 0;

            for (LookaheadActivityItemResponse item : windowItems) {
                if (item.getActiveOnDays().contains(currentDay)) {
                    activeCodes.add(item.getCode());
                    activeIds.add(item.getActivityId());
                }
                if (item.getPlannedStartDate() != null && item.getPlannedStartDate().equals(currentDay)) {
                    startingOnDay++;
                }
                if (item.getPlannedEndDate() != null && item.getPlannedEndDate().equals(currentDay)) {
                    finishingOnDay++;
                }
            }

            int weekNum = (dayIndex <= 7) ? 1 : ((dayIndex <= 14) ? 2 : ((dayIndex - 1) / 7 + 1));

            dailySummaries.add(LookaheadDailySummary.builder()
                    .date(currentDay)
                    .dayOfWeek(currentDay.getDayOfWeek().name())
                    .dayNumber(dayIndex++)
                    .weekNumber(weekNum)
                    .activeActivityCount(activeCodes.size())
                    .startingCount(startingOnDay)
                    .finishingCount(finishingOnDay)
                    .activityCodes(activeCodes)
                    .activityIds(activeIds)
                    .build());

            dayIter = dayIter.plusDays(1);
        }

        // 5. Aggregate Metrics
        int totalItems = windowItems.size();
        double readinessScore = totalItems > 0 ? ((double) readyCount / totalItems) * 100.0 : 100.0;
        readinessScore = Math.round(readinessScore * 10.0) / 10.0;

        LookaheadMetricsResponse metrics = LookaheadMetricsResponse.builder()
                .totalActivitiesInWindow(totalItems)
                .startingCount(startingCount)
                .finishingCount(finishingCount)
                .ongoingCount(ongoingCount)
                .overdueCount(overdueCount)
                .committedCount(committedCount)
                .uncommittedCount(Math.max(0, totalItems - committedCount))
                .blockedCount(blockedCount)
                .readyCount(readyCount)
                .readinessScorePercentage(readinessScore)
                .totalPlannedWorkloadDays(totalWorkloadDays)
                .disciplinesRepresented(new ArrayList<>(disciplines))
                .workPackagesRepresented(workPackageIds.size())
                .build();

        return LookaheadScheduleResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .windowStartDate(winStart)
                .windowEndDate(winEnd)
                .lookaheadDays(windowDays)
                .metrics(metrics)
                .dailyBreakdown(dailySummaries)
                .activities(windowItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LookaheadMetricsResponse getLookaheadMetrics(Long projectId, LocalDate startDate, Integer days) {
        LookaheadScheduleResponse schedule = getLookaheadSchedule(projectId, startDate, days, null, null, null, null, null, true);
        return schedule.getMetrics();
    }

    @Override
    @Transactional(readOnly = true)
    public LookaheadConstraintSummaryResponse getLookaheadConstraints(Long projectId, LocalDate startDate, Integer days) {
        LookaheadScheduleResponse schedule = getLookaheadSchedule(projectId, startDate, days, null, null, null, null, null, true);

        List<LookaheadActivityItemResponse> constrainedItems = schedule.getActivities().stream()
                .filter(act -> !"READY_FOR_EXECUTION".equalsIgnoreCase(act.getReadinessStatus()) && !"COMPLETED".equalsIgnoreCase(act.getStatus()))
                .collect(Collectors.toList());

        int matCount = 0;
        int drawCount = 0;
        int eqCount = 0;
        int manCount = 0;
        int predCount = 0;
        int accessCount = 0;

        for (LookaheadActivityItemResponse act : constrainedItems) {
            if (act.getUnresolvedPredecessorCount() != null && act.getUnresolvedPredecessorCount() > 0) {
                predCount++;
            }
            if (act.getCommitment() != null) {
                LookaheadCommitmentResponse c = act.getCommitment();
                if (Boolean.FALSE.equals(c.getMaterialsReady())) matCount++;
                if (Boolean.FALSE.equals(c.getDrawingsReady())) drawCount++;
                if (Boolean.FALSE.equals(c.getEquipmentReady())) eqCount++;
                if (Boolean.FALSE.equals(c.getManpowerReady())) manCount++;
                if (Boolean.FALSE.equals(c.getAccessClear())) accessCount++;
            }
        }

        return LookaheadConstraintSummaryResponse.builder()
                .projectId(projectId)
                .projectName(schedule.getProjectName())
                .windowStartDate(schedule.getWindowStartDate())
                .windowEndDate(schedule.getWindowEndDate())
                .totalBlockedActivities(constrainedItems.size())
                .materialConstraintCount(matCount)
                .drawingConstraintCount(drawCount)
                .equipmentConstraintCount(eqCount)
                .manpowerConstraintCount(manCount)
                .predecessorConstraintCount(predCount)
                .accessConstraintCount(accessCount)
                .constrainedActivities(constrainedItems)
                .build();
    }

    // =========================================================================
    // OVERDUE ACTIVITY IDENTIFICATION & MITIGATION IMPLEMENTATION
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<OverdueActivityItemResponse> getOverdueActivities(
            Long projectId,
            LocalDate asOfDate,
            String severity,
            String agingBucket,
            String type,
            String discipline,
            Long workPackageId,
            Long siteId,
            Long buildingId,
            String contractor) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        LocalDate refDate = asOfDate != null ? asOfDate : LocalDate.now();

        List<WbsActivity> allActivities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<LookaheadCommitment> commitments = lookaheadCommitmentRepository.findByProjectIdOrderByWeekStartDateDesc(projectId);
        Map<Long, LookaheadCommitment> commitmentByActivityId = new HashMap<>();
        for (LookaheadCommitment c : commitments) {
            if (c.getActivity() != null && !commitmentByActivityId.containsKey(c.getActivity().getId())) {
                commitmentByActivityId.put(c.getActivity().getId(), c);
            }
        }

        List<OverdueActivityItemResponse> overdueItems = new ArrayList<>();

        for (WbsActivity act : allActivities) {
            // Optional filters
            if (workPackageId != null && (act.getWorkPackage() == null || !workPackageId.equals(act.getWorkPackage().getId()))) {
                continue;
            }
            if (siteId != null && (act.getSite() == null || !siteId.equals(act.getSite().getId()))) {
                continue;
            }
            if (buildingId != null && (act.getBuilding() == null || !buildingId.equals(act.getBuilding().getId()))) {
                continue;
            }
            if (discipline != null && !discipline.isBlank() && (act.getDiscipline() == null || !act.getDiscipline().equalsIgnoreCase(discipline.trim()))) {
                continue;
            }
            if (contractor != null && !contractor.isBlank() && (act.getAssignedContractor() == null || !act.getAssignedContractor().equalsIgnoreCase(contractor.trim()))) {
                continue;
            }

            LocalDate pStart = act.getPlannedStartDate();
            LocalDate pEnd = act.getPlannedEndDate();
            if (pStart == null && pEnd == null) {
                continue;
            }

            boolean isCompleted = "COMPLETED".equalsIgnoreCase(act.getStatus())
                    || (act.getProgressPercentage() != null && act.getProgressPercentage() >= 100.0);

            if (isCompleted) {
                continue; // Completed activities are not currently overdue
            }

            double actualProgress = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
            double expectedProgress = 0.0;
            double progressSlippage = 0.0;
            long plannedDuration = (pStart != null && pEnd != null) ? Math.max(1, ChronoUnit.DAYS.between(pStart, pEnd)) : 0;

            OverdueType overdueType = null;
            long daysOverdue = 0;

            // Scenario 1: OVERDUE_FINISH (Target end date has passed and activity is incomplete)
            if (pEnd != null && pEnd.isBefore(refDate)) {
                overdueType = OverdueType.OVERDUE_FINISH;
                daysOverdue = ChronoUnit.DAYS.between(pEnd, refDate);
                expectedProgress = 100.0;
                progressSlippage = Math.max(0.0, 100.0 - actualProgress);
            }
            // Scenario 2: OVERDUE_START (Planned start date has passed, but work has not started)
            else if (pStart != null && pStart.isBefore(refDate)
                    && (act.getActualStartDate() == null || "PLANNED".equalsIgnoreCase(act.getStatus()) || "NOT_STARTED".equalsIgnoreCase(act.getStatus()) || actualProgress <= 0.0)) {
                overdueType = OverdueType.OVERDUE_START;
                daysOverdue = ChronoUnit.DAYS.between(pStart, refDate);
                if (plannedDuration > 0) {
                    long elapsed = ChronoUnit.DAYS.between(pStart, refDate);
                    expectedProgress = Math.min(100.0, ((double) elapsed / plannedDuration) * 100.0);
                    progressSlippage = Math.max(0.0, expectedProgress - actualProgress);
                }
            }
            // Scenario 3: BEHIND_SCHEDULE (Active within date window, but progress significantly lags schedule)
            else if (pStart != null && pEnd != null && !pStart.isAfter(refDate) && !pEnd.isBefore(refDate)) {
                long elapsed = ChronoUnit.DAYS.between(pStart, refDate);
                expectedProgress = plannedDuration > 0 ? Math.min(100.0, ((double) elapsed / plannedDuration) * 100.0) : 0.0;
                progressSlippage = expectedProgress - actualProgress;
                if (progressSlippage >= 20.0) {
                    overdueType = OverdueType.BEHIND_SCHEDULE;
                    daysOverdue = Math.max(1, Math.round((progressSlippage / 100.0) * plannedDuration));
                }
            }

            if (overdueType == null) {
                continue; // Not overdue
            }

            // Aging Bucket
            OverdueAgingBucket bucket;
            if (daysOverdue <= 7) {
                bucket = OverdueAgingBucket.DAYS_1_TO_7;
            } else if (daysOverdue <= 14) {
                bucket = OverdueAgingBucket.DAYS_8_TO_14;
            } else if (daysOverdue <= 30) {
                bucket = OverdueAgingBucket.DAYS_15_TO_30;
            } else {
                bucket = OverdueAgingBucket.OVER_30_DAYS;
            }

            // Downstream Successor Dependencies
            List<ActivityDependency> succDeps = activityDependencyRepository.findByPredecessorId(act.getId());
            List<String> impactedSuccessors = new ArrayList<>();
            for (ActivityDependency dep : succDeps) {
                WbsActivity succ = dep.getSuccessor();
                if (succ != null && !"COMPLETED".equalsIgnoreCase(succ.getStatus())) {
                    impactedSuccessors.add(succ.getCode() + " - " + succ.getName() + " [" + succ.getStatus() + "]");
                }
            }
            int impactedSuccessorCount = impactedSuccessors.size();

            // Severity Calculation
            OverdueSeverity sev;
            if (daysOverdue > 30 || impactedSuccessorCount >= 2 || (act.getLevel() != null && act.getLevel() == 1 && daysOverdue > 14)) {
                sev = OverdueSeverity.CRITICAL;
            } else if (daysOverdue >= 15 || impactedSuccessorCount >= 1) {
                sev = OverdueSeverity.HIGH;
            } else if (daysOverdue >= 8) {
                sev = OverdueSeverity.MEDIUM;
            } else {
                sev = OverdueSeverity.LOW;
            }

            // Quantities
            double plannedQty = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
            double completedQty = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
            double remainingQty = Math.max(0.0, plannedQty - completedQty);

            // Lookahead Commitment & Constraint Blockers
            LookaheadCommitment commitment = commitmentByActivityId.get(act.getId());
            boolean hasCommitment = commitment != null;
            String commitmentStatus = commitment != null ? commitment.getStatus().name() : "NOT_COMMITTED";
            List<String> blockers = new ArrayList<>();
            String latestBlocker = null;

            if (commitment != null) {
                if (Boolean.FALSE.equals(commitment.getMaterialsReady())) blockers.add("Materials Staging Deficit");
                if (Boolean.FALSE.equals(commitment.getDrawingsReady())) blockers.add("GFC Drawing Approval Pending");
                if (Boolean.FALSE.equals(commitment.getEquipmentReady())) blockers.add("Equipment / Machinery Breakdown");
                if (Boolean.FALSE.equals(commitment.getManpowerReady())) blockers.add("Subcontractor Gang Shortage");
                if (Boolean.FALSE.equals(commitment.getSafetyPermitApproved())) blockers.add("Safety PTW Clearance Missing");
                if (Boolean.FALSE.equals(commitment.getAccessClear())) blockers.add("Site Work Area Obstructed");
                if (commitment.getBlockerReason() != null && !commitment.getBlockerReason().isBlank()) {
                    blockers.add(commitment.getBlockerReason());
                    latestBlocker = commitment.getBlockerReason();
                } else if (commitment.getVarianceReason() != null && !commitment.getVarianceReason().isBlank()) {
                    latestBlocker = commitment.getVarianceReason();
                }
            }

            // Check predecessor readiness
            List<ActivityDependency> predDeps = activityDependencyRepository.findBySuccessorId(act.getId());
            for (ActivityDependency dep : predDeps) {
                WbsActivity pred = dep.getPredecessor();
                if (pred != null && !"COMPLETED".equalsIgnoreCase(pred.getStatus())) {
                    blockers.add("Blocked by Predecessor: " + pred.getCode() + " (" + pred.getStatus() + ")");
                }
            }

            // Recovery Action Suggestion
            String recoveryAction;
            if (sev == OverdueSeverity.CRITICAL) {
                recoveryAction = "CRITICAL ACTION: Expedite immediately. " +
                        (impactedSuccessorCount > 0 ? "Blocks " + impactedSuccessorCount + " downstream tasks (" + String.join(", ", impactedSuccessors) + "). " : "") +
                        "Mobilize double shift, additional gang, or fast-track rebar/concrete works.";
            } else if (overdueType == OverdueType.OVERDUE_START) {
                recoveryAction = "Kickoff delay: Issue immediate work commencement notice to " +
                        (act.getAssignedContractor() != null ? act.getAssignedContractor() : "contractor") + " and verify drawing & material readiness.";
            } else if (!blockers.isEmpty()) {
                recoveryAction = "Resolve pending constraints (" + String.join("; ", blockers) + ") to allow unimpeded progress.";
            } else {
                recoveryAction = "Review daily production rate with " +
                        (act.getAssignedContractor() != null ? act.getAssignedContractor() : "site team") + " to recover " + daysOverdue + " days delay.";
            }

            // Type filter
            if (type != null && !type.isBlank() && !overdueType.name().equalsIgnoreCase(type.trim())) {
                continue;
            }
            // Severity filter
            if (severity != null && !severity.isBlank() && !sev.name().equalsIgnoreCase(severity.trim())) {
                continue;
            }
            // Aging filter
            if (agingBucket != null && !agingBucket.isBlank() && !bucket.name().equalsIgnoreCase(agingBucket.trim())) {
                continue;
            }

            OverdueActivityItemResponse item = OverdueActivityItemResponse.builder()
                    .activityId(act.getId())
                    .code(act.getCode())
                    .name(act.getName())
                    .discipline(act.getDiscipline())
                    .level(act.getLevel())
                    .wbsPath(act.getWbsPath())
                    .workPackageId(act.getWorkPackage() != null ? act.getWorkPackage().getId() : null)
                    .workPackageCode(act.getWorkPackage() != null ? act.getWorkPackage().getCode() : null)
                    .workPackageName(act.getWorkPackage() != null ? act.getWorkPackage().getName() : null)
                    .siteId(act.getSite() != null ? act.getSite().getId() : null)
                    .siteName(act.getSite() != null ? act.getSite().getName() : null)
                    .buildingId(act.getBuilding() != null ? act.getBuilding().getId() : null)
                    .buildingName(act.getBuilding() != null ? act.getBuilding().getName() : null)
                    .floorName(act.getFloor() != null ? act.getFloor().getFloorName() : null)
                    .zoneName(act.getZone() != null ? act.getZone().getName() : null)
                    .status(act.getStatus())
                    .plannedStartDate(pStart)
                    .plannedEndDate(pEnd)
                    .actualStartDate(act.getActualStartDate())
                    .actualEndDate(act.getActualEndDate())
                    .plannedDurationDays((int) plannedDuration)
                    .overdueType(overdueType)
                    .daysOverdue(daysOverdue)
                    .agingBucket(bucket)
                    .severity(sev)
                    .plannedQuantity(plannedQty)
                    .completedQuantity(completedQty)
                    .remainingQuantity(remainingQty)
                    .uom(act.getUom())
                    .progressPercentage(Math.round(actualProgress * 10.0) / 10.0)
                    .expectedProgressPercentage(Math.round(expectedProgress * 10.0) / 10.0)
                    .progressSlippagePercentage(Math.round(progressSlippage * 10.0) / 10.0)
                    .assignedContractor(act.getAssignedContractor())
                    .inchargeUserId(act.getInchargeUserId())
                    .impactedSuccessorCount(impactedSuccessorCount)
                    .impactedSuccessors(impactedSuccessors)
                    .hasCommitment(hasCommitment)
                    .commitmentStatus(commitmentStatus)
                    .constraintBlockers(blockers)
                    .latestBlockerReason(latestBlocker)
                    .recommendedRecoveryAction(recoveryAction)
                    .build();

            overdueItems.add(item);
        }

        // Sort by severity (CRITICAL > HIGH > MEDIUM > LOW), then by daysOverdue descending
        overdueItems.sort((a, b) -> {
            int sevComp = Integer.compare(getSeverityRank(b.getSeverity()), getSeverityRank(a.getSeverity()));
            if (sevComp != 0) return sevComp;
            return Long.compare(b.getDaysOverdue() != null ? b.getDaysOverdue() : 0, a.getDaysOverdue() != null ? a.getDaysOverdue() : 0);
        });

        return overdueItems;
    }

    private int getSeverityRank(OverdueSeverity sev) {
        if (sev == null) return 0;
        return switch (sev) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public OverdueActivitySummaryResponse getOverdueActivitySummary(Long projectId, LocalDate asOfDate) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        LocalDate refDate = asOfDate != null ? asOfDate : LocalDate.now();

        List<OverdueActivityItemResponse> overdueList = getOverdueActivities(
                projectId, refDate, null, null, null, null, null, null, null, null
        );

        long totalActivities = wbsActivityRepository.countByProjectId(projectId);

        int overdueFinishCount = 0;
        int overdueStartCount = 0;
        int behindScheduleCount = 0;
        long totalDelayDays = 0;
        long maxDelayDays = 0;
        int totalImpactedSuccessors = 0;

        Map<String, Integer> agingMap = new LinkedHashMap<>();
        agingMap.put("DAYS_1_TO_7", 0);
        agingMap.put("DAYS_8_TO_14", 0);
        agingMap.put("DAYS_15_TO_30", 0);
        agingMap.put("OVER_30_DAYS", 0);

        Map<String, Integer> severityMap = new LinkedHashMap<>();
        severityMap.put("CRITICAL", 0);
        severityMap.put("HIGH", 0);
        severityMap.put("MEDIUM", 0);
        severityMap.put("LOW", 0);

        Map<String, Integer> disciplineMap = new HashMap<>();
        Map<String, Integer> contractorMap = new HashMap<>();
        Map<String, Double> remainingQtyMap = new HashMap<>();

        for (OverdueActivityItemResponse item : overdueList) {
            if (item.getOverdueType() == OverdueType.OVERDUE_FINISH) overdueFinishCount++;
            else if (item.getOverdueType() == OverdueType.OVERDUE_START) overdueStartCount++;
            else if (item.getOverdueType() == OverdueType.BEHIND_SCHEDULE) behindScheduleCount++;

            long delay = item.getDaysOverdue() != null ? item.getDaysOverdue() : 0;
            totalDelayDays += delay;
            if (delay > maxDelayDays) maxDelayDays = delay;

            totalImpactedSuccessors += (item.getImpactedSuccessorCount() != null ? item.getImpactedSuccessorCount() : 0);

            if (item.getAgingBucket() != null) {
                agingMap.put(item.getAgingBucket().name(), agingMap.getOrDefault(item.getAgingBucket().name(), 0) + 1);
            }
            if (item.getSeverity() != null) {
                severityMap.put(item.getSeverity().name(), severityMap.getOrDefault(item.getSeverity().name(), 0) + 1);
            }
            if (item.getDiscipline() != null) {
                disciplineMap.put(item.getDiscipline(), disciplineMap.getOrDefault(item.getDiscipline(), 0) + 1);
            }
            String contractor = item.getAssignedContractor() != null ? item.getAssignedContractor() : "Unassigned";
            contractorMap.put(contractor, contractorMap.getOrDefault(contractor, 0) + 1);

            if (item.getUom() != null && item.getRemainingQuantity() != null && item.getRemainingQuantity() > 0) {
                remainingQtyMap.put(item.getUom(), Math.round((remainingQtyMap.getOrDefault(item.getUom(), 0.0) + item.getRemainingQuantity()) * 10.0) / 10.0);
            }
        }

        int overdueCount = overdueList.size();
        double avgDelay = overdueCount > 0 ? (double) totalDelayDays / overdueCount : 0.0;
        avgDelay = Math.round(avgDelay * 10.0) / 10.0;

        double overdueRate = totalActivities > 0 ? ((double) overdueCount / totalActivities) * 100.0 : 0.0;
        overdueRate = Math.round(overdueRate * 10.0) / 10.0;

        List<OverdueActivityItemResponse> topBottlenecks = overdueList.stream()
                .limit(5)
                .collect(Collectors.toList());

        List<String> recommendations = new ArrayList<>();
        if (overdueCount == 0) {
            recommendations.add("All activities on schedule as of " + refDate + ". Maintain current weekly velocity.");
        } else {
            if (severityMap.getOrDefault("CRITICAL", 0) > 0) {
                recommendations.add("Urgent: " + severityMap.get("CRITICAL") + " CRITICAL activities identified. Prioritize resources and crash critical path predecessors.");
            }
            if (overdueStartCount > 0) {
                recommendations.add(overdueStartCount + " activities missed their scheduled kickoff dates. Verify site access, staging, and subcontractor mobilization.");
            }
            if (totalImpactedSuccessors > 0) {
                recommendations.add(totalImpactedSuccessors + " downstream tasks are blocked by pending delays. Focus on bottleneck activities to prevent cascading project slippage.");
            }
            if (!remainingQtyMap.isEmpty()) {
                recommendations.add("Cumulative pending physical workload across delayed activities: " + remainingQtyMap + ".");
            }
        }

        return OverdueActivitySummaryResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .asOfDate(refDate)
                .totalProjectActivities((int) totalActivities)
                .totalOverdueActivities(overdueCount)
                .overdueRatePercentage(overdueRate)
                .overdueFinishCount(overdueFinishCount)
                .overdueStartCount(overdueStartCount)
                .behindScheduleCount(behindScheduleCount)
                .averageDelayDays(avgDelay)
                .maxDelayDays(maxDelayDays)
                .totalImpactedSuccessors(totalImpactedSuccessors)
                .agingBreakdown(agingMap)
                .severityBreakdown(severityMap)
                .disciplineBreakdown(disciplineMap)
                .contractorBreakdown(contractorMap)
                .remainingWorkloadByUom(remainingQtyMap)
                .topCriticalBottlenecks(topBottlenecks)
                .plannerRecommendations(recommendations)
                .build();
    }

    @Override
    @Transactional
    public OverdueMitigationResponse recordOverdueMitigation(
            Long projectId,
            Long activityId,
            OverdueMitigationRequest request,
            String performedBy) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        WbsActivity activity = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        if (!activity.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Activity " + activity.getCode() + " does not belong to Project ID " + projectId);
        }

        // Attach mitigation info to LookaheadCommitment
        LocalDate today = LocalDate.now();
        List<LookaheadCommitment> existing = lookaheadCommitmentRepository.findByActivityIdOrderByWeekStartDateDesc(activityId);
        LookaheadCommitment commitment;
        if (!existing.isEmpty()) {
            commitment = existing.get(0);
        } else {
            commitment = new LookaheadCommitment();
            commitment.setProject(project);
            commitment.setActivity(activity);
            commitment.setWeekStartDate(today);
            commitment.setWeekEndDate(today.plusDays(6));
            commitment.setStatus(CommitmentStatus.COMMITTED);
            commitment.setTargetQuantity(activity.getPlannedQuantity() != null ? activity.getPlannedQuantity() : 0.0);
            commitment.setUom(activity.getUom());
            commitment.setCommittedBy(performedBy);
        }

        commitment.setVarianceReason(request.getDelayReasonCategory() + ": " + (request.getDelayReasonDetails() != null ? request.getDelayReasonDetails() : ""));
        commitment.setNotes("MITIGATION PLAN: " + request.getRecoveryPlanAction() +
                (request.getNotes() != null ? " | Notes: " + request.getNotes() : ""));

        if (request.getAdditionalLaborAllocated() != null && request.getAdditionalLaborAllocated() > 0) {
            int currentCrew = commitment.getCrewSize() != null ? commitment.getCrewSize() : 0;
            commitment.setCrewSize(currentCrew + request.getAdditionalLaborAllocated());
        }

        lookaheadCommitmentRepository.save(commitment);

        // Update activity status to DELAYED if currently PLANNED or IN_PROGRESS
        if (!"COMPLETED".equalsIgnoreCase(activity.getStatus())) {
            activity.setStatus("DELAYED");
            wbsActivityRepository.save(activity);
        }

        auditService.logAction(
                performedBy,
                "OVERDUE_ACTIVITY_MITIGATION",
                "WBS_ACTIVITY",
                String.valueOf(activity.getId()),
                "Logged root-cause delay reason [" + request.getDelayReasonCategory() + "] and catch-up action: " + request.getRecoveryPlanAction(),
                null
        );

        return OverdueMitigationResponse.builder()
                .activityId(activity.getId())
                .activityCode(activity.getCode())
                .activityName(activity.getName())
                .delayReasonCategory(request.getDelayReasonCategory())
                .delayReasonDetails(request.getDelayReasonDetails())
                .recoveryPlanAction(request.getRecoveryPlanAction())
                .revisedExpectedCompletionDate(request.getRevisedExpectedCompletionDate())
                .additionalLaborAllocated(request.getAdditionalLaborAllocated())
                .recordedBy(performedBy)
                .recordedAt(LocalDateTime.now())
                .message("Overdue activity mitigation and recovery catch-up plan logged successfully.")
                .build();
    }

    // =========================================================================
    // COMMITMENT CRUD
    // =========================================================================

    @Override
    @Transactional
    public LookaheadCommitmentResponse createCommitment(Long projectId, CreateLookaheadCommitmentRequest request, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        WbsActivity activity = wbsActivityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", request.getActivityId()));

        if (!activity.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Activity " + activity.getCode() + " does not belong to Project ID " + projectId);
        }

        LocalDate start = request.getWeekStartDate() != null ? request.getWeekStartDate() : LocalDate.now();
        LocalDate end = request.getWeekEndDate() != null ? request.getWeekEndDate() : start.plusDays(6);

        if (end.isBefore(start)) {
            throw new BadRequestException("Week end date (" + end + ") cannot be before week start date (" + start + ")");
        }

        // Check if an existing commitment for this exact activity and date window already exists
        Optional<LookaheadCommitment> existingOpt = lookaheadCommitmentRepository.findByActivityIdAndWeekStartDateAndWeekEndDate(
                activity.getId(), start, end);

        LookaheadCommitment commitment;
        if (existingOpt.isPresent()) {
            commitment = existingOpt.get();
        } else {
            commitment = new LookaheadCommitment();
            commitment.setProject(project);
            commitment.setActivity(activity);
            commitment.setWeekStartDate(start);
            commitment.setWeekEndDate(end);
        }

        Double targetQty = request.getTargetQuantity() != null ? request.getTargetQuantity() : activity.getPlannedQuantity();
        String uom = request.getUom() != null ? request.getUom() : activity.getUom();
        CommitmentStatus status = request.getStatus() != null ? request.getStatus() : CommitmentStatus.COMMITTED;

        commitment.setTargetQuantity(targetQty);
        commitment.setUom(uom);
        commitment.setStatus(status);
        commitment.setCrewSize(request.getCrewSize());
        commitment.setAssignedContractor(request.getAssignedContractor() != null ? request.getAssignedContractor() : activity.getAssignedContractor());
        commitment.setMaterialsReady(request.getMaterialsReady() != null ? request.getMaterialsReady() : true);
        commitment.setDrawingsReady(request.getDrawingsReady() != null ? request.getDrawingsReady() : true);
        commitment.setEquipmentReady(request.getEquipmentReady() != null ? request.getEquipmentReady() : true);
        commitment.setManpowerReady(request.getManpowerReady() != null ? request.getManpowerReady() : true);
        commitment.setSafetyPermitApproved(request.getSafetyPermitApproved() != null ? request.getSafetyPermitApproved() : true);
        commitment.setAccessClear(request.getAccessClear() != null ? request.getAccessClear() : true);
        commitment.setBlockerReason(request.getBlockerReason());
        commitment.setNotes(request.getNotes());
        commitment.setCommittedBy(performedBy);

        LookaheadCommitment saved = lookaheadCommitmentRepository.save(commitment);

        auditService.logAction(
                performedBy,
                "CREATE_LOOKAHEAD_COMMITMENT",
                "LOOKAHEAD_COMMITMENT",
                String.valueOf(saved.getId()),
                "Committed activity " + activity.getCode() + " for window " + start + " to " + end + " with target " + targetQty + " " + uom,
                null
        );

        return mapCommitmentToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookaheadCommitmentResponse> getCommitments(Long projectId, LocalDate weekStartDate, CommitmentStatus status) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<LookaheadCommitment> list;
        if (status != null) {
            list = lookaheadCommitmentRepository.findByProjectIdAndStatusOrderByWeekStartDateDesc(projectId, status);
        } else {
            list = lookaheadCommitmentRepository.findByProjectIdOrderByWeekStartDateDesc(projectId);
        }

        if (weekStartDate != null) {
            list = list.stream().filter(c -> c.getWeekStartDate().equals(weekStartDate)).collect(Collectors.toList());
        }

        return list.stream().map(this::mapCommitmentToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LookaheadCommitmentResponse getCommitmentById(Long commitmentId) {
        LookaheadCommitment commitment = lookaheadCommitmentRepository.findById(commitmentId)
                .orElseThrow(() -> new ResourceNotFoundException("LookaheadCommitment", "id", commitmentId));
        return mapCommitmentToResponse(commitment);
    }

    @Override
    @Transactional
    public LookaheadCommitmentResponse updateCommitment(Long commitmentId, UpdateLookaheadCommitmentRequest request, String performedBy) {
        LookaheadCommitment commitment = lookaheadCommitmentRepository.findById(commitmentId)
                .orElseThrow(() -> new ResourceNotFoundException("LookaheadCommitment", "id", commitmentId));

        if (request.getTargetQuantity() != null) {
            commitment.setTargetQuantity(request.getTargetQuantity());
        }
        if (request.getActualQuantityAchieved() != null) {
            commitment.setActualQuantityAchieved(request.getActualQuantityAchieved());
            if (commitment.getTargetQuantity() != null && commitment.getTargetQuantity() > 0) {
                if (request.getActualQuantityAchieved() >= commitment.getTargetQuantity()) {
                    commitment.setStatus(CommitmentStatus.COMPLETED);
                }
            }
        }
        if (request.getStatus() != null) {
            commitment.setStatus(request.getStatus());
        }
        if (request.getCrewSize() != null) {
            commitment.setCrewSize(request.getCrewSize());
        }
        if (request.getAssignedContractor() != null) {
            commitment.setAssignedContractor(request.getAssignedContractor());
        }
        if (request.getMaterialsReady() != null) {
            commitment.setMaterialsReady(request.getMaterialsReady());
        }
        if (request.getDrawingsReady() != null) {
            commitment.setDrawingsReady(request.getDrawingsReady());
        }
        if (request.getEquipmentReady() != null) {
            commitment.setEquipmentReady(request.getEquipmentReady());
        }
        if (request.getManpowerReady() != null) {
            commitment.setManpowerReady(request.getManpowerReady());
        }
        if (request.getSafetyPermitApproved() != null) {
            commitment.setSafetyPermitApproved(request.getSafetyPermitApproved());
        }
        if (request.getAccessClear() != null) {
            commitment.setAccessClear(request.getAccessClear());
        }
        if (request.getBlockerReason() != null) {
            commitment.setBlockerReason(request.getBlockerReason());
        }
        if (request.getVarianceReason() != null) {
            commitment.setVarianceReason(request.getVarianceReason());
        }
        if (request.getNotes() != null) {
            commitment.setNotes(request.getNotes());
        }

        LookaheadCommitment saved = lookaheadCommitmentRepository.save(commitment);

        auditService.logAction(
                performedBy,
                "UPDATE_LOOKAHEAD_COMMITMENT",
                "LOOKAHEAD_COMMITMENT",
                String.valueOf(saved.getId()),
                "Updated weekly commitment for activity " + saved.getActivity().getCode() + " status: " + saved.getStatus(),
                null
        );

        return mapCommitmentToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCommitment(Long commitmentId, String performedBy) {
        LookaheadCommitment commitment = lookaheadCommitmentRepository.findById(commitmentId)
                .orElseThrow(() -> new ResourceNotFoundException("LookaheadCommitment", "id", commitmentId));

        lookaheadCommitmentRepository.delete(commitment);

        auditService.logAction(
                performedBy,
                "DELETE_LOOKAHEAD_COMMITMENT",
                "LOOKAHEAD_COMMITMENT",
                String.valueOf(commitmentId),
                "Deleted lookahead commitment ID " + commitmentId,
                null
        );
    }

    private boolean isCommitmentConstraintFree(LookaheadCommitment c) {
        return Boolean.TRUE.equals(c.getMaterialsReady())
                && Boolean.TRUE.equals(c.getDrawingsReady())
                && Boolean.TRUE.equals(c.getEquipmentReady())
                && Boolean.TRUE.equals(c.getManpowerReady())
                && Boolean.TRUE.equals(c.getSafetyPermitApproved())
                && Boolean.TRUE.equals(c.getAccessClear());
    }

    private LookaheadCommitmentResponse mapCommitmentToResponse(LookaheadCommitment c) {
        double achievementPct = 0.0;
        if (c.getTargetQuantity() != null && c.getTargetQuantity() > 0 && c.getActualQuantityAchieved() != null) {
            achievementPct = Math.min(100.0, (c.getActualQuantityAchieved() / c.getTargetQuantity()) * 100.0);
            achievementPct = Math.round(achievementPct * 10.0) / 10.0;
        }

        WbsActivity act = c.getActivity();

        return LookaheadCommitmentResponse.builder()
                .id(c.getId())
                .projectId(c.getProject() != null ? c.getProject().getId() : null)
                .activityId(act != null ? act.getId() : null)
                .activityCode(act != null ? act.getCode() : null)
                .activityName(act != null ? act.getName() : null)
                .workPackageCode(act != null && act.getWorkPackage() != null ? act.getWorkPackage().getCode() : null)
                .weekStartDate(c.getWeekStartDate())
                .weekEndDate(c.getWeekEndDate())
                .targetQuantity(c.getTargetQuantity())
                .uom(c.getUom())
                .actualQuantityAchieved(c.getActualQuantityAchieved())
                .achievementPercentage(achievementPct)
                .status(c.getStatus())
                .crewSize(c.getCrewSize())
                .assignedContractor(c.getAssignedContractor())
                .materialsReady(c.getMaterialsReady())
                .drawingsReady(c.getDrawingsReady())
                .equipmentReady(c.getEquipmentReady())
                .manpowerReady(c.getManpowerReady())
                .safetyPermitApproved(c.getSafetyPermitApproved())
                .accessClear(c.getAccessClear())
                .isConstraintFree(isCommitmentConstraintFree(c))
                .blockerReason(c.getBlockerReason())
                .varianceReason(c.getVarianceReason())
                .notes(c.getNotes())
                .committedBy(c.getCommittedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
