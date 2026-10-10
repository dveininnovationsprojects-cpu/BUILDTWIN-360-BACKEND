package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.alert.*;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.service.ScheduleDelayAlertService;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScheduleDelayAlertControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ScheduleDelayAlertService scheduleDelayAlertService;

    @InjectMocks
    private ScheduleDelayAlertController scheduleDelayAlertController;

    private ScheduleDelayAlertResponse mockAlertResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleDelayAlertController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockAlertResponse = ScheduleDelayAlertResponse.builder()
                .id(501L)
                .alertCode("ALT-PADUR-001-OVERDU")
                .projectId(1L)
                .projectCode("PADUR-AG-01")
                .projectName("Ashok Grandeur - Padur, Chennai")
                .activityId(101L)
                .activityCode("ACT-CIV-001")
                .activityName("Substructure Concrete Pour")
                .discipline("CIVIL")
                .alertType(DelayAlertType.OVERDUE_FINISH)
                .severity(OverdueSeverity.CRITICAL)
                .status(DelayAlertStatus.ACTIVE)
                .agingBucket(OverdueAgingBucket.DAYS_15_TO_30)
                .delayDays(20L)
                .progressSlippagePercentage(70.0)
                .currentProgressPercentage(30.0)
                .expectedProgressPercentage(100.0)
                .impactedSuccessorCount(2)
                .title("[CRITICAL] OVERDUE_FINISH: Substructure Concrete Pour (ACT-CIV-001)")
                .triggeredAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/schedule-alerts/scan should trigger delay scan")
    void testScanAndGenerateAlerts() throws Exception {
        ScanDelayAlertsResultResponse mockScanResult = ScanDelayAlertsResultResponse.builder()
                .projectId(1L)
                .projectCode("PADUR-AG-01")
                .projectName("Ashok Grandeur - Padur, Chennai")
                .asOfDate(LocalDate.of(2026, 10, 10))
                .totalActivitiesScanned(15)
                .newAlertsCreated(2)
                .existingAlertsUpdated(1)
                .alertsAutoResolved(0)
                .totalActiveAlerts(3)
                .scanTimestamp(LocalDateTime.now())
                .summary("Scan completed: 3 active alerts")
                .alerts(List.of(mockAlertResponse))
                .build();

        when(scheduleDelayAlertService.scanAndGenerateAlerts(eq(1L), any(), any()))
                .thenReturn(mockScanResult);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/v1/projects/1/schedule-alerts/scan")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ScanDelayAlertsRequest.builder().asOfDate(LocalDate.of(2026, 10, 10)).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.newAlertsCreated").value(2))
                .andExpect(jsonPath("$.data.totalActiveAlerts").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/schedule-alerts should return filtered alerts")
    void testGetAlerts() throws Exception {
        when(scheduleDelayAlertService.getAlerts(eq(1L), eq(DelayAlertStatus.ACTIVE), eq(OverdueSeverity.CRITICAL), any(), any(), any(), any()))
                .thenReturn(List.of(mockAlertResponse));

        mockMvc.perform(get("/api/v1/projects/1/schedule-alerts")
                        .param("status", "ACTIVE")
                        .param("severity", "CRITICAL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(501))
                .andExpect(jsonPath("$.data[0].alertCode").value("ALT-PADUR-001-OVERDU"))
                .andExpect(jsonPath("$.data[0].severity").value("CRITICAL"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/schedule-alerts/summary should return executive summary")
    void testGetAlertSummary() throws Exception {
        ScheduleDelayAlertSummaryResponse mockSummary = ScheduleDelayAlertSummaryResponse.builder()
                .projectId(1L)
                .projectCode("PADUR-AG-01")
                .projectName("Ashok Grandeur - Padur, Chennai")
                .asOfDate(LocalDate.of(2026, 10, 10))
                .totalAlerts(5)
                .activeAlerts(3)
                .criticalAlerts(2)
                .highAlerts(1)
                .averageDelayDays(14.5)
                .maxDelayDays(22L)
                .totalImpactedSuccessors(4)
                .build();

        when(scheduleDelayAlertService.getAlertSummary(eq(1L), any()))
                .thenReturn(mockSummary);

        mockMvc.perform(get("/api/v1/projects/1/schedule-alerts/summary")
                        .param("asOfDate", "2026-10-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.totalAlerts").value(5))
                .andExpect(jsonPath("$.data.criticalAlerts").value(2));
    }

    @Test
    @DisplayName("PUT /api/v1/schedule-alerts/{id}/acknowledge should acknowledge alert")
    void testAcknowledgeAlert() throws Exception {
        AcknowledgeAlertRequest req = AcknowledgeAlertRequest.builder()
                .notes("Reviewed by site team. Reinforcement team deployed.")
                .targetResolutionDate(LocalDate.of(2026, 10, 25))
                .build();

        ScheduleDelayAlertResponse acknowledgedResponse = mockAlertResponse;
        acknowledgedResponse.setStatus(DelayAlertStatus.ACKNOWLEDGED);
        acknowledgedResponse.setIsAcknowledged(true);
        acknowledgedResponse.setAcknowledgedBy("site_engineer");

        when(scheduleDelayAlertService.acknowledgeAlert(eq(501L), any(), any()))
                .thenReturn(acknowledgedResponse);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("site_engineer");

        mockMvc.perform(put("/api/v1/schedule-alerts/501/acknowledge")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.data.isAcknowledged").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/schedule-alerts/{id}/mitigate should log recovery catch-up plan")
    void testMitigateAlert() throws Exception {
        MitigateAlertRequest req = MitigateAlertRequest.builder()
                .delayReasonCategory("MATERIAL_SHORTAGE")
                .delayReasonDetails("Cement delivery halted")
                .recoveryPlanAction("Mobilized alternate supplier and night shift")
                .additionalLaborAllocated(6)
                .build();

        ScheduleDelayAlertResponse mitigatedResponse = mockAlertResponse;
        mitigatedResponse.setStatus(DelayAlertStatus.IN_MITIGATION);
        mitigatedResponse.setMitigationAction("Mobilized alternate supplier and night shift");

        when(scheduleDelayAlertService.mitigateAlert(eq(501L), any(), any()))
                .thenReturn(mitigatedResponse);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("project_manager");

        mockMvc.perform(put("/api/v1/schedule-alerts/501/mitigate")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_MITIGATION"));
    }

    @Test
    @DisplayName("PUT /api/v1/schedule-alerts/{id}/resolve should mark alert resolved")
    void testResolveAlert() throws Exception {
        ResolveAlertRequest req = ResolveAlertRequest.builder()
                .resolutionNotes("All backlog recovered. Concrete pour finished 100%.")
                .build();

        ScheduleDelayAlertResponse resolvedResponse = mockAlertResponse;
        resolvedResponse.setStatus(DelayAlertStatus.RESOLVED);
        resolvedResponse.setIsResolved(true);

        when(scheduleDelayAlertService.resolveAlert(eq(501L), any(), any()))
                .thenReturn(resolvedResponse);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("site_engineer");

        mockMvc.perform(put("/api/v1/schedule-alerts/501/resolve")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("DELETE /api/v1/schedule-alerts/{id} should delete alert")
    void testDeleteAlert() throws Exception {
        doNothing().when(scheduleDelayAlertService).deleteAlert(eq(501L), any());

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        mockMvc.perform(delete("/api/v1/schedule-alerts/501")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(scheduleDelayAlertService).deleteAlert(eq(501L), eq("admin"));
    }
}
