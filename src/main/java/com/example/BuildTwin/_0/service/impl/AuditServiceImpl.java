package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.model.AuditLog;
import com.example.BuildTwin._0.repository.AuditLogRepository;
import com.example.BuildTwin._0.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(String username, String action, String entityType, String entityId, String details, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username != null ? username : "SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit logged: user={}, action={}, entityType={}, entityId={}", username, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> pageResult = auditLogRepository.findAll(pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogsByUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> pageResult = auditLogRepository.findByUsername(username, pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogsByEntity(String entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> pageResult = auditLogRepository.findByEntityType(entityType, pageable);
        return mapToPageResponse(pageResult);
    }

    private PageResponse<AuditLogResponse> mapToPageResponse(Page<AuditLog> pageResult) {
        List<AuditLogResponse> content = pageResult.getContent().stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());

        return PageResponse.<AuditLogResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isFirst(pageResult.isFirst())
                .isLast(pageResult.isLast())
                .build();
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUsername())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
