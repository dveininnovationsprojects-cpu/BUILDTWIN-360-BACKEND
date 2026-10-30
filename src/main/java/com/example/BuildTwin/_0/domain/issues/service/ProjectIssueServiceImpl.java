package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.dto.*;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.model.ProjectIssue;
import com.example.BuildTwin._0.domain.issues.repository.ProjectIssueRepository;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProjectIssueServiceImpl implements ProjectIssueService {

    private final ProjectIssueRepository projectIssueRepository;
    private final Optional<NotificationService> notificationService;

    public ProjectIssueServiceImpl(ProjectIssueRepository projectIssueRepository) {
        this(projectIssueRepository, Optional.empty());
    }

    @Autowired
    public ProjectIssueServiceImpl(ProjectIssueRepository projectIssueRepository, Optional<NotificationService> notificationService) {
        this.projectIssueRepository = projectIssueRepository;
        this.notificationService = notificationService != null ? notificationService : Optional.empty();
    }

    @Override
    @Transactional
    public ProjectIssueResponseDto createIssue(CreateProjectIssueDto dto) {
        String ticketNumber = "ISS-PRJ" + dto.getProjectId() + "-" + System.currentTimeMillis();

        ProjectIssue issue = ProjectIssue.builder()
                .ticketNumber(ticketNumber)
                .projectId(dto.getProjectId())
                .wbsActivityId(dto.getWbsActivityId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .issueType(dto.getIssueType())
                .severity(dto.getSeverity())
                .status(IssueStatus.OPEN)
                .reportedBy(dto.getReportedBy())
                .reportedRole(dto.getReportedRole())
                .initialPhotoUrl(dto.getInitialPhotoUrl())
                .build();

        ProjectIssue saved = projectIssueRepository.save(issue);
        log.info("[Step 1: Capture] Raised new issue Ticket: {} for Project: {} | Type: {} | Severity: {}",
                saved.getTicketNumber(), saved.getProjectId(), saved.getIssueType(), saved.getSeverity());

        // Module 3: Critical Blocker Alert (issueType = BLOCKER or severity = CRITICAL)
        if (saved.getIssueType() == IssueType.BLOCKER || saved.getSeverity() == IssueSeverity.CRITICAL) {
            notificationService.ifPresent(ns -> {
                // Primary: PROJECT_MANAGER
                ns.dispatchNotification(SendNotificationDto.builder()
                        .projectId(saved.getProjectId())
                        .recipientUsername("pm_karthik")
                        .targetRole("PROJECT_MANAGER")
                        .title("CRITICAL BLOCKER ALERT: Ticket " + saved.getTicketNumber())
                        .message(String.format("Critical impediment reported: '%s' [%s]. Site work halted, immediate PM intervention required.",
                                saved.getTitle(), saved.getSeverity()))
                        .notificationType(NotificationType.CRITICAL_BLOCKER)
                        .priority(NotificationPriority.URGENT)
                        .referenceModule("QUALITY_ISSUE")
                        .referenceId(saved.getId())
                        .build());

                // Primary: DIRECTOR
                ns.dispatchNotification(SendNotificationDto.builder()
                        .projectId(saved.getProjectId())
                        .recipientUsername("director_rajesh")
                        .targetRole("DIRECTOR")
                        .title("CRITICAL BLOCKER ESCALATION: Ticket " + saved.getTicketNumber())
                        .message(String.format("Executive escalation: Critical blocker on Project ID %s: '%s'. Immediate oversight recommended.",
                                saved.getProjectId(), saved.getTitle()))
                        .notificationType(NotificationType.CRITICAL_BLOCKER)
                        .priority(NotificationPriority.URGENT)
                        .referenceModule("QUALITY_ISSUE")
                        .referenceId(saved.getId())
                        .build());

                log.info("Dispatched URGENT CRITICAL_BLOCKER alerts to PROJECT_MANAGER & DIRECTOR for ticket {}", saved.getTicketNumber());
            });
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ProjectIssueResponseDto assignIssue(Long id, AssignProjectIssueDto dto) {
        ProjectIssue issue = findEntityById(id);

        issue.setAssignedTo(dto.getAssignedTo());
        issue.setAssignedToRole(dto.getAssignedToRole());
        issue.setTargetResolutionDate(dto.getTargetResolutionDate());
        if (dto.getSeverity() != null) {
            issue.setSeverity(dto.getSeverity());
        }
        issue.setStatus(IssueStatus.ASSIGNED);

        ProjectIssue saved = projectIssueRepository.save(issue);
        log.info("[Step 2: Allocation] Assigned Issue: {} to: {} (Role: {}) | SLA Target Date: {}",
                saved.getTicketNumber(), saved.getAssignedTo(), saved.getAssignedToRole(), saved.getTargetResolutionDate());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ProjectIssueResponseDto startProgress(Long id) {
        ProjectIssue issue = findEntityById(id);

        issue.setStatus(IssueStatus.IN_PROGRESS);
        ProjectIssue saved = projectIssueRepository.save(issue);
        log.info("[Step 3: Action] Contractor started fixing Issue: {}", saved.getTicketNumber());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ProjectIssueResponseDto resolveIssue(Long id, ResolveProjectIssueDto dto) {
        ProjectIssue issue = findEntityById(id);

        issue.setResolutionRemarks(dto.getResolutionRemarks());
        issue.setResolutionPhotoUrl(dto.getResolutionPhotoUrl());
        issue.setActualResolutionDate(dto.getActualResolutionDate() != null ? dto.getActualResolutionDate() : LocalDate.now());
        issue.setStatus(IssueStatus.RESOLVED);

        ProjectIssue saved = projectIssueRepository.save(issue);
        log.info("[Step 3: Rectification] Contractor resolved Issue: {} with remarks: '{}'",
                saved.getTicketNumber(), saved.getResolutionRemarks());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ProjectIssueResponseDto verifyIssue(Long id, VerifyProjectIssueDto dto) {
        ProjectIssue issue = findEntityById(id);

        issue.setVerifiedBy(dto.getVerifiedBy());
        issue.setVerificationRemarks(dto.getVerificationRemarks());
        issue.setVerifiedAt(LocalDateTime.now());

        if (dto.isApproved()) {
            issue.setStatus(IssueStatus.CLOSED);
            log.info("[Step 4: Audit PASS] QA Inspector: {} verified and CLOSED Issue: {}",
                    dto.getVerifiedBy(), issue.getTicketNumber());
        } else {
            // Verification failed -> Reject and revert cycle back to OPEN
            issue.setStatus(IssueStatus.OPEN);
            log.warn("[Step 4: Audit FAIL] QA Inspector: {} REJECTED Issue: {} - Reverting status to OPEN. Reason: '{}'",
                    dto.getVerifiedBy(), issue.getTicketNumber(), dto.getVerificationRemarks());

            // Module 3: Quality Audit Rejection Alarm (Step 4 audit fails -> reverts to OPEN)
            notificationService.ifPresent(ns -> {
                String targetEngineer = issue.getReportedBy() != null ? issue.getReportedBy() : "se_suresh";
                String targetContractor = issue.getAssignedTo() != null ? issue.getAssignedTo() : "contractor_lnt";

                // Notify Assigned SITE_ENGINEER (Primary)
                ns.dispatchNotification(SendNotificationDto.builder()
                        .projectId(issue.getProjectId())
                        .recipientUsername(targetEngineer)
                        .targetRole("SITE_ENGINEER")
                        .title("NCR AUDIT REJECTED: Ticket " + issue.getTicketNumber())
                        .message(String.format("QA audit failed for ticket '%s'. Reverted to OPEN. Inspector Remarks: '%s'. Re-execute rectification.",
                                issue.getTicketNumber(), dto.getVerificationRemarks()))
                        .notificationType(NotificationType.NCR_AUDIT_REJECTED)
                        .priority(NotificationPriority.HIGH)
                        .referenceModule("QUALITY_ISSUE")
                        .referenceId(issue.getId())
                        .build());

                // Notify Assigned Contractor (Primary)
                ns.dispatchNotification(SendNotificationDto.builder()
                        .projectId(issue.getProjectId())
                        .recipientUsername(targetContractor)
                        .targetRole("SITE_SUPERVISOR")
                        .title("RECTIFICATION REJECTED: Ticket " + issue.getTicketNumber())
                        .message(String.format("Work rectification rejected by QA Inspector %s. Reason: '%s'. Rework immediately.",
                                dto.getVerifiedBy(), dto.getVerificationRemarks()))
                        .notificationType(NotificationType.NCR_AUDIT_REJECTED)
                        .priority(NotificationPriority.HIGH)
                        .referenceModule("QUALITY_ISSUE")
                        .referenceId(issue.getId())
                        .build());

                log.info("Dispatched NCR_AUDIT_REJECTED alerts to SITE_ENGINEER ({}) & Contractor ({})", targetEngineer, targetContractor);
            });
        }

        ProjectIssue saved = projectIssueRepository.save(issue);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectIssueResponseDto getIssueById(Long id) {
        return mapToDto(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectIssueResponseDto> getIssuesByProject(Long projectId, IssueType type, IssueStatus status, IssueSeverity severity) {
        List<ProjectIssue> list = projectIssueRepository.findByProjectId(projectId);

        return list.stream()
                .filter(i -> type == null || i.getIssueType() == type)
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> severity == null || i.getSeverity() == severity)
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectIssueResponseDto> getOverdueIssuesByProject(Long projectId) {
        return projectIssueRepository.findOverdueIssuesByProject(projectId, IssueStatus.CLOSED, LocalDate.now())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectIssueSummaryDto getIssueSummaryByProject(Long projectId) {
        List<ProjectIssue> issues = projectIssueRepository.findByProjectId(projectId);

        long total = issues.size();
        long open = issues.stream().filter(i -> i.getStatus() == IssueStatus.OPEN).count();
        long assigned = issues.stream().filter(i -> i.getStatus() == IssueStatus.ASSIGNED).count();
        long inProgress = issues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
        long resolved = issues.stream().filter(i -> i.getStatus() == IssueStatus.RESOLVED).count();
        long closed = issues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count();

        long snags = issues.stream().filter(i -> i.getIssueType() == IssueType.SNAG).count();
        long ncrs = issues.stream().filter(i -> i.getIssueType() == IssueType.NCR).count();
        long general = issues.stream().filter(i -> i.getIssueType() == IssueType.ISSUE).count();
        long risks = issues.stream().filter(i -> i.getIssueType() == IssueType.RISK).count();
        long blockers = issues.stream().filter(i -> i.getIssueType() == IssueType.BLOCKER).count();

        long critical = issues.stream().filter(i -> i.getSeverity() == IssueSeverity.CRITICAL).count();
        long high = issues.stream().filter(i -> i.getSeverity() == IssueSeverity.HIGH).count();
        long medium = issues.stream().filter(i -> i.getSeverity() == IssueSeverity.MEDIUM).count();
        long low = issues.stream().filter(i -> i.getSeverity() == IssueSeverity.LOW).count();

        long overdue = issues.stream().filter(ProjectIssue::isOverdue).count();
        double resRate = total > 0 ? (closed * 100.0) / total : 0.0;

        return ProjectIssueSummaryDto.builder()
                .projectId(projectId)
                .totalIssues(total)
                .openCount(open)
                .assignedCount(assigned)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .closedCount(closed)
                .snagCount(snags)
                .ncrCount(ncrs)
                .generalIssueCount(general)
                .riskCount(risks)
                .blockerCount(blockers)
                .criticalCount(critical)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .overdueCount(overdue)
                .resolutionPercentage(Math.round(resRate * 100.0) / 100.0)
                .build();
    }

    @Override
    @Scheduled(cron = "${app.issues.overdue-scan.cron:0 0 2 * * ?}")
    public void runDailyOverdueScan() {
        log.info("[Step 5: Monitoring] Running daily background scan for overdue project issues past target SLA...");
        List<ProjectIssue> overdues = projectIssueRepository.findAllOverdueIssues(IssueStatus.CLOSED, LocalDate.now());

        if (!overdues.isEmpty()) {
            log.warn("[OVERDUE RED ALERT] Found {} site issues/snags/blockers that exceeded target resolution SLA!", overdues.size());
            for (ProjectIssue pi : overdues) {
                log.warn(" --> [RED ALERT] Project: {} | Ticket: '{}' | Type: {} | Severity: {} | Target SLA: {} (Assigned To: {})",
                        pi.getProjectId(), pi.getTicketNumber(), pi.getIssueType(), pi.getSeverity(), pi.getTargetResolutionDate(), pi.getAssignedTo());
            }
        } else {
            log.info("[Step 5: Monitoring] All site issues are within resolution SLAs. Zero overdue items detected.");
        }
    }

    private ProjectIssue findEntityById(Long id) {
        return projectIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project issue not found with ID: " + id));
    }

    private ProjectIssueResponseDto mapToDto(ProjectIssue issue) {
        return ProjectIssueResponseDto.builder()
                .id(issue.getId())
                .ticketNumber(issue.getTicketNumber())
                .projectId(issue.getProjectId())
                .wbsActivityId(issue.getWbsActivityId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .issueType(issue.getIssueType())
                .severity(issue.getSeverity())
                .status(issue.getStatus())
                .reportedBy(issue.getReportedBy())
                .reportedRole(issue.getReportedRole())
                .assignedTo(issue.getAssignedTo())
                .assignedToRole(issue.getAssignedToRole())
                .initialPhotoUrl(issue.getInitialPhotoUrl())
                .targetResolutionDate(issue.getTargetResolutionDate())
                .actualResolutionDate(issue.getActualResolutionDate())
                .resolutionRemarks(issue.getResolutionRemarks())
                .resolutionPhotoUrl(issue.getResolutionPhotoUrl())
                .verifiedBy(issue.getVerifiedBy())
                .verificationRemarks(issue.getVerificationRemarks())
                .verifiedAt(issue.getVerifiedAt())
                .isOverdue(issue.isOverdue())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}
