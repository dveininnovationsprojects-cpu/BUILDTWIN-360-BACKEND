package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.audit.AuditStatsResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.service.AuditService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    private AuditLogResponse mockLog;
    private PageResponse<AuditLogResponse> mockPage;
    private AuditStatsResponse mockStats;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockLog = AuditLogResponse.builder()
                .id(1L)
                .username("admin")
                .action("CREATE_PROJECT")
                .entityType("PROJECT")
                .entityId("101")
                .details("Created project Ashok Grandeur")
                .ipAddress("127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();

        mockPage = PageResponse.<AuditLogResponse>builder()
                .content(List.of(mockLog))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        mockStats = AuditStatsResponse.builder()
                .totalLogs(100L)
                .todayLogs(12L)
                .uniqueUsersCount(8L)
                .topActions(Map.of("CREATE_PROJECT", 20L, "APPROVE_DPR", 15L))
                .topEntities(Map.of("PROJECT", 30L, "WBS_ACTIVITY", 25L))
                .recentActivities(List.of(mockLog))
                .systemHealth("HEALTHY")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs should return paginated audit logs")
    void testGetAuditLogs() throws Exception {
        when(auditService.getAuditLogs(eq(0), eq(20))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/audit-logs?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"))
                .andExpect(jsonPath("$.data.content[0].action").value("CREATE_PROJECT"));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/{id} should return single audit log")
    void testGetAuditLogById() throws Exception {
        when(auditService.getAuditLogById(eq(1L))).thenReturn(mockLog);

        mockMvc.perform(get("/api/v1/audit-logs/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.entityType").value("PROJECT"));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/user/{username} should return logs by user")
    void testGetAuditLogsByUser() throws Exception {
        when(auditService.getAuditLogsByUser(eq("admin"), eq(0), eq(20))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/audit-logs/user/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/entity/{entityType} should return logs by entity type")
    void testGetAuditLogsByEntity() throws Exception {
        when(auditService.getAuditLogsByEntity(eq("PROJECT"), eq(0), eq(20))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/audit-logs/entity/PROJECT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entityType").value("PROJECT"));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/action/{action} should return logs by action")
    void testGetAuditLogsByAction() throws Exception {
        when(auditService.getAuditLogsByAction(eq("CREATE_PROJECT"), eq(0), eq(20))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/audit-logs/action/CREATE_PROJECT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].action").value("CREATE_PROJECT"));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/entity/{entityType}/{entityId} should return logs by entity ID")
    void testGetAuditLogsByEntityAndId() throws Exception {
        when(auditService.getAuditLogsByEntityAndId(eq("PROJECT"), eq("101"), eq(0), eq(20))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/audit-logs/entity/PROJECT/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entityId").value("101"));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/search should return filtered audit logs")
    void testSearchAuditLogs() throws Exception {
        when(auditService.searchAuditLogs(
                any(), any(), any(), any(), any(), any(), any(), eq(0), eq(20), eq("createdAt"), eq("DESC")))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/audit-logs/search?keyword=Ashok")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/stats should return subsystem statistics")
    void testGetAuditStats() throws Exception {
        when(auditService.getAuditStats()).thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/audit-logs/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalLogs").value(100))
                .andExpect(jsonPath("$.data.todayLogs").value(12))
                .andExpect(jsonPath("$.data.uniqueUsersCount").value(8))
                .andExpect(jsonPath("$.data.systemHealth").value("HEALTHY"));
    }
}
