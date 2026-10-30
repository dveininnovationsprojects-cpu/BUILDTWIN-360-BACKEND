package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.progress.*;
import com.example.BuildTwin._0.dto.wbs.UpdateWbsActivityProgressRequest;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.model.ActivityProgressLog;
import com.example.BuildTwin._0.model.WbsActivity;
import com.example.BuildTwin._0.model.WorkPackage;
import com.example.BuildTwin._0.repository.ActivityProgressLogRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.impl.ProgressHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressHistoryServiceImplTest {

    @Mock
    private ActivityProgressLogRepository progressLogRepository;

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private WorkPackageRepository workPackageRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WbsActivityService wbsActivityService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProgressHistoryServiceImpl progressHistoryService;

    private Project mockProject;
    private WorkPackage mockWorkPackage;
    private WbsActivity mockActivity;
    private ActivityProgressLog mockLog;

    @BeforeEach
    void setUp() {
        mockProject = Project.builder()
                .id(1L)
                .name("Skyline Residency")
                .code("PRJ-SKY-001")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 12, 31))
                .build();

        mockWorkPackage = WorkPackage.builder()
                .id(2L)
                .name("Civil Works")
                .code("WP-CIV-01")
                .project(mockProject)
                .build();

        mockActivity = WbsActivity.builder()
                .id(10L)
                .code("ACT-CIV-001")
                .name("Substructure Concrete Works")
                .discipline("CIVIL")
                .uom("CUM")
                .plannedQuantity(500.0)
                .completedQuantity(200.0)
                .progressPercentage(40.0)
                .status("IN_PROGRESS")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 10, 31))
                .actualStartDate(LocalDate.of(2026, 9, 5))
                .project(mockProject)
                .workPackage(mockWorkPackage)
                .build();

        mockLog = ActivityProgressLog.builder()
                .id(101L)
                .activity(mockActivity)
                .logDate(LocalDate.of(2026, 9, 28))
                .quantityCompletedToday(45.0)
                .cumulativeQuantityCompleted(200.0)
                .cumulativeProgressPercentage(40.0)
                .manpowerCount(14)
                .equipmentUsed("1 JCB Excavator")
                .siteHindranceNotes(null)
                .weatherCondition("CLEAR")
                .recordedBy("site_engineer_01")
                .isBlocker(false)
                .source("DPR")
                .dprId(5L)
                .dprReportCode("DPR-20260928-S1")
                .remarks("Poured slab C1")
                .build();
    }

    @Test
    @DisplayName("Should retrieve full activity progress history with summary stats")
    void testGetActivityProgressHistory() {
        when(wbsActivityRepository.findById(10L)).thenReturn(Optional.of(mockActivity));
        when(progressLogRepository.findActivityLogsFiltered(eq(10L), any(), any(), any(), any()))
                .thenReturn(List.of(mockLog));

        ActivityProgressHistoryResponse res = progressHistoryService.getActivityProgressHistory(
                10L, null, null, null, null, "desc");

        assertNotNull(res);
        assertEquals(10L, res.getActivityId());
        assertEquals("ACT-CIV-001", res.getActivityCode());
        assertEquals(500.0, res.getPlannedQuantity());
        assertEquals(200.0, res.getCompletedQuantity());
        assertEquals(300.0, res.getBalanceQuantity());
        assertEquals(1, res.getTotalLogsCount());
        assertEquals(45.0, res.getTotalQuantityLogged());
        assertEquals(1, res.getHistory().size());
        assertEquals("DPR", res.getHistory().get(0).getSource());
        assertEquals(5L, res.getHistory().get(0).getDprId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when activity does not exist")
    void testGetActivityProgressHistory_NotFound() {
        when(wbsActivityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                progressHistoryService.getActivityProgressHistory(999L, null, null, null, null, "desc"));
    }

    @Test
    @DisplayName("Should retrieve paginated activity progress history")
    void testGetActivityProgressHistoryPaginated() {
        when(wbsActivityRepository.findById(10L)).thenReturn(Optional.of(mockActivity));
        Page<ActivityProgressLog> page = new PageImpl<>(List.of(mockLog));
        when(progressLogRepository.findActivityLogsFilteredPaged(eq(10L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<ProgressHistoryItemResponse> res = progressHistoryService.getActivityProgressHistoryPaginated(
                10L, null, null, null, null, 0, 10, "desc");

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals(101L, res.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should retrieve project progress history dashboard")
    void testGetProjectProgressHistory() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        Page<ActivityProgressLog> page = new PageImpl<>(List.of(mockLog));
        when(progressLogRepository.findProjectLogsFiltered(eq(1L), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(progressLogRepository.countByProjectId(1L)).thenReturn(1L);
        when(progressLogRepository.sumQuantityByProjectId(1L)).thenReturn(45.0);
        when(progressLogRepository.countDistinctActivitiesByProjectId(1L)).thenReturn(1);
        when(progressLogRepository.sumManpowerByProjectId(1L)).thenReturn(14L);
        when(progressLogRepository.countBlockersByProjectId(1L)).thenReturn(0L);

        ProjectProgressHistoryResponse res = progressHistoryService.getProjectProgressHistory(
                1L, null, null, null, null, null, null, null, 0, 10, "logDate", "desc");

        assertNotNull(res);
        assertEquals(1L, res.getProjectId());
        assertEquals("PRJ-SKY-001", res.getProjectCode());
        assertEquals(1L, res.getTotalEntries());
        assertEquals(45.0, res.getTotalQuantityLogged());
        assertEquals(1, res.getItems().getContent().size());
    }

    @Test
    @DisplayName("Should retrieve work package progress history")
    void testGetWorkPackageProgressHistory() {
        when(workPackageRepository.findById(2L)).thenReturn(Optional.of(mockWorkPackage));
        Page<ActivityProgressLog> page = new PageImpl<>(List.of(mockLog));
        when(progressLogRepository.findWorkPackageLogsFiltered(eq(2L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(progressLogRepository.countByWorkPackageId(2L)).thenReturn(1L);
        when(progressLogRepository.sumQuantityByWorkPackageId(2L)).thenReturn(45.0);
        when(progressLogRepository.countDistinctActivitiesByWorkPackageId(2L)).thenReturn(1);
        when(progressLogRepository.countBlockersByWorkPackageId(2L)).thenReturn(0L);

        WorkPackageProgressHistoryResponse res = progressHistoryService.getWorkPackageProgressHistory(
                2L, null, null, null, null, 0, 10, "desc");

        assertNotNull(res);
        assertEquals(2L, res.getWorkPackageId());
        assertEquals("WP-CIV-01", res.getWorkPackageCode());
        assertEquals(1L, res.getTotalEntries());
    }

    @Test
    @DisplayName("Should calculate activity progress analytics & S-curve progression")
    void testGetActivityProgressAnalytics() {
        when(wbsActivityRepository.findById(10L)).thenReturn(Optional.of(mockActivity));
        when(progressLogRepository.findByActivityIdOrderByLogDateAsc(10L))
                .thenReturn(List.of(mockLog));

        ProgressAnalyticsResponse res = progressHistoryService.getActivityProgressAnalytics(10L);

        assertNotNull(res);
        assertEquals(10L, res.getActivityId());
        assertEquals(40.0, res.getProgressPercentage());
        assertEquals(1, res.getTimeline().size());
        assertEquals(LocalDate.of(2026, 9, 28), res.getTimeline().get(0).getDate());
        assertEquals(45.0, res.getTimeline().get(0).getQuantityToday());
    }

    @Test
    @DisplayName("Should log direct site progress and cascade to WBS activity")
    void testLogProgress() {
        when(wbsActivityRepository.findById(10L)).thenReturn(Optional.of(mockActivity));
        when(progressLogRepository.save(any(ActivityProgressLog.class))).thenAnswer(invocation -> {
            ActivityProgressLog saved = invocation.getArgument(0);
            saved.setId(201L);
            return saved;
        });

        LogProgressHistoryRequest req = LogProgressHistoryRequest.builder()
                .logDate(LocalDate.of(2026, 9, 29))
                .quantityCompletedToday(30.0)
                .manpowerCount(10)
                .equipmentUsed("1 Transit Mixer")
                .weatherCondition("CLEAR")
                .remarks("Poured column C2")
                .build();

        ProgressHistoryItemResponse res = progressHistoryService.logProgress(10L, req, "eng_ramesh");

        assertNotNull(res);
        assertEquals(30.0, res.getQuantityCompletedToday());
        assertEquals(230.0, res.getCumulativeQuantityCompleted());
        assertEquals("MANUAL_LOG", res.getSource());
        verify(wbsActivityService, times(1)).updateActivityProgress(eq(10L), any(UpdateWbsActivityProgressRequest.class), eq("eng_ramesh"));
        verify(auditService, times(1)).logAction(eq("eng_ramesh"), eq("LOG_PROGRESS_HISTORY"), anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should update an existing progress log entry")
    void testUpdateProgressLog() {
        when(progressLogRepository.findById(101L)).thenReturn(Optional.of(mockLog));
        when(progressLogRepository.save(any(ActivityProgressLog.class))).thenReturn(mockLog);

        UpdateProgressHistoryRequest req = UpdateProgressHistoryRequest.builder()
                .remarks("Updated remarks from QA")
                .manpowerCount(16)
                .build();

        ProgressHistoryItemResponse res = progressHistoryService.updateProgressLog(101L, req, "pm_john");

        assertNotNull(res);
        assertEquals("Updated remarks from QA", res.getRemarks());
        assertEquals(16, res.getManpowerCount());
        verify(progressLogRepository, times(1)).save(mockLog);
    }

    @Test
    @DisplayName("Should delete a progress log entry and recalculate activity progress")
    void testDeleteProgressLog() {
        when(progressLogRepository.findById(101L)).thenReturn(Optional.of(mockLog));
        when(progressLogRepository.findByActivityIdOrderByLogDateDesc(10L))
                .thenReturn(Collections.emptyList());

        progressHistoryService.deleteProgressLog(101L, "admin_user");

        verify(progressLogRepository, times(1)).delete(mockLog);
        verify(wbsActivityService, times(1)).updateActivityProgress(eq(10L), any(UpdateWbsActivityProgressRequest.class), eq("admin_user"));
        verify(auditService, times(1)).logAction(eq("admin_user"), eq("DELETE_PROGRESS_HISTORY"), anyString(), anyString(), anyString(), any());
    }
}
