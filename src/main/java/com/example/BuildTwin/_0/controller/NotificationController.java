package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.notifications.dto.NotificationSummaryDto;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationStatus;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.model.Notification;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "14. Notification & Communication Service", description = "Real-time alerts, inventory shortage alarms, SLA breach alerts, and scheduled jobs (Epic 6)")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'QUALITY_ENGINEER', 'SITE_ENGINEER')")
    @Operation(summary = "Dispatch System Alert / Notification", description = "Manually triggers a targeted alert for users or roles.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Notification>> sendNotification(@Valid @RequestBody SendNotificationDto dto) {
        Notification dispatched = notificationService.dispatchNotification(dto);
        return new ResponseEntity<>(ApiResponse.created(dispatched, "Notification dispatched successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/inbox/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get User Notification Inbox", description = "Fetches paginated user notifications with optional UNREAD or READ status filter.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Page<Notification>>> getUserInbox(
            @PathVariable String username,
            @RequestParam(required = false) NotificationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Notification> inbox = notificationService.getUserInbox(username, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(inbox, "User inbox fetched successfully"));
    }

    @GetMapping("/my-inbox")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Current User's Personal Inbox", description = "Fetches paginated notifications for the currently authenticated user.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Page<Notification>>> getMyInbox(
            Authentication authentication,
            @RequestParam(required = false) NotificationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        Page<Notification> inbox = notificationService.getUserInbox(username, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(inbox, "Personal inbox fetched successfully"));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark Notification as Read", description = "Transitions notification status from UNREAD to READ.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id) {
        Notification updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification marked as read"));
    }

    @PutMapping("/inbox/{username}/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications as read for a given user.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@PathVariable String username) {
        int count = notificationService.markAllAsRead(username);
        return ResponseEntity.ok(ApiResponse.success(count, count + " notifications marked as read"));
    }

    @GetMapping("/summary/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Notification Badge Counters", description = "Returns active unread and urgent counts for app header badge display.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<NotificationSummaryDto>> getUserSummary(@PathVariable String username) {
        NotificationSummaryDto summary = notificationService.getUserSummary(username);
        return ResponseEntity.ok(ApiResponse.success(summary, "Notification summary fetched successfully"));
    }

    @GetMapping("/my-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Current User's Notification Summary Badge", description = "Returns active unread and urgent counts for current user badge display.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<NotificationSummaryDto>> getMySummary(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        NotificationSummaryDto summary = notificationService.getUserSummary(username);
        return ResponseEntity.ok(ApiResponse.success(summary, "Personal notification summary fetched successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Project Notifications", description = "Fetches all notifications related to a specific project with optional type filter.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Notification>>> getProjectNotifications(
            @PathVariable Long projectId,
            @RequestParam(required = false) NotificationType type) {
        List<Notification> list = notificationService.getProjectNotifications(projectId, type);
        return ResponseEntity.ok(ApiResponse.success(list, "Project notifications fetched successfully"));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Archive Notification", description = "Moves notification to ARCHIVED status for 90-day compliance retention.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Notification>> archiveNotification(@PathVariable Long id) {
        Notification updated = notificationService.archiveNotification(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification moved to archive for compliance retention"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Delete / Archive Notification", description = "Moves notification record to ARCHIVED status for 90-day compliance retention.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification archived successfully for compliance retention"));
    }
}
