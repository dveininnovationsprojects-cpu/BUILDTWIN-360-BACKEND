package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import com.example.BuildTwin._0.model.DprActivityItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprActivityItemRepository extends JpaRepository<DprActivityItem, Long> {

    List<DprActivityItem> findByDprId(Long dprId);

    List<DprActivityItem> findByActivityId(Long activityId);

    List<DprActivityItem> findByDprIdAndIsBlockerTrueOrderByCreatedAtDesc(Long dprId);

    List<DprActivityItem> findByDprProjectIdAndIsBlockerTrueOrderByCreatedAtDesc(Long projectId);

    Long countByDprProjectIdAndIsBlockerTrue(Long projectId);

    @Query("SELECT COUNT(i) FROM DprActivityItem i WHERE i.dpr.project.id = :projectId AND i.isBlocker = true " +
           "AND (i.blockerStatus = com.example.BuildTwin._0.model.BlockerStatus.OPEN OR i.blockerStatus = com.example.BuildTwin._0.model.BlockerStatus.IN_PROGRESS OR i.blockerStatus IS NULL)")
    Long countOpenBlockersByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT i FROM DprActivityItem i WHERE i.dpr.project.id = :projectId AND i.isBlocker = true " +
           "AND (:status IS NULL OR i.blockerStatus = :status) " +
           "AND (:category IS NULL OR i.blockerCategory = :category) " +
           "AND (:severity IS NULL OR i.blockerSeverity = :severity) " +
           "AND (:activityId IS NULL OR i.activity.id = :activityId) " +
           "ORDER BY i.createdAt DESC")
    List<DprActivityItem> findProjectBlockersWithFilters(
            @Param("projectId") Long projectId,
            @Param("status") BlockerStatus status,
            @Param("category") BlockerCategory category,
            @Param("severity") BlockerSeverity severity,
            @Param("activityId") Long activityId);
}
