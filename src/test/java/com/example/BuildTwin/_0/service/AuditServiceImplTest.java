package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.audit.AuditStatsResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.AuditLog;
import com.example.BuildTwin._0.repository.AuditLogRepository;
import com.example.BuildTwin._0.service.impl.AuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = AuditLog.builder()
                .id(1L)
                .username("director_rajesh")
                .action("CREATE_PROJECT")
                .entityType("PROJECT")
                .entityId("101")
                .details("Created project Ashok Grandeur")
                .ipAddress("192.168.1.10")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("logAction with explicit parameters should save uppercase action and entityType")
    void testLogActionExplicit() {
        auditService.logAction("director_rajesh", "create_project", "project", "101", "Created project", "192.168.1.10");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog captured = captor.getValue();
        assertEquals("director_rajesh", captured.getUsername());
        assertEquals("CREATE_PROJECT", captured.getAction());
        assertEquals("PROJECT", captured.getEntityType());
        assertEquals("101", captured.getEntityId());
        assertEquals("Created project", captured.getDetails());
        assertEquals("192.168.1.10", captured.getIpAddress());
    }

    @Test
    @DisplayName("logAction with empty username should fallback to SYSTEM")
    void testLogActionFallbackUsername() {
        auditService.logAction("", null, null, "102", "Automated backup", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog captured = captor.getValue();
        assertEquals("SYSTEM", captured.getUsername());
        assertEquals("UNKNOWN", captured.getAction());
        assertEquals("GENERAL", captured.getEntityType());
    }

    @Test
    @DisplayName("logAction failure should be silently caught without throwing exception to caller")
    void testLogActionExceptionHandled() {
        doThrow(new RuntimeException("DB Connection down")).when(auditLogRepository).save(any(AuditLog.class));

        assertDoesNotThrow(() ->
                auditService.logAction("user1", "LOGIN", "AUTH", "1", "User login", "127.0.0.1")
        );
    }

    @Test
    @DisplayName("logAction overload without username/ip should resolve or default gracefully")
    void testLogActionOverload() {
        auditService.logAction("DELETE_TASK", "TASK", "555", "Deleted task item");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog captured = captor.getValue();
        assertEquals("SYSTEM", captured.getUsername());
        assertEquals("DELETE_TASK", captured.getAction());
        assertEquals("TASK", captured.getEntityType());
        assertEquals("555", captured.getEntityId());
    }

    @Test
    @DisplayName("getAuditLogById should return mapped AuditLogResponse when present")
    void testGetAuditLogByIdSuccess() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(sampleLog));

        AuditLogResponse response = auditService.getAuditLogById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("director_rajesh", response.getUsername());
        assertEquals("CREATE_PROJECT", response.getAction());
        assertEquals("PROJECT", response.getEntityType());
    }

    @Test
    @DisplayName("getAuditLogById should throw ResourceNotFoundException when log does not exist")
    void testGetAuditLogByIdNotFound() {
        when(auditLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditService.getAuditLogById(999L));
    }

    @Test
    @DisplayName("getAuditLogs should return paginated response")
    void testGetAuditLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditService.getAuditLogs(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("director_rajesh", result.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("getAuditLogsByUser should return logs filtered by user")
    void testGetAuditLogsByUser() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findByUsername(eq("director_rajesh"), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditService.getAuditLogsByUser("director_rajesh", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("director_rajesh", result.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("getAuditLogsByEntity should return logs filtered by entityType")
    void testGetAuditLogsByEntity() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findByEntityType(eq("PROJECT"), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditService.getAuditLogsByEntity("project", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("PROJECT", result.getContent().get(0).getEntityType());
    }

    @Test
    @DisplayName("getAuditLogsByAction should return logs filtered by action")
    void testGetAuditLogsByAction() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findByAction(eq("CREATE_PROJECT"), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditService.getAuditLogsByAction("create_project", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("CREATE_PROJECT", result.getContent().get(0).getAction());
    }

    @Test
    @DisplayName("getAuditLogsByEntityAndId should return logs filtered by entityType and entityId")
    void testGetAuditLogsByEntityAndId() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findByEntityTypeAndEntityId(eq("PROJECT"), eq("101"), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditService.getAuditLogsByEntityAndId("project", "101", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("101", result.getContent().get(0).getEntityId());
    }

    @Test
    @DisplayName("searchAuditLogs should query specification with provided parameters")
    void testSearchAuditLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> result = auditService.searchAuditLogs(
                "director_rajesh",
                "CREATE_PROJECT",
                "PROJECT",
                "101",
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                "Ashok",
                0,
                10,
                "createdAt",
                "DESC"
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(auditLogRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getAuditStats should calculate aggregates and top activity distributions")
    void testGetAuditStats() {
        when(auditLogRepository.count()).thenReturn(150L);
        when(auditLogRepository.countLogsSince(any(LocalDateTime.class))).thenReturn(25L);
        when(auditLogRepository.countDistinctUsers()).thenReturn(12L);

        List<Object[]> actions = new ArrayList<>();
        actions.add(new Object[]{"CREATE_PROJECT", 45L});
        actions.add(new Object[]{"UPDATE_TASK", 30L});
        when(auditLogRepository.countGroupByAction()).thenReturn(actions);

        List<Object[]> entities = new ArrayList<>();
        entities.add(new Object[]{"PROJECT", 60L});
        entities.add(new Object[]{"TASK", 40L});
        when(auditLogRepository.countGroupByEntityType()).thenReturn(entities);

        Page<AuditLog> recentPage = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(recentPage);

        AuditStatsResponse stats = auditService.getAuditStats();

        assertNotNull(stats);
        assertEquals(150L, stats.getTotalLogs());
        assertEquals(25L, stats.getTodayLogs());
        assertEquals(12L, stats.getUniqueUsersCount());
        assertEquals("HEALTHY", stats.getSystemHealth());
        assertEquals(2, stats.getTopActions().size());
        assertEquals(45L, stats.getTopActions().get("CREATE_PROJECT"));
        assertEquals(2, stats.getTopEntities().size());
        assertEquals(60L, stats.getTopEntities().get("PROJECT"));
        assertEquals(1, stats.getRecentActivities().size());
        assertNotNull(stats.getGeneratedAt());
    }
}
