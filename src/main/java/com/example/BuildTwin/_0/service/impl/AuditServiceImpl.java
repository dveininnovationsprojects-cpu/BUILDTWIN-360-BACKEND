package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.audit.AuditStatsResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.AuditLog;
import com.example.BuildTwin._0.repository.AuditLogRepository;
import com.example.BuildTwin._0.service.AuditService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
                    .username(username != null && !username.isBlank() ? username : "SYSTEM")
                    .action(action != null ? action.toUpperCase() : "UNKNOWN")
                    .entityType(entityType != null ? entityType.toUpperCase() : "GENERAL")
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit logged: user={}, action={}, entityType={}, entityId={}", username, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logAction(String action, String entityType, String entityId, String details) {
        String username = resolveCurrentUsername();
        String ipAddress = resolveClientIp();
        logAction(username, action, entityType, entityId, details, ipAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long id) {
        log.info("Fetching audit log by id={}", id);
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return mapToAuditLogResponse(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(int page, int size) {
        Pageable pageable = createPageable(page, size, "createdAt", "DESC");
        Page<AuditLog> pageResult = auditLogRepository.findAll(pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogsByUser(String username, int page, int size) {
        Pageable pageable = createPageable(page, size, "createdAt", "DESC");
        Page<AuditLog> pageResult = auditLogRepository.findByUsername(username, pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogsByEntity(String entityType, int page, int size) {
        Pageable pageable = createPageable(page, size, "createdAt", "DESC");
        Page<AuditLog> pageResult = auditLogRepository.findByEntityType(entityType != null ? entityType.toUpperCase() : null, pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogsByAction(String action, int page, int size) {
        Pageable pageable = createPageable(page, size, "createdAt", "DESC");
        Page<AuditLog> pageResult = auditLogRepository.findByAction(action != null ? action.toUpperCase() : null, pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogsByEntityAndId(String entityType, String entityId, int page, int size) {
        Pageable pageable = createPageable(page, size, "createdAt", "DESC");
        Page<AuditLog> pageResult = auditLogRepository.findByEntityTypeAndEntityId(
                entityType != null ? entityType.toUpperCase() : null, entityId, pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> searchAuditLogs(
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
            String direction) {

        log.info("Searching audit logs with criteria: user={}, action={}, entityType={}, entityId={}, keyword={}",
                username, action, entityType, entityId, keyword);

        Pageable pageable = createPageable(page, size, sortBy, direction);

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (username != null && !username.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("username")), username.toLowerCase().trim()));
            }

            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("action")), action.toUpperCase().trim()));
            }

            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("entityType")), entityType.toUpperCase().trim()));
            }

            if (entityId != null && !entityId.isBlank()) {
                predicates.add(cb.equal(root.get("entityId"), entityId.trim()));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }

            if (keyword != null && !keyword.isBlank()) {
                String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate matchUser = cb.like(cb.lower(root.get("username")), searchPattern);
                Predicate matchAction = cb.like(cb.lower(root.get("action")), searchPattern);
                Predicate matchEntity = cb.like(cb.lower(root.get("entityType")), searchPattern);
                Predicate matchDetails = cb.like(cb.lower(root.get("details")), searchPattern);
                predicates.add(cb.or(matchUser, matchAction, matchEntity, matchDetails));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> pageResult = auditLogRepository.findAll(spec, pageable);
        return mapToPageResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditStatsResponse getAuditStats() {
        log.info("Computing audit log statistics and activity breakdown");

        long totalLogs = auditLogRepository.count();
        long todayLogs = auditLogRepository.countLogsSince(LocalDate.now().atStartOfDay());
        long uniqueUsers = auditLogRepository.countDistinctUsers();

        Map<String, Long> topActions = new LinkedHashMap<>();
        List<Object[]> actionsGrouped = auditLogRepository.countGroupByAction();
        for (int i = 0; i < Math.min(10, actionsGrouped.size()); i++) {
            Object[] row = actionsGrouped.get(i);
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                topActions.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> topEntities = new LinkedHashMap<>();
        List<Object[]> entitiesGrouped = auditLogRepository.countGroupByEntityType();
        for (int i = 0; i < Math.min(10, entitiesGrouped.size()); i++) {
            Object[] row = entitiesGrouped.get(i);
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                topEntities.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        Pageable recentPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<AuditLogResponse> recent = auditLogRepository.findAll(recentPageable).getContent()
                .stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());

        return AuditStatsResponse.builder()
                .totalLogs(totalLogs)
                .todayLogs(todayLogs)
                .uniqueUsersCount(uniqueUsers)
                .topActions(topActions)
                .topEntities(topEntities)
                .recentActivities(recent)
                .systemHealth("HEALTHY")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private Pageable createPageable(int page, int size, String sortBy, String direction) {
        String cleanSort = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(sortDirection, cleanSort));
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

    private String resolveCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception ignored) {
        }
        return "SYSTEM";
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
