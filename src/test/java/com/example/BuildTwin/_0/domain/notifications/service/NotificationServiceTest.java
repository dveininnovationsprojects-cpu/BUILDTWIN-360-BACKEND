package com.example.BuildTwin._0.domain.notifications.service;

import com.example.BuildTwin._0.domain.notifications.dto.NotificationSummaryDto;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationChannel;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import com.example.BuildTwin._0.domain.notifications.repository.NotificationRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .projectId(101L)
                .recipientUsername("PM_Selvamani")
                .targetRole("PROJECT_MANAGER")
                .title("Low Stock Warning")
                .message("Cement stock below reorder level")
                .notificationType(NotificationType.LOW_STOCK)
                .priority(NotificationPriority.HIGH)
                .status(NotificationStatus.UNREAD)
                .channel(NotificationChannel.IN_APP)
                .referenceModule("MATERIALS")
                .referenceId(5L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully dispatch notification")
    void testDispatchNotification() {
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

        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        Notification result = notificationService.dispatchNotification(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PM_Selvamani", result.getRecipientUsername());
        assertEquals(NotificationType.LOW_STOCK, result.getNotificationType());
        assertEquals(NotificationPriority.HIGH, result.getPriority());
        assertEquals(NotificationStatus.UNREAD, result.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should successfully mark notification as read")
    void testMarkAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.markAsRead(1L);

        assertNotNull(result);
        assertEquals(NotificationStatus.READ, result.getStatus());
        assertNotNull(result.getReadAt());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when marking non-existent notification as read")
    void testMarkAsReadNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(999L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark all notifications as read for a user")
    void testMarkAllAsRead() {
        when(notificationRepository.markAllAsReadForUser("PM_Selvamani", NotificationStatus.READ)).thenReturn(7);

        int updatedCount = notificationService.markAllAsRead("PM_Selvamani");

        assertEquals(7, updatedCount);
        verify(notificationRepository, times(1)).markAllAsReadForUser("PM_Selvamani", NotificationStatus.READ);
    }

    @Test
    @DisplayName("Should retrieve user inbox with status filter")
    void testGetUserInboxWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(sampleNotification), pageable, 1);

        when(notificationRepository.findByRecipientUsernameAndStatus("PM_Selvamani", NotificationStatus.UNREAD, pageable))
                .thenReturn(page);

        Page<Notification> result = notificationService.getUserInbox("PM_Selvamani", NotificationStatus.UNREAD, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Low Stock Warning", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Should retrieve user inbox without status filter")
    void testGetUserInboxWithoutStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(sampleNotification), pageable, 1);

        when(notificationRepository.findByRecipientUsername("PM_Selvamani", pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getUserInbox("PM_Selvamani", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository, times(1)).findByRecipientUsername("PM_Selvamani", pageable);
    }

    @Test
    @DisplayName("Should retrieve user notification summary counts")
    void testGetUserSummary() {
        when(notificationRepository.countByRecipientUsername("PM_Selvamani")).thenReturn(12L);
        when(notificationRepository.countByRecipientUsernameAndStatus("PM_Selvamani", NotificationStatus.UNREAD)).thenReturn(4L);
        when(notificationRepository.countUrgentUnread("PM_Selvamani")).thenReturn(2L);
        when(notificationRepository.countByRecipientUsernameAndPriority("PM_Selvamani", NotificationPriority.HIGH)).thenReturn(3L);
        when(notificationRepository.countByRecipientUsernameAndNotificationType("PM_Selvamani", NotificationType.LOW_STOCK)).thenReturn(2L);
        when(notificationRepository.countByRecipientUsernameAndNotificationType("PM_Selvamani", NotificationType.CRITICAL_BLOCKER)).thenReturn(1L);
        when(notificationRepository.countByRecipientUsernameAndNotificationType("PM_Selvamani", NotificationType.BUDGET_OVERRUN)).thenReturn(1L);

        NotificationSummaryDto summary = notificationService.getUserSummary("PM_Selvamani");

        assertNotNull(summary);
        assertEquals(12L, summary.getTotalNotifications());
        assertEquals(4L, summary.getUnreadCount());
        assertEquals(2L, summary.getUrgentCount());
        assertEquals(3L, summary.getHighCount());
        assertEquals(2L, summary.getLowStockCount());
        assertEquals(1L, summary.getBlockerCount());
        assertEquals(1L, summary.getBudgetOverrunCount());
    }

    @Test
    @DisplayName("Should soft-archive notification on delete for 90-day retention")
    void testDeleteNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.deleteNotification(1L);

        assertEquals(NotificationStatus.ARCHIVED, sampleNotification.getStatus());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    @DisplayName("Should retrieve project notifications filtered by type")
    void testGetProjectNotificationsWithType() {
        when(notificationRepository.findByProjectIdAndNotificationType(101L, NotificationType.LOW_STOCK))
                .thenReturn(List.of(sampleNotification));

        List<Notification> result = notificationService.getProjectNotifications(101L, NotificationType.LOW_STOCK);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationType.LOW_STOCK, result.get(0).getNotificationType());
        verify(notificationRepository, times(1)).findByProjectIdAndNotificationType(101L, NotificationType.LOW_STOCK);
    }

    @Test
    @DisplayName("Should retrieve project notifications without type filter (all)")
    void testGetProjectNotificationsAll() {
        when(notificationRepository.findByProjectIdOrderByCreatedAtDesc(101L))
                .thenReturn(List.of(sampleNotification));

        List<Notification> result = notificationService.getProjectNotifications(101L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByProjectIdOrderByCreatedAtDesc(101L);
    }

    @Test
    @DisplayName("Should retrieve user inbox filtered by priority")
    void testGetUserInboxByPriority() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(sampleNotification), pageable, 1);

        when(notificationRepository.findByRecipientUsernameAndPriority("PM_Selvamani", NotificationPriority.HIGH, pageable))
                .thenReturn(page);

        Page<Notification> result = notificationService.getUserInboxByPriority("PM_Selvamani", NotificationPriority.HIGH, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(NotificationPriority.HIGH, result.getContent().get(0).getPriority());
        verify(notificationRepository, times(1)).findByRecipientUsernameAndPriority("PM_Selvamani", NotificationPriority.HIGH, pageable);
    }

    @Test
    @DisplayName("Should successfully archive notification for audit compliance retention")
    void testArchiveNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.archiveNotification(1L);

        assertNotNull(result);
        assertEquals(NotificationStatus.ARCHIVED, result.getStatus());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    @DisplayName("Should verify active unread duplicate alert within 24 hours")
    void testIsDuplicateActiveAlert() {
        when(notificationRepository.existsActiveUnreadAlert(eq(101L), eq(NotificationType.LOW_STOCK), eq(5L), any(LocalDateTime.class)))
                .thenReturn(true);

        boolean isDuplicate = notificationService.isDuplicateActiveAlert(101L, NotificationType.LOW_STOCK, 5L);

        assertTrue(isDuplicate);
        verify(notificationRepository, times(1))
                .existsActiveUnreadAlert(eq(101L), eq(NotificationType.LOW_STOCK), eq(5L), any(LocalDateTime.class));
    }
}
