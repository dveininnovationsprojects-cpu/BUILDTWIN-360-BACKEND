package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.alert.*;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.impl.ScheduleDelayAlertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleDelayAlertServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private ActivityDependencyRepository activityDependencyRepository;

    @Mock
    private LookaheadCommitmentRepository lookaheadCommitmentRepository;

    @Mock
    private ScheduleDelayAlertRepository scheduleDelayAlertRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ScheduleDelayAlertServiceImpl scheduleDelayAlertService;

    private Project testProject;
    private WbsActivity overdueActivity;
    private ScheduleDelayAlert testAlert;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .code("PADUR-AG-01")
                .name("Ashok Grandeur - Padur, Chennai")
                .plannedStartDate(LocalDate.of(2026, 1, 1))
                .plannedEndDate(LocalDate.of(2026, 12, 31))
                .build();

        overdueActivity = WbsActivity.builder()
                .id(101L)
                .project(testProject)
                .code("ACT-CIV-001")
                .name("Substructure Concrete Pour")
                .discipline("CIVIL")
                .level(2)
                .status("IN_PROGRESS")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 9, 20))
                .actualStartDate(LocalDate.of(2026, 9, 1))
                .plannedQuantity(100.0)
                .completedQuantity(30.0)
                .progressPercentage(30.0)
                .uom("CUM")
                .assignedContractor("L&T Construction")
                .build();

        testAlert = ScheduleDelayAlert.builder()
                .id(501L)
                .project(testProject)
                .activity(overdueActivity)
                .alertCode("ALT-PADUR-001-OVERDU")
                .alertType(DelayAlertType.OVERDUE_FINISH)
                .severity(OverdueSeverity.HIGH)
                .status(DelayAlertStatus.ACTIVE)
                .agingBucket(OverdueAgingBucket.DAYS_15_TO_30)
                .delayDays(20L)
                .progressSlippagePercentage(70.0)
                .currentProgressPercentage(30.0)
                .expectedProgressPercentage(100.0)
                .impactedSuccessorCount(1)
                .impactedSuccessorCodes("ACT-CIV-002")
                .title("[HIGH] OVERDUE_FINISH: Substructure Concrete Pour (ACT-CIV-001) - 20 days delay")
                .description("Task overdue")
                .recommendedAction("Expedite shift")
                .discipline("CIVIL")
                .assignedContractor("L&T Construction")
                .triggeredAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("scanAndGenerateAlerts should identify overdue activity and generate alert")
    void testScanAndGenerateAlerts() {
        LocalDate asOfDate = LocalDate.of(2026, 10, 10);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(overdueActivity));
        when(activityDependencyRepository.findByPredecessorId(101L)).thenReturn(Collections.emptyList());
        when(scheduleDelayAlertRepository.findByProjectIdAndActivityIdAndAlertTypeAndStatusNotIn(
                eq(1L), eq(101L), any(), any())).thenReturn(Optional.empty());
        when(scheduleDelayAlertRepository.countByProjectId(1L)).thenReturn(0L);
        when(scheduleDelayAlertRepository.save(any(ScheduleDelayAlert.class))).thenAnswer(i -> {
            ScheduleDelayAlert a = i.getArgument(0);
            a.setId(501L);
            return a;
        });
        when(scheduleDelayAlertRepository.countByProjectIdAndStatusIn(eq(1L), any())).thenReturn(1L);

        ScanDelayAlertsRequest req = ScanDelayAlertsRequest.builder()
                .asOfDate(asOfDate)
                .build();

        ScanDelayAlertsResultResponse result = scheduleDelayAlertService.scanAndGenerateAlerts(1L, req, "admin");

        assertNotNull(result);
        assertEquals(1L, result.getProjectId());
        assertEquals(1, result.getNewAlertsCreated());
        assertEquals(0, result.getExistingAlertsUpdated());
        assertEquals(1, result.getAlerts().size());
        assertEquals(DelayAlertType.OVERDUE_FINISH, result.getAlerts().get(0).getAlertType());
        verify(scheduleDelayAlertRepository, times(1)).save(any(ScheduleDelayAlert.class));
        verify(auditService, times(1)).logAction(eq("admin"), eq("SCAN_SCHEDULE_DELAY_ALERTS"), eq("PROJECT"), eq("1"), anyString(), isNull());
    }

    @Test
    @DisplayName("acknowledgeAlert should transition status to ACKNOWLEDGED")
    void testAcknowledgeAlert() {
        when(scheduleDelayAlertRepository.findById(501L)).thenReturn(Optional.of(testAlert));
        when(scheduleDelayAlertRepository.save(any(ScheduleDelayAlert.class))).thenReturn(testAlert);

        AcknowledgeAlertRequest req = AcknowledgeAlertRequest.builder()
                .notes("Reviewed in site standup. Additional materials arriving tomorrow.")
                .targetResolutionDate(LocalDate.of(2026, 10, 25))
                .build();

        ScheduleDelayAlertResponse resp = scheduleDelayAlertService.acknowledgeAlert(501L, req, "site_engineer");

        assertNotNull(resp);
        assertEquals(DelayAlertStatus.ACKNOWLEDGED, resp.getStatus());
        assertEquals("site_engineer", resp.getAcknowledgedBy());
        assertNotNull(resp.getAcknowledgedAt());
        verify(scheduleDelayAlertRepository).save(testAlert);
        verify(auditService).logAction(eq("site_engineer"), eq("ACKNOWLEDGE_DELAY_ALERT"), eq("SCHEDULE_DELAY_ALERT"), eq("501"), anyString(), isNull());
    }

    @Test
    @DisplayName("mitigateAlert should transition status to IN_MITIGATION and sync commitment")
    void testMitigateAlert() {
        when(scheduleDelayAlertRepository.findById(501L)).thenReturn(Optional.of(testAlert));
        when(scheduleDelayAlertRepository.save(any(ScheduleDelayAlert.class))).thenReturn(testAlert);
        when(lookaheadCommitmentRepository.findByActivityIdOrderByWeekStartDateDesc(101L)).thenReturn(Collections.emptyList());

        MitigateAlertRequest req = MitigateAlertRequest.builder()
                .delayReasonCategory("MATERIAL_SHORTAGE")
                .delayReasonDetails("Batching plant pump failure")
                .recoveryPlanAction("Mobilized backup pump and 8 additional laborers")
                .revisedExpectedCompletionDate(LocalDate.of(2026, 10, 28))
                .additionalLaborAllocated(8)
                .notes("Approved by Project Manager")
                .build();

        ScheduleDelayAlertResponse resp = scheduleDelayAlertService.mitigateAlert(501L, req, "project_manager");

        assertNotNull(resp);
        assertEquals(DelayAlertStatus.IN_MITIGATION, resp.getStatus());
        assertEquals("MATERIAL_SHORTAGE", resp.getDelayReasonCategory());
        assertEquals(8, resp.getAdditionalLaborAllocated());
        verify(lookaheadCommitmentRepository).save(any(LookaheadCommitment.class));
        verify(wbsActivityRepository).save(overdueActivity);
        verify(auditService).logAction(eq("project_manager"), eq("MITIGATE_DELAY_ALERT"), eq("SCHEDULE_DELAY_ALERT"), eq("501"), anyString(), isNull());
    }

    @Test
    @DisplayName("resolveAlert should mark status as RESOLVED")
    void testResolveAlert() {
        when(scheduleDelayAlertRepository.findById(501L)).thenReturn(Optional.of(testAlert));
        when(scheduleDelayAlertRepository.save(any(ScheduleDelayAlert.class))).thenReturn(testAlert);

        ResolveAlertRequest req = ResolveAlertRequest.builder()
                .resolutionNotes("Night shift caught up 20 days backlog. Work completed 100%.")
                .build();

        ScheduleDelayAlertResponse resp = scheduleDelayAlertService.resolveAlert(501L, req, "site_engineer");

        assertNotNull(resp);
        assertEquals(DelayAlertStatus.RESOLVED, resp.getStatus());
        assertEquals("site_engineer", resp.getResolvedBy());
        assertNotNull(resp.getResolvedAt());
        verify(auditService).logAction(eq("site_engineer"), eq("RESOLVE_DELAY_ALERT"), eq("SCHEDULE_DELAY_ALERT"), eq("501"), anyString(), isNull());
    }

    @Test
    @DisplayName("dismissAlert should mark status as DISMISSED")
    void testDismissAlert() {
        when(scheduleDelayAlertRepository.findById(501L)).thenReturn(Optional.of(testAlert));
        when(scheduleDelayAlertRepository.save(any(ScheduleDelayAlert.class))).thenReturn(testAlert);

        DismissAlertRequest req = DismissAlertRequest.builder()
                .dismissalReason("Activity was descoped under VO-02.")
                .build();

        ScheduleDelayAlertResponse resp = scheduleDelayAlertService.dismissAlert(501L, req, "project_manager");

        assertNotNull(resp);
        assertEquals(DelayAlertStatus.DISMISSED, resp.getStatus());
        assertEquals("project_manager", resp.getDismissedBy());
        verify(auditService).logAction(eq("project_manager"), eq("DISMISS_DELAY_ALERT"), eq("SCHEDULE_DELAY_ALERT"), eq("501"), anyString(), isNull());
    }

    @Test
    @DisplayName("getAlertSummary should calculate executive metrics and KPIs")
    void testGetAlertSummary() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(scheduleDelayAlertRepository.findByProjectIdOrderByTriggeredAtDesc(1L)).thenReturn(List.of(testAlert));

        ScheduleDelayAlertSummaryResponse summary = scheduleDelayAlertService.getAlertSummary(1L, LocalDate.of(2026, 10, 10));

        assertNotNull(summary);
        assertEquals(1L, summary.getProjectId());
        assertEquals(1, summary.getTotalAlerts());
        assertEquals(1, summary.getActiveAlerts());
        assertEquals(1, summary.getHighAlerts());
        assertEquals(20.0, summary.getAverageDelayDays());
        assertFalse(summary.getPlannerRecommendations().isEmpty());
    }

    @Test
    @DisplayName("getEarlyWarnings should identify upcoming tasks with blocked predecessors")
    void testGetEarlyWarnings() {
        LocalDate refDate = LocalDate.of(2026, 10, 1);
        WbsActivity upcomingAct = WbsActivity.builder()
                .id(102L)
                .code("ACT-CIV-002")
                .name("Column Starter Shuttering")
                .discipline("CIVIL")
                .status("PLANNED")
                .plannedStartDate(LocalDate.of(2026, 10, 5))
                .plannedEndDate(LocalDate.of(2026, 10, 15))
                .build();

        WbsActivity incompletePred = WbsActivity.builder()
                .id(101L)
                .code("ACT-CIV-001")
                .name("Substructure Concrete")
                .status("DELAYED")
                .build();

        ActivityDependency dep = ActivityDependency.builder()
                .id(1L)
                .predecessor(incompletePred)
                .successor(upcomingAct)
                .build();

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(upcomingAct));
        when(lookaheadCommitmentRepository.findByProjectIdOrderByWeekStartDateDesc(1L)).thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findBySuccessorId(102L)).thenReturn(List.of(dep));

        List<EarlyWarningDelayRiskResponse> warnings = scheduleDelayAlertService.getEarlyWarnings(1L, refDate, 7);

        assertNotNull(warnings);
        assertEquals(1, warnings.size());
        assertEquals("ACT-CIV-002", warnings.get(0).getActivityCode());
        assertEquals(1, warnings.get(0).getPendingPredecessorCount());
    }
}
