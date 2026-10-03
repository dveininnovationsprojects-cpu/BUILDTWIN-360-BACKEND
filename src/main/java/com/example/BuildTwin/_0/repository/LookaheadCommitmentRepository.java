package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.CommitmentStatus;
import com.example.BuildTwin._0.model.LookaheadCommitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LookaheadCommitmentRepository extends JpaRepository<LookaheadCommitment, Long> {

    List<LookaheadCommitment> findByProjectIdOrderByWeekStartDateDesc(Long projectId);

    List<LookaheadCommitment> findByProjectIdAndStatusOrderByWeekStartDateDesc(Long projectId, CommitmentStatus status);

    List<LookaheadCommitment> findByActivityIdOrderByWeekStartDateDesc(Long activityId);

    @Query("SELECT c FROM LookaheadCommitment c WHERE c.project.id = :projectId " +
           "AND ((c.weekStartDate BETWEEN :startDate AND :endDate) OR (c.weekEndDate BETWEEN :startDate AND :endDate)) " +
           "ORDER BY c.weekStartDate ASC")
    List<LookaheadCommitment> findByProjectIdAndDateRange(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<LookaheadCommitment> findByActivityIdAndWeekStartDateAndWeekEndDate(
            Long activityId, LocalDate weekStartDate, LocalDate weekEndDate);

    boolean existsByActivityIdAndWeekStartDateAndWeekEndDate(
            Long activityId, LocalDate weekStartDate, LocalDate weekEndDate);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, CommitmentStatus status);
}
