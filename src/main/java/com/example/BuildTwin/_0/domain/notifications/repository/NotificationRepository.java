package com.example.BuildTwin._0.domain.notifications.repository;

import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String username);

    Page<Notification> findByRecipientUsernameAndStatus(String username, NotificationStatus status, Pageable pageable);

    Page<Notification> findByRecipientUsername(String username, Pageable pageable);

    List<Notification> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Notification> findByProjectIdAndNotificationType(Long projectId, NotificationType type);

    List<Notification> findByProjectIdAndPriority(Long projectId, NotificationPriority priority);

    Page<Notification> findByRecipientUsernameAndPriority(String username, NotificationPriority priority, Pageable pageable);

    long countByRecipientUsernameAndStatus(String username, NotificationStatus status);

    long countByRecipientUsername(String username);

    long countByRecipientUsernameAndPriority(String username, NotificationPriority priority);

    long countByRecipientUsernameAndNotificationType(String username, NotificationType type);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientUsername = :username AND n.status = 'UNREAD'")
    int markAllAsReadForUser(@Param("username") String username, @Param("status") NotificationStatus status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUsername = :username AND n.priority = 'URGENT' AND n.status = 'UNREAD'")
    long countUrgentUnread(@Param("username") String username);

    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.projectId = :projectId AND n.notificationType = :type " +
           "AND (:referenceId IS NULL OR n.referenceId = :referenceId) " +
           "AND n.status = 'UNREAD' AND n.createdAt >= :since")
    boolean existsActiveUnreadAlert(
            @Param("projectId") Long projectId,
            @Param("type") NotificationType type,
            @Param("referenceId") Long referenceId,
            @Param("since") java.time.LocalDateTime since
    );
}
