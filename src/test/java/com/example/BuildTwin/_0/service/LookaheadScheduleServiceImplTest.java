package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.lookahead.FourteenDayLookaheadResponse;
import com.example.BuildTwin._0.dto.lookahead.FourteenDayMetricsResponse;
import com.example.BuildTwin._0.dto.lookahead.LookaheadConstraintSummaryResponse;
import com.example.BuildTwin._0.dto.lookahead.LookaheadScheduleResponse;
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
}
