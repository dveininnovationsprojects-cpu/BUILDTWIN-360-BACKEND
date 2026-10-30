package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.wbs.ActivityProgressPercentageResponse;
import com.example.BuildTwin._0.dto.wbs.ProjectProgressPercentageResponse;
import com.example.BuildTwin._0.dto.wbs.WorkPackageProgressPercentageResponse;
import com.example.BuildTwin._0.service.WbsActivityService;
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

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WbsActivityProgressTrackingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WbsActivityService wbsActivityService;

    @InjectMocks
    private WbsActivityController wbsActivityController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wbsActivityController).build();
    }

    @Test
    @DisplayName("GET /api/v1/wbs-activities/{id}/progress-tracking - Success")
    void testGetActivityProgressTracking() throws Exception {
        ActivityProgressPercentageResponse response = ActivityProgressPercentageResponse.builder()
                .activityId(101L)
                .activityCode("ACT-001")
                .activityName("Foundation Concrete Pouring")
                .plannedProgressPercentage(50.0)
                .actualProgressPercentage(60.0)
                .progressVariance(10.0)
                .performanceStatus("AHEAD")
                .plannedQuantity(100.0)
                .completedQuantity(60.0)
                .remainingQuantity(40.0)
                .uom("CU_M")
                .plannedStartDate(LocalDate.of(2026, 10, 1))
                .plannedEndDate(LocalDate.of(2026, 10, 20))
                .build();

        when(wbsActivityService.getActivityProgressTracking(eq(101L))).thenReturn(response);

        mockMvc.perform(get("/api/v1/wbs-activities/101/progress-tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.activityId").value(101))
                .andExpect(jsonPath("$.data.activityCode").value("ACT-001"))
                .andExpect(jsonPath("$.data.actualProgressPercentage").value(60.0))
                .andExpect(jsonPath("$.data.performanceStatus").value("AHEAD"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/progress-percentage-tracking - Success")
    void testGetProjectProgressTracking() throws Exception {
        ProjectProgressPercentageResponse response = ProjectProgressPercentageResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-SKY-001")
                .projectName("Skyline Towers")
                .totalActivities(10)
                .completedActivities(3)
                .inProgressActivities(5)
                .plannedActivities(2)
                .overallPlannedProgressPercentage(45.0)
                .overallActualProgressPercentage(48.5)
                .overallProgressVariance(3.5)
                .overallPerformanceStatus("ON_TRACK")
                .disciplineBreakdown(Collections.emptyList())
                .workPackageBreakdown(Collections.emptyList())
                .locationBreakdown(Collections.emptyList())
                .build();

        when(wbsActivityService.getProjectProgressTracking(eq(1L), eq(null), eq(null))).thenReturn(response);

        mockMvc.perform(get("/api/v1/projects/1/progress-percentage-tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.overallActualProgressPercentage").value(48.5))
                .andExpect(jsonPath("$.data.overallPerformanceStatus").value("ON_TRACK"));
    }

    @Test
    @DisplayName("GET /api/v1/work-packages/{workPackageId}/progress-percentage-tracking - Success")
    void testGetWorkPackageProgressTracking() throws Exception {
        WorkPackageProgressPercentageResponse response = WorkPackageProgressPercentageResponse.builder()
                .workPackageId(5L)
                .workPackageName("Substructure Works")
                .totalActivities(4)
                .completedActivities(1)
                .inProgressActivities(2)
                .plannedActivities(1)
                .overallPlannedProgressPercentage(40.0)
                .overallActualProgressPercentage(35.0)
                .progressVariance(-5.0)
                .performanceStatus("BEHIND")
                .activities(Collections.emptyList())
                .build();

        when(wbsActivityService.getWorkPackageProgressTracking(eq(5L))).thenReturn(response);

        mockMvc.perform(get("/api/v1/work-packages/5/progress-percentage-tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.workPackageId").value(5))
                .andExpect(jsonPath("$.data.performanceStatus").value("BEHIND"));
    }
}
