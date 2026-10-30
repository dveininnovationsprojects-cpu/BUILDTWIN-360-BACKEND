package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.wbs.ActivityProgressPercentageResponse;
import com.example.BuildTwin._0.dto.wbs.ProjectProgressPercentageResponse;
import com.example.BuildTwin._0.dto.wbs.WorkPackageProgressPercentageResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.impl.WbsActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WbsActivityProgressTrackingServiceTest {

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private WorkPackageRepository workPackageRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private FloorRepository floorRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ActivityDependencyRepository activityDependencyRepository;

    @InjectMocks
    private WbsActivityServiceImpl wbsActivityService;

    private Project testProject;
    private WorkPackage testWorkPackage;
    private Building testBuilding;
    private WbsActivity parentActivity;
    private WbsActivity childActivity;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .code("PRJ-SKY-001")
                .name("Skyline Towers")
                .build();

        testWorkPackage = WorkPackage.builder()
                .id(10L)
                .project(testProject)
                .code("WP-CIV-01")
                .name("Civil & Structural Works")
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .build();

        testBuilding = Building.builder()
                .id(100L)
                .name("Tower A")
                .build();

        parentActivity = WbsActivity.builder()
                .id(101L)
                .project(testProject)
                .workPackage(testWorkPackage)
                .building(testBuilding)
                .code("ACT-CIV-001")
                .name("Column Casting")
                .discipline("CIVIL")
                .uom("CUM")
                .plannedQuantity(500.0)
                .completedQuantity(250.0)
                .progressPercentage(50.0)
                .plannedStartDate(LocalDate.now().minusDays(10))
                .plannedEndDate(LocalDate.now().plusDays(10))
                .status("IN_PROGRESS")
                .weightage(1.0)
                .build();

        childActivity = WbsActivity.builder()
                .id(102L)
                .project(testProject)
                .workPackage(testWorkPackage)
                .parent(parentActivity)
                .building(testBuilding)
                .code("ACT-CIV-002")
                .name("Column Shuttering")
                .discipline("CIVIL")
                .uom("SQFT")
                .plannedQuantity(200.0)
                .completedQuantity(100.0)
                .progressPercentage(50.0)
                .plannedStartDate(LocalDate.now().minusDays(10))
                .plannedEndDate(LocalDate.now().plusDays(10))
                .status("IN_PROGRESS")
                .weightage(1.0)
                .build();
    }

    @Test
    @DisplayName("Get Activity Progress Tracking - Success for Activity without subtasks")
    void getActivityProgressTracking_Success_Leaf() {
        when(wbsActivityRepository.findById(102L)).thenReturn(Optional.of(childActivity));
        when(wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(102L)).thenReturn(new ArrayList<>());

        ActivityProgressPercentageResponse response = wbsActivityService.getActivityProgressTracking(102L);

        assertNotNull(response);
        assertEquals(102L, response.getActivityId());
        assertEquals("ACT-CIV-002", response.getActivityCode());
        assertEquals(50.0, response.getActualProgressPercentage());
        assertEquals(200.0, response.getPlannedQuantity());
        assertEquals(100.0, response.getCompletedQuantity());
        assertEquals(100.0, response.getRemainingQuantity());
        assertNotNull(response.getPlannedProgressPercentage());
        assertNotNull(response.getProgressVariance());
        assertNotNull(response.getPerformanceStatus());
        assertEquals(0, response.getSubtaskCount());
        assertTrue(response.getSubtasks().isEmpty());
    }

    @Test
    @DisplayName("Get Activity Progress Tracking - Success for Parent Activity with subtasks")
    void getActivityProgressTracking_Success_Parent() {
        when(wbsActivityRepository.findById(101L)).thenReturn(Optional.of(parentActivity));
        when(wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(childActivity));
        when(wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(102L)).thenReturn(new ArrayList<>());

        ActivityProgressPercentageResponse response = wbsActivityService.getActivityProgressTracking(101L);

        assertNotNull(response);
        assertEquals(101L, response.getActivityId());
        assertEquals(1, response.getSubtaskCount());
        assertNotNull(response.getSubtasksWeightedProgress());
        assertEquals(1, response.getSubtasks().size());
        assertEquals(102L, response.getSubtasks().get(0).getActivityId());
    }

    @Test
    @DisplayName("Get Activity Progress Tracking - Throws ResourceNotFoundException if activity missing")
    void getActivityProgressTracking_NotFound_ThrowsException() {
        when(wbsActivityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wbsActivityService.getActivityProgressTracking(999L));
    }

    @Test
    @DisplayName("Get Work Package Progress Tracking - Success")
    void getWorkPackageProgressTracking_Success() {
        when(workPackageRepository.findById(10L)).thenReturn(Optional.of(testWorkPackage));
        when(wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(10L)).thenReturn(List.of(parentActivity, childActivity));
        when(wbsActivityRepository.findByWorkPackageIdAndParentIsNullOrderBySequenceOrderAsc(10L)).thenReturn(List.of(parentActivity));
        when(wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(101L)).thenReturn(List.of(childActivity));
        when(wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(102L)).thenReturn(new ArrayList<>());

        WorkPackageProgressPercentageResponse response = wbsActivityService.getWorkPackageProgressTracking(10L);

        assertNotNull(response);
        assertEquals(10L, response.getWorkPackageId());
        assertEquals("WP-CIV-01", response.getWorkPackageCode());
        assertEquals("CIVIL", response.getDiscipline());
        assertEquals(2, response.getTotalActivities());
        assertEquals(2, response.getInProgressActivities());
        assertNotNull(response.getOverallActualProgressPercentage());
        assertNotNull(response.getOverallPlannedProgressPercentage());
        assertNotNull(response.getProgressVariance());
        assertNotNull(response.getPerformanceStatus());
        assertFalse(response.getActivities().isEmpty());
    }

    @Test
    @DisplayName("Get Work Package Progress Tracking - Throws ResourceNotFoundException if WP missing")
    void getWorkPackageProgressTracking_NotFound_ThrowsException() {
        when(workPackageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wbsActivityService.getWorkPackageProgressTracking(999L));
    }

    @Test
    @DisplayName("Get Project Progress Tracking - Success Dashboard with Breakdowns")
    void getProjectProgressTracking_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(workPackageRepository.findByProjectId(1L)).thenReturn(List.of(testWorkPackage));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(parentActivity, childActivity));

        ProjectProgressPercentageResponse response = wbsActivityService.getProjectProgressTracking(1L, null, null);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals("PRJ-SKY-001", response.getProjectCode());
        assertEquals(2, response.getTotalActivities());
        assertEquals(2, response.getInProgressActivities());
        assertNotNull(response.getOverallActualProgressPercentage());
        assertNotNull(response.getOverallPlannedProgressPercentage());
        assertNotNull(response.getOverallProgressVariance());
        assertNotNull(response.getOverallPerformanceStatus());

        // Verify Breakdowns
        assertFalse(response.getDisciplineBreakdown().isEmpty());
        assertEquals("CIVIL", response.getDisciplineBreakdown().get(0).getDiscipline());

        assertFalse(response.getWorkPackageBreakdown().isEmpty());
        assertEquals(10L, response.getWorkPackageBreakdown().get(0).getWorkPackageId());

        assertFalse(response.getLocationBreakdown().isEmpty());
        assertEquals("Tower A", response.getLocationBreakdown().get(0).getBuildingName());
    }

    @Test
    @DisplayName("Get Project Progress Tracking - Filtered by Discipline and Work Package")
    void getProjectProgressTracking_Filtered() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(workPackageRepository.findByProjectId(1L)).thenReturn(List.of(testWorkPackage));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(parentActivity, childActivity));

        ProjectProgressPercentageResponse response = wbsActivityService.getProjectProgressTracking(1L, "CIVIL", 10L);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals(2, response.getTotalActivities());
    }

    @Test
    @DisplayName("Get Project Progress Tracking - Throws ResourceNotFoundException if Project missing")
    void getProjectProgressTracking_NotFound_ThrowsException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wbsActivityService.getProjectProgressTracking(999L, null, null));
    }
}
