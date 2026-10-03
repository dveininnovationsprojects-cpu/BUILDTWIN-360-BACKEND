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
