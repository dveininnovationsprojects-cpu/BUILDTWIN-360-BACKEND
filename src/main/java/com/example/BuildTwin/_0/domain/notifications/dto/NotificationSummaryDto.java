package com.example.BuildTwin._0.domain.notifications.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated notification metrics and badge counters")
public class NotificationSummaryDto {

    @Schema(description = "Total notifications for user", example = "42")
    private long totalNotifications;

    @Schema(description = "Count of unread notifications", example = "5")
    private long unreadCount;

    @Schema(description = "Count of urgent unread notifications", example = "2")
    private long urgentCount;

    @Schema(description = "Count of high priority notifications", example = "4")
    private long highCount;

    @Schema(description = "Count of low stock / shortage alerts", example = "3")
    private long lowStockCount;

    @Schema(description = "Count of critical blocker alerts", example = "1")
    private long blockerCount;

    @Schema(description = "Count of budget overrun alerts", example = "2")
    private long budgetOverrunCount;
}
