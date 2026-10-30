package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.progress.*;
import com.example.BuildTwin._0.service.ProgressHistoryService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProgressHistoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProgressHistoryService progressHistoryService;

    @InjectMocks
    private ProgressHistoryController progressHistoryController;

    private ProgressHistoryItemResponse mockItem;
    private ActivityProgressHistoryResponse mockActHistory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(progressHistoryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockItem = ProgressHistoryItemResponse.builder()
                .id(101L)
                .activityId(15L)
                .activityCode("ACT-CIV-001")
                .activityName("Substructure Concrete Works")
                .discipline("CIVIL")
                .uom("CUM")
                .logDate(LocalDate.of(2026, 9, 28))
                .quantityCompletedToday(45.0)
                .cumulativeQuantityCompleted(220.0)
                .cumulativeProgressPercentage(44.0)
                .progressPercentageToday(9.0)
                .source("DPR")
                .dprId(12L)
                .dprReportCode("DPR-20260928-S1")
                .manpowerCount(14)
                .weatherCondition("CLEAR")
                .recordedBy("site_engineer_01")
                .remarks("Floor slab C1 poured")
                .build();

        mockActHistory = ActivityProgressHistoryResponse.builder()
                .activityId(15L)
                .activityCode("ACT-CIV-001")
                .activityName("Substructure Concrete Works")
                .discipline("CIVIL")
                .uom("CUM")
                .plannedQuantity(500.0)
                .completedQuantity(220.0)
                .balanceQuantity(280.0)
                .currentProgressPercentage(44.0)
                .status("IN_PROGRESS")
                .totalLogsCount(1)
                .totalQuantityLogged(45.0)
                .history(List.of(mockItem))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId}/progress-history should return 200 OK")
    void testGetActivityProgressHistory() throws Exception {
        when(progressHistoryService.getActivityProgressHistory(eq(15L), any(), any(), any(), any(), eq("desc")))
                .thenReturn(mockActHistory);

        mockMvc.perform(get("/api/v1/activities/15/progress-history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(15))
                .andExpect(jsonPath("$.data.activityCode").value("ACT-CIV-001"))
                .andExpect(jsonPath("$.data.plannedQuantity").value(500.0))
                .andExpect(jsonPath("$.data.completedQuantity").value(220.0))
                .andExpect(jsonPath("$.data.history[0].id").value(101))
                .andExpect(jsonPath("$.data.history[0].source").value("DPR"));
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId}/progress-history/analytics should return 200 OK")
    void testGetActivityProgressAnalytics() throws Exception {
        ProgressAnalyticsResponse analytics = ProgressAnalyticsResponse.builder()
                .activityId(15L)
                .activityCode("ACT-CIV-001")
                .plannedQuantity(500.0)
                .completedQuantity(220.0)
                .balanceQuantity(280.0)
                .progressPercentage(44.0)
                .trendStatus("ON_TRACK")
                .build();

        when(progressHistoryService.getActivityProgressAnalytics(15L)).thenReturn(analytics);

        mockMvc.perform(get("/api/v1/activities/15/progress-history/analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(15))
                .andExpect(jsonPath("$.data.trendStatus").value("ON_TRACK"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/progress-history should return 200 OK")
    void testGetProjectProgressHistory() throws Exception {
        ProjectProgressHistoryResponse projectHistory = ProjectProgressHistoryResponse.builder()
                .projectId(1L)
                .projectName("Skyline Residency")
                .projectCode("PRJ-BLR-001")
                .totalEntries(10L)
                .totalQuantityLogged(450.0)
                .uniqueActivitiesCount(5)
                .build();

        when(progressHistoryService.getProjectProgressHistory(
                eq(1L), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(projectHistory);

        mockMvc.perform(get("/api/v1/projects/1/progress-history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.projectCode").value("PRJ-BLR-001"))
                .andExpect(jsonPath("$.data.totalEntries").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/progress-history/{id} should return 200 OK")
    void testGetProgressHistoryItemById() throws Exception {
        when(progressHistoryService.getProgressHistoryItemById(101L)).thenReturn(mockItem);

        mockMvc.perform(get("/api/v1/progress-history/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.activityCode").value("ACT-CIV-001"));
    }

    @Test
    @DisplayName("POST /api/v1/activities/{activityId}/progress-history should return 201 Created")
    void testLogProgress() throws Exception {
        LogProgressHistoryRequest req = LogProgressHistoryRequest.builder()
                .logDate(LocalDate.of(2026, 9, 29))
                .quantityCompletedToday(30.0)
                .manpowerCount(12)
                .remarks("Poured column C3")
                .build();

        when(progressHistoryService.logProgress(eq(15L), any(LogProgressHistoryRequest.class), anyString()))
                .thenReturn(mockItem);

        mockMvc.perform(post("/api/v1/activities/15/progress-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101));
    }

    @Test
    @DisplayName("PUT /api/v1/progress-history/{id} should return 200 OK")
    void testUpdateProgressLog() throws Exception {
        UpdateProgressHistoryRequest req = UpdateProgressHistoryRequest.builder()
                .remarks("Updated handover notes")
                .manpowerCount(15)
                .build();

        when(progressHistoryService.updateProgressLog(eq(101L), any(UpdateProgressHistoryRequest.class), anyString()))
                .thenReturn(mockItem);

        mockMvc.perform(put("/api/v1/progress-history/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/progress-history/{id} should return 200 OK")
    void testDeleteProgressLog() throws Exception {
        doNothing().when(progressHistoryService).deleteProgressLog(eq(101L), anyString());

        mockMvc.perform(delete("/api/v1/progress-history/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Progress history entry ID 101 deleted and activity metrics recalculated successfully"));
    }
}
