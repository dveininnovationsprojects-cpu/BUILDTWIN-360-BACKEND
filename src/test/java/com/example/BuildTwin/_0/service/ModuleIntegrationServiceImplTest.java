package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.integration.*;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.impl.ModuleIntegrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModuleIntegrationServiceImplTest {

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
    private WorkPackageRepository workPackageRepository;
    @Mock
    private WbsActivityRepository wbsActivityRepository;
    @Mock
    private ProjectBaselineRepository projectBaselineRepository;
    @Mock
    private BaselineActivitySnapshotRepository baselineActivitySnapshotRepository;
    @Mock
    private ScheduleRevisionRepository scheduleRevisionRepository;
    @Mock
    private ScheduleDelayAlertRepository scheduleDelayAlertRepository;
    @Mock
    private ActivityDependencyRepository activityDependencyRepository;
    @Mock
    private LookaheadCommitmentRepository lookaheadCommitmentRepository;
    @Mock
    private DailyProgressReportRepository dailyProgressReportRepository;
    @Mock
    private ActivityProgressLogRepository activityProgressLogRepository;
    @Mock
    private ProjectProgressSummaryService projectProgressSummaryService;

    @InjectMocks
    private ModuleIntegrationServiceImpl moduleIntegrationService;

    private Project mockProject;
    private Site mockSite;
    private Building mockBuilding;
    private Floor mockFloor;
    private Zone mockZone;
    private WorkPackage mockWorkPackage;
    private WbsActivity mockActivity;
    private ProjectBaseline mockBaseline;

    @BeforeEach
    void setUp() {
        mockProject = Project.builder()
                .id(1L)
                .code("PRJ-001")
                .name("Grand Commercial Tower")
                .status("ACTIVE")
                .estimatedBudget(new BigDecimal("10000000.00"))
                .build();

        mockSite = Site.builder().id(10L).name("Site North").project(mockProject).build();
        mockBuilding = Building.builder().id(20L).name("Tower A").site(mockSite).build();
        mockFloor = Floor.builder().id(30L).floorNumber(1).floorName("Level 1").building(mockBuilding).build();
        mockZone = Zone.builder().id(40L).name("Zone A").floor(mockFloor).build();

        mockWorkPackage = WorkPackage.builder()
                .id(100L)
                .code("WP-CIV-01")
                .name("Substructure Concrete")
                .project(mockProject)
                .discipline("CIVIL")
                .build();

        mockActivity = WbsActivity.builder()
                .id(1001L)
                .code("ACT-001")
                .name("Foundation Raft Concrete")
                .project(mockProject)
                .workPackage(mockWorkPackage)
                .site(mockSite)
                .building(mockBuilding)
                .floor(mockFloor)
                .zone(mockZone)
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 9, 30))
                .actualStartDate(LocalDate.of(2026, 9, 3))
                .plannedQuantity(500.0)
                .completedQuantity(300.0)
                .uom("CUM")
                .weightage(2.0)
                .progressPercentage(60.0)
                .assignedContractor("L&T Construction")
                .build();

        mockBaseline = ProjectBaseline.builder()
                .id(50L)
                .project(mockProject)
                .version(1)
                .name("Approved Master Baseline")
                .isActive(true)
                .approvedAt(LocalDateTime.of(2026, 8, 30, 10, 0))
                .approvedBy("Director Rajesh")
                .build();
    }

    @Test
    @DisplayName("getProjectModuleIntegrationSummary should aggregate all subsystem metrics cleanly")
    void testGetProjectModuleIntegrationSummary_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(siteRepository.countByProjectId(1L)).thenReturn(1L);
        when(siteRepository.findByProjectId(1L)).thenReturn(List.of(mockSite));
        when(buildingRepository.findBySiteId(10L)).thenReturn(List.of(mockBuilding));
        when(floorRepository.findByBuildingIdOrderByFloorNumberAsc(20L)).thenReturn(List.of(mockFloor));
        when(zoneRepository.countByFloorId(30L)).thenReturn(1L);

        when(workPackageRepository.countByProjectId(1L)).thenReturn(1L);
        when(wbsActivityRepository.countByProjectId(1L)).thenReturn(1L);
        when(wbsActivityRepository.countByProjectIdAndStatus(1L, "COMPLETED")).thenReturn(0L);
        when(wbsActivityRepository.countByProjectIdAndStatus(1L, "IN_PROGRESS")).thenReturn(1L);
        when(wbsActivityRepository.countByProjectIdAndStatus(1L, "PLANNED")).thenReturn(0L);
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(mockActivity));

        when(projectBaselineRepository.findByProjectIdAndIsActiveTrue(1L)).thenReturn(Optional.of(mockBaseline));
        when(scheduleRevisionRepository.countByProjectId(1L)).thenReturn(2L);
        when(scheduleDelayAlertRepository.countByProjectId(1L)).thenReturn(1L);
        when(activityDependencyRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        when(lookaheadCommitmentRepository.countByProjectId(1L)).thenReturn(5L);
        when(lookaheadCommitmentRepository.countByProjectIdAndStatus(1L, CommitmentStatus.COMMITTED)).thenReturn(4L);
        when(lookaheadCommitmentRepository.countByProjectIdAndStatus(1L, CommitmentStatus.COMPLETED)).thenReturn(1L);
        when(lookaheadCommitmentRepository.countByProjectIdAndStatus(1L, CommitmentStatus.BLOCKED)).thenReturn(0L);
        when(lookaheadCommitmentRepository.countByProjectIdAndStatus(1L, CommitmentStatus.NOT_ACHIEVED)).thenReturn(0L);

        when(dailyProgressReportRepository.countByProjectId(1L)).thenReturn(10L);
        when(dailyProgressReportRepository.countByProjectIdAndStatus(1L, DprStatus.APPROVED)).thenReturn(9L);
        when(dailyProgressReportRepository.findTopByProjectIdOrderByReportDateDesc(1L)).thenReturn(Optional.empty());
        when(dailyProgressReportRepository.sumManpowerByProjectId(1L)).thenReturn(250L);
        when(dailyProgressReportRepository.sumSafetyIncidentsByProjectId(1L)).thenReturn(0L);

        when(activityProgressLogRepository.countByProjectId(1L)).thenReturn(25L);
        when(activityProgressLogRepository.findMaxLogDateByProjectId(1L)).thenReturn(LocalDate.of(2026, 10, 25));

        ProjectProgressSummaryResponse mockSummary = ProjectProgressSummaryResponse.builder()
                .overallProgressPercentage(60.0)
                .plannedProgressPercentage(65.0)
                .progressVariance(-5.0)
                .scheduleHealth("ON_TRACK")
                .build();
        when(projectProgressSummaryService.getProjectProgressSummary(1L)).thenReturn(mockSummary);

        ProjectModuleIntegrationSummaryResponse response = moduleIntegrationService.getProjectModuleIntegrationSummary(1L);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals("PRJ-001", response.getProjectCode());
        assertEquals("FULLY_INTEGRATED", response.getIntegrationStatus());
        assertTrue(response.getReadinessScore() >= 70);
        assertEquals(1L, response.getHierarchy().getSiteCount());
        assertEquals(1L, response.getHierarchy().getBuildingCount());
        assertEquals(1L, response.getHierarchy().getFloorCount());
        assertEquals(1L, response.getHierarchy().getZoneCount());
        assertEquals(1L, response.getWbs().getActivityCount());
        assertEquals(1, response.getScheduleBaseline().getActiveBaselineVersion());
        assertTrue(response.getScheduleBaseline().getIsBaselineApproved());
        assertEquals(5L, response.getLookahead().getTotalCommitments());
        assertEquals(10L, response.getDpr().getTotalDprs());
        assertEquals(60.0, response.getProgressEngine().getCumulativeProgressPercentage());
    }

    @Test
    @DisplayName("getProjectModuleIntegrationSummary should throw ResourceNotFoundException for unknown project")
    void testGetProjectModuleIntegrationSummary_NotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                moduleIntegrationService.getProjectModuleIntegrationSummary(999L));
    }

    @Test
    @DisplayName("getProjectModulesList should return 8 registered modules with status and counts")
    void testGetProjectModulesList() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(siteRepository.countByProjectId(1L)).thenReturn(2L);
        when(wbsActivityRepository.countByProjectId(1L)).thenReturn(10L);
        when(projectBaselineRepository.findByProjectIdOrderByVersionDesc(1L)).thenReturn(List.of(mockBaseline));
        when(lookaheadCommitmentRepository.countByProjectId(1L)).thenReturn(4L);
        when(dailyProgressReportRepository.countByProjectId(1L)).thenReturn(8L);
        when(activityProgressLogRepository.countByProjectId(1L)).thenReturn(20L);

        ProjectModulesListResponse response = moduleIntegrationService.getProjectModulesList(1L);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals(8, response.getTotalModules());
        assertTrue(response.getActiveModules() > 0);
        assertTrue(response.getModules().stream().anyMatch(m -> "HIERARCHY".equals(m.getModuleKey())));
        assertTrue(response.getModules().stream().anyMatch(m -> "WBS".equals(m.getModuleKey())));
        assertTrue(response.getModules().stream().anyMatch(m -> "CUMULATIVE_PROGRESS".equals(m.getModuleKey())));
    }

    @Test
    @DisplayName("getProjectCrossReferences should map activities to work package, site, building, floor, zone")
    void testGetProjectCrossReferences() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(mockActivity));

        ProjectCrossReferenceResponse response = moduleIntegrationService.getProjectCrossReferences(1L);

        assertNotNull(response);
        assertEquals(1, response.getTotalActivities());
        ActivityCrossReferenceDto dto = response.getActivities().get(0);
        assertEquals(1001L, dto.getActivityId());
        assertEquals("ACT-001", dto.getActivityCode());
        assertEquals("WP-CIV-01", dto.getWorkPackageCode());
        assertEquals("Site North", dto.getSiteName());
        assertEquals("Tower A", dto.getBuildingName());
        assertEquals("Level 1", dto.getFloorName());
        assertEquals("Zone A", dto.getZoneName());
        assertEquals("SYNCED", dto.getSyncStatus());
    }

    @Test
    @DisplayName("getActivityIntegrationDetails should return comprehensive activity integration view")
    void testGetActivityIntegrationDetails_Success() {
        when(wbsActivityRepository.findById(1001L)).thenReturn(Optional.of(mockActivity));
        when(activityDependencyRepository.findBySuccessorId(1001L)).thenReturn(Collections.emptyList());
        when(activityDependencyRepository.findByPredecessorId(1001L)).thenReturn(Collections.emptyList());
        when(projectBaselineRepository.findByProjectIdAndIsActiveTrue(1L)).thenReturn(Optional.of(mockBaseline));

        BaselineActivitySnapshot snapshot = BaselineActivitySnapshot.builder()
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 9, 30))
                .build();
        when(baselineActivitySnapshotRepository.findByBaselineIdAndActivityId(50L, 1001L)).thenReturn(Optional.of(snapshot));
        when(activityProgressLogRepository.findTopByActivityIdOrderByLogDateDesc(1001L)).thenReturn(Optional.empty());

        ActivityIntegrationDetailsResponse response = moduleIntegrationService.getActivityIntegrationDetails(1001L);

        assertNotNull(response);
        assertEquals(1001L, response.getActivityId());
        assertEquals("ACT-001", response.getActivityCode());
        assertEquals("CIVIL", response.getDiscipline());
        assertEquals("Site North", response.getSiteName());
        assertEquals("Tower A", response.getBuildingName());
        assertEquals("Level 1", response.getFloorName());
        assertEquals("Zone A", response.getZoneName());
        assertEquals(1, response.getActiveBaselineVersion());
        assertEquals("FULLY_INTEGRATED", response.getIntegrationStatus());
    }

    @Test
    @DisplayName("getActivityIntegrationDetails should throw ResourceNotFoundException for unknown activity")
    void testGetActivityIntegrationDetails_NotFound() {
        when(wbsActivityRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                moduleIntegrationService.getActivityIntegrationDetails(9999L));
    }

    @Test
    @DisplayName("getSystemIntegrationHealth should return UP status for all subsystems")
    void testGetSystemIntegrationHealth() {
        ModuleHealthResponse health = moduleIntegrationService.getSystemIntegrationHealth();

        assertNotNull(health);
        assertEquals("UP", health.getStatus());
        assertEquals("1.0.0-ENTERPRISE", health.getVersion());
        assertEquals("UP", health.getSubsystemStatus().get("SPATIAL_HIERARCHY"));
        assertEquals("UP", health.getSubsystemStatus().get("CUMULATIVE_PROGRESS_ENGINE"));
    }
}
