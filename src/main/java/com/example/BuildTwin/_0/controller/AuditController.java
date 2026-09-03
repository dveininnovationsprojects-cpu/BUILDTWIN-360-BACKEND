package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "1. Identity & Access Management", description = "System Audit Trails, Security Logs & Activity Tracking")
@SecurityRequirement(name = "BearerAuth")
public class AuditController {

    private final AuditService auditService;

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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or AUDITOR role")
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs retrieved successfully"));
    }

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
            @PathVariable String username,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogsByUser(username, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "User audit logs retrieved successfully"));
    }

    @GetMapping("/entity/{entityType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List audit logs by entity type",
            description = "Retrieves audit trails for a specific entity type (e.g., USER, ROLE, USER_PROJECT_ROLE, DPR, etc.)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Entity audit logs retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAuditLogsByEntity(entityType, page, size);
        return ResponseEntity.ok(ApiResponse.success(logs, "Entity audit logs retrieved successfully"));
    }
}
