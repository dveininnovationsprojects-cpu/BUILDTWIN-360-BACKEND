package com.example.BuildTwin._0.domain.notifications.model;

import com.example.BuildTwin._0.domain.notifications.enums.NotificationChannel;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_recipient", columnList = "recipient_username, status"),
        @Index(name = "idx_notif_project_created", columnList = "project_id, created_at"),
        @Index(name = "idx_notif_type", columnList = "notification_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "In-App Notification and System Alert record")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotBlank(message = "Recipient username is required")
    @Column(name = "recipient_username", nullable = false, length = 100)
    private String recipientUsername; // e.g. "PM_Selvamani" or "ALL_MANAGERS"

    @Column(name = "target_role", length = 50)
    private String targetRole; // e.g. "PROJECT_MANAGER", "SITE_ENGINEER"

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Message content is required")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType notificationType; 
    // LOW_STOCK, CRITICAL_BLOCKER, OVERDUE_SLA, BUDGET_OVERRUN, NCR_FAILED, GENERAL_INFO

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority; 
    // LOW, MEDIUM, HIGH, URGENT

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD; 
    // UNREAD, READ, DISMISSED, ARCHIVED

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 30)
    private NotificationChannel channel = NotificationChannel.IN_APP; 
    // IN_APP, EMAIL, SMS, WEBSOCKET

    @Column(name = "reference_module", length = 50)
    private String referenceModule; // "PROCUREMENT", "COST_BUDGET", "QUALITY_ISSUE"

    @Column(name = "reference_id")
    private Long referenceId; // e.g., Issue ID, PO ID, or Budget ID

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
