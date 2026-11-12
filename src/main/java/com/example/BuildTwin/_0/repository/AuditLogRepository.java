package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startOfDay")
    long countLogsSince(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(DISTINCT a.username) FROM AuditLog a")
    long countDistinctUsers();

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countGroupByAction();

    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a GROUP BY a.entityType ORDER BY COUNT(a) DESC")
    List<Object[]> countGroupByEntityType();
}
