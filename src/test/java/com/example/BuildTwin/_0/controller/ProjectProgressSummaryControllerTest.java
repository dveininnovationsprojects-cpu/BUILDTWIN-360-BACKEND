package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.summary.DisciplineProgressSummaryDto;
import com.example.BuildTwin._0.dto.summary.PortfolioProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.WorkPackageProgressSummaryDto;
import com.example.BuildTwin._0.service.ProjectProgressSummaryService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectProgressSummaryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProjectProgressSummaryService projectProgressSummaryService;

    @InjectMocks
    private ProjectProgressSummaryController projectProgressSummaryController;

    private ProjectProgressSummaryResponse mockProjectSummary;
    private WorkPackageProgressSummaryDto mockWpSummary;
    private PortfolioProgressSummaryResponse mockPortfolioSummary;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectProgressSummaryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockWpSummary = WorkPackageProgressSummaryDto.builder()
                .workPackageId(1L)
                .code("WP-01")
                .name("Substructure Concrete")
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .progressPercentage(65.5)
                .plannedProgressPercentage(70.0)
                .progressVariance(-4.5)
                .totalActivities(15L)
                .completedActivities(8L)
                .inProgressActivities(5L)
                .plannedActivities(2L)
                .budgetAmount(new BigDecimal("1200000.00"))
                .build();

        DisciplineProgressSummaryDto mockDiscipline = DisciplineProgressSummaryDto.builder()
                .discipline("CIVIL")
                .totalActivities(15L)
                .completedActivities(8L)
                .inProgressActivities(5L)
                .actualProgressPercentage(65.5)
                .plannedProgressPercentage(70.0)
                .progressVariance(-4.5)
                .build();

        mockProjectSummary = ProjectProgressSummaryResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-001")
                .projectName("Grand Commercial Tower")
                .projectStatus("ACTIVE")
                .projectType("COMMERCIAL")
                .clientName("Apex Infra")
                .location("Chennai")
                .plannedStartDate(LocalDate.of(2026, 1, 1))
                .plannedEndDate(LocalDate.of(2026, 12, 31))
                .estimatedBudget(new BigDecimal("10000000.00"))
                .currency("INR")
                .overallProgressPercentage(68.5)
                .plannedProgressPercentage(72.0)
                .progressVariance(-3.5)
                .scheduleHealth("ON_TRACK")
                .earnedValue(new BigDecimal("6850000.00"))
                .plannedValue(new BigDecimal("7200000.00"))
                .schedulePerformanceIndex(0.95)
                .totalActivities(45L)
                .completedActivities(22L)
                .inProgressActivities(16L)
                .plannedActivities(7L)
                .completionRate(48.89)
                .totalPlannedQuantity(15000.0)
                .totalCompletedQuantity(10275.0)
                .totalRemainingQuantity(4725.0)
                .quantityProgressPercentage(68.5)
                .lastProgressUpdateDate(LocalDate.of(2026, 10, 27))
                .totalProgressEntriesCount(142L)
                .activeBlockersCount(2L)
                .workPackages(List.of(mockWpSummary))
                .disciplines(List.of(mockDiscipline))
                .build();

        mockPortfolioSummary = PortfolioProgressSummaryResponse.builder()
                .totalProjects(1L)
                .activeProjects(1L)
                .completedProjects(0L)
                .plannedOrOnHoldProjects(0L)
                .averageProgressPercentage(68.5)
                .averagePlannedProgressPercentage(72.0)
                .totalPortfolioBudget(new BigDecimal("10000000.00"))
                .totalPortfolioEarnedValue(new BigDecimal("6850000.00"))
                .totalActivities(45L)
                .totalCompletedActivities(22L)
                .projectSummaries(List.of(mockProjectSummary))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/progress-summary should return 200 OK with summary data")
    void testGetProjectProgressSummary() throws Exception {
        when(projectProgressSummaryService.getProjectProgressSummary(eq(1L)))
                .thenReturn(mockProjectSummary);

        mockMvc.perform(get("/api/v1/projects/1/progress-summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.projectCode").value("PRJ-001"))
                .andExpect(jsonPath("$.data.overallProgressPercentage").value(68.5))
                .andExpect(jsonPath("$.data.scheduleHealth").value("ON_TRACK"))
                .andExpect(jsonPath("$.data.totalActivities").value(45))
                .andExpect(jsonPath("$.data.completedActivities").value(22))
                .andExpect(jsonPath("$.data.totalCompletedQuantity").value(10275.0))
                .andExpect(jsonPath("$.data.workPackages[0].workPackageId").value(1))
                .andExpect(jsonPath("$.data.disciplines[0].discipline").value("CIVIL"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/progress/summary (alias) should return 200 OK")
    void testGetProjectProgressSummary_Alias() throws Exception {
        when(projectProgressSummaryService.getProjectProgressSummary(eq(1L)))
                .thenReturn(mockProjectSummary);

        mockMvc.perform(get("/api/v1/projects/1/progress/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/projects/progress-summary should return portfolio summary")
    void testGetPortfolioProgressSummary() throws Exception {
        when(projectProgressSummaryService.getPortfolioProgressSummary())
                .thenReturn(mockPortfolioSummary);

        mockMvc.perform(get("/api/v1/projects/progress-summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProjects").value(1))
                .andExpect(jsonPath("$.data.activeProjects").value(1))
                .andExpect(jsonPath("$.data.averageProgressPercentage").value(68.5));
    }

    @Test
    @DisplayName("GET /api/v1/work-packages/{workPackageId}/progress-summary should return 200 OK")
    void testGetWorkPackageProgressSummary() throws Exception {
        when(projectProgressSummaryService.getWorkPackageProgressSummary(eq(1L)))
                .thenReturn(mockWpSummary);

        mockMvc.perform(get("/api/v1/work-packages/1/progress-summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workPackageId").value(1))
                .andExpect(jsonPath("$.data.code").value("WP-01"))
                .andExpect(jsonPath("$.data.progressPercentage").value(65.5));
    }
}
