package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;

public interface AuditService {
    void logAction(String username, String action, String entityType, String entityId, String details, String ipAddress);
    PageResponse<AuditLogResponse> getAuditLogs(int page, int size);
    PageResponse<AuditLogResponse> getAuditLogsByUser(String username, int page, int size);
    PageResponse<AuditLogResponse> getAuditLogsByEntity(String entityType, int page, int size);
}
