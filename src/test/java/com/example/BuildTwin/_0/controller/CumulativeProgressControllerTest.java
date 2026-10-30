package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.cumulative.*;
import com.example.BuildTwin._0.service.CumulativeProgressService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CumulativeProgressControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CumulativeProgressService cumulativeProgressService;

    @InjectMocks
    private CumulativeProgressController cumulativeProgressController;

    private CumulativeProgressResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cumulativeProgressController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockResponse = CumulativeProgressResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-SKY-001")
                .projectName("Skyline Residency")
                .weightingModel(WeightingModel.WEIGHTAGE_FACTOR)
                .overallActualProgressPercentage(55.0)
                .overallPlannedProgressPercentage(50.0)
                .overallProgressVariancePercentage(5.0)
                .overallPerformanceStatus("AHEAD")
                .totalActivities(20)
                .completedActivities(8)
                .inProgressActivities(10)
                .plannedActivities(2)
                .totalPlannedQuantity(1000.0)
                .totalCompletedQuantity(550.0)
                .totalBalanceQuantity(450.0)
                .earnedValue(new BigDecimal("550000.00"))
                .plannedValue(new BigDecimal("500000.00"))
                .schedulePerformanceIndex(1.10)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/cumulative-progress should return 200 OK")
    void testGetProjectCumulativeProgress() throws Exception {
        when(cumulativeProgressService.getProjectCumulativeProgress(eq(1L), any(), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/projects/1/cumulative-progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.projectCode").value("PRJ-SKY-001"))
                .andExpect(jsonPath("$.data.overallActualProgressPercentage").value(55.0))
                .andExpect(jsonPath("$.data.overallPerformanceStatus").value("AHEAD"))
                .andExpect(jsonPath("$.data.schedulePerformanceIndex").value(1.10));
    }

    @Test
    @DisplayName("GET /api/v1/work-packages/{workPackageId}/cumulative-progress should return 200 OK")
    void testGetWorkPackageCumulativeProgress() throws Exception {
        WorkPackageCumulativeProgressResponse wpRes = WorkPackageCumulativeProgressResponse.builder()
                .workPackageId(2L)
                .workPackageCode("WP-CIV-01")
                .workPackageName("Civil Works")
                .cumulativeActualProgressPercentage(60.0)
                .cumulativePlannedProgressPercentage(55.0)
                .performanceStatus("AHEAD")
                .build();

        when(cumulativeProgressService.getWorkPackageCumulativeProgress(eq(2L), any()))
                .thenReturn(wpRes);

        mockMvc.perform(get("/api/v1/work-packages/2/cumulative-progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workPackageId").value(2))
                .andExpect(jsonPath("$.data.workPackageCode").value("WP-CIV-01"));
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId}/cumulative-progress should return 200 OK")
    void testGetActivityCumulativeProgress() throws Exception {
        ActivityCumulativeProgressResponse actRes = ActivityCumulativeProgressResponse.builder()
                .activityId(10L)
                .activityCode("ACT-CIV-001")
                .actualProgressPercentage(45.0)
                .plannedProgressPercentage(40.0)
                .performanceStatus("AHEAD")
                .build();

        when(cumulativeProgressService.getActivityCumulativeProgress(10L)).thenReturn(actRes);

        mockMvc.perform(get("/api/v1/activities/10/cumulative-progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(10))
                .andExpect(jsonPath("$.data.activityCode").value("ACT-CIV-001"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/cumulative-progress/as-of-date should return 200 OK")
    void testGetAsOfDateCumulativeProgress() throws Exception {
        AsOfDateProgressResponse asOfRes = AsOfDateProgressResponse.builder()
                .projectId(1L)
                .cutoffDate(LocalDate.of(2026, 9, 30))
                .cumulativeActualProgressPercentage(42.0)
                .cumulativePlannedProgressPercentage(40.0)
                .performanceStatus("ON_TRACK")
                .build();

        when(cumulativeProgressService.getAsOfDateCumulativeProgress(eq(1L), any())).thenReturn(asOfRes);

        mockMvc.perform(get("/api/v1/projects/1/cumulative-progress/as-of-date?cutoffDate=2026-09-30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cumulativeActualProgressPercentage").value(42.0));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/cumulative-progress/s-curve should return 200 OK")
    void testGetProjectCumulativeSCurve() throws Exception {
        CumulativeSCurveResponse scurve = CumulativeSCurveResponse.builder()
                .scope("PROJECT")
                .entityId(1L)
                .interval("WEEKLY")
                .currentActualPercentage(55.0)
                .curveData(List.of(SCurveDataPointDto.builder()
                        .date(LocalDate.of(2026, 9, 7))
                        .plannedProgressPercentage(10.0)
                        .actualProgressPercentage(12.0)
                        .build()))
                .build();

        when(cumulativeProgressService.getProjectCumulativeSCurve(eq(1L), anyString())).thenReturn(scurve);

        mockMvc.perform(get("/api/v1/projects/1/cumulative-progress/s-curve?interval=WEEKLY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope").value("PROJECT"))
                .andExpect(jsonPath("$.data.curveData[0].plannedProgressPercentage").value(10.0));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/cumulative-progress/recalculate should return 200 OK")
    void testRecalculateProjectCumulativeProgress() throws Exception {
        RecalculateProgressRequest req = RecalculateProgressRequest.builder()
                .weightingModel(WeightingModel.WEIGHTAGE_FACTOR)
                .syncActualDates(true)
                .build();

        when(cumulativeProgressService.recalculateProjectCumulativeProgress(eq(1L), any(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/projects/1/cumulative-progress/recalculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallActualProgressPercentage").value(55.0));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/cumulative-progress/audit should return 200 OK")
    void testAuditCumulativeProgress() throws Exception {
        ProgressAuditReportResponse auditRes = ProgressAuditReportResponse.builder()
                .projectId(1L)
                .totalActivitiesAudited(20)
                .cleanActivitiesCount(20)
                .discrepantActivitiesCount(0)
                .integrityStatus("HEALTHY")
                .build();

        when(cumulativeProgressService.auditCumulativeProgress(1L)).thenReturn(auditRes);

        mockMvc.perform(get("/api/v1/projects/1/cumulative-progress/audit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.integrityStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.data.cleanActivitiesCount").value(20));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/cumulative-progress/reconcile should return 200 OK")
    void testReconcileCumulativeProgress() throws Exception {
        ProgressReconciliationResponse recRes = ProgressReconciliationResponse.builder()
                .projectId(1L)
                .totalActivitiesScanned(20)
                .activitiesRepairedCount(2)
                .repairedActivityCodes(List.of("ACT-CIV-001"))
                .message("Cumulative progress successfully reconciled")
                .build();

        when(cumulativeProgressService.reconcileCumulativeProgress(eq(1L), anyString()))
                .thenReturn(recRes);

        mockMvc.perform(post("/api/v1/projects/1/cumulative-progress/reconcile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activitiesRepairedCount").value(2));
    }
}
