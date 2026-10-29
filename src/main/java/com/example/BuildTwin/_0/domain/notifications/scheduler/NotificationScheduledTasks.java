package com.example.BuildTwin._0.domain.notifications.scheduler;

import com.example.BuildTwin._0.domain.cost.dto.BudgetOverrunReportDto;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.model.ProjectIssue;
import com.example.BuildTwin._0.domain.issues.repository.ProjectIssueRepository;
import com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.domain.procurement.service.ProcurementService;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduledTasks {

    private final ProjectRepository projectRepository;
    private final ProcurementService procurementService;
    private final ProjectIssueRepository projectIssueRepository;
    private final CostService costService;
    private final NotificationService notificationService;

    /**
     * Daily 06:00 AM: Scan for Material Low Stock and Projected Shortages
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void scanMaterialShortages() {
        log.info("[CRON] Running daily morning material inventory shortage scan...");
        List<Project> projects = projectRepository.findAll();

        for (Project proj : projects) {
            try {
                List<ProjectedShortageDto> shortages = procurementService.detectProjectedShortage(proj.getId());
                for (ProjectedShortageDto item : shortages) {
                    if ("SHORTAGE".equalsIgnoreCase(item.getStatus()) || "LOW_STOCK".equalsIgnoreCase(item.getStatus())) {
                        if (notificationService.isDuplicateActiveAlert(proj.getId(), NotificationType.LOW_STOCK, item.getMaterialId())) {
                            log.debug("Active LOW_STOCK alert exists within 24h for material {}. Skipping duplicate dispatch.", item.getMaterialId());
                            continue;
                        }

                        notificationService.dispatchNotification(SendNotificationDto.builder()
                                .projectId(proj.getId())
                                .recipientUsername("proc_ramesh")
                                .targetRole("PROCUREMENT_STORE")
                                .title("Inventory Alert: " + item.getMaterialName() + " (" + item.getStatus() + ")")
                                .message(String.format("Material '%s' is critical. Current Stock: %s, Required: %s, Shortage: %s",
                                        item.getMaterialName(), item.getCurrentStock(), item.getTotalRequestedQty(), item.getProjectedShortage()))
                                .notificationType(NotificationType.LOW_STOCK)
                                .priority(NotificationPriority.HIGH)
                                .referenceModule("MATERIALS")
                                .referenceId(item.getMaterialId())
                                .build());
                    }
                }
            } catch (Exception e) {
                log.error("Failed material scan for project {}: {}", proj.getId(), e.getMessage());
            }
        }
    }

    /**
     * Daily 07:00 AM: Scan for Overdue SLA Issues & Critical Blockers
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void scanOverdueSlaAndBlockers() {
        log.info("[CRON] Running daily overdue issue and critical blocker escalation scan...");
        List<ProjectIssue> overdueIssues = projectIssueRepository.findAllOverdueIssues(IssueStatus.CLOSED, LocalDate.now());

        for (ProjectIssue issue : overdueIssues) {
            if (notificationService.isDuplicateActiveAlert(issue.getProjectId(), NotificationType.OVERDUE_SLA, issue.getId())) {
                log.debug("Active OVERDUE_SLA alert exists within 24h for ticket {}. Skipping duplicate dispatch.", issue.getTicketNumber());
                continue;
            }

            notificationService.dispatchNotification(SendNotificationDto.builder()
                    .projectId(issue.getProjectId())
                    .recipientUsername(issue.getAssignedTo() != null ? issue.getAssignedTo() : "pm_karthik")
                    .targetRole("PROJECT_MANAGER")
                    .title("SLA BREACH ALERT: Ticket " + issue.getTicketNumber())
                    .message(String.format("Issue '%s' assigned to %s was due on %s and is OVERDUE!",
                            issue.getTitle(), issue.getAssignedTo(), issue.getTargetResolutionDate()))
                    .notificationType(NotificationType.OVERDUE_SLA)
                    .priority(NotificationPriority.URGENT)
                    .referenceModule("QUALITY_ISSUE")
                    .referenceId(issue.getId())
                    .build());
        }
    }

    /**
     * Daily 01:00 AM: Trigger Epic 4 Budget Overrun Analysis Scan & Dispatch Notifications
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runMidnightBudgetVarianceScan() {
        log.info("[CRON] Running daily midnight budget overrun scan and alert dispatch...");
        costService.runDailyBudgetOverrunCheck();
        List<Project> projects = projectRepository.findAll();

        for (Project proj : projects) {
            try {
                BudgetOverrunReportDto report = costService.getBudgetOverruns(proj.getId());
                if (report != null && report.getOverruns() != null) {
                    for (BudgetOverrunReportDto.BudgetOverrunItemDto item : report.getOverruns()) {
                        if ("CRITICAL_OVERRUN".equalsIgnoreCase(item.getAlertSeverity()) || "WARNING_90_PERCENT".equalsIgnoreCase(item.getAlertSeverity())) {
                            if (notificationService.isDuplicateActiveAlert(proj.getId(), NotificationType.BUDGET_OVERRUN, item.getActivityId())) {
                                log.debug("Active BUDGET_OVERRUN alert exists within 24h for activity {}. Skipping duplicate dispatch.", item.getActivityId());
                                continue;
                            }

                            notificationService.dispatchNotification(SendNotificationDto.builder()
                                    .projectId(proj.getId())
                                    .recipientUsername("pm_karthik")
                                    .targetRole("PROJECT_MANAGER")
                                    .title("BUDGET OVERRUN ALERT: " + item.getCostCode() + " (" + item.getCostHead() + ")")
                                    .message(item.getAlertMessage())
                                    .notificationType(NotificationType.BUDGET_OVERRUN)
                                    .priority("CRITICAL_OVERRUN".equalsIgnoreCase(item.getAlertSeverity()) ? NotificationPriority.URGENT : NotificationPriority.HIGH)
                                    .referenceModule("COST_BUDGET")
                                    .referenceId(item.getActivityId())
                                    .build());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed budget overrun notification scan for project {}: {}", proj.getId(), e.getMessage());
            }
        }
    }
}
