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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final Optional<UserRepository> userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this(notificationRepository, Optional.empty());
    }

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, Optional<UserRepository> userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository != null ? userRepository : Optional.empty();
    }

    @Override
    public Notification dispatchNotification(SendNotificationDto dto) {
        // Module 1: Role-Based Broadcast Resolution
        boolean isDirectTarget = dto.getRecipientUsername() != null 
                && !dto.getRecipientUsername().isBlank() 
                && !"ALL".equalsIgnoreCase(dto.getRecipientUsername());

        if (!isDirectTarget && dto.getTargetRole() != null && !dto.getTargetRole().isBlank()) {
            List<User> matchingUsers = userRepository.map(repo -> {
                String role = dto.getTargetRole();
                String prefixed = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                String unprefixed = role.startsWith("ROLE_") ? role.substring(5) : role;
                return repo.findActiveUsersByRole(unprefixed, prefixed);
            }).orElse(List.of());

            if (!matchingUsers.isEmpty()) {
                Notification firstSaved = null;
                for (User u : matchingUsers) {
                    Notification notif = Notification.builder()
                            .projectId(dto.getProjectId())
                            .recipientUsername(u.getUsername())
                            .targetRole(dto.getTargetRole())
                            .title(dto.getTitle())
                            .message(dto.getMessage())
                            .notificationType(dto.getNotificationType())
                            .priority(dto.getPriority())
                            .channel(dto.getChannel() != null ? dto.getChannel() : NotificationChannel.IN_APP)
                            .referenceModule(dto.getReferenceModule())
                            .referenceId(dto.getReferenceId())
                            .status(NotificationStatus.UNREAD)
                            .build();
                    Notification saved = notificationRepository.save(notif);
                    if (firstSaved == null) firstSaved = saved;
                }
                log.info("[ROLE BROADCAST DISPATCHED] Role: {} ({} active recipients) | Type: {} | Title: '{}'",
                        dto.getTargetRole(), matchingUsers.size(), dto.getNotificationType(), dto.getTitle());
                return firstSaved;
            }
        }

        // Direct user target or fallback broadcast
        Notification notification = Notification.builder()
                .projectId(dto.getProjectId())
                .recipientUsername(dto.getRecipientUsername() != null ? dto.getRecipientUsername() : "ALL")
                .targetRole(dto.getTargetRole())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .notificationType(dto.getNotificationType())
                .priority(dto.getPriority())
                .channel(dto.getChannel() != null ? dto.getChannel() : NotificationChannel.IN_APP)
                .referenceModule(dto.getReferenceModule())
                .referenceId(dto.getReferenceId())
                .status(NotificationStatus.UNREAD)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("[NOTIFICATION DISPATCHED] Type: {} | Priority: {} | Recipient: {} | Title: '{}'",
                saved.getNotificationType(), saved.getPriority(), saved.getRecipientUsername(), saved.getTitle());
        return saved;
    }

    @Override
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public int markAllAsRead(String username) {
        int count = notificationRepository.markAllAsReadForUser(username, NotificationStatus.READ);
        log.info("Marked {} notifications as READ for user '{}'", count, username);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUserInbox(String username, NotificationStatus status, Pageable pageable) {
        if (status != null) {
            return notificationRepository.findByRecipientUsernameAndStatus(username, status, pageable);
        }
        return notificationRepository.findByRecipientUsername(username, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUserInboxByPriority(String username, NotificationPriority priority, Pageable pageable) {
        if (priority != null) {
            return notificationRepository.findByRecipientUsernameAndPriority(username, priority, pageable);
        }
        return notificationRepository.findByRecipientUsername(username, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSummaryDto getUserSummary(String username) {
        long total = notificationRepository.countByRecipientUsername(username);
        long unread = notificationRepository.countByRecipientUsernameAndStatus(username, NotificationStatus.UNREAD);
        long urgent = notificationRepository.countUrgentUnread(username);
        long high = notificationRepository.countByRecipientUsernameAndPriority(username, NotificationPriority.HIGH);
        long lowStock = notificationRepository.countByRecipientUsernameAndNotificationType(username, NotificationType.LOW_STOCK);
        long blocker = notificationRepository.countByRecipientUsernameAndNotificationType(username, NotificationType.CRITICAL_BLOCKER);
        long budgetOverrun = notificationRepository.countByRecipientUsernameAndNotificationType(username, NotificationType.BUDGET_OVERRUN);

        return NotificationSummaryDto.builder()
                .totalNotifications(total)
                .unreadCount(unread)
                .urgentCount(urgent)
                .highCount(high)
                .lowStockCount(lowStock)
                .blockerCount(blocker)
                .budgetOverrunCount(budgetOverrun)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getProjectNotifications(Long projectId, NotificationType type) {
        if (type != null) {
            return notificationRepository.findByProjectIdAndNotificationType(projectId, type);
        }
        return notificationRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    @Override
    public Notification archiveNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        notification.setStatus(NotificationStatus.ARCHIVED);
        Notification saved = notificationRepository.save(notification);
        log.info("Archived notification ID: {} for audit compliance retention", id);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicateActiveAlert(Long projectId, NotificationType type, Long referenceId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return notificationRepository.existsActiveUnreadAlert(projectId, type, referenceId, since);
    }

    @Override
    public void deleteNotification(Long id) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        // Soft archive for 90-day compliance retention instead of hard deletion
        notif.setStatus(NotificationStatus.ARCHIVED);
        notificationRepository.save(notif);
        log.info("Soft-archived notification ID: {} for 90-day retention", id);
    }
}
