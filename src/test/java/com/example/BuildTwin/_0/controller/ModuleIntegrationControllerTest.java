package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.integration.*;
import com.example.BuildTwin._0.service.ModuleIntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ModuleIntegrationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ModuleIntegrationService moduleIntegrationService;

    @InjectMocks
    private ModuleIntegrationController moduleIntegrationController;

    private ProjectModuleIntegrationSummaryResponse mockSummary;
    private ProjectModulesListResponse mockModulesList;
    private ProjectCrossReferenceResponse mockCrossReferences;
    private ActivityIntegrationDetailsResponse mockActivityDetails;
    private ModuleHealthResponse mockHealth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(moduleIntegrationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockSummary = ProjectModuleIntegrationSummaryResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-001")
                .projectName("Grand Commercial Tower")
                .projectStatus("ACTIVE")
                .estimatedBudget(new BigDecimal("10000000.00"))
                .integrationStatus("FULLY_INTEGRATED")
                .readinessScore(95)
                .hierarchy(HierarchyIntegrationDto.builder()
                        .siteCount(2L)
                        .buildingCount(4L)
                        .floorCount(20L)
                        .zoneCount(60L)
                        .status("CONFIGURED")
                        .build())
                .wbs(WbsIntegrationDto.builder()
                        .workPackageCount(6L)
                        .activityCount(40L)
                        .completedActivitiesCount(18L)
                        .inProgressActivitiesCount(15L)
                        .plannedActivitiesCount(7L)
                        .milestoneCount(4L)
                        .status("ACTIVE")
                        .build())
                .scheduleBaseline(ScheduleBaselineIntegrationDto.builder()
                        .activeBaselineVersion(1)
                        .activeBaselineName("Contract Schedule v1")
                        .isBaselineApproved(true)
                        .scheduleRevisionsCount(2L)
                        .delayAlertsCount(1L)
                        .dependencyCount(25L)
                        .status("APPROVED_FROZEN")
                        .build())
                .lookahead(LookaheadIntegrationDto.builder()
                        .totalCommitments(14L)
                        .activeCommitments(10L)
                        .completedCommitments(4L)
                        .overdueCommitments(0L)
                        .status("SYNCHRONIZED")
                        .build())
                .dpr(DprIntegrationDto.builder()
                        .totalDprs(35L)
                        .approvedDprs(32L)
                        .pendingDprs(3L)
                        .latestReportDate(LocalDate.of(2026, 10, 30))
                        .latestWorkShift("GENERAL")
                        .totalManpowerHeadcount(850L)
                        .totalSafetyIncidents(0L)
                        .status("FIELD_ACTIVE")
                        .build())
                .progressEngine(ProgressEngineIntegrationDto.builder()
                        .totalProgressLogs(120L)
                        .lastProgressUpdateDate(LocalDate.of(2026, 10, 30))
                        .cumulativeProgressPercentage(64.5)
                        .plannedProgressPercentage(68.0)
                        .progressVariance(-3.5)
                        .calculationModel("WEIGHTAGE_FACTOR")
                        .scheduleHealth("ON_TRACK")
                        .status("SYNCHRONIZED")
                        .build())
                .integratedModules(List.of("SPATIAL_HIERARCHY", "WBS_ACTIVITIES", "BASELINE_SCHEDULE", "LOOKAHEAD_PLANNING", "DPR_FIELD_TRACKING", "PROGRESS_ENGINE"))
                .syncedAt(LocalDateTime.now())
                .build();

        mockModulesList = ProjectModulesListResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-001")
                .totalModules(8)
                .activeModules(8)
                .modules(List.of(
                        ModuleDescriptorDto.builder()
                                .moduleKey("HIERARCHY")
                                .moduleName("Spatial Hierarchy")
                                .basePath("/api/v1/hierarchy")
                                .version("1.0.0")
                                .status("SYNCHRONIZED")
                                .entityCount(2L)
                                .health("HEALTHY")
                                .build()
                ))
                .build();

        mockCrossReferences = ProjectCrossReferenceResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-001")
                .totalActivities(1)
                .activities(List.of(
                        ActivityCrossReferenceDto.builder()
                                .activityId(101L)
                                .activityCode("ACT-CIV-001")
                                .activityName("Substructure Concrete Works")
                                .activityStatus("IN_PROGRESS")
                                .discipline("CIVIL")
                                .workPackageId(1L)
                                .workPackageCode("WP-01")
                                .workPackageName("Foundation")
                                .siteId(10L)
                                .siteName("Site A")
                                .buildingName("Tower 1")
                                .floorName("Basement 1")
                                .zoneId(20L)
                                .zoneName("Zone 1")
                                .progressPercentage(65.0)
                                .assignedContractor("L&T")
                                .syncStatus("SYNCED")
                                .build()
                ))
                .build();

        mockActivityDetails = ActivityIntegrationDetailsResponse.builder()
                .activityId(101L)
                .activityCode("ACT-CIV-001")
                .activityName("Substructure Concrete Works")
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 9, 30))
                .durationDays(30)
                .weightage(2.0)
                .projectId(1L)
                .projectCode("PRJ-001")
                .workPackageId(1L)
                .workPackageName("Foundation")
                .siteId(10L)
                .siteName("Site A")
                .buildingId(5L)
                .buildingName("Tower 1")
                .floorId(12L)
                .floorName("Basement 1")
                .zoneId(20L)
                .zoneName("Zone 1")
                .predecessorCount(1)
                .successorCount(2)
                .activeBaselineVersion(1)
                .baselineStartDate(LocalDate.of(2026, 9, 1))
                .baselineEndDate(LocalDate.of(2026, 9, 30))
                .scheduleVarianceDays(0)
                .plannedQuantity(500.0)
                .completedQuantity(325.0)
                .uom("CUM")
                .progressPercentage(65.0)
                .lastProgressUpdateDate(LocalDate.of(2026, 10, 28))
                .assignedContractor("L&T")
                .integrationStatus("FULLY_INTEGRATED")
                .build();

        mockHealth = ModuleHealthResponse.builder()
                .status("UP")
                .version("1.0.0-ENTERPRISE")
                .timestamp(LocalDateTime.now())
                .subsystemStatus(Map.of(
                        "SPATIAL_HIERARCHY", "UP",
                        "WBS_ACTIVITIES", "UP"
                ))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/integration/projects/{projectId}/summary should return 200 OK with summary data")
    void testGetProjectModuleIntegrationSummary() throws Exception {
        when(moduleIntegrationService.getProjectModuleIntegrationSummary(eq(1L)))
                .thenReturn(mockSummary);

        mockMvc.perform(get("/api/v1/integration/projects/1/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.projectCode").value("PRJ-001"))
                .andExpect(jsonPath("$.data.integrationStatus").value("FULLY_INTEGRATED"))
                .andExpect(jsonPath("$.data.readinessScore").value(95))
                .andExpect(jsonPath("$.data.hierarchy.siteCount").value(2))
                .andExpect(jsonPath("$.data.wbs.activityCount").value(40))
                .andExpect(jsonPath("$.data.scheduleBaseline.activeBaselineVersion").value(1))
                .andExpect(jsonPath("$.data.dpr.totalDprs").value(35));
    }

    @Test
    @DisplayName("GET /api/v1/integration/project/{projectId}/summary (alias) should return 200 OK")
    void testGetProjectModuleIntegrationSummary_Alias() throws Exception {
        when(moduleIntegrationService.getProjectModuleIntegrationSummary(eq(1L)))
                .thenReturn(mockSummary);

        mockMvc.perform(get("/api/v1/integration/project/1/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/integration/projects/{projectId}/modules should return 200 OK with registered modules")
    void testGetProjectModulesList() throws Exception {
        when(moduleIntegrationService.getProjectModulesList(eq(1L)))
                .thenReturn(mockModulesList);

        mockMvc.perform(get("/api/v1/integration/projects/1/modules")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.totalModules").value(8))
                .andExpect(jsonPath("$.data.modules[0].moduleKey").value("HIERARCHY"));
    }

    @Test
    @DisplayName("GET /api/v1/integration/projects/{projectId}/cross-references should return 200 OK")
    void testGetProjectCrossReferences() throws Exception {
        when(moduleIntegrationService.getProjectCrossReferences(eq(1L)))
                .thenReturn(mockCrossReferences);

        mockMvc.perform(get("/api/v1/integration/projects/1/cross-references")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.totalActivities").value(1))
                .andExpect(jsonPath("$.data.activities[0].activityCode").value("ACT-CIV-001"))
                .andExpect(jsonPath("$.data.activities[0].syncStatus").value("SYNCED"));
    }

    @Test
    @DisplayName("GET /api/v1/integration/activities/{activityId} should return 200 OK with deep integration details")
    void testGetActivityIntegrationDetails() throws Exception {
        when(moduleIntegrationService.getActivityIntegrationDetails(eq(101L)))
                .thenReturn(mockActivityDetails);

        mockMvc.perform(get("/api/v1/integration/activities/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(101))
                .andExpect(jsonPath("$.data.activityCode").value("ACT-CIV-001"))
                .andExpect(jsonPath("$.data.integrationStatus").value("FULLY_INTEGRATED"))
                .andExpect(jsonPath("$.data.predecessorCount").value(1))
                .andExpect(jsonPath("$.data.successorCount").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/integration/health should return 200 OK with system integration health")
    void testGetSystemIntegrationHealth() throws Exception {
        when(moduleIntegrationService.getSystemIntegrationHealth())
                .thenReturn(mockHealth);

        mockMvc.perform(get("/api/v1/integration/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.version").value("1.0.0-ENTERPRISE"));
    }
}
