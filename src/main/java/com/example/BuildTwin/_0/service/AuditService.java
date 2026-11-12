package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.audit.AuditStatsResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;

import java.time.LocalDate;

public interface AuditService {

    /**
     * Records a system audit entry with full user, action, entity, details and client IP parameters.
     */
    void logAction(String username, String action, String entityType, String entityId, String details, String ipAddress);

    /**
     * Convenience method to record audit action with automatic username and IP resolution from context.
     */
    void logAction(String action, String entityType, String entityId, String details);

    /**
     * Retrieves single audit log record by its ID.
     */
    AuditLogResponse getAuditLogById(Long id);

    /**
     * Lists paginated audit logs ordered by creation timestamp descending.
     */
    PageResponse<AuditLogResponse> getAuditLogs(int page, int size);

    /**
     * Retrieves paginated audit logs filtered by username.
     */
    PageResponse<AuditLogResponse> getAuditLogsByUser(String username, int page, int size);

    /**
     * Retrieves paginated audit logs filtered by target entity type (e.g. PROJECT, WBS, DPR).
     */
    PageResponse<AuditLogResponse> getAuditLogsByEntity(String entityType, int page, int size);

    /**
     * Retrieves paginated audit logs filtered by action name (e.g. CREATE_PROJECT, APPROVE_DPR).
     */
    PageResponse<AuditLogResponse> getAuditLogsByAction(String action, int page, int size);

    /**
     * Retrieves paginated audit logs filtered by entity type and specific entity record ID.
     */
    PageResponse<AuditLogResponse> getAuditLogsByEntityAndId(String entityType, String entityId, int page, int size);

    /**
     * Dynamic multi-criteria search and filter across audit trails with custom pagination & sorting.
     */
    PageResponse<AuditLogResponse> searchAuditLogs(
            String username,
            String action,
            String entityType,
            String entityId,
            LocalDate startDate,
            LocalDate endDate,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction);

    /**
     * Returns comprehensive audit subsystem health, volume statistics, and activity breakdown.
     */
    AuditStatsResponse getAuditStats();
}
