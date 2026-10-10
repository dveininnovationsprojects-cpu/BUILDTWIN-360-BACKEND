package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.lookahead.*;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.ActivityDependencyRepository;
import com.example.BuildTwin._0.repository.LookaheadCommitmentRepository;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.service.impl.LookaheadScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LookaheadScheduleServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private ActivityDependencyRepository activityDependencyRepository;

    @Mock
    private LookaheadCommitmentRepository lookaheadCommitmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private LookaheadScheduleServiceImpl lookaheadScheduleService;

    private Project testProject;
    private WbsActivity actWeek1;
    private WbsActivity actWeek2;
    private WbsActivity actSpanning;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .code("PADUR-AG-01")
                .name("Ashok Grandeur - Padur, Chennai")
                .build();

        LocalDate baseDate = LocalDate.of(2026, 10, 1);

        // Activity starting and finishing in Week 1 (Days 1 to 7)
        actWeek1 = WbsActivity.builder()
                .id(101L)
                .code("ACT-CIV-001")
                .name("Footing Excavation & PCC")
                .project(testProject)
                .status("IN_PROGRESS")
                .discipline("CIVIL")
                .plannedStartDate(baseDate.plusDays(1))
                .plannedEndDate(baseDate.plusDays(4))
                .plannedQuantity(100.0)
                .completedQuantity(40.0)
                .uom("CUM")
                .assignedContractor("L&T Construction (Civil Div)")
                .build();

        // Activity starting in Week 2 (Days 8 to 14)
        actWeek2 = WbsActivity.builder()
                .id(102L)
                .code("ACT-CIV-002")
                .name("Pedestal Column Formwork & Casting")
                .project(testProject)
                .status("NOT_STARTED")
                .discipline("CIVIL")
                .plannedStartDate(baseDate.plusDays(8))
                .plannedEndDate(baseDate.plusDays(12))
                .plannedQuantity(60.0)
                .completedQuantity(0.0)
                .uom("SQM")
                .assignedContractor("L&T Construction (Civil Div)")
                .build();

        // Activity spanning across both Week 1 and Week 2
        actSpanning = WbsActivity.builder()
                .id(103L)
                .code("ACT-MEP-001")
                .name("Underground Drainage & Plumbing Conduit Rough-in")
                .project(testProject)
                .status("NOT_STARTED")
                .discipline("MEP")
                .plannedStartDate(baseDate.plusDays(2))
                .plannedEndDate(baseDate.plusDays(10))
                .plannedQuantity(200.0)
                .completedQuantity(10.0)
                .uom("MTR")
                .assignedContractor("Voltas MEP Solutions")
                .build();
    }

    @Test
    @DisplayName("Should successfully generate 14-Day Lookahead Schedule with Week 1 and Week 2 segregation")
    void testGetFourteenDayLookaheadSchedule() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(actWeek1, actWeek2, actSpanning));
        when(lookaheadCommitmentRepository.findByProjectIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findBySuccessorId(any(Long.class)))
                .thenReturn(Collections.emptyList());

        FourteenDayLookaheadResponse response = lookaheadScheduleService.getFourteenDayLookaheadSchedule(
                1L, startDate, null, null, null, null, null, true
        );

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals("PADUR-AG-01", response.getProjectCode());
        assertEquals(14, response.getLookaheadDays());
        assertEquals(startDate, response.getWindowStartDate());
        assertEquals(startDate.plusDays(13), response.getWindowEndDate());

        // Check Week 1 & Week 2 dates
        assertEquals(startDate, response.getWeek1StartDate());
        assertEquals(startDate.plusDays(6), response.getWeek1EndDate());
        assertEquals(startDate.plusDays(7), response.getWeek2StartDate());
        assertEquals(startDate.plusDays(13), response.getWeek2EndDate());

        // Check Daily Breakdown has 14 days
        assertNotNull(response.getDailyBreakdown());
        assertEquals(14, response.getDailyBreakdown().size());
        assertEquals(1, response.getDailyBreakdown().get(0).getDayNumber());
        assertEquals(1, response.getDailyBreakdown().get(0).getWeekNumber());
        assertEquals(14, response.getDailyBreakdown().get(13).getDayNumber());
        assertEquals(2, response.getDailyBreakdown().get(13).getWeekNumber());

        // Check Activities segregation
        assertNotNull(response.getAllActivities());
        assertEquals(3, response.getAllActivities().size());

        // Week 1 should have actWeek1 and actSpanning
        assertEquals(2, response.getWeek1Activities().size());
        // Week 2 should have actWeek2 and actSpanning
        assertEquals(2, response.getWeek2Activities().size());

        // Overall Metrics
        assertNotNull(response.getOverallMetrics());
        assertEquals(3, response.getOverallMetrics().getTotalActivitiesInWindow());

        // Health & Summary
        assertNotNull(response.getLookaheadHealthStatus());
        assertNotNull(response.getPlannerSummary());
        assertTrue(response.getPlannerSummary().contains("14-Day Lookahead"));
    }

    @Test
    @DisplayName("Should successfully retrieve 14-Day Lookahead Metrics")
    void testGetFourteenDayMetrics() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(actWeek1, actWeek2, actSpanning));
        when(lookaheadCommitmentRepository.findByProjectIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findBySuccessorId(any(Long.class)))
                .thenReturn(Collections.emptyList());

        FourteenDayMetricsResponse metrics = lookaheadScheduleService.getFourteenDayMetrics(1L, startDate);

        assertNotNull(metrics);
        assertEquals(1L, metrics.getProjectId());
        assertNotNull(metrics.getOverallMetrics());
        assertNotNull(metrics.getWeek1Metrics());
        assertNotNull(metrics.getWeek2Metrics());
        assertEquals(3, metrics.getOverallMetrics().getTotalActivitiesInWindow());
        assertEquals(2, metrics.getWeek1Metrics().getTotalActivitiesInWindow());
        assertEquals(2, metrics.getWeek2Metrics().getTotalActivitiesInWindow());
        assertNotNull(metrics.getLookaheadHealthStatus());
        assertNotNull(metrics.getPlannerSummary());
    }

    @Test
    @DisplayName("Should successfully retrieve 14-Day Constraints Summary")
    void testGetFourteenDayConstraints() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(actWeek1, actWeek2));
        when(lookaheadCommitmentRepository.findByProjectIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findBySuccessorId(any(Long.class)))
                .thenReturn(Collections.emptyList());

        LookaheadConstraintSummaryResponse constraints = lookaheadScheduleService.getFourteenDayConstraints(1L, startDate);

        assertNotNull(constraints);
        assertEquals(1L, constraints.getProjectId());
        assertEquals(startDate, constraints.getWindowStartDate());
        assertEquals(startDate.plusDays(13), constraints.getWindowEndDate());
        assertEquals(0, constraints.getTotalBlockedActivities());
    }

    @Test
    @DisplayName("Should identify overdue activities (Overdue Finish & Overdue Start)")
    void testGetOverdueActivities() {
        LocalDate asOfDate = LocalDate.of(2026, 10, 15);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // actWeek1 planned end was 2026-10-05, asOfDate is 2026-10-15 -> Overdue finish by 10 days
        // actWeek2 planned start was 2026-10-09, planned end was 2026-10-13 -> Overdue finish by 2 days
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(actWeek1, actWeek2));
        when(lookaheadCommitmentRepository.findByProjectIdOrderByWeekStartDateDesc(1L))
                .thenReturn(Collections.emptyList());

        // Setup successor dependency: actWeek1 blocks actWeek2
        ActivityDependency dep = ActivityDependency.builder()
                .id(1L)
                .project(testProject)
                .predecessor(actWeek1)
                .successor(actWeek2)
                .dependencyType(DependencyType.FS)
                .build();
        when(activityDependencyRepository.findByPredecessorId(101L)).thenReturn(List.of(dep));
        when(activityDependencyRepository.findByPredecessorId(102L)).thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findBySuccessorId(any(Long.class))).thenReturn(Collections.emptyList());

        List<OverdueActivityItemResponse> overdues = lookaheadScheduleService.getOverdueActivities(
                1L, asOfDate, null, null, null, null, null, null, null, null
        );

        assertNotNull(overdues);
        assertEquals(2, overdues.size());

        // actWeek1 should be identified as overdue finish
        OverdueActivityItemResponse item1 = overdues.stream()
                .filter(i -> i.getActivityId().equals(101L))
                .findFirst().orElse(null);
        assertNotNull(item1);
        assertEquals(OverdueType.OVERDUE_FINISH, item1.getOverdueType());
        assertEquals(10L, item1.getDaysOverdue());
        assertEquals(OverdueAgingBucket.DAYS_8_TO_14, item1.getAgingBucket());
        assertEquals(1, item1.getImpactedSuccessorCount());
        assertTrue(item1.getImpactedSuccessors().get(0).contains("ACT-CIV-002"));
    }

    @Test
    @DisplayName("Should calculate overdue activities executive summary and KPIs")
    void testGetOverdueActivitySummary() {
        LocalDate asOfDate = LocalDate.of(2026, 10, 15);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(actWeek1, actWeek2));
        when(wbsActivityRepository.countByProjectId(1L)).thenReturn(2L);
        when(lookaheadCommitmentRepository.findByProjectIdOrderByWeekStartDateDesc(1L))
                .thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findByPredecessorId(any(Long.class))).thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findBySuccessorId(any(Long.class))).thenReturn(Collections.emptyList());

        OverdueActivitySummaryResponse summary = lookaheadScheduleService.getOverdueActivitySummary(1L, asOfDate);

        assertNotNull(summary);
        assertEquals(1L, summary.getProjectId());
        assertEquals(2, summary.getTotalProjectActivities());
        assertEquals(2, summary.getTotalOverdueActivities());
        assertEquals(100.0, summary.getOverdueRatePercentage());
        assertEquals(2, summary.getOverdueFinishCount());
        assertNotNull(summary.getAgingBreakdown());
        assertNotNull(summary.getSeverityBreakdown());
        assertNotNull(summary.getTopCriticalBottlenecks());
        assertFalse(summary.getPlannerRecommendations().isEmpty());
    }

    @Test
    @DisplayName("Should successfully record overdue activity mitigation plan")
    void testRecordOverdueMitigation() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findById(101L)).thenReturn(Optional.of(actWeek1));
        when(lookaheadCommitmentRepository.findByActivityIdOrderByWeekStartDateDesc(101L)).thenReturn(Collections.emptyList());

        OverdueMitigationRequest request = OverdueMitigationRequest.builder()
                .delayReasonCategory("MATERIAL_SHORTAGE")
                .delayReasonDetails("Supplier delay in delivering Fe550D rebar")
                .recoveryPlanAction("Procure 10 MT from local distributor and authorize weekend overtime shift")
                .revisedExpectedCompletionDate(LocalDate.of(2026, 10, 20))
                .additionalLaborAllocated(4)
                .notes("Reviewed in site coordination meeting")
                .build();

        OverdueMitigationResponse response = lookaheadScheduleService.recordOverdueMitigation(1L, 101L, request, "admin");

        assertNotNull(response);
        assertEquals(101L, response.getActivityId());
        assertEquals("ACT-CIV-001", response.getActivityCode());
        assertEquals("MATERIAL_SHORTAGE", response.getDelayReasonCategory());
        assertEquals("admin", response.getRecordedBy());
        assertNotNull(response.getRecordedAt());

        verify(lookaheadCommitmentRepository).save(any(LookaheadCommitment.class));
        verify(wbsActivityRepository).save(any(WbsActivity.class));
        verify(auditService).logAction(eq("admin"), eq("OVERDUE_ACTIVITY_MITIGATION"), eq("WBS_ACTIVITY"), eq("101"), any(), any());
    }
}
