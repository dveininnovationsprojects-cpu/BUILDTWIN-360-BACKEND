package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.lookahead.*;
import com.example.BuildTwin._0.model.OverdueAgingBucket;
import com.example.BuildTwin._0.model.OverdueSeverity;
import com.example.BuildTwin._0.model.OverdueType;
import com.example.BuildTwin._0.service.LookaheadScheduleService;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LookaheadScheduleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LookaheadScheduleService lookaheadScheduleService;

    @InjectMocks
    private LookaheadScheduleController lookaheadScheduleController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(lookaheadScheduleController)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/lookahead/14-day should return 14-day schedule")
    void testGetFourteenDayLookaheadSchedule() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        FourteenDayLookaheadResponse mockResponse = FourteenDayLookaheadResponse.builder()
                .projectId(1L)
                .projectCode("PADUR-AG-01")
                .projectName("Ashok Grandeur - Padur, Chennai")
                .windowStartDate(startDate)
                .windowEndDate(startDate.plusDays(13))
                .lookaheadDays(14)
                .week1StartDate(startDate)
                .week1EndDate(startDate.plusDays(6))
                .week2StartDate(startDate.plusDays(7))
                .week2EndDate(startDate.plusDays(13))
                .lookaheadHealthStatus("HEALTHY")
                .plannerSummary("14-Day Lookahead plan ready")
                .dailyBreakdown(new ArrayList<>())
                .week1Activities(new ArrayList<>())
                .week2Activities(new ArrayList<>())
                .allActivities(new ArrayList<>())
                .build();

        when(lookaheadScheduleService.getFourteenDayLookaheadSchedule(
                eq(1L), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/projects/1/lookahead/14-day")
                        .param("startDate", "2026-10-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.lookaheadDays").value(14))
                .andExpect(jsonPath("$.data.windowStartDate").value("2026-10-01"))
                .andExpect(jsonPath("$.data.windowEndDate").value("2026-10-14"))
                .andExpect(jsonPath("$.data.lookaheadHealthStatus").value("HEALTHY"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/lookahead/14-day/metrics should return 14-day metrics")
    void testGetFourteenDayMetrics() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        FourteenDayMetricsResponse mockMetrics = FourteenDayMetricsResponse.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur - Padur, Chennai")
                .windowStartDate(startDate)
                .windowEndDate(startDate.plusDays(13))
                .overallMetrics(LookaheadMetricsResponse.builder().totalActivitiesInWindow(5).readinessScorePercentage(90.0).build())
                .lookaheadHealthStatus("HEALTHY")
                .plannerSummary("Week 1 has 90% readiness")
                .build();

        when(lookaheadScheduleService.getFourteenDayMetrics(eq(1L), any())).thenReturn(mockMetrics);

        mockMvc.perform(get("/api/v1/projects/1/lookahead/14-day/metrics")
                        .param("startDate", "2026-10-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.overallMetrics.totalActivitiesInWindow").value(5))
                .andExpect(jsonPath("$.data.lookaheadHealthStatus").value("HEALTHY"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/lookahead/14-day/constraints should return 14-day constraints")
    void testGetFourteenDayConstraints() throws Exception {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LookaheadConstraintSummaryResponse mockConstraints = LookaheadConstraintSummaryResponse.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur - Padur, Chennai")
                .windowStartDate(startDate)
                .windowEndDate(startDate.plusDays(13))
                .totalBlockedActivities(0)
                .materialConstraintCount(0)
                .build();

        when(lookaheadScheduleService.getFourteenDayConstraints(eq(1L), any())).thenReturn(mockConstraints);

        mockMvc.perform(get("/api/v1/projects/1/lookahead/14-day/constraints")
                        .param("startDate", "2026-10-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalBlockedActivities").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/lookahead/overdue should return list of overdue activities")
    void testGetOverdueActivities() throws Exception {
        OverdueActivityItemResponse item = OverdueActivityItemResponse.builder()
                .activityId(101L)
                .code("ACT-CIV-001")
                .name("Footing Excavation & PCC")
                .discipline("CIVIL")
                .overdueType(OverdueType.OVERDUE_FINISH)
                .daysOverdue(10L)
                .agingBucket(OverdueAgingBucket.DAYS_8_TO_14)
                .severity(OverdueSeverity.HIGH)
                .plannedQuantity(100.0)
                .completedQuantity(40.0)
                .remainingQuantity(60.0)
                .uom("CUM")
                .impactedSuccessorCount(1)
                .build();

        when(lookaheadScheduleService.getOverdueActivities(
                eq(1L), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/projects/1/lookahead/overdue")
                        .param("asOfDate", "2026-10-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].activityId").value(101))
                .andExpect(jsonPath("$.data[0].code").value("ACT-CIV-001"))
                .andExpect(jsonPath("$.data[0].daysOverdue").value(10))
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/lookahead/overdue/summary should return overdue summary")
    void testGetOverdueActivitySummary() throws Exception {
        OverdueActivitySummaryResponse summary = OverdueActivitySummaryResponse.builder()
                .projectId(1L)
                .projectCode("PADUR-AG-01")
                .projectName("Ashok Grandeur - Padur, Chennai")
                .asOfDate(LocalDate.of(2026, 10, 15))
                .totalProjectActivities(10)
                .totalOverdueActivities(2)
                .overdueRatePercentage(20.0)
                .overdueFinishCount(2)
                .averageDelayDays(8.5)
                .maxDelayDays(12L)
                .totalImpactedSuccessors(3)
                .build();

        when(lookaheadScheduleService.getOverdueActivitySummary(eq(1L), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/projects/1/lookahead/overdue/summary")
                        .param("asOfDate", "2026-10-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.totalOverdueActivities").value(2))
                .andExpect(jsonPath("$.data.overdueRatePercentage").value(20.0));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/lookahead/overdue/{activityId}/mitigation should log mitigation plan")
    void testRecordOverdueMitigation() throws Exception {
        OverdueMitigationRequest request = OverdueMitigationRequest.builder()
                .delayReasonCategory("MATERIAL_SHORTAGE")
                .delayReasonDetails("Delay in supply of cement bags")
                .recoveryPlanAction("Procured from alternate depot and started overtime shift")
                .revisedExpectedCompletionDate(LocalDate.of(2026, 10, 20))
                .additionalLaborAllocated(5)
                .notes("Reviewed in site coordination meeting")
                .build();

        OverdueMitigationResponse response = OverdueMitigationResponse.builder()
                .activityId(101L)
                .activityCode("ACT-CIV-001")
                .activityName("Footing Excavation & PCC")
                .delayReasonCategory("MATERIAL_SHORTAGE")
                .delayReasonDetails("Delay in supply of cement bags")
                .recoveryPlanAction("Procured from alternate depot and started overtime shift")
                .revisedExpectedCompletionDate(LocalDate.of(2026, 10, 20))
                .additionalLaborAllocated(5)
                .recordedBy("admin")
                .recordedAt(LocalDateTime.now())
                .message("Overdue activity mitigation and recovery catch-up plan logged successfully.")
                .build();

        when(lookaheadScheduleService.recordOverdueMitigation(eq(1L), eq(101L), any(), any()))
                .thenReturn(response);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/v1/projects/1/lookahead/overdue/101/mitigation")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(101))
                .andExpect(jsonPath("$.data.delayReasonCategory").value("MATERIAL_SHORTAGE"))
                .andExpect(jsonPath("$.data.recordedBy").value("admin"));
    }
}
