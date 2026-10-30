package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.progress.*;
import com.example.BuildTwin._0.dto.wbs.UpdateWbsActivityProgressRequest;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.ActivityProgressLog;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.WbsActivity;
import com.example.BuildTwin._0.model.WorkPackage;
import com.example.BuildTwin._0.repository.ActivityProgressLogRepository;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.ProgressHistoryService;
import com.example.BuildTwin._0.service.WbsActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class ProgressHistoryServiceImpl implements ProgressHistoryService {

    private final ActivityProgressLogRepository progressLogRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final WorkPackageRepository workPackageRepository;
    private final ProjectRepository projectRepository;
    private final WbsActivityService wbsActivityService;
    private final AuditService auditService;

    // =========================================================================
    // 1. ACTIVITY PROGRESS HISTORY
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ActivityProgressHistoryResponse getActivityProgressHistory(
            Long activityId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            String sortDir) {

        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        boolean isAsc = "asc".equalsIgnoreCase(sortDir);
        List<ActivityProgressLog> logs = isAsc
                ? progressLogRepository.findActivityLogsFilteredAsc(activityId, startDate, endDate, isBlocker, source)
                : progressLogRepository.findActivityLogsFiltered(activityId, startDate, endDate, isBlocker, source);

        List<ProgressHistoryItemResponse> items = logs.stream()
                .map(log -> mapToItemResponse(log, act))
                .collect(Collectors.toList());

        // Performance & Statistical Analytics
        int totalLogs = logs.size();
        double totalQuantityLogged = logs.stream()
                .mapToDouble(l -> l.getQuantityCompletedToday() != null ? l.getQuantityCompletedToday() : 0.0)
                .sum();
        totalQuantityLogged = round2(totalQuantityLogged);

        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double completed = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
        double balance = Math.max(0.0, round2(planned - completed));

        double avgDaily = totalLogs > 0 ? round2(totalQuantityLogged / totalLogs) : 0.0;
        double maxDaily = logs.stream()
                .mapToDouble(l -> l.getQuantityCompletedToday() != null ? l.getQuantityCompletedToday() : 0.0)
                .max()
                .orElse(0.0);
        maxDaily = round2(maxDaily);

        LocalDate firstDate = logs.stream()
                .map(ActivityProgressLog::getLogDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDate lastDate = logs.stream()
                .map(ActivityProgressLog::getLogDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        int blockerDays = (int) logs.stream()
                .filter(l -> Boolean.TRUE.equals(l.getIsBlocker()))
                .count();

        // Run rate calculations
        LocalDate today = LocalDate.now();
        LocalDate plannedEnd = act.getPlannedEndDate() != null ? act.getPlannedEndDate() : today.plusDays(30);
        long daysRemaining = Math.max(1L, ChronoUnit.DAYS.between(today, plannedEnd));
        double requiredRunRate = balance > 0 ? round2(balance / daysRemaining) : 0.0;

        LocalDate actualStart = act.getActualStartDate() != null ? act.getActualStartDate() : firstDate;
        long elapsedDays = actualStart != null ? Math.max(1L, ChronoUnit.DAYS.between(actualStart, today) + 1) : 1L;
        double actualAvgRate = completed > 0 ? round2(completed / elapsedDays) : 0.0;

        WorkPackage wp = act.getWorkPackage();
        Project prj = act.getProject();

        return ActivityProgressHistoryResponse.builder()
                .activityId(act.getId())
                .activityCode(act.getCode())
                .activityName(act.getName())
                .discipline(act.getDiscipline())
                .workPackageId(wp != null ? wp.getId() : null)
                .workPackageName(wp != null ? wp.getName() : null)
                .projectId(prj != null ? prj.getId() : null)
                .projectName(prj != null ? prj.getName() : null)
                .uom(act.getUom())
                .plannedQuantity(planned)
                .completedQuantity(completed)
                .balanceQuantity(balance)
                .currentProgressPercentage(act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0)
                .status(act.getStatus())
                .assignedContractor(act.getAssignedContractor())
                .plannedStartDate(act.getPlannedStartDate())
                .plannedEndDate(act.getPlannedEndDate())
                .actualStartDate(act.getActualStartDate())
                .actualEndDate(act.getActualEndDate())
                .totalLogsCount(totalLogs)
                .totalQuantityLogged(totalQuantityLogged)
                .averageDailyQuantity(avgDaily)
                .maxDailyQuantity(maxDaily)
                .firstLogDate(firstDate)
                .latestLogDate(lastDate)
                .totalBlockerDays(blockerDays)
                .requiredDailyRunRate(requiredRunRate)
                .actualDailyAverageRate(actualAvgRate)
                .history(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProgressHistoryItemResponse> getActivityProgressHistoryPaginated(
            Long activityId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            int page,
            int size,
            String sortDir) {

        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, "logDate", "id");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ActivityProgressLog> logPage = progressLogRepository.findActivityLogsFilteredPaged(
                activityId, startDate, endDate, isBlocker, source, pageable);

        List<ProgressHistoryItemResponse> content = logPage.getContent().stream()
                .map(log -> mapToItemResponse(log, act))
                .collect(Collectors.toList());

        return PageResponse.<ProgressHistoryItemResponse>builder()
                .content(content)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .isFirst(logPage.isFirst())
                .isLast(logPage.isLast())
                .build();
    }

    // =========================================================================
    // 2. PROJECT-WIDE PROGRESS HISTORY
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ProjectProgressHistoryResponse getProjectProgressHistory(
            Long projectId,
            Long workPackageId,
            Long activityId,
            String discipline,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String sortProperty = (sortBy != null && !sortBy.isBlank()) ? sortBy : "logDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortProperty).and(Sort.by(direction, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ActivityProgressLog> logPage = progressLogRepository.findProjectLogsFiltered(
                projectId, workPackageId, activityId, discipline, startDate, endDate, isBlocker, source, pageable);

        List<ProgressHistoryItemResponse> items = logPage.getContent().stream()
                .map(log -> mapToItemResponse(log, log.getActivity()))
                .collect(Collectors.toList());

        PageResponse<ProgressHistoryItemResponse> pagedResponse = PageResponse.<ProgressHistoryItemResponse>builder()
                .content(items)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .isFirst(logPage.isFirst())
                .isLast(logPage.isLast())
                .build();

        // Project overall rollups
        Long totalEntries = progressLogRepository.countByProjectId(projectId);
        Double totalQty = round2(progressLogRepository.sumQuantityByProjectId(projectId));
        Integer uniqueActs = progressLogRepository.countDistinctActivitiesByProjectId(projectId);
        Long totalManpower = progressLogRepository.sumManpowerByProjectId(projectId);
        Long totalBlockers = progressLogRepository.countBlockersByProjectId(projectId);
        LocalDate earliest = progressLogRepository.findMinLogDateByProjectId(projectId);
        LocalDate latest = progressLogRepository.findMaxLogDateByProjectId(projectId);

        return ProjectProgressHistoryResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .projectCode(project.getCode())
                .totalEntries(totalEntries != null ? totalEntries : 0L)
                .totalQuantityLogged(totalQty != null ? totalQty : 0.0)
                .uniqueActivitiesCount(uniqueActs != null ? uniqueActs : 0)
                .totalManpowerLogged(totalManpower != null ? totalManpower : 0L)
                .totalBlockersEncountered(totalBlockers != null ? totalBlockers : 0L)
                .earliestLogDate(earliest)
                .latestLogDate(latest)
                .items(pagedResponse)
                .build();
    }

    // =========================================================================
    // 3. WORK PACKAGE PROGRESS HISTORY
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public WorkPackageProgressHistoryResponse getWorkPackageProgressHistory(
            Long workPackageId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            int page,
            int size,
            String sortDir) {

        WorkPackage wp = workPackageRepository.findById(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", workPackageId));

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, "logDate", "id");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ActivityProgressLog> logPage = progressLogRepository.findWorkPackageLogsFiltered(
                workPackageId, startDate, endDate, isBlocker, source, pageable);

        List<ProgressHistoryItemResponse> items = logPage.getContent().stream()
                .map(log -> mapToItemResponse(log, log.getActivity()))
                .collect(Collectors.toList());

        PageResponse<ProgressHistoryItemResponse> pagedResponse = PageResponse.<ProgressHistoryItemResponse>builder()
                .content(items)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .isFirst(logPage.isFirst())
                .isLast(logPage.isLast())
                .build();

        Long totalEntries = progressLogRepository.countByWorkPackageId(workPackageId);
        Double totalQty = round2(progressLogRepository.sumQuantityByWorkPackageId(workPackageId));
        Integer uniqueActs = progressLogRepository.countDistinctActivitiesByWorkPackageId(workPackageId);
        Long totalBlockers = progressLogRepository.countBlockersByWorkPackageId(workPackageId);

        Project prj = wp.getProject();

        return WorkPackageProgressHistoryResponse.builder()
                .workPackageId(wp.getId())
                .workPackageName(wp.getName())
                .workPackageCode(wp.getCode())
                .projectId(prj != null ? prj.getId() : null)
                .projectName(prj != null ? prj.getName() : null)
                .totalEntries(totalEntries != null ? totalEntries : 0L)
                .totalQuantityLogged(totalQty != null ? totalQty : 0.0)
                .uniqueActivitiesCount(uniqueActs != null ? uniqueActs : 0)
                .totalBlockersEncountered(totalBlockers != null ? totalBlockers : 0L)
                .history(items)
                .paginatedHistory(pagedResponse)
                .build();
    }

    // =========================================================================
    // 4. PROGRESS HISTORY ANALYTICS & S-CURVE TIMELINE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ProgressAnalyticsResponse getActivityProgressAnalytics(Long activityId) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        List<ActivityProgressLog> logs = progressLogRepository.findByActivityIdOrderByLogDateAsc(activityId);

        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double completed = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
        double balance = Math.max(0.0, round2(planned - completed));
        double currentPct = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;

        LocalDate plannedStart = act.getPlannedStartDate();
        LocalDate plannedEnd = act.getPlannedEndDate();
        LocalDate actualStart = act.getActualStartDate();
        LocalDate actualEnd = act.getActualEndDate();

        LocalDate today = LocalDate.now();
        long daysRemaining = (plannedEnd != null && plannedEnd.isAfter(today))
                ? ChronoUnit.DAYS.between(today, plannedEnd)
                : 0L;

        double requiredRate = (daysRemaining > 0 && balance > 0) ? round2(balance / daysRemaining) : 0.0;

        long elapsedDays = (actualStart != null)
                ? Math.max(1L, ChronoUnit.DAYS.between(actualStart, (actualEnd != null ? actualEnd : today)) + 1)
                : 1L;
        double actualAvgRate = completed > 0 ? round2(completed / elapsedDays) : 0.0;

        // Total planned duration in days
        long totalPlannedDays = (plannedStart != null && plannedEnd != null)
                ? Math.max(1L, ChronoUnit.DAYS.between(plannedStart, plannedEnd) + 1)
                : 30L;

        // Build chronological timeline points
        List<ProgressTimelinePointDto> timeline = new ArrayList<>();
        for (ActivityProgressLog log : logs) {
            LocalDate d = log.getLogDate();
            double plannedTargetPct = 0.0;
            if (plannedStart != null && plannedEnd != null && !d.isBefore(plannedStart)) {
                long daysSinceStart = ChronoUnit.DAYS.between(plannedStart, d) + 1;
                plannedTargetPct = Math.min(100.0, round2(((double) daysSinceStart / totalPlannedDays) * 100.0));
            }

            timeline.add(ProgressTimelinePointDto.builder()
                    .date(d)
                    .quantityToday(log.getQuantityCompletedToday())
                    .cumulativeQuantity(log.getCumulativeQuantityCompleted())
                    .cumulativePercentage(log.getCumulativeProgressPercentage())
                    .plannedCumulativePercentage(plannedTargetPct)
                    .manpower(log.getManpowerCount())
                    .isBlocker(log.getIsBlocker())
                    .build());
        }

        // Velocity Trend Status
        String trendStatus;
        if (currentPct >= 100.0 || "COMPLETED".equalsIgnoreCase(act.getStatus())) {
            trendStatus = "COMPLETED";
        } else if (plannedStart != null && plannedEnd != null) {
            long daysSinceStart = Math.max(0L, ChronoUnit.DAYS.between(plannedStart, today) + 1);
            double targetProgress = Math.min(100.0, ((double) daysSinceStart / totalPlannedDays) * 100.0);
            if (currentPct >= targetProgress + 5.0) {
                trendStatus = "AHEAD";
            } else if (currentPct < targetProgress - 5.0) {
                trendStatus = "BEHIND";
            } else {
                trendStatus = "ON_TRACK";
            }
        } else {
            trendStatus = "ON_TRACK";
        }

        return ProgressAnalyticsResponse.builder()
                .activityId(act.getId())
                .activityCode(act.getCode())
                .activityName(act.getName())
                .uom(act.getUom())
                .plannedQuantity(planned)
                .completedQuantity(completed)
                .balanceQuantity(balance)
                .progressPercentage(currentPct)
                .plannedStartDate(plannedStart)
                .plannedEndDate(plannedEnd)
                .actualStartDate(actualStart)
                .actualEndDate(actualEnd)
                .daysRemaining(daysRemaining)
                .requiredDailyRunRate(requiredRate)
                .actualDailyAverageRate(actualAvgRate)
                .trendStatus(trendStatus)
                .timeline(timeline)
                .build();
    }

    // =========================================================================
    // 5. GET SINGLE LOG ITEM
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ProgressHistoryItemResponse getProgressHistoryItemById(Long logId) {
        ActivityProgressLog log = progressLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityProgressLog", "id", logId));
        return mapToItemResponse(log, log.getActivity());
    }

    // =========================================================================
    // 6. DIRECT SITE PROGRESS LOGGING
    // =========================================================================

    @Override
    @Transactional
    public ProgressHistoryItemResponse logProgress(Long activityId, LogProgressHistoryRequest request, String recordedBy) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        LocalDate date = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();
        double quantityToday = request.getQuantityCompletedToday();

        double prevCumulative = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
        double newCumulative = round2(prevCumulative + quantityToday);

        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double newProgress = planned > 0.0
                ? round2(Math.min(100.0, (newCumulative / planned) * 100.0))
                : 100.0;

        // Cascade progress update to WBS activity and tree
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
                .recordedBy(recordedBy != null ? recordedBy : "SITE_ENGINEER")
                .isBlocker(Boolean.TRUE.equals(request.getIsBlocker()))
                .blockerCategory(request.getBlockerCategory())
                .blockerReason(request.getBlockerReason())
                .remarks(request.getRemarks())
                .source("MANUAL_LOG")
                .build();

        ActivityProgressLog savedLog = progressLogRepository.save(logEntry);

        auditService.logAction(
                recordedBy != null ? recordedBy : "SYSTEM",
                "LOG_PROGRESS_HISTORY",
                "WBS_ACTIVITY",
                String.valueOf(activityId),
                "Logged Progress History: +" + quantityToday + " " + act.getUom() + " on " + date +
                        " (Cumulative: " + newCumulative + "/" + planned + " " + act.getUom() + ", " + newProgress + "%)",
                null
        );

        return mapToItemResponse(savedLog, act);
    }

    // =========================================================================
    // 7. UPDATE PROGRESS LOG ENTRY
    // =========================================================================

    @Override
    @Transactional
    public ProgressHistoryItemResponse updateProgressLog(Long logId, UpdateProgressHistoryRequest request, String performedBy) {
        ActivityProgressLog log = progressLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityProgressLog", "id", logId));

        WbsActivity act = log.getActivity();

        if (request.getLogDate() != null) {
            log.setLogDate(request.getLogDate());
        }

        // If quantity completed is adjusted:
        if (request.getQuantityCompletedToday() != null &&
                !request.getQuantityCompletedToday().equals(log.getQuantityCompletedToday())) {
            double oldQty = log.getQuantityCompletedToday();
            double newQty = request.getQuantityCompletedToday();
            double delta = newQty - oldQty;

            double currCompleted = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
            double adjustedCompleted = Math.max(0.0, round2(currCompleted + delta));
            double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
            double adjustedProgress = planned > 0.0
                    ? round2(Math.min(100.0, (adjustedCompleted / planned) * 100.0))
                    : 100.0;

            log.setQuantityCompletedToday(newQty);
            log.setCumulativeQuantityCompleted(adjustedCompleted);
            log.setCumulativeProgressPercentage(adjustedProgress);

            UpdateWbsActivityProgressRequest progressReq = UpdateWbsActivityProgressRequest.builder()
                    .completedQuantity(adjustedCompleted)
                    .progressPercentage(adjustedProgress)
                    .build();
            wbsActivityService.updateActivityProgress(act.getId(), progressReq, performedBy);
        }

        if (request.getManpowerCount() != null) log.setManpowerCount(request.getManpowerCount());
        if (request.getEquipmentUsed() != null) log.setEquipmentUsed(request.getEquipmentUsed());
        if (request.getSiteHindranceNotes() != null) log.setSiteHindranceNotes(request.getSiteHindranceNotes());
        if (request.getWeatherCondition() != null) log.setWeatherCondition(request.getWeatherCondition());
        if (request.getIsBlocker() != null) log.setIsBlocker(request.getIsBlocker());
        if (request.getBlockerCategory() != null) log.setBlockerCategory(request.getBlockerCategory());
        if (request.getBlockerReason() != null) log.setBlockerReason(request.getBlockerReason());
        if (request.getRemarks() != null) log.setRemarks(request.getRemarks());

        ActivityProgressLog updated = progressLogRepository.save(log);

        auditService.logAction(
                performedBy != null ? performedBy : "SYSTEM",
                "UPDATE_PROGRESS_HISTORY",
                "ACTIVITY_PROGRESS_LOG",
                String.valueOf(logId),
                "Updated progress history log ID " + logId + " for activity " + act.getCode(),
                null
        );

        return mapToItemResponse(updated, act);
    }

    // =========================================================================
    // 8. DELETE PROGRESS LOG ENTRY
    // =========================================================================

    @Override
    @Transactional
    public void deleteProgressLog(Long logId, String performedBy) {
        ActivityProgressLog log = progressLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityProgressLog", "id", logId));

        WbsActivity act = log.getActivity();
        Long actId = act.getId();

        progressLogRepository.delete(log);

        // Recalculate activity's cumulative quantity from remaining progress logs
        List<ActivityProgressLog> remainingLogs = progressLogRepository.findByActivityIdOrderByLogDateDesc(actId);

        double newCumulative;
        double newProgress;
        LocalDate actualEnd = null;

        if (!remainingLogs.isEmpty()) {
            ActivityProgressLog topLog = remainingLogs.get(0);
            newCumulative = topLog.getCumulativeQuantityCompleted();
            newProgress = topLog.getCumulativeProgressPercentage();
            if (newProgress >= 100.0) {
                actualEnd = topLog.getLogDate();
            }
        } else {
            newCumulative = 0.0;
            newProgress = 0.0;
        }

        UpdateWbsActivityProgressRequest progressReq = UpdateWbsActivityProgressRequest.builder()
                .completedQuantity(newCumulative)
                .progressPercentage(newProgress)
                .actualEndDate(actualEnd)
                .build();
        wbsActivityService.updateActivityProgress(actId, progressReq, performedBy);

        auditService.logAction(
                performedBy != null ? performedBy : "SYSTEM",
                "DELETE_PROGRESS_HISTORY",
                "ACTIVITY_PROGRESS_LOG",
                String.valueOf(logId),
                "Deleted progress log entry ID " + logId + ". Activity " + act.getCode() + " cumulative rolled back to " + newCumulative,
                null
        );
    }

    // =========================================================================
    // HELPER MAPPERS
    // =========================================================================

    private ProgressHistoryItemResponse mapToItemResponse(ActivityProgressLog log, WbsActivity act) {
        double planned = (act != null && act.getPlannedQuantity() != null && act.getPlannedQuantity() > 0.0)
                ? act.getPlannedQuantity() : 0.0;
        double todayPct = planned > 0.0 && log.getQuantityCompletedToday() != null
                ? round2((log.getQuantityCompletedToday() / planned) * 100.0)
                : 0.0;

        WorkPackage wp = (act != null) ? act.getWorkPackage() : null;
        Project prj = (act != null) ? act.getProject() : null;

        return ProgressHistoryItemResponse.builder()
                .id(log.getId())
                .activityId(act != null ? act.getId() : null)
                .activityCode(act != null ? act.getCode() : null)
                .activityName(act != null ? act.getName() : null)
                .discipline(act != null ? act.getDiscipline() : null)
                .workPackageId(wp != null ? wp.getId() : null)
                .workPackageName(wp != null ? wp.getName() : null)
                .projectId(prj != null ? prj.getId() : null)
                .projectName(prj != null ? prj.getName() : null)
                .uom(act != null ? act.getUom() : null)
                .logDate(log.getLogDate())
                .quantityCompletedToday(log.getQuantityCompletedToday())
                .cumulativeQuantityCompleted(log.getCumulativeQuantityCompleted())
                .cumulativeProgressPercentage(log.getCumulativeProgressPercentage())
                .progressPercentageToday(todayPct)
                .source(log.getSource() != null ? log.getSource() : "MANUAL_LOG")
                .dprId(log.getDprId())
                .dprReportCode(log.getDprReportCode())
                .manpowerCount(log.getManpowerCount())
                .equipmentUsed(log.getEquipmentUsed())
                .siteHindranceNotes(log.getSiteHindranceNotes())
                .weatherCondition(log.getWeatherCondition())
                .recordedBy(log.getRecordedBy())
                .isBlocker(log.getIsBlocker())
                .blockerCategory(log.getBlockerCategory())
                .blockerReason(log.getBlockerReason())
                .remarks(log.getRemarks())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private double round2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
