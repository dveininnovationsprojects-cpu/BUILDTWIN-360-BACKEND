package com.example.BuildTwin._0.domain.notifications.dto;

import com.example.BuildTwin._0.domain.notifications.enums.NotificationChannel;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to dispatch an in-app or system notification")
public class SendNotificationDto {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Associated Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Specific recipient username (null or ALL for role broadcast)", example = "PM_Selvamani")
    private String recipientUsername;

    @Schema(description = "Target RBAC role for broadcast", example = "PROJECT_MANAGER")
    private String targetRole;

    @NotBlank(message = "Title is required")
    @Schema(description = "Alert notification title", example = "SLA Breach Alert: Ticket ISS-101")
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Detailed alert content", example = "Issue assigned to Contractor is past SLA target resolution date.")
    private String message;

    @NotNull(message = "Notification type is required")
    @Schema(description = "Categorized notification type", example = "OVERDUE_SLA")
    private NotificationType notificationType;

    @NotNull(message = "Priority is required")
    @Schema(description = "Priority level", example = "URGENT")
    private NotificationPriority priority;

    @Builder.Default
    @Schema(description = "Delivery channel", example = "IN_APP")
    private NotificationChannel channel = NotificationChannel.IN_APP;

    @Schema(description = "Originating module reference", example = "QUALITY_ISSUE")
    private String referenceModule;

    @Schema(description = "Originating entity ID", example = "101")
    private Long referenceId;
}
