package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.audit.AuditStatsResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "11. Audit & History Logging", description = "Enterprise System Audit Trails, Compliance Logs & Operational Activity Tracking")
@SecurityRequirement(name = "BearerAuth")
public class AuditController {

    private final AuditService auditService;

    // =========================================================================
    // 1. LIST AUDIT LOGS (PAGINATED)
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List audit logs with pagination",
            description = "Retrieves all system activity audit entries ordered by latest first."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN, AUDITOR, or DIRECTOR role")
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs retrieved successfully"));
    }

    // =========================================================================
    // 2. GET AUDIT LOG BY ID
    // =========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Get audit log by ID",
            description = "Retrieves a single system audit trail entry by its primary key ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit log retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuditLogResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(
            @Parameter(description = "Audit Log ID", example = "1") @PathVariable Long id) {
        AuditLogResponse logResponse = auditService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(logResponse, "Audit log retrieved successfully"));
    }

    // =========================================================================
    // 3. LIST AUDIT LOGS BY USERNAME
    // =========================================================================

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List audit logs by username",
            description = "Retrieves all system actions performed by a specific user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User audit logs retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByUser(
            @Parameter(description = "Target username", example = "admin") @PathVariable String username,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogsByUser(username, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "User audit logs retrieved successfully"));
    }

    // =========================================================================
    // 4. LIST AUDIT LOGS BY ENTITY TYPE
    // =========================================================================

    @GetMapping("/entity/{entityType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List audit logs by entity type",
            description = "Retrieves audit trails for a specific entity type (e.g., USER, PROJECT, WBS_ACTIVITY, DPR, etc.)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Entity audit logs retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByEntity(
            @Parameter(description = "Entity Type (e.g. PROJECT, USER, WBS_ACTIVITY, DPR)", example = "PROJECT")
            @PathVariable String entityType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogsByEntity(entityType, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "Entity audit logs retrieved successfully"));
    }

    // =========================================================================
    // 5. LIST AUDIT LOGS BY ACTION
    // =========================================================================

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List audit logs by action name",
            description = "Retrieves all audit entries matching a specific business action (e.g. CREATE_PROJECT, APPROVE_DPR, RECALCULATE_CUMULATIVE_PROGRESS)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Action audit logs retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByAction(
            @Parameter(description = "Action name", example = "CREATE_PROJECT") @PathVariable String action,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogsByAction(action, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "Action audit logs retrieved successfully"));
    }

    // =========================================================================
    // 6. LIST AUDIT LOGS BY ENTITY TYPE AND ENTITY ID
    // =========================================================================

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List audit logs by entity type and ID",
            description = "Retrieves the complete audit history lifecycle of a specific entity record."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Entity record audit history retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByEntityAndId(
            @Parameter(description = "Entity Type (e.g. PROJECT, WBS_ACTIVITY)", example = "PROJECT") @PathVariable String entityType,
            @Parameter(description = "Entity primary key or identifier", example = "1") @PathVariable String entityId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogsByEntityAndId(entityType, entityId, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "Entity audit history retrieved successfully"));
    }

    // =========================================================================
    // 7. DYNAMIC MULTI-CRITERIA SEARCH
    // =========================================================================

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Search and filter audit logs dynamically",
            description = "Multi-criteria filter supporting username, action, entityType, entityId, date range, and free-text keyword search across details."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Filtered audit logs retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchAuditLogs(
            @Parameter(description = "Filter by username") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by action") @RequestParam(required = false) String action,
            @Parameter(description = "Filter by entity type") @RequestParam(required = false) String entityType,
            @Parameter(description = "Filter by entity ID") @RequestParam(required = false) String entityId,
            @Parameter(description = "Start Date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Free-text search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort property (e.g. createdAt, username, action)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "DESC") String direction) {

        PageResponse<AuditLogResponse> logs = auditService.searchAuditLogs(
                username, action, entityType, entityId, startDate, endDate, keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs search completed successfully"));
    }

    // =========================================================================
    // 8. AUDIT SUBSYSTEM STATISTICS & ANALYTICS
    // =========================================================================

    @GetMapping({"/stats", "/summary"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Get audit subsystem statistics and volume analytics",
            description = "Retrieves total audit count, today's audit events, unique user count, top action breakdown, and recent activities."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuditStatsResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<AuditStatsResponse>> getAuditStats() {
        AuditStatsResponse stats = auditService.getAuditStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Audit statistics retrieved successfully"));
    }
}
