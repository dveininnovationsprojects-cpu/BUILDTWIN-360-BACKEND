package com.example.BuildTwin._0.security;

import com.example.BuildTwin._0.controller.NotificationController;
import com.example.BuildTwin._0.domain.notifications.dto.NotificationSummaryDto;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationChannel;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class Epic6NotificationRbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // =========================================================================
    // 1. Unauthenticated Requests (401 Unauthorized)
    // =========================================================================

    @Test
    @DisplayName("Unauthenticated request to send notification should return 401")
    void testUnauthenticatedSendNotification() throws Exception {
        SendNotificationDto dto = SendNotificationDto.builder()
                .projectId(101L)
                .title("Alert")
                .message("Message")
                .notificationType(NotificationType.CRITICAL_BLOCKER)
                .priority(NotificationPriority.URGENT)
                .build();

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Unauthenticated request to user inbox should return 401")
    void testUnauthenticatedInbox() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/inbox/PM_Selvamani"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated request to mark read should return 401")
    void testUnauthenticatedMarkRead() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated request to delete notification should return 401")
    void testUnauthenticatedDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // =========================================================================
    // 2. Dispatch Notifications RBAC (ADMIN, PM, QUALITY_ENGINEER, SITE_ENGINEER)
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "PROJECT_MANAGER", "QUALITY_ENGINEER", "SITE_ENGINEER"})
    @DisplayName("Authorized roles can dispatch notifications (201 Created)")
    void testAuthorizedSendNotification(String role) throws Exception {
        SendNotificationDto dto = SendNotificationDto.builder()
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Low Stock")
                .message("Message")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .channel(NotificationChannel.IN_APP)
                .build();

        Notification notif = Notification.builder()
                .id(1L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Low Stock")
                .message("Message")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.dispatchNotification(any(SendNotificationDto.class))).thenReturn(notif);

        mockMvc.perform(post("/api/v1/notifications/send")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testUser").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONTRACTOR", "AUDITOR", "DATA_ANALYST", "PROCUREMENT_STORE", "QUANTITY_COST_COORDINATOR", "SITE_SUPERVISOR"})
    @DisplayName("Forbidden roles cannot dispatch notifications (403 Forbidden)")
    void testForbiddenSendNotification(String role) throws Exception {
        SendNotificationDto dto = SendNotificationDto.builder()
                .projectId(101L)
                .title("Unauthorized")
                .message("Message")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .build();

        mockMvc.perform(post("/api/v1/notifications/send")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testUser").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // =========================================================================
    // 3. Authenticated Inbox Access (Any Authenticated Role)
    // =========================================================================

    @Test
    @WithMockUser(username = "contractor_user", roles = {"CONTRACTOR"})
    @DisplayName("Contractor can access personal inbox")
    void testContractorInboxAccess() throws Exception {
        when(notificationService.getUserInbox(eq("contractor_user"), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/v1/notifications/inbox/contractor_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @WithMockUser(username = "contractor_user", roles = {"CONTRACTOR"})
    @DisplayName("Contractor can mark notification as read")
    void testContractorMarkRead() throws Exception {
        Notification notif = Notification.builder()
                .id(1L)
                .status(NotificationStatus.READ)
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(notif);

        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @WithMockUser(username = "site_sup", roles = {"SITE_SUPERVISOR"})
    @DisplayName("Site Supervisor can fetch notification summary counters")
    void testSiteSupervisorSummary() throws Exception {
        NotificationSummaryDto summary = NotificationSummaryDto.builder()
                .totalNotifications(5)
                .unreadCount(2)
                .urgentCount(1)
                .build();

        when(notificationService.getUserSummary("site_sup")).thenReturn(summary);

        mockMvc.perform(get("/api/v1/notifications/summary/site_sup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(2));
    }

    // =========================================================================
    // 4. Delete Notification RBAC (ADMIN, PROJECT_MANAGER only)
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "PROJECT_MANAGER"})
    @DisplayName("ADMIN and PROJECT_MANAGER can delete notification")
    void testAuthorizedDelete(String role) throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/v1/notifications/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("mgr").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SITE_ENGINEER", "SITE_SUPERVISOR", "CONTRACTOR", "QUALITY_ENGINEER", "AUDITOR"})
    @DisplayName("Unauthorized roles cannot delete notification (403 Forbidden)")
    void testForbiddenDelete(String role) throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("user").roles(role)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
