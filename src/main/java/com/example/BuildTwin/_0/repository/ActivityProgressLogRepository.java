package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.ActivityProgressLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityProgressLogRepository extends JpaRepository<ActivityProgressLog, Long> {

    List<ActivityProgressLog> findByActivityIdOrderByLogDateDesc(Long activityId);

    List<ActivityProgressLog> findByActivityIdOrderByLogDateAsc(Long activityId);

    List<ActivityProgressLog> findByActivityIdAndLogDateBetweenOrderByLogDateAsc(Long activityId, LocalDate startDate, LocalDate endDate);

    List<ActivityProgressLog> findByActivityIdAndLogDateBetweenOrderByLogDateDesc(Long activityId, LocalDate startDate, LocalDate endDate);

    Optional<ActivityProgressLog> findTopByActivityIdOrderByLogDateDesc(Long activityId);

    void deleteByActivityId(Long activityId);

    // =========================================================================
    // Filtered Activity Queries
    // =========================================================================

    @Query("SELECT p FROM ActivityProgressLog p WHERE p.activity.id = :activityId " +
            "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
            "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
            "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source)) " +
            "ORDER BY p.logDate DESC, p.id DESC")
    List<ActivityProgressLog> findActivityLogsFiltered(
            @Param("activityId") Long activityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isBlocker") Boolean isBlocker,
            @Param("source") String source);

    @Query("SELECT p FROM ActivityProgressLog p WHERE p.activity.id = :activityId " +
            "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
            "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
            "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source)) " +
            "ORDER BY p.logDate ASC, p.id ASC")
    List<ActivityProgressLog> findActivityLogsFilteredAsc(
            @Param("activityId") Long activityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isBlocker") Boolean isBlocker,
            @Param("source") String source);

    @Query(value = "SELECT p FROM ActivityProgressLog p WHERE p.activity.id = :activityId " +
            "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
            "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
            "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source))",
            countQuery = "SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.id = :activityId " +
                    "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
                    "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
                    "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
                    "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source))")
    Page<ActivityProgressLog> findActivityLogsFilteredPaged(
            @Param("activityId") Long activityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isBlocker") Boolean isBlocker,
            @Param("source") String source,
            Pageable pageable);

    // =========================================================================
    // Filtered Project Queries
    // =========================================================================

    @Query(value = "SELECT p FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId " +
            "AND (:workPackageId IS NULL OR p.activity.workPackage.id = :workPackageId) " +
            "AND (:activityId IS NULL OR p.activity.id = :activityId) " +
            "AND (:discipline IS NULL OR LOWER(p.activity.discipline) = LOWER(:discipline)) " +
            "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
            "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
            "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source))",
            countQuery = "SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId " +
                    "AND (:workPackageId IS NULL OR p.activity.workPackage.id = :workPackageId) " +
                    "AND (:activityId IS NULL OR p.activity.id = :activityId) " +
                    "AND (:discipline IS NULL OR LOWER(p.activity.discipline) = LOWER(:discipline)) " +
                    "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
                    "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
                    "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
                    "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source))")
    Page<ActivityProgressLog> findProjectLogsFiltered(
            @Param("projectId") Long projectId,
            @Param("workPackageId") Long workPackageId,
            @Param("activityId") Long activityId,
            @Param("discipline") String discipline,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isBlocker") Boolean isBlocker,
            @Param("source") String source,
            Pageable pageable);

    // =========================================================================
    // Filtered Work Package Queries
    // =========================================================================

    @Query(value = "SELECT p FROM ActivityProgressLog p WHERE p.activity.workPackage.id = :workPackageId " +
            "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
            "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
            "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source))",
            countQuery = "SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.workPackage.id = :workPackageId " +
                    "AND (:startDate IS NULL OR p.logDate >= :startDate) " +
                    "AND (:endDate IS NULL OR p.logDate <= :endDate) " +
                    "AND (:isBlocker IS NULL OR p.isBlocker = :isBlocker) " +
                    "AND (:source IS NULL OR LOWER(p.source) = LOWER(:source))")
    Page<ActivityProgressLog> findWorkPackageLogsFiltered(
            @Param("workPackageId") Long workPackageId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isBlocker") Boolean isBlocker,
            @Param("source") String source,
            Pageable pageable);

    // =========================================================================
    // Aggregate & KPI Methods
    // =========================================================================

    @Query("SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.id = :activityId")
    Long countByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT COALESCE(SUM(p.quantityCompletedToday), 0.0) FROM ActivityProgressLog p WHERE p.activity.id = :activityId")
    Double sumQuantityByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.id = :activityId AND p.isBlocker = true")
    Long countBlockersByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT MIN(p.logDate) FROM ActivityProgressLog p WHERE p.activity.id = :activityId")
    LocalDate findMinLogDateByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT MAX(p.logDate) FROM ActivityProgressLog p WHERE p.activity.id = :activityId")
    LocalDate findMaxLogDateByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT MAX(p.quantityCompletedToday) FROM ActivityProgressLog p WHERE p.activity.id = :activityId")
    Double findMaxQuantityByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(p.quantityCompletedToday), 0.0) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId")
    Double sumQuantityByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(DISTINCT p.activity.id) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId")
    Integer countDistinctActivitiesByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(p.manpowerCount), 0) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId")
    Long sumManpowerByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId AND p.isBlocker = true")
    Long countBlockersByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT MIN(p.logDate) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId")
    LocalDate findMinLogDateByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT MAX(p.logDate) FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId")
    LocalDate findMaxLogDateByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.workPackage.id = :workPackageId")
    Long countByWorkPackageId(@Param("workPackageId") Long workPackageId);

    @Query("SELECT COALESCE(SUM(p.quantityCompletedToday), 0.0) FROM ActivityProgressLog p WHERE p.activity.workPackage.id = :workPackageId")
    Double sumQuantityByWorkPackageId(@Param("workPackageId") Long workPackageId);

    @Query("SELECT COUNT(DISTINCT p.activity.id) FROM ActivityProgressLog p WHERE p.activity.workPackage.id = :workPackageId")
    Integer countDistinctActivitiesByWorkPackageId(@Param("workPackageId") Long workPackageId);

    @Query("SELECT COUNT(p) FROM ActivityProgressLog p WHERE p.activity.workPackage.id = :workPackageId AND p.isBlocker = true")
    Long countBlockersByWorkPackageId(@Param("workPackageId") Long workPackageId);

    // =========================================================================
    // Cutoff / As-of-Date & S-Curve Calculation Queries
    // =========================================================================

    @Query("SELECT COALESCE(SUM(p.quantityCompletedToday), 0.0) FROM ActivityProgressLog p WHERE p.activity.id = :activityId AND p.logDate <= :cutoffDate")
    Double sumQuantityByActivityIdAndLogDateLessThanEqual(@Param("activityId") Long activityId, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT p FROM ActivityProgressLog p WHERE p.activity.id = :activityId AND p.logDate <= :cutoffDate ORDER BY p.logDate DESC, p.id DESC")
    List<ActivityProgressLog> findByActivityIdAndLogDateLessThanEqualOrderByLogDateDesc(@Param("activityId") Long activityId, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT p FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId AND p.logDate <= :cutoffDate ORDER BY p.logDate ASC, p.id ASC")
    List<ActivityProgressLog> findByProjectIdAndLogDateLessThanEqualOrderByLogDateAsc(@Param("projectId") Long projectId, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT p FROM ActivityProgressLog p WHERE p.activity.project.id = :projectId ORDER BY p.logDate ASC, p.id ASC")
    List<ActivityProgressLog> findByProjectIdOrderByLogDateAsc(@Param("projectId") Long projectId);
}
