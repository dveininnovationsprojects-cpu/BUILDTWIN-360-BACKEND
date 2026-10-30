package com.example.BuildTwin._0.domain.notifications.service;

import com.example.BuildTwin._0.domain.notifications.dto.NotificationSummaryDto;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Notification dispatchNotification(SendNotificationDto dto);

    Notification markAsRead(Long id);

    int markAllAsRead(String username);

    Page<Notification> getUserInbox(String username, NotificationStatus status, Pageable pageable);

    Page<Notification> getUserInboxByPriority(String username, NotificationPriority priority, Pageable pageable);

    NotificationSummaryDto getUserSummary(String username);

    List<Notification> getProjectNotifications(Long projectId, NotificationType type);

    Notification archiveNotification(Long id);

    boolean isDuplicateActiveAlert(Long projectId, NotificationType type, Long referenceId);

    void deleteNotification(Long id);
}
