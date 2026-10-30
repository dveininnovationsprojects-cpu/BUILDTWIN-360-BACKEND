package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.alert.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.ScheduleDelayAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleDelayAlertServiceImpl implements ScheduleDelayAlertService {

    private final ProjectRepository projectRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ActivityDependencyRepository activityDependencyRepository;
    private final LookaheadCommitmentRepository lookaheadCommitmentRepository;
    private final ScheduleDelayAlertRepository scheduleDelayAlertRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ScanDelayAlertsResultResponse scanAndGenerateAlerts(Long projectId, ScanDelayAlertsRequest request, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        LocalDate refDate = (request != null && request.getAsOfDate() != null) ? request.getAsOfDate() : LocalDate.now();
        double slippageThreshold = (request != null && request.getSlippageThresholdPercentage() != null)
                ? request.getSlippageThresholdPercentage() : 20.0;
        int horizonDays = (request != null && request.getUpcomingRiskDaysHorizon() != null)
                ? request.getUpcomingRiskDaysHorizon() : 7;
        boolean includeUpcoming = (request != null && request.getIncludeUpcomingRisks() != null)
                ? request.getIncludeUpcomingRisks() : true;

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<ScheduleDelayAlert> allProcessedAlerts = new ArrayList<>();

        int newAlertsCreated = 0;
        int existingAlertsUpdated = 0;
        int alertsAutoResolved = 0;

        for (WbsActivity act : activities) {
            boolean isCompleted = "COMPLETED".equalsIgnoreCase(act.getStatus())
                    || (act.getProgressPercentage() != null && act.getProgressPercentage() >= 100.0);

            // 1. If activity is completed, auto-resolve any active alerts
            if (isCompleted) {
                List<ScheduleDelayAlert> activeAlerts = scheduleDelayAlertRepository.findByProjectIdAndActivityIdAndStatusIn(
                        projectId, act.getId(), List.of(DelayAlertStatus.ACTIVE, DelayAlertStatus.ACKNOWLEDGED, DelayAlertStatus.IN_MITIGATION)
                );
                for (ScheduleDelayAlert openAlert : activeAlerts) {
                    openAlert.setStatus(DelayAlertStatus.RESOLVED);
                    openAlert.setResolvedBy("SYSTEM_AUTO_SCAN");
                    openAlert.setResolvedAt(LocalDateTime.now());
                    openAlert.setResolutionNotes("Auto-resolved: Activity " + act.getCode() + " marked COMPLETED on site.");
                    openAlert.setLastEvaluatedAt(LocalDateTime.now());
                    scheduleDelayAlertRepository.save(openAlert);
                    alertsAutoResolved++;
                }
                continue;
            }

            LocalDate pStart = act.getPlannedStartDate();
            LocalDate pEnd = act.getPlannedEndDate();
            if (pStart == null && pEnd == null) {
                continue;
            }

            double actualProgress = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
            long plannedDuration = (pStart != null && pEnd != null) ? Math.max(1, ChronoUnit.DAYS.between(pStart, pEnd)) : 0;

            // Successors downstream impact check
            List<ActivityDependency> succDeps = activityDependencyRepository.findByPredecessorId(act.getId());
            List<String> impactedSuccessors = new ArrayList<>();
            for (ActivityDependency dep : succDeps) {
                WbsActivity succ = dep.getSuccessor();
                if (succ != null && !"COMPLETED".equalsIgnoreCase(succ.getStatus())) {
                    impactedSuccessors.add(succ.getCode());
                }
            }
            int impactedSuccessorCount = impactedSuccessors.size();
            String impactedSuccessorCodes = String.join(", ", impactedSuccessors);

            DelayAlertType detectedType = null;
            long delayDays = 0;
            double expectedProgress = 0.0;
            double progressSlippage = 0.0;

            // Scenario 1: OVERDUE_FINISH
            if (pEnd != null && pEnd.isBefore(refDate)) {
                detectedType = DelayAlertType.OVERDUE_FINISH;
                delayDays = ChronoUnit.DAYS.between(pEnd, refDate);
                expectedProgress = 100.0;
                progressSlippage = Math.max(0.0, 100.0 - actualProgress);
            }
            // Scenario 2: OVERDUE_START
            else if (pStart != null && pStart.isBefore(refDate)
                    && (act.getActualStartDate() == null || "PLANNED".equalsIgnoreCase(act.getStatus()) || "NOT_STARTED".equalsIgnoreCase(act.getStatus()) || actualProgress <= 0.0)) {
                detectedType = DelayAlertType.OVERDUE_START;
                delayDays = ChronoUnit.DAYS.between(pStart, refDate);
                if (plannedDuration > 0) {
                    long elapsed = ChronoUnit.DAYS.between(pStart, refDate);
                    expectedProgress = Math.min(100.0, ((double) elapsed / plannedDuration) * 100.0);
                    progressSlippage = Math.max(0.0, expectedProgress - actualProgress);
                }
            }
            // Scenario 3: PROGRESS_SLIPPAGE
            else if (pStart != null && pEnd != null && !pStart.isAfter(refDate) && !pEnd.isBefore(refDate)) {
                long elapsed = ChronoUnit.DAYS.between(pStart, refDate);
                expectedProgress = plannedDuration > 0 ? Math.min(100.0, ((double) elapsed / plannedDuration) * 100.0) : 0.0;
                progressSlippage = Math.max(0.0, expectedProgress - actualProgress);
                if (progressSlippage >= slippageThreshold) {
                    detectedType = DelayAlertType.PROGRESS_SLIPPAGE;
                    delayDays = Math.max(1, Math.round((progressSlippage / 100.0) * plannedDuration));
                }
            }

            // Severity escalation to SUCCESSOR_IMPACT_CASCADE or CRITICAL_PATH_AT_RISK
            if (detectedType != null) {
                if (impactedSuccessorCount >= 2) {
                    detectedType = DelayAlertType.SUCCESSOR_IMPACT_CASCADE;
                } else if (act.getLevel() != null && act.getLevel() == 1 && delayDays > 7) {
                    detectedType = DelayAlertType.CRITICAL_PATH_AT_RISK;
                }
            }
            // Scenario 4: Early Warning (UPCOMING_DEADLINE_RISK)
            else if (includeUpcoming && pStart != null && !pStart.isBefore(refDate)) {
                long daysToStart = ChronoUnit.DAYS.between(refDate, pStart);
                if (daysToStart <= horizonDays) {
                    List<ActivityDependency> predDeps = activityDependencyRepository.findBySuccessorId(act.getId());
                    boolean hasPendingPredecessor = predDeps.stream().anyMatch(d -> d.getPredecessor() != null && !"COMPLETED".equalsIgnoreCase(d.getPredecessor().getStatus()));
                    if (hasPendingPredecessor) {
                        detectedType = DelayAlertType.UPCOMING_DEADLINE_RISK;
                        delayDays = 0;
                    }
                }
            }

            if (detectedType == null) {
                continue; // Activity is on schedule
            }

            // Calculate Severity
            OverdueSeverity severity;
            if (delayDays > 30 || impactedSuccessorCount >= 2
                    || detectedType == DelayAlertType.CRITICAL_PATH_AT_RISK
                    || detectedType == DelayAlertType.SUCCESSOR_IMPACT_CASCADE) {
                severity = OverdueSeverity.CRITICAL;
            } else if (delayDays >= 15 || impactedSuccessorCount >= 1) {
                severity = OverdueSeverity.HIGH;
            } else if (delayDays >= 8) {
                severity = OverdueSeverity.MEDIUM;
            } else {
                severity = OverdueSeverity.LOW;
            }

            // Calculate Aging Bucket
            OverdueAgingBucket bucket;
            if (delayDays <= 7) {
                bucket = OverdueAgingBucket.DAYS_1_TO_7;
            } else if (delayDays <= 14) {
                bucket = OverdueAgingBucket.DAYS_8_TO_14;
            } else if (delayDays <= 30) {
                bucket = OverdueAgingBucket.DAYS_15_TO_30;
            } else {
                bucket = OverdueAgingBucket.OVER_30_DAYS;
            }

            // Descriptive text & recovery suggestion
            String title = String.format("[%s] %s: %s (%s) - %s",
                    severity, detectedType, act.getName(), act.getCode(),
                    delayDays > 0 ? delayDays + " days delay" : "Early risk detected");

            String description = buildAlertDescription(act, detectedType, delayDays, progressSlippage, actualProgress, expectedProgress, impactedSuccessorCount, impactedSuccessorCodes, refDate);
            String recommendedAction = buildRecommendedAction(act, detectedType, severity, delayDays, impactedSuccessors);

            // Deduplication & Upsert: Check if active/acknowledged alert already exists for this activity & alertType
            Optional<ScheduleDelayAlert> existingAlertOpt = scheduleDelayAlertRepository.findByProjectIdAndActivityIdAndAlertTypeAndStatusNotIn(
                    projectId, act.getId(), detectedType, List.of(DelayAlertStatus.RESOLVED, DelayAlertStatus.DISMISSED)
            );

            ScheduleDelayAlert alertToSave;
            if (existingAlertOpt.isPresent()) {
                alertToSave = existingAlertOpt.get();
                alertToSave.setDelayDays(delayDays);
                alertToSave.setSeverity(severity);
                alertToSave.setAgingBucket(bucket);
                alertToSave.setProgressSlippagePercentage(Math.round(progressSlippage * 10.0) / 10.0);
                alertToSave.setCurrentProgressPercentage(Math.round(actualProgress * 10.0) / 10.0);
                alertToSave.setExpectedProgressPercentage(Math.round(expectedProgress * 10.0) / 10.0);
                alertToSave.setImpactedSuccessorCount(impactedSuccessorCount);
                alertToSave.setImpactedSuccessorCodes(impactedSuccessorCodes);
                alertToSave.setTitle(title);
                alertToSave.setDescription(description);
                alertToSave.setRecommendedAction(recommendedAction);
                alertToSave.setLastEvaluatedAt(LocalDateTime.now());
                existingAlertsUpdated++;
            } else {
                long seq = scheduleDelayAlertRepository.countByProjectId(projectId) + 1;
                String alertCode = String.format("ALT-%s-%03d-%s",
                        project.getCode() != null ? project.getCode() : "PRJ",
                        seq,
                        detectedType.name().substring(0, Math.min(6, detectedType.name().length())));

                alertToSave = ScheduleDelayAlert.builder()
                        .project(project)
                        .activity(act)
                        .alertCode(alertCode)
                        .alertType(detectedType)
                        .severity(severity)
                        .status(DelayAlertStatus.ACTIVE)
                        .agingBucket(bucket)
                        .delayDays(delayDays)
                        .progressSlippagePercentage(Math.round(progressSlippage * 10.0) / 10.0)
                        .currentProgressPercentage(Math.round(actualProgress * 10.0) / 10.0)
                        .expectedProgressPercentage(Math.round(expectedProgress * 10.0) / 10.0)
                        .impactedSuccessorCount(impactedSuccessorCount)
                        .impactedSuccessorCodes(impactedSuccessorCodes)
                        .title(title)
                        .description(description)
                        .recommendedAction(recommendedAction)
                        .discipline(act.getDiscipline())
                        .assignedContractor(act.getAssignedContractor())
                        .triggeredAt(LocalDateTime.now())
                        .lastEvaluatedAt(LocalDateTime.now())
                        .build();
                newAlertsCreated++;
            }

            ScheduleDelayAlert saved = scheduleDelayAlertRepository.save(alertToSave);
            allProcessedAlerts.add(saved);
        }

        long totalActive = scheduleDelayAlertRepository.countByProjectIdAndStatusIn(
                projectId, List.of(DelayAlertStatus.ACTIVE, DelayAlertStatus.ACKNOWLEDGED, DelayAlertStatus.IN_MITIGATION)
        );

        String summaryMsg = String.format(
                "Delay alert scan completed for Project '%s' as of %s: %d activities scanned, %d new alerts raised, %d existing alerts updated, %d auto-resolved. Total currently active alerts: %d.",
                project.getName(), refDate, activities.size(), newAlertsCreated, existingAlertsUpdated, alertsAutoResolved, totalActive
        );

        auditService.logAction(
                performedBy,
                "SCAN_SCHEDULE_DELAY_ALERTS",
                "PROJECT",
                String.valueOf(projectId),
                summaryMsg,
                null
        );

        List<ScheduleDelayAlertResponse> responseList = allProcessedAlerts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ScanDelayAlertsResultResponse.builder()
                .projectId(projectId)
                .projectCode(project.getCode())
                .projectName(project.getName())
                .asOfDate(refDate)
                .totalActivitiesScanned(activities.size())
                .newAlertsCreated(newAlertsCreated)
                .existingAlertsUpdated(existingAlertsUpdated)
                .alertsAutoResolved(alertsAutoResolved)
                .totalActiveAlerts((int) totalActive)
                .scanTimestamp(LocalDateTime.now())
                .summary(summaryMsg)
                .alerts(responseList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDelayAlertResponse> getAlerts(
            Long projectId,
            DelayAlertStatus status,
            OverdueSeverity severity,
            DelayAlertType alertType,
            OverdueAgingBucket agingBucket,
            String discipline,
            String contractor) {

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<ScheduleDelayAlert> list = scheduleDelayAlertRepository.findByProjectIdOrderByTriggeredAtDesc(projectId);

        return list.stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> severity == null || a.getSeverity() == severity)
                .filter(a -> alertType == null || a.getAlertType() == alertType)
                .filter(a -> agingBucket == null || a.getAgingBucket() == agingBucket)
                .filter(a -> discipline == null || discipline.isBlank() || (a.getDiscipline() != null && a.getDiscipline().equalsIgnoreCase(discipline.trim())))
                .filter(a -> contractor == null || contractor.isBlank() || (a.getAssignedContractor() != null && a.getAssignedContractor().equalsIgnoreCase(contractor.trim())))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDelayAlertSummaryResponse getAlertSummary(Long projectId, LocalDate asOfDate) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        LocalDate refDate = asOfDate != null ? asOfDate : LocalDate.now();
        List<ScheduleDelayAlert> allAlerts = scheduleDelayAlertRepository.findByProjectIdOrderByTriggeredAtDesc(projectId);

        int total = allAlerts.size();
        int active = 0;
        int acknowledged = 0;
        int inMitigation = 0;
        int resolved = 0;
        int dismissed = 0;
        int unacknowledged = 0;

        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;

        long totalDelay = 0;
        long maxDelay = 0;
        int totalImpactedSucc = 0;

        Map<String, Integer> severityMap = new LinkedHashMap<>();
        severityMap.put("CRITICAL", 0);
        severityMap.put("HIGH", 0);
        severityMap.put("MEDIUM", 0);
        severityMap.put("LOW", 0);

        Map<String, Integer> statusMap = new LinkedHashMap<>();
        statusMap.put("ACTIVE", 0);
        statusMap.put("ACKNOWLEDGED", 0);
        statusMap.put("IN_MITIGATION", 0);
        statusMap.put("RESOLVED", 0);
        statusMap.put("DISMISSED", 0);

        Map<String, Integer> typeMap = new HashMap<>();
        Map<String, Integer> agingMap = new LinkedHashMap<>();
        agingMap.put("DAYS_1_TO_7", 0);
        agingMap.put("DAYS_8_TO_14", 0);
        agingMap.put("DAYS_15_TO_30", 0);
        agingMap.put("OVER_30_DAYS", 0);

        Map<String, Integer> disciplineMap = new HashMap<>();
        Map<String, Integer> contractorMap = new HashMap<>();

        for (ScheduleDelayAlert a : allAlerts) {
            statusMap.put(a.getStatus().name(), statusMap.getOrDefault(a.getStatus().name(), 0) + 1);
            switch (a.getStatus()) {
                case ACTIVE -> { active++; unacknowledged++; }
                case ACKNOWLEDGED -> acknowledged++;
                case IN_MITIGATION -> inMitigation++;
                case RESOLVED -> resolved++;
                case DISMISSED -> dismissed++;
            }

            if (a.getStatus() != DelayAlertStatus.RESOLVED && a.getStatus() != DelayAlertStatus.DISMISSED) {
                severityMap.put(a.getSeverity().name(), severityMap.getOrDefault(a.getSeverity().name(), 0) + 1);
                switch (a.getSeverity()) {
                    case CRITICAL -> critical++;
                    case HIGH -> high++;
                    case MEDIUM -> medium++;
                    case LOW -> low++;
                }

                long d = a.getDelayDays() != null ? a.getDelayDays() : 0L;
                totalDelay += d;
                if (d > maxDelay) maxDelay = d;

                int succ = a.getImpactedSuccessorCount() != null ? a.getImpactedSuccessorCount() : 0;
                totalImpactedSucc += succ;

                if (a.getAgingBucket() != null) {
                    agingMap.put(a.getAgingBucket().name(), agingMap.getOrDefault(a.getAgingBucket().name(), 0) + 1);
                }
                typeMap.put(a.getAlertType().name(), typeMap.getOrDefault(a.getAlertType().name(), 0) + 1);

                if (a.getDiscipline() != null) {
                    disciplineMap.put(a.getDiscipline(), disciplineMap.getOrDefault(a.getDiscipline(), 0) + 1);
                }
                String cont = a.getAssignedContractor() != null ? a.getAssignedContractor() : "Unassigned";
                contractorMap.put(cont, contractorMap.getOrDefault(cont, 0) + 1);
            }
        }

        int openAlertCount = active + acknowledged + inMitigation;
        double avgDelay = openAlertCount > 0 ? (double) totalDelay / openAlertCount : 0.0;
        avgDelay = Math.round(avgDelay * 10.0) / 10.0;

        List<ScheduleDelayAlertResponse> topCritical = allAlerts.stream()
                .filter(a -> a.getStatus() != DelayAlertStatus.RESOLVED && a.getStatus() != DelayAlertStatus.DISMISSED)
                .sorted((a, b) -> {
                    int sevComp = Integer.compare(getSeverityRank(b.getSeverity()), getSeverityRank(a.getSeverity()));
                    if (sevComp != 0) return sevComp;
                    return Long.compare(b.getDelayDays() != null ? b.getDelayDays() : 0, a.getDelayDays() != null ? a.getDelayDays() : 0);
                })
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<String> recommendations = new ArrayList<>();
        if (openAlertCount == 0) {
            recommendations.add("No active schedule delay alerts as of " + refDate + ". Project is pacing on track with baseline.");
        } else {
            if (critical > 0) {
                recommendations.add("URGENT: " + critical + " CRITICAL delay alerts require immediate intervention. Convene critical path alignment meeting.");
            }
            if (unacknowledged > 0) {
                recommendations.add(unacknowledged + " alerts are unacknowledged. Site engineers must review and log initial mitigation assessments.");
            }
            if (totalImpactedSucc > 0) {
                recommendations.add(totalImpactedSucc + " successor activities are impacted. Crash predecessor tasks to prevent cascading schedule delay.");
            }
            if (!contractorMap.isEmpty()) {
                recommendations.add("Contractors with highest delay exposure: " + contractorMap + ". Review labor staffing and machinery deployment.");
            }
        }

        return ScheduleDelayAlertSummaryResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .asOfDate(refDate)
                .totalAlerts(total)
                .activeAlerts(active)
                .acknowledgedAlerts(acknowledged)
                .inMitigationAlerts(inMitigation)
                .resolvedAlerts(resolved)
                .dismissedAlerts(dismissed)
                .unacknowledgedAlerts(unacknowledged)
                .criticalAlerts(critical)
                .highAlerts(high)
                .mediumAlerts(medium)
                .lowAlerts(low)
                .averageDelayDays(avgDelay)
                .maxDelayDays(maxDelay)
                .totalImpactedSuccessors(totalImpactedSucc)
                .severityBreakdown(severityMap)
                .statusBreakdown(statusMap)
                .alertTypeBreakdown(typeMap)
                .agingBreakdown(agingMap)
                .disciplineBreakdown(disciplineMap)
                .contractorBreakdown(contractorMap)
                .topCriticalAlerts(topCritical)
                .plannerRecommendations(recommendations)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDelayAlertResponse getAlertById(Long alertId) {
        ScheduleDelayAlert alert = scheduleDelayAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleDelayAlert", "id", alertId));
        return mapToResponse(alert);
    }

    @Override
    @Transactional
    public ScheduleDelayAlertResponse acknowledgeAlert(Long alertId, AcknowledgeAlertRequest request, String performedBy) {
        ScheduleDelayAlert alert = scheduleDelayAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleDelayAlert", "id", alertId));

        if (alert.getStatus() == DelayAlertStatus.RESOLVED || alert.getStatus() == DelayAlertStatus.DISMISSED) {
            throw new BadRequestException("Cannot acknowledge alert with status " + alert.getStatus() + ". Only open alerts can be acknowledged.");
        }

        alert.setStatus(DelayAlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(performedBy);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgmentNotes(request.getNotes());
        if (request.getTargetResolutionDate() != null) {
            alert.setTargetResolutionDate(request.getTargetResolutionDate());
        }

        ScheduleDelayAlert saved = scheduleDelayAlertRepository.save(alert);

        auditService.logAction(
                performedBy,
                "ACKNOWLEDGE_DELAY_ALERT",
                "SCHEDULE_DELAY_ALERT",
                String.valueOf(alert.getId()),
                "Acknowledged delay alert " + alert.getAlertCode() + " for activity " + alert.getActivity().getCode() + ". Notes: " + request.getNotes(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleDelayAlertResponse mitigateAlert(Long alertId, MitigateAlertRequest request, String performedBy) {
        ScheduleDelayAlert alert = scheduleDelayAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleDelayAlert", "id", alertId));

        if (alert.getStatus() == DelayAlertStatus.RESOLVED || alert.getStatus() == DelayAlertStatus.DISMISSED) {
            throw new BadRequestException("Cannot mitigate alert with status " + alert.getStatus() + ".");
        }

        alert.setStatus(DelayAlertStatus.IN_MITIGATION);
        alert.setMitigationAction(request.getRecoveryPlanAction());
        alert.setDelayReasonCategory(request.getDelayReasonCategory());
        alert.setDelayReasonDetails(request.getDelayReasonDetails());
        alert.setAdditionalLaborAllocated(request.getAdditionalLaborAllocated());
        if (request.getRevisedExpectedCompletionDate() != null) {
            alert.setTargetResolutionDate(request.getRevisedExpectedCompletionDate());
        }

        // Sync with Lookahead Commitment
        WbsActivity act = alert.getActivity();
        List<LookaheadCommitment> existingComm = lookaheadCommitmentRepository.findByActivityIdOrderByWeekStartDateDesc(act.getId());
        LookaheadCommitment commitment;
        if (!existingComm.isEmpty()) {
            commitment = existingComm.get(0);
        } else {
            LocalDate today = LocalDate.now();
            commitment = LookaheadCommitment.builder()
                    .project(alert.getProject())
                    .activity(act)
                    .weekStartDate(today)
                    .weekEndDate(today.plusDays(6))
                    .status(CommitmentStatus.COMMITTED)
                    .targetQuantity(act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0)
                    .uom(act.getUom())
                    .committedBy(performedBy)
                    .build();
        }

        commitment.setVarianceReason(request.getDelayReasonCategory() + ": " + (request.getDelayReasonDetails() != null ? request.getDelayReasonDetails() : ""));
        commitment.setNotes("ALERT RECOVERY ACTION: " + request.getRecoveryPlanAction() + (request.getNotes() != null ? " | Notes: " + request.getNotes() : ""));
        if (request.getAdditionalLaborAllocated() != null && request.getAdditionalLaborAllocated() > 0) {
            int curCrew = commitment.getCrewSize() != null ? commitment.getCrewSize() : 0;
            commitment.setCrewSize(curCrew + request.getAdditionalLaborAllocated());
        }
        lookaheadCommitmentRepository.save(commitment);

        // Update activity status to DELAYED if not completed
        if (!"COMPLETED".equalsIgnoreCase(act.getStatus())) {
            act.setStatus("DELAYED");
            wbsActivityRepository.save(act);
        }

        ScheduleDelayAlert saved = scheduleDelayAlertRepository.save(alert);

        auditService.logAction(
                performedBy,
                "MITIGATE_DELAY_ALERT",
                "SCHEDULE_DELAY_ALERT",
                String.valueOf(alert.getId()),
                "Logged mitigation plan for alert " + alert.getAlertCode() + " on activity " + act.getCode() + ": " + request.getRecoveryPlanAction(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleDelayAlertResponse resolveAlert(Long alertId, ResolveAlertRequest request, String performedBy) {
        ScheduleDelayAlert alert = scheduleDelayAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleDelayAlert", "id", alertId));

        alert.setStatus(DelayAlertStatus.RESOLVED);
        alert.setResolvedBy(performedBy);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(request.getResolutionNotes());

        ScheduleDelayAlert saved = scheduleDelayAlertRepository.save(alert);

        auditService.logAction(
                performedBy,
                "RESOLVE_DELAY_ALERT",
                "SCHEDULE_DELAY_ALERT",
                String.valueOf(alert.getId()),
                "Resolved delay alert " + alert.getAlertCode() + ". Notes: " + request.getResolutionNotes(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleDelayAlertResponse dismissAlert(Long alertId, DismissAlertRequest request, String performedBy) {
        ScheduleDelayAlert alert = scheduleDelayAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleDelayAlert", "id", alertId));

        alert.setStatus(DelayAlertStatus.DISMISSED);
        alert.setDismissedBy(performedBy);
        alert.setDismissedAt(LocalDateTime.now());
        alert.setDismissalReason(request.getDismissalReason());

        ScheduleDelayAlert saved = scheduleDelayAlertRepository.save(alert);

        auditService.logAction(
                performedBy,
                "DISMISS_DELAY_ALERT",
                "SCHEDULE_DELAY_ALERT",
                String.valueOf(alert.getId()),
                "Dismissed delay alert " + alert.getAlertCode() + ". Reason: " + request.getDismissalReason(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAlert(Long alertId, String performedBy) {
        ScheduleDelayAlert alert = scheduleDelayAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleDelayAlert", "id", alertId));

        scheduleDelayAlertRepository.delete(alert);

        auditService.logAction(
                performedBy,
                "DELETE_DELAY_ALERT",
                "SCHEDULE_DELAY_ALERT",
                String.valueOf(alertId),
                "Deleted schedule delay alert " + alert.getAlertCode() + " for activity " + alert.getActivity().getCode(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EarlyWarningDelayRiskResponse> getEarlyWarnings(Long projectId, LocalDate asOfDate, Integer horizonDays) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        LocalDate refDate = asOfDate != null ? asOfDate : LocalDate.now();
        int horizon = horizonDays != null && horizonDays > 0 ? horizonDays : 7;
        LocalDate horizonLimit = refDate.plusDays(horizon);

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<LookaheadCommitment> commitments = lookaheadCommitmentRepository.findByProjectIdOrderByWeekStartDateDesc(projectId);
        Map<Long, LookaheadCommitment> commitmentMap = new HashMap<>();
        for (LookaheadCommitment c : commitments) {
            if (c.getActivity() != null && !commitmentMap.containsKey(c.getActivity().getId())) {
                commitmentMap.put(c.getActivity().getId(), c);
            }
        }

        List<EarlyWarningDelayRiskResponse> risks = new ArrayList<>();

        for (WbsActivity act : activities) {
            if ("COMPLETED".equalsIgnoreCase(act.getStatus())
                    || (act.getProgressPercentage() != null && act.getProgressPercentage() >= 100.0)) {
                continue;
            }

            LocalDate pStart = act.getPlannedStartDate();
            LocalDate pEnd = act.getPlannedEndDate();
            if (pStart == null && pEnd == null) continue;

            boolean startsNearTerm = pStart != null && !pStart.isBefore(refDate) && !pStart.isAfter(horizonLimit);
            boolean endsNearTerm = pEnd != null && !pEnd.isBefore(refDate) && !pEnd.isAfter(horizonLimit);

            if (!startsNearTerm && !endsNearTerm) {
                continue;
            }

            // Inspect dependencies
            List<ActivityDependency> predDeps = activityDependencyRepository.findBySuccessorId(act.getId());
            List<String> pendingPreds = new ArrayList<>();
            for (ActivityDependency dep : predDeps) {
                WbsActivity pred = dep.getPredecessor();
                if (pred != null && !"COMPLETED".equalsIgnoreCase(pred.getStatus())) {
                    pendingPreds.add(pred.getCode() + " - " + pred.getName() + " (" + pred.getStatus() + ")");
                }
            }

            // Inspect commitment constraints
            List<String> unclearedConstraints = new ArrayList<>();
            LookaheadCommitment comm = commitmentMap.get(act.getId());
            if (comm != null) {
                if (Boolean.FALSE.equals(comm.getMaterialsReady())) unclearedConstraints.add("Material Staging Deficit");
                if (Boolean.FALSE.equals(comm.getDrawingsReady())) unclearedConstraints.add("Drawing Approval Pending");
                if (Boolean.FALSE.equals(comm.getEquipmentReady())) unclearedConstraints.add("Equipment Readiness Deficit");
                if (Boolean.FALSE.equals(comm.getManpowerReady())) unclearedConstraints.add("Labor Allocation Shortage");
                if (Boolean.FALSE.equals(comm.getSafetyPermitApproved())) unclearedConstraints.add("PTW Clearance Pending");
                if (Boolean.FALSE.equals(comm.getAccessClear())) unclearedConstraints.add("Site Access Blocked");
            }

            if (pendingPreds.isEmpty() && unclearedConstraints.isEmpty()) {
                continue; // Activity has clear runway
            }

            String riskLevel;
            if (!pendingPreds.isEmpty() && !unclearedConstraints.isEmpty()) {
                riskLevel = "CRITICAL_RISK";
            } else if (!pendingPreds.isEmpty() || unclearedConstraints.size() >= 2) {
                riskLevel = "HIGH_RISK";
            } else {
                riskLevel = "MODERATE_RISK";
            }

            String reason = String.format("Scheduled to %s in %d days, but %s%s.",
                    startsNearTerm ? "start" : "finish",
                    startsNearTerm ? ChronoUnit.DAYS.between(refDate, pStart) : ChronoUnit.DAYS.between(refDate, pEnd),
                    !pendingPreds.isEmpty() ? pendingPreds.size() + " predecessor(s) remain uncompleted" : "",
                    !unclearedConstraints.isEmpty() ? (!pendingPreds.isEmpty() ? " and " : "") + unclearedConstraints.size() + " field constraint(s) are uncleared" : ""
            );

            String action = "Expedite predecessor clearance (" + String.join(", ", pendingPreds) + ") and resolve field hindrances prior to scheduled start.";

            risks.add(EarlyWarningDelayRiskResponse.builder()
                    .activityId(act.getId())
                    .activityCode(act.getCode())
                    .activityName(act.getName())
                    .discipline(act.getDiscipline())
                    .assignedContractor(act.getAssignedContractor())
                    .plannedStartDate(pStart)
                    .plannedEndDate(pEnd)
                    .daysUntilPlannedStart(pStart != null ? ChronoUnit.DAYS.between(refDate, pStart) : null)
                    .daysUntilPlannedEnd(pEnd != null ? ChronoUnit.DAYS.between(refDate, pEnd) : null)
                    .currentStatus(act.getStatus())
                    .currentProgressPercentage(act.getProgressPercentage())
                    .pendingPredecessorCount(pendingPreds.size())
                    .pendingPredecessors(pendingPreds)
                    .unclearedConstraints(unclearedConstraints)
                    .riskLevel(riskLevel)
                    .riskReason(reason)
                    .recommendedPreventiveAction(action)
                    .build());
        }

        return risks;
    }

    private String buildAlertDescription(
            WbsActivity act,
            DelayAlertType type,
            long delayDays,
            double progressSlippage,
            double actualProgress,
            double expectedProgress,
            int successorCount,
            String successorCodes,
            LocalDate refDate) {

        return switch (type) {
            case OVERDUE_FINISH -> String.format(
                    "Activity '%s' (%s) was scheduled to finish by %s, but remains incomplete as of %s (%d days overdue). Current progress is %.1f%% (remaining: %.1f %s).",
                    act.getName(), act.getCode(), act.getPlannedEndDate(), refDate, delayDays, actualProgress,
                    Math.max(0.0, (act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0) - (act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0)),
                    act.getUom() != null ? act.getUom() : ""
            );
            case OVERDUE_START -> String.format(
                    "Activity '%s' (%s) was scheduled to begin on %s, but has not started as of %s (%d days kickoff delay). Status is %s with %.1f%% progress.",
                    act.getName(), act.getCode(), act.getPlannedStartDate(), refDate, delayDays, act.getStatus(), actualProgress
            );
            case PROGRESS_SLIPPAGE -> String.format(
                    "Activity '%s' (%s) progress has slipped by %.1f%% behind planned schedule. Expected progress by %s was %.1f%%, but actual achievement is only %.1f%%.",
                    act.getName(), act.getCode(), progressSlippage, refDate, expectedProgress, actualProgress
            );
            case SUCCESSOR_IMPACT_CASCADE -> String.format(
                    "Cascading Bottleneck: Delays on Activity '%s' (%s) are directly obstructing %d downstream dependent task(s) [%s], compounding total project delay.",
                    act.getName(), act.getCode(), successorCount, successorCodes
            );
            case CRITICAL_PATH_AT_RISK -> String.format(
                    "Critical Path Jeopardy: High-level Milestone / Level 1 Activity '%s' (%s) has suffered %d days delay. Direct handover deadline is jeopardized.",
                    act.getName(), act.getCode(), delayDays
            );
            case UPCOMING_DEADLINE_RISK -> String.format(
                    "Upcoming Deadline Warning: Activity '%s' (%s) starts soon, but prerequisite predecessors or material staging constraints remain pending.",
                    act.getName(), act.getCode()
            );
        };
    }

    private String buildRecommendedAction(
            WbsActivity act,
            DelayAlertType type,
            OverdueSeverity severity,
            long delayDays,
            List<String> successors) {

        if (severity == OverdueSeverity.CRITICAL) {
            return "CRITICAL INTERVENTION: Deploy emergency overtime shifts or additional gang immediately. Crash critical path predecessor to unblock " +
                    (successors.isEmpty() ? "milestone handover." : "dependent successors: " + String.join(", ", successors));
        } else if (type == DelayAlertType.OVERDUE_START) {
            return "Kickoff Intervention: Issue immediate work commencement notice to assigned contractor (" +
                    (act.getAssignedContractor() != null ? act.getAssignedContractor() : "team") + ") and verify drawing / site staging readiness.";
        } else if (type == DelayAlertType.PROGRESS_SLIPPAGE) {
            return "Production Acceleration: Increase daily output rate with additional labor/equipment to recover " + delayDays + " days slippage.";
        } else {
            return "Review daily production targets and eliminate site hindrances during daily standup coordination.";
        }
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

    private ScheduleDelayAlertResponse mapToResponse(ScheduleDelayAlert alert) {
        WbsActivity act = alert.getActivity();
        Project prj = alert.getProject();

        double plannedQty = (act != null && act.getPlannedQuantity() != null) ? act.getPlannedQuantity() : 0.0;
        double compQty = (act != null && act.getCompletedQuantity() != null) ? act.getCompletedQuantity() : 0.0;
        double remQty = Math.max(0.0, plannedQty - compQty);

        return ScheduleDelayAlertResponse.builder()
                .id(alert.getId())
                .alertCode(alert.getAlertCode())
                .projectId(prj != null ? prj.getId() : null)
                .projectCode(prj != null ? prj.getCode() : null)
                .projectName(prj != null ? prj.getName() : null)
                .activityId(act != null ? act.getId() : null)
                .activityCode(act != null ? act.getCode() : null)
                .activityName(act != null ? act.getName() : null)
                .discipline(alert.getDiscipline() != null ? alert.getDiscipline() : (act != null ? act.getDiscipline() : null))
                .level(act != null ? act.getLevel() : null)
                .wbsPath(act != null ? act.getWbsPath() : null)
                .workPackageCode(act != null && act.getWorkPackage() != null ? act.getWorkPackage().getCode() : null)
                .workPackageName(act != null && act.getWorkPackage() != null ? act.getWorkPackage().getName() : null)
                .siteName(act != null && act.getSite() != null ? act.getSite().getName() : null)
                .buildingName(act != null && act.getBuilding() != null ? act.getBuilding().getName() : null)
                .floorName(act != null && act.getFloor() != null ? act.getFloor().getFloorName() : null)
                .zoneName(act != null && act.getZone() != null ? act.getZone().getName() : null)
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .agingBucket(alert.getAgingBucket())
                .delayDays(alert.getDelayDays())
                .progressSlippagePercentage(alert.getProgressSlippagePercentage())
                .currentProgressPercentage(alert.getCurrentProgressPercentage())
                .expectedProgressPercentage(alert.getExpectedProgressPercentage())
                .plannedStartDate(act != null ? act.getPlannedStartDate() : null)
                .plannedEndDate(act != null ? act.getPlannedEndDate() : null)
                .actualStartDate(act != null ? act.getActualStartDate() : null)
                .actualEndDate(act != null ? act.getActualEndDate() : null)
                .plannedQuantity(plannedQty)
                .completedQuantity(compQty)
                .remainingQuantity(remQty)
                .uom(act != null ? act.getUom() : null)
                .activityStatus(act != null ? act.getStatus() : null)
                .assignedContractor(alert.getAssignedContractor() != null ? alert.getAssignedContractor() : (act != null ? act.getAssignedContractor() : null))
                .impactedSuccessorCount(alert.getImpactedSuccessorCount())
                .impactedSuccessorCodes(alert.getImpactedSuccessorCodes())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .recommendedAction(alert.getRecommendedAction())
                .isAcknowledged(alert.getStatus() == DelayAlertStatus.ACKNOWLEDGED || alert.getAcknowledgedAt() != null)
                .acknowledgedBy(alert.getAcknowledgedBy())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .acknowledgmentNotes(alert.getAcknowledgmentNotes())
                .targetResolutionDate(alert.getTargetResolutionDate())
                .mitigationAction(alert.getMitigationAction())
                .delayReasonCategory(alert.getDelayReasonCategory())
                .delayReasonDetails(alert.getDelayReasonDetails())
                .additionalLaborAllocated(alert.getAdditionalLaborAllocated())
                .isResolved(alert.getStatus() == DelayAlertStatus.RESOLVED)
                .resolvedBy(alert.getResolvedBy())
                .resolvedAt(alert.getResolvedAt())
                .resolutionNotes(alert.getResolutionNotes())
                .isDismissed(alert.getStatus() == DelayAlertStatus.DISMISSED)
                .dismissedBy(alert.getDismissedBy())
                .dismissedAt(alert.getDismissedAt())
                .dismissalReason(alert.getDismissalReason())
                .triggeredAt(alert.getTriggeredAt())
                .lastEvaluatedAt(alert.getLastEvaluatedAt())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}
