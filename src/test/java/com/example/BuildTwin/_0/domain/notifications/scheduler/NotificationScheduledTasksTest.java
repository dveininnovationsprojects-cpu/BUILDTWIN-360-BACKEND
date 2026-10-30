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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationScheduledTasksTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProcurementService procurementService;

    @Mock
    private ProjectIssueRepository projectIssueRepository;

    @Mock
    private CostService costService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationScheduledTasks scheduledTasks;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .id(1L)
                .name("Ashok Grandeur - Padur, Chennai")
                .code("PADUR-AG-01")
                .build();
    }

    @Test
    @DisplayName("scanMaterialShortages should detect shortage and dispatch LOW_STOCK notification")
    void testScanMaterialShortages() {
        ProjectedShortageDto shortage = ProjectedShortageDto.builder()
                .materialId(10L)
                .materialName("UltraTech Cement 53 OPC")
                .currentStock(new BigDecimal("45.00"))
                .totalRequestedQty(new BigDecimal("200.00"))
                .projectedShortage(new BigDecimal("155.00"))
                .status("SHORTAGE")
                .build();

        when(projectRepository.findAll()).thenReturn(List.of(sampleProject));
        when(procurementService.detectProjectedShortage(1L)).thenReturn(List.of(shortage));

        scheduledTasks.scanMaterialShortages();

        ArgumentCaptor<SendNotificationDto> captor = ArgumentCaptor.forClass(SendNotificationDto.class);
        verify(notificationService, times(1)).dispatchNotification(captor.capture());

        SendNotificationDto captured = captor.getValue();
        assertEquals(1L, captured.getProjectId());
        assertEquals(NotificationType.LOW_STOCK, captured.getNotificationType());
        assertEquals(NotificationPriority.HIGH, captured.getPriority());
        assertEquals("PROCUREMENT_STORE", captured.getTargetRole());
        assertNull(captured.getRecipientUsername());
        assertTrue(captured.getTitle().contains("UltraTech Cement 53 OPC"));
    }

    @Test
    @DisplayName("scanOverdueSlaAndBlockers should detect overdue issues and dispatch OVERDUE_SLA notification")
    void testScanOverdueSlaAndBlockers() {
        ProjectIssue overdueIssue = ProjectIssue.builder()
                .id(5L)
                .projectId(1L)
                .ticketNumber("ISS-PADUR-NCR-01")
                .title("Cube test strength fail")
                .assignedTo("pm_karthik")
                .targetResolutionDate(LocalDate.now().minusDays(2))
                .build();

        when(projectIssueRepository.findAllOverdueIssues(eq(IssueStatus.CLOSED), any(LocalDate.class)))
                .thenReturn(List.of(overdueIssue));

        scheduledTasks.scanOverdueSlaAndBlockers();

        ArgumentCaptor<SendNotificationDto> captor = ArgumentCaptor.forClass(SendNotificationDto.class);
        verify(notificationService, times(1)).dispatchNotification(captor.capture());

        SendNotificationDto captured = captor.getValue();
        assertEquals(1L, captured.getProjectId());
        assertEquals(NotificationType.OVERDUE_SLA, captured.getNotificationType());
        assertEquals(NotificationPriority.URGENT, captured.getPriority());
        assertEquals("PROJECT_MANAGER", captured.getTargetRole());
        assertEquals("pm_karthik", captured.getRecipientUsername());
        assertTrue(captured.getTitle().contains("ISS-PADUR-NCR-01"));
    }

    @Test
    @DisplayName("runMidnightBudgetVarianceScan should run check and dispatch BUDGET_OVERRUN notification")
    void testRunMidnightBudgetVarianceScan() {
        BudgetOverrunReportDto.BudgetOverrunItemDto overrunItem = BudgetOverrunReportDto.BudgetOverrunItemDto.builder()
                .costCode("ACT-PADUR-STR-01")
                .activityId(101L)
                .costHead("CIVIL")
                .alertSeverity("CRITICAL_OVERRUN")
                .alertMessage("Cost head exceeded 100% budget limit")
                .build();

        BudgetOverrunReportDto report = BudgetOverrunReportDto.builder()
                .projectId(1L)
                .overrunCount(1)
                .overruns(List.of(overrunItem))
                .build();

        when(projectRepository.findAll()).thenReturn(List.of(sampleProject));
        when(costService.getBudgetOverruns(1L)).thenReturn(report);

        scheduledTasks.runMidnightBudgetVarianceScan();

        verify(costService, times(1)).runDailyBudgetOverrunCheck();

        ArgumentCaptor<SendNotificationDto> captor = ArgumentCaptor.forClass(SendNotificationDto.class);
        verify(notificationService, times(1)).dispatchNotification(captor.capture());

        SendNotificationDto captured = captor.getValue();
        assertEquals(1L, captured.getProjectId());
        assertEquals(NotificationType.BUDGET_OVERRUN, captured.getNotificationType());
        assertEquals(NotificationPriority.URGENT, captured.getPriority());
        assertEquals("PROJECT_MANAGER", captured.getTargetRole());
        assertNull(captured.getRecipientUsername());
        assertTrue(captured.getTitle().contains("ACT-PADUR-STR-01"));
    }
}
