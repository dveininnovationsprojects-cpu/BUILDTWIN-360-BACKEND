package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.lookahead.FourteenDayLookaheadResponse;
import com.example.BuildTwin._0.dto.lookahead.FourteenDayMetricsResponse;
import com.example.BuildTwin._0.dto.lookahead.LookaheadConstraintSummaryResponse;
import com.example.BuildTwin._0.dto.lookahead.LookaheadMetricsResponse;
import com.example.BuildTwin._0.service.LookaheadScheduleService;
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
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LookaheadScheduleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LookaheadScheduleService lookaheadScheduleService;

    @InjectMocks
    private LookaheadScheduleController lookaheadScheduleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(lookaheadScheduleController).build();
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
}
