package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.notifications.dto.NotificationSummaryDto;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationChannel;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/notifications/send - Dispatch Notification")
    void testSendNotification() throws Exception {
        SendNotificationDto dto = SendNotificationDto.builder()
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .targetRole("PROJECT_MANAGER")
                .title("Low Stock Warning")
                .message("Cement stock below reorder level")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .channel(NotificationChannel.IN_APP)
                .referenceModule("MATERIALS")
                .referenceId(5L)
                .build();

        Notification response = Notification.builder()
                .id(1L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Low Stock Warning")
                .message("Cement stock below reorder level")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.dispatchNotification(any(SendNotificationDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Low Stock Warning"))
                .andExpect(jsonPath("$.data.status").value("UNREAD"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/inbox/{username} - Get User Inbox")
    void testGetUserInbox() throws Exception {
        Notification notif = Notification.builder()
                .id(1L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Alert")
                .message("Message")
                .notificationType(NotificationType.CRITICAL_BLOCKER)
                .priority(NotificationPriority.URGENT)
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationService.getUserInbox(eq("PM_Selvamani"), eq(NotificationStatus.UNREAD), any()))
                .thenReturn(new PageImpl<>(List.of(notif), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/notifications/inbox/PM_Selvamani")
                        .param("status", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].recipientUsername").value("PM_Selvamani"));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read - Mark Notification As Read")
    void testMarkAsRead() throws Exception {
        Notification response = Notification.builder()
                .id(1L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Alert")
                .message("Message")
                .notificationType(NotificationType.CRITICAL_BLOCKER)
                .priority(NotificationPriority.URGENT)
                .status(NotificationStatus.READ)
                .readAt(LocalDateTime.now())
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("READ"));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/inbox/{username}/read-all - Mark All Read")
    void testMarkAllAsRead() throws Exception {
        when(notificationService.markAllAsRead("PM_Selvamani")).thenReturn(5);

        mockMvc.perform(put("/api/v1/notifications/inbox/PM_Selvamani/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/summary/{username} - Get Summary")
    void testGetUserSummary() throws Exception {
        NotificationSummaryDto summary = NotificationSummaryDto.builder()
                .totalNotifications(15)
                .unreadCount(5)
                .urgentCount(2)
                .highCount(4)
                .lowStockCount(3)
                .blockerCount(1)
                .budgetOverrunCount(2)
                .build();

        when(notificationService.getUserSummary("PM_Selvamani")).thenReturn(summary);

        mockMvc.perform(get("/api/v1/notifications/summary/PM_Selvamani"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(5))
                .andExpect(jsonPath("$.data.urgentCount").value(2));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} - Delete Notification")
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Notification archived successfully for compliance retention"));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/archive - Archive Notification")
    void testArchiveNotification() throws Exception {
        Notification archived = Notification.builder()
                .id(1L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Low Stock Warning")
                .message("Message")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .status(NotificationStatus.ARCHIVED)
                .build();

        when(notificationService.archiveNotification(1L)).thenReturn(archived);

        mockMvc.perform(put("/api/v1/notifications/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/my-inbox - Get Authenticated User Personal Inbox")
    void testGetMyInbox() throws Exception {
        Notification notif = Notification.builder()
                .id(2L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Low Stock Warning")
                .message("Message")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationService.getUserInbox(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(notif), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/notifications/my-inbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/my-summary - Get Authenticated User Personal Summary")
    void testGetMySummary() throws Exception {
        NotificationSummaryDto summary = NotificationSummaryDto.builder()
                .totalNotifications(8)
                .unreadCount(3)
                .urgentCount(1)
                .build();

        when(notificationService.getUserSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/notifications/my-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/project/{projectId} - Get Project Notifications")
    void testGetProjectNotifications() throws Exception {
        Notification notif = Notification.builder()
                .id(3L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .title("Budget Overrun Alert")
                .message("Over budget")
                .notificationType(NotificationType.BUDGET_OVERRUN)
                .priority(NotificationPriority.URGENT)
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationService.getProjectNotifications(eq(101L), eq(NotificationType.BUDGET_OVERRUN)))
                .thenReturn(List.of(notif));

        mockMvc.perform(get("/api/v1/notifications/project/101")
                        .param("type", "BUDGET_OVERRUN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(3))
                .andExpect(jsonPath("$.data[0].notificationType").value("BUDGET_OVERRUN"));
    }
}
