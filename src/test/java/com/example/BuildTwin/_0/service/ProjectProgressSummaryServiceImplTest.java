package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.summary.PortfolioProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.WorkPackageProgressSummaryDto;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.WbsActivity;
import com.example.BuildTwin._0.model.WorkPackage;
import com.example.BuildTwin._0.repository.ActivityProgressLogRepository;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.impl.ProjectProgressSummaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectProgressSummaryServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkPackageRepository workPackageRepository;

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private ActivityProgressLogRepository activityProgressLogRepository;

    @InjectMocks
    private ProjectProgressSummaryServiceImpl projectProgressSummaryService;

    private Project mockProject;
    private WorkPackage mockWorkPackage;
    private WbsActivity mockActivity1;
    private WbsActivity mockActivity2;

    @BeforeEach
    void setUp() {
        mockProject = Project.builder()
                .id(1L)
                .code("PRJ-001")
                .name("Grand Commercial Tower")
                .status("ACTIVE")
                .projectType("COMMERCIAL")
                .clientName("Apex Infra")
                .location("Chennai")
                .plannedStartDate(LocalDate.of(2026, 1, 1))
                .plannedEndDate(LocalDate.of(2026, 12, 31))
                .actualStartDate(LocalDate.of(2026, 1, 10))
                .estimatedBudget(new BigDecimal("10000000.00"))
                .currency("INR")
                .build();

        mockWorkPackage = WorkPackage.builder()
                .id(1L)
                .project(mockProject)
                .code("WP-01")
                .name("Substructure Concrete")
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .budgetAmount(new BigDecimal("4000000.00"))
                .build();

        mockActivity1 = WbsActivity.builder()
                .id(101L)
                .project(mockProject)
                .workPackage(mockWorkPackage)
                .code("ACT-101")
                .name("Excavation")
                .discipline("CIVIL")
                .status("COMPLETED")
                .plannedQuantity(1000.0)
                .completedQuantity(1000.0)
                .progressPercentage(100.0)
                .weightage(1.0)
                .plannedStartDate(LocalDate.of(2026, 1, 1))
                .plannedEndDate(LocalDate.of(2026, 2, 28))
                .build();

        mockActivity2 = WbsActivity.builder()
                .id(102L)
                .project(mockProject)
                .workPackage(mockWorkPackage)
                .code("ACT-102")
                .name("Piling Work")
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .plannedQuantity(2000.0)
                .completedQuantity(1000.0)
                .progressPercentage(50.0)
                .weightage(1.0)
                .plannedStartDate(LocalDate.of(2026, 3, 1))
                .plannedEndDate(LocalDate.of(2026, 6, 30))
                .build();
    }

    @Test
    @DisplayName("Should calculate project progress summary successfully with KPIs and rollups")
    void testGetProjectProgressSummary_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));
        when(workPackageRepository.findByProjectId(1L))
                .thenReturn(List.of(mockWorkPackage));
        when(activityProgressLogRepository.findMaxLogDateByProjectId(1L))
                .thenReturn(LocalDate.of(2026, 10, 20));
        when(activityProgressLogRepository.countByProjectId(1L))
                .thenReturn(42L);
        when(activityProgressLogRepository.countBlockersByProjectId(1L))
                .thenReturn(2L);

        ProjectProgressSummaryResponse response = projectProgressSummaryService.getProjectProgressSummary(1L);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals("PRJ-001", response.getProjectCode());
        assertEquals("Grand Commercial Tower", response.getProjectName());
        assertEquals(75.0, response.getOverallProgressPercentage()); // (100*1 + 50*1) / 2 = 75.0%
        assertEquals(2L, response.getTotalActivities());
        assertEquals(1L, response.getCompletedActivities());
        assertEquals(1L, response.getInProgressActivities());
        assertEquals(50.0, response.getCompletionRate());
        assertEquals(3000.0, response.getTotalPlannedQuantity());
        assertEquals(2000.0, response.getTotalCompletedQuantity());
        assertEquals(1000.0, response.getTotalRemainingQuantity());
        assertEquals(42L, response.getTotalProgressEntriesCount());
        assertEquals(2L, response.getActiveBlockersCount());
        assertNotNull(response.getScheduleHealth());
        assertNotNull(response.getEarnedValue());
        assertFalse(response.getWorkPackages().isEmpty());
        assertFalse(response.getDisciplines().isEmpty());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project does not exist")
    void testGetProjectProgressSummary_NotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                projectProgressSummaryService.getProjectProgressSummary(99L));
    }

    @Test
    @DisplayName("Should handle project with no activities gracefully without division by zero")
    void testGetProjectProgressSummary_EmptyActivities() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(Collections.emptyList());
        when(workPackageRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());
        when(activityProgressLogRepository.findMaxLogDateByProjectId(1L)).thenReturn(null);
        when(activityProgressLogRepository.countByProjectId(1L)).thenReturn(0L);
        when(activityProgressLogRepository.countBlockersByProjectId(1L)).thenReturn(0L);

        ProjectProgressSummaryResponse response = projectProgressSummaryService.getProjectProgressSummary(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getOverallProgressPercentage());
        assertEquals(0L, response.getTotalActivities());
        assertEquals(0L, response.getCompletedActivities());
        assertEquals(0.0, response.getCompletionRate());
    }

    @Test
    @DisplayName("Should calculate portfolio progress summary across all projects")
    void testGetPortfolioProgressSummary_Success() {
        when(projectRepository.findAll()).thenReturn(List.of(mockProject));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));
        when(workPackageRepository.findByProjectId(1L))
                .thenReturn(List.of(mockWorkPackage));

        PortfolioProgressSummaryResponse response = projectProgressSummaryService.getPortfolioProgressSummary();

        assertNotNull(response);
        assertEquals(1L, response.getTotalProjects());
        assertEquals(1L, response.getActiveProjects());
        assertEquals(75.0, response.getAverageProgressPercentage());
        assertEquals(1, response.getProjectSummaries().size());
    }

    @Test
    @DisplayName("Should retrieve work package progress summary successfully")
    void testGetWorkPackageProgressSummary_Success() {
        when(workPackageRepository.findById(1L)).thenReturn(Optional.of(mockWorkPackage));
        when(wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));

        WorkPackageProgressSummaryDto response = projectProgressSummaryService.getWorkPackageProgressSummary(1L);

        assertNotNull(response);
        assertEquals(1L, response.getWorkPackageId());
        assertEquals("WP-01", response.getCode());
        assertEquals("Substructure Concrete", response.getName());
        assertEquals(75.0, response.getProgressPercentage());
        assertEquals(2L, response.getTotalActivities());
        assertEquals(1L, response.getCompletedActivities());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when work package does not exist")
    void testGetWorkPackageProgressSummary_NotFound() {
        when(workPackageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                projectProgressSummaryService.getWorkPackageProgressSummary(99L));
    }
}
